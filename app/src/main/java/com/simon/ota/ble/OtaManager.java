package com.simon.ota.ble;


import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;

import com.simon.ota.ble.entity.OtaDeviceInfo;
import com.simon.ota.ble.listener.OtaDeviceConnectStateListener;
import com.simon.ota.ble.listener.OtaDeviceVersionListener;
import com.simon.ota.ble.listener.OtaNativeDataListener;
import com.simon.ota.ble.listener.OtaPromoteListener;
import com.simon.ota.ble.listener.OtaScanDeviceListener;
import com.simon.ota.ble.listener.OtaSerialListener;
import com.simon.ota.ble.listener.OtaTemperatureListener;
import com.simon.ota.ble.parse.OtaParseDataUtil;
import com.simon.ota.ble.util.OtaConvertUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

@SuppressWarnings("all")
public class OtaManager implements OtaNativeDataListener {
    private static OtaManager mManager;
    private static BluetoothAdapter mAdapter;
    private static Context mContext;

    private OtaServices mBluetoothServices;
    private CBTScanCallback mCbtScanCallback;
    private BluetoothLeScanner mLEScanner;
    private ScanSettings mLeSettings;
    private OtaScanDeviceListener mScanDeviceListener;
    private ArrayList<ScanFilter> mLeFilters;
    private OtaParseDataUtil mParseUtil;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

        }
    };

    public static OtaManager getInstance(Context context) {
        mContext = context;
        if (mManager == null) {
            mManager = new OtaManager();
            BluetoothManager manager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
            mAdapter = manager.getAdapter();


        }

        return mManager;
    }

    //初始化
    public OtaManager build() {
        //注册广播
        registBroadCast();
        //创建Bluetooth对象
        mBluetoothServices = new OtaServices();
        mBluetoothServices.setNativeDataListener(this);
        mParseUtil = new OtaParseDataUtil();
        return mManager;
    }

    public void setScanDeviceListener(OtaScanDeviceListener scanDeviceListener) {
        this.mScanDeviceListener = scanDeviceListener;
    }

    public void setTemperatureListener(OtaTemperatureListener temperatureListener) {
        mParseUtil.setTemperatureListener(temperatureListener);
    }

    public void setSerialListener(OtaSerialListener serialListener) {
        mParseUtil.setSerialListener(serialListener);
    }

    public void setDeviceVersionListener(OtaDeviceVersionListener deviceVersionListener) {
        mParseUtil.setDeviceVersionListener(deviceVersionListener);
    }

    public void setPromoteListener(OtaPromoteListener promoteListener) {
        mParseUtil.setPromoteListener(promoteListener);
    }

    private void registBroadCast() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        OtaReceiver mReceiver = new OtaReceiver(null);
        mContext.registerReceiver(mReceiver, filter);

    }

    public boolean isSupportBLE() {

        //检查是否关闭，如果关闭的话自动打开
        return mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) || (mAdapter == null);

    }

    public boolean isOpenBLE() {
        return mAdapter.enable();
    }

    public void startScan(int scanTime) {
        mHandler.postDelayed(scanThread, scanTime);
        mScanDeviceListener.onPreScanDevice();
        scanDevice();
    }

    Runnable scanThread = new Runnable() {
        @Override
        public void run() {
            stopScan();
        }
    };

    private void stopScan() {
        if (Build.VERSION.SDK_INT < 21) {
            mAdapter.stopLeScan(mLeScanCallback);
        } else {
            mLEScanner.stopScan(mCbtScanCallback);
        }
        mScanDeviceListener.onScanFinished();
    }

    public void stopScanDeive() {
        mHandler.removeCallbacks(scanThread);
        stopScan();
    }

    private void scanDevice() {
        //设置回调,获取设备信息
        if (Build.VERSION.SDK_INT < 21) {
            mAdapter.startLeScan(mLeScanCallback);
        } else {

            mLEScanner = mAdapter.getBluetoothLeScanner();
            mLeSettings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .build();
//            mLeFilters = new ArrayList<ScanFilter>();
//            mLeFilters.add(new ScanFilter.Builder().setServiceUuid(ParcelUuid.fromString(OtaGattAttributes.BATTERY_SERVICE)).
//                    setServiceUuid(ParcelUuid.fromString(OtaGattAttributes.DEVICE_SERVICE)).
//                    setServiceUuid(ParcelUuid.fromString(OtaGattAttributes.TEMPERATURE_SERVICE)).build());

            if (mCbtScanCallback == null) {
                mCbtScanCallback = new CBTScanCallback();
            }


            mLEScanner.startScan(mLeFilters, mLeSettings, mCbtScanCallback);
        }
        //
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {

                    OtaDeviceInfo info = getDeviceInfo(rssi, device, scanRecord);
                    if (info != null)
                        mScanDeviceListener.onScanningExecute(info);
                }

            };

    public boolean prepare2Update() {
        return mBluetoothServices.writeFactoryCmd(new byte[]{3});
    }

    public boolean writeSerial(String serialNum) {
        byte[] data = OtaConvertUtil.hexStr2Bytes(serialNum);
        byte[] msg = new byte[12];
        msg[0] = 0x6A;
        msg[1] = 0x58;
        long sum = 0;

        for (int i = 0; i < data.length; i++) {
            sum += data[i] & 0xff;
        }

        msg[2] = (byte) (sum & 0xff);
        msg[3] = (byte) ((sum >> 8) & 0xff);
        System.arraycopy(data, 0, msg, 4, data.length);

        return mBluetoothServices.writeSerial(msg);
    }

    public void readSerial() {
        mBluetoothServices.readSerial();
    }

    public void readDeviceVersion() {
        mBluetoothServices.readDeviceVersion();
    }

    public void setDeviceConnectListener(OtaDeviceConnectStateListener connectStateListener) {
        mBluetoothServices.setDeviceConnectListener(connectStateListener);
    }

    public void writeOTAHeader(byte[] datas) {
        mBluetoothServices.writeOTAHeader(datas);
    }

    public boolean writeOTABodyPre() {
        return mBluetoothServices.writeOTABodyPre();
    }

    public boolean writeOTABody(byte[] datas) {
        return mBluetoothServices.writeOTABody(datas);
    }

    public void disonnect() {
        mBluetoothServices.close();
    }

    public void synTime() {
        Calendar now = Calendar.getInstance();
        int second = now.get(Calendar.SECOND);
        int minute = now.get(Calendar.MINUTE);
        int hour = now.get(Calendar.HOUR_OF_DAY);
        int day = now.get(Calendar.DATE);
        int month = now.get(Calendar.MONTH) + 1;
        int year = now.get(Calendar.YEAR);
        byte[] data = new byte[]{(byte) second, (byte) minute, (byte) hour, (byte) day, (byte) month,
                (byte) (year & 0xff), (byte) (year >> 8)};

        mBluetoothServices.synTime(data);
    }

    @TargetApi(21)
    private class CBTScanCallback extends ScanCallback {

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            OtaDeviceInfo info = getDeviceInfo(result.getRssi(), result.getDevice(), result.getScanRecord().getBytes());
            if (info != null)
                mScanDeviceListener.onScanningExecute(info);

        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }
    }

    private OtaDeviceInfo getDeviceInfo(int rssi, BluetoothDevice device, byte[] datas) {
        int deviceNameIndex = 13;
        int otaNameIndex = 21;
        int deviceNameLen = OtaConstants.DEVICENAME.length();
        int otaNameLen = OtaConstants.OTANAME.length();
        String deviceName = new String(Arrays.copyOfRange(datas, deviceNameIndex, deviceNameIndex
                + deviceNameLen));
        String otaName = new String(Arrays.copyOfRange(datas, otaNameIndex, otaNameIndex
                + otaNameLen));
        if (!OtaConstants.DEVICENAME.equals(deviceName) && !OtaConstants.OTANAME.equals(otaName))
            return null;
        byte[] serialData = new byte[8];
        if (OtaConstants.DEVICENAME.equals(deviceName)) {
            System.arraycopy(datas, deviceNameIndex + deviceNameLen, serialData, 0, 8);
            String serialNum = OtaConvertUtil.bytesToHexString(serialData);
            String address = device.getAddress();
            return new OtaDeviceInfo(rssi, serialNum, address);
        } else {
            System.arraycopy(datas, otaNameIndex + otaNameLen + 10, serialData, 0, 8);
            String serialNum = OtaConvertUtil.bytesToHexString(serialData);
            String address = device.getAddress();
            return new OtaDeviceInfo(rssi, serialNum, address);
        }
    }

    public void connect(String mac) {
        mBluetoothServices.connect(mContext, mac);
    }

    @Override
    public void onReceiveData(BluetoothGattCharacteristic characteristic) {
        mParseUtil.setRawDataParse(characteristic);
    }

    @Override
    public void onWriteData(BluetoothGattCharacteristic characteristic) {
        mParseUtil.setRawDataWrite(characteristic);
    }

    public void getErrorRate() {
        mBluetoothServices.writeFactoryCmd(new byte[]{2});
    }

    public boolean reset() {
        return mBluetoothServices.writeFactoryCmd(new byte[]{1});
    }

}
