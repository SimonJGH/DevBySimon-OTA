package com.simon.ota;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.simon.ota.ble.OtaConstants;
import com.simon.ota.ble.OtaManager;
import com.simon.ota.ble.listener.OtaDeviceConnectStateListener;
import com.simon.ota.ble.listener.OtaDeviceVersionListener;
import com.simon.ota.ble.listener.OtaPromoteListener;
import com.simon.ota.ble.listener.OtaSerialListener;
import com.simon.ota.ble.util.OtaConvertUtil;
import com.simon.ota.ble.util.OtaStreamUtils;

import java.io.InputStream;
import java.util.Arrays;

@SuppressWarnings("all")
public class OtaUpgradeActivity extends Activity implements OtaSerialListener, OtaDeviceVersionListener, OtaDeviceConnectStateListener, OtaPromoteListener {
    private UpgadeView mUv_progress;
    private TextView mTv_progress;
    private ImageView mIv_oad_upgrading;

    private OtaManager mManager;
    private byte[] promoteDatas;
    private int blockIndex = 0;
    private int mBlockCount;
    static final int OTA_BLOCK_SIZE = 16;
    private String otaMac = "C8:FD:19:4A:59:B1";
    private int progress = -1;
    private boolean IS_RESET = false;

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 2:
                    boolean b = mManager.writeOTABodyPre();
                    if (b) {
                        startUpgrade();
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_promote);

        mUv_progress = (UpgadeView) findViewById(R.id.uv_progress);
        mTv_progress = (TextView) findViewById(R.id.tv_progress);
        mIv_oad_upgrading = (ImageView) findViewById(R.id.iv_oad_upgrading);

        Animation animLoading = AnimationUtils.loadAnimation(OtaUpgradeActivity.this, R.anim.anim_loading);
        LinearInterpolator lin = new LinearInterpolator();
        animLoading.setInterpolator(lin);
        mIv_oad_upgrading.startAnimation(animLoading);

        //进行设备的连接通讯
        mManager = OtaManager.getInstance(getApplicationContext()).build();
        mManager.setDeviceConnectListener(this);
        mManager.setSerialListener(this);
        mManager.setDeviceVersionListener(this);
        mManager.setPromoteListener(this);

        try {
            InputStream open = getAssets().open("thermometer.bin");
            promoteDatas = OtaStreamUtils.convertStreamToByte(open);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        int count = promoteDatas.length / OTA_BLOCK_SIZE;
        mBlockCount = promoteDatas.length % OTA_BLOCK_SIZE == 0 ? count : count + 1;
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            otaMac = bundle.getString("OTA_MAC");
            if (!TextUtils.isEmpty(otaMac)) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mManager.connect(otaMac);
                    }
                }, 2500);
            }
        }
    }

    @Override
    public void onSerailData(String serial) {
        int hardware_version = Integer.parseInt(serial.substring(2, 4));
        mManager.readDeviceVersion();
    }

    @Override
    public void onSerialWriteSuccess() {
    }

    @Override
    public void onSoftVersion(String version) {//SW8.07
        mManager.prepare2Update();
        IS_RESET = true;
    }

    @Override
    public void onConnectionStateListener(int state) {
        if (state == OtaConstants.ConnectState.STATE_SUPPORT_SERVICE_SUCCEED) {
            mManager.readSerial();
        } else if (state == OtaConstants.ConnectState.STATE_DISCONNECTED) {
            Log.i("OTA", "断开连接");
            if (blockIndex == 0)
                mManager.connect(otaMac);
        } else if (state == OtaConstants.ConnectState.STATE_SUPPORT_OTA) {
            Log.i("OTA", "写入头文件");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTv_progress.setText(R.string.start_promote);
                }
            });
            byte[] datas = new byte[8];
            System.arraycopy(promoteDatas, 4, datas, 0, datas.length);
            mManager.writeOTAHeader(datas);
        } else if (state == OtaConstants.ConnectState.STATE_CONNECTING) {
        } else if (state == OtaConstants.ConnectState.STATE_CONNECTED) {
            Log.i("OTA", "连接成功");
        }
    }

    @Override
    public void onPromotePositionListener(final byte[] datas) {
        if (Arrays.equals(datas, new byte[]{(byte) 0xee, (byte) 0xee})) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i("OTA", "失败");
                    mTv_progress.setText(R.string.promote_error);
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mManager.disonnect();
                        }
                    }, 1000);
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mManager.connect(otaMac);
                        }
                    }, 2500);
                }
            });
        } else if (Arrays.equals(datas, new byte[]{(byte) 0xfe, (byte) 0xfe})) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i("OTA", "成功");
                }
            });
        } else if (Arrays.equals(datas, new byte[]{0, 0})) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i("OTA", "写入");
                }
            });
            startUpgrade();
        }
    }

    @Override
    public void onPromoteHeaderWriteSuccess() {
        mHandler.sendEmptyMessageDelayed(2, 2500);
    }

    @Override
    public void onPromoteWriteSuccess() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int i = blockIndex / (mBlockCount / 100);
                if (i != progress) {
                    mUv_progress.setProgress(i);
                    mTv_progress.setText(i + " %");
                    progress = i;
                    if (progress == 100) {
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                otaUpgradeSuccess();
                            }
                        }, 8000);
                    }
                }
            }
        });
        startUpgrade();
    }

    /* 开始ota升级 */
    private void startUpgrade() {
        IS_RESET = true;
        byte[] bodyData = new byte[18];
        if (blockIndex * 16 < promoteDatas.length) {
            int len = (blockIndex * 16 + 16) > promoteDatas.length ? (promoteDatas.length - blockIndex * 16) : OTA_BLOCK_SIZE;
            bodyData[0] = OtaConvertUtil.loUint16((short) blockIndex);
            bodyData[1] = OtaConvertUtil.hiUint16((short) blockIndex);
            System.arraycopy(promoteDatas, 16 * blockIndex, bodyData, 2, len);
            blockIndex++;
            boolean b = mManager.writeOTABody(bodyData);
            // Log.i("OTA", "数据写入 = " + blockIndex + "  " + OtaConvertUtil.bytesToHexString(bodyData) + "  " + b);
        }
    }

    /* ota升级成功*/
    public void otaUpgradeSuccess() {
        IS_RESET = false;
        mTv_progress.setText(R.string.promote_success);
        OtaUpgradeActivity.this.finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (!IS_RESET) {
                mManager.disonnect();
                finish();
            }
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
        mIv_oad_upgrading.clearAnimation();
    }
}
