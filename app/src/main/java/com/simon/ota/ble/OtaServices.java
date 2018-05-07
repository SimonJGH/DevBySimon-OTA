package com.simon.ota.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.SystemClock;
import android.util.Log;

import com.simon.ota.ble.listener.OtaDeviceConnectStateListener;
import com.simon.ota.ble.listener.OtaNativeDataListener;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;

@SuppressWarnings("all")
public class OtaServices {
    Context mContext;
    BluetoothAdapter mAdapter;
    private int mState;
    BluetoothGatt mGatt;

    private BluetoothGattCharacteristic mCFCharacteristic;
    private BluetoothGattCharacteristic mErrorRateCharacteristic;
    private BluetoothGattCharacteristic mWriteSerailCharacteristic;
    private BluetoothGattCharacteristic mReadSerialCharacteristic;
    private BluetoothGattCharacteristic mReadDeviceVersionCharacteristic;
    private BluetoothGattCharacteristic mOTAHeadCharacteristic;
    private BluetoothGattCharacteristic mOTABodyCharacteristic;
    private BluetoothGattCharacteristic mTimeCharacteristic;
    private BluetoothGattCharacteristic mUserIdCharacteristic;
    private BluetoothGattCharacteristic mLCDCharacteristic;
    private BluetoothGattCharacteristic mSendIntervalCharacteristic;
    private BluetoothGattCharacteristic mOtaBodyBgc;

    private BluetoothGattService otaService;

    private Queue<BluetoothGattDescriptor> descriptorWriteQueue = new LinkedList<BluetoothGattDescriptor>();
    private Queue<BluetoothGattCharacteristic> characteristicReadQueue = new LinkedList<BluetoothGattCharacteristic>();

    private OtaNativeDataListener mBleNativeDataListener;
    private OtaDeviceConnectStateListener mConnectStateListener;

    public void connect(final Context context, final String mac) {
        mContext = context;
        BluetoothManager manager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        mAdapter = manager.getAdapter();
        if (mGatt != null) {
            mGatt.close();
            mGatt = null;
        }
        final BluetoothDevice device = mAdapter.getRemoteDevice(mac);
        mGatt = device.connectGatt(context, false, callback);
        refreshDeviceCache(mGatt);
        if (mConnectStateListener != null) {
            mConnectStateListener.onConnectionStateListener(OtaConstants.ConnectState.STATE_CONNECTING);
        }
    }

    BluetoothGattCallback callback = new BluetoothGattCallback() {
        @Override
        public synchronized void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    boolean state = gatt.discoverServices();
                    if (mConnectStateListener != null)
                        mConnectStateListener.onConnectionStateListener(OtaConstants.ConnectState.STATE_CONNECTED);
                    break;

                case BluetoothProfile.STATE_DISCONNECTED:
                    refreshDeviceCache(gatt);
                    if (mGatt != null) {
                        if (mConnectStateListener != null)
                            mConnectStateListener.onConnectionStateListener(OtaConstants.ConnectState.STATE_DISCONNECTED);
//                        setState(cimDevice, BluetoothState.STATE_DISCONNECTED);
                    } else {
                        if (mConnectStateListener != null)
                            mConnectStateListener.onConnectionStateListener(OtaConstants.ConnectState.STATE_FAILE);
                    }
                    break;
                case BluetoothProfile.STATE_CONNECTING:
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            // Loops through available GATT Services.
            if (status == BluetoothGatt.GATT_SUCCESS) {
                otaService = gatt.getService(UUID.fromString(OtaGattAttributes.OTA_SERVICE));
                List<BluetoothGattService> gattServices = gatt.getServices();
                for (BluetoothGattService gattService : gattServices) {
                    String sUuid = gattService.getUuid().toString();
                    List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
                    // Loops through available Characteristics.
                    for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                        String uuid = gattCharacteristic.getUuid().toString();
                        if (OtaGattAttributes.TEMPERATURE_SERVICE.equals(sUuid)) {
                            if (OtaGattAttributes.TEMPERATURE_UNIT.equals(uuid)) {
                                mCFCharacteristic = gattCharacteristic;
                            } else if (OtaGattAttributes.TEMPERATURE_TIME.equals(uuid)) {
                                mTimeCharacteristic = gattCharacteristic;
                            } else if (OtaGattAttributes.USER_ID.equals(uuid)) {
                                mUserIdCharacteristic = gattCharacteristic;
                            } else if (OtaGattAttributes.LCD.equals(uuid)) {
                                mLCDCharacteristic = gattCharacteristic;
                            } else if (OtaGattAttributes.SEND.equals(uuid)) {
                                mSendIntervalCharacteristic = gattCharacteristic;
                            } else if ((OtaGattAttributes.CURRENT_TEMPERATURE).equals(uuid)) {
                                enableNotifications(gattCharacteristic, true);
                            } else if (OtaGattAttributes.HISTORY_TEMPERATURE.equals(uuid)) {
                                enableIndications(gattCharacteristic);
                            } else if (OtaGattAttributes.ERROR_RATE.equals(uuid)) {
                                mErrorRateCharacteristic = gattCharacteristic;
                            }
                        } else if (OtaGattAttributes.DEVICE_SERVICE.equals(sUuid)) {
                            if (OtaGattAttributes.READ_SERIAL.equals(uuid)) {
                                mReadSerialCharacteristic = gattCharacteristic;
//                            characteristicReadQueue.add(gattCharacteristic);
                            } else if (OtaGattAttributes.WRITE_SERIAL.equals(uuid)) {
                                mWriteSerailCharacteristic = gattCharacteristic;
                            } else if (OtaGattAttributes.DEVICE_VERSION.equals(uuid)) {
                                mReadDeviceVersionCharacteristic = gattCharacteristic;
                            }
                        } else if (OtaGattAttributes.OTA_SERVICE.equals(sUuid)) {
                            if (OtaGattAttributes.OTA_HEADER.equals(uuid)) {
//                                enableNotifications(gattCharacteristic,true);
                                mOTAHeadCharacteristic = gattCharacteristic;
                                continue;
                            } else if (OtaGattAttributes.OTA_BODY.equals(uuid)) {
                                mOTABodyCharacteristic = gattCharacteristic;
//                                openToNotificationQueue(gattCharacteristic,true);
//                                openToNotificationQueue(gattCharacteristic);
//                                mGatt.writeDescriptor(descriptorWriteQueue.element());
                                if (mConnectStateListener != null)
                                    mConnectStateListener.onConnectionStateListener(OtaConstants.ConnectState.STATE_SUPPORT_OTA);
                                return;
                            }
                        }
                    }
                }
                if (mConnectStateListener != null)
                    mConnectStateListener.onConnectionStateListener(OtaConstants.ConnectState.STATE_SUPPORT_SERVICE_SUCCEED);
            }
        }


        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic
                characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            mBleNativeDataListener.onReceiveData(characteristic);
            if (OtaGattAttributes.OTA_BODY.equals(characteristic.getUuid())) {
                // Log.i("Simon", "mOTABody = " + OtaConvertUtil.bytesToHexString(characteristic.getValue()));
            }
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor,
                                     int status) {

        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor,
                                      int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            descriptorWriteQueue.remove();
            if (descriptorWriteQueue.size() > 0) {
                mGatt.writeDescriptor(descriptorWriteQueue.element());
            } else if (characteristicReadQueue.size() > 0) {
                mGatt.readCharacteristic(characteristicReadQueue.element());
                characteristicReadQueue.remove();
//            } else if (descriptor.getCharacteristic().getUuid().toString().equals(OtaGattAttributes.OTA_BODY)) {
//                if (mConnectStateListener != null)
//                    mConnectStateListener.onConnectionStateListener(OtaConstants.ConnectState.STATE_SUPPORT_OTA);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic
                characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                mBleNativeDataListener.onReceiveData(characteristic);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, final BluetoothGattCharacteristic
                characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            //  Log.i("Simon", "写入的数据是 = " + OtaConvertUtil.bytesToHexString(characteristic.getValue()));
            mBleNativeDataListener.onWriteData(characteristic);
        }
    };

    public boolean writeFactoryCmd(byte[] data) {
        if (mGatt != null) {
            if (mErrorRateCharacteristic != null) {
                mErrorRateCharacteristic.setValue(data);
                boolean state = mGatt.writeCharacteristic(mErrorRateCharacteristic);
                return state;
            }
        }
        return false;
    }

    public boolean writeSerial(byte[] data) {
        if (mGatt != null) {
            if (mWriteSerailCharacteristic != null) {
                mWriteSerailCharacteristic.setValue(data);
                mWriteSerailCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
                boolean state = mGatt.writeCharacteristic(mWriteSerailCharacteristic);
                return state;
            }
        }
        return false;
    }

    public void setNativeDataListener(OtaNativeDataListener listener) {
        this.mBleNativeDataListener = listener;
    }

    public void readSerial() {
        if (mGatt != null) {
            if (mReadSerialCharacteristic != null) {
                if (descriptorWriteQueue.size() > 0) {
                    characteristicReadQueue.add(mReadSerialCharacteristic);
                } else {
                    boolean state = mGatt.readCharacteristic(mReadSerialCharacteristic);
                }
            }
        }
    }

    public void setDeviceConnectListener(OtaDeviceConnectStateListener connectStateListener) {
        this.mConnectStateListener = connectStateListener;
    }
    public void synTime(byte[] data) {
        if (mGatt != null) {

            if (mTimeCharacteristic != null) {

                mTimeCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
                mTimeCharacteristic.setValue(data);
                boolean state = mGatt.writeCharacteristic(mTimeCharacteristic);
            }
        }


    }

    public void readDeviceVersion() {
        if (mGatt != null) {
            if (mReadDeviceVersionCharacteristic != null) {
                boolean state = mGatt.readCharacteristic(mReadDeviceVersionCharacteristic);
            }
        }
    }

    public void writeOTAHeader(byte[] datas) {
        if (mGatt != null) {
            if (mOTAHeadCharacteristic != null) {
                mOTAHeadCharacteristic.setValue(datas);
                mOTAHeadCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
                boolean state = mGatt.writeCharacteristic(mOTAHeadCharacteristic);
            }
        }
    }

    public boolean writeOTABodyPre() {
        if (mGatt != null) {
            mOtaBodyBgc = otaService.getCharacteristic(UUID.fromString(OtaGattAttributes.OTA_BODY));
            if (mOtaBodyBgc != null) {
                mOtaBodyBgc.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
                return true;
            }
        }
        return false;
    }

    public boolean writeOTABody(byte[] datas) {
        if (mOtaBodyBgc != null) {
            mOtaBodyBgc.setValue(datas);
            SystemClock.sleep(20);
            boolean state = mGatt.writeCharacteristic(mOtaBodyBgc);
            return state;
        }
        return false;
    }

    public void disConnect() {
        mGatt.disconnect();
    }

    public void close() {
        if (mGatt != null) {
            refreshDeviceCache(mGatt);
            mGatt.disconnect();
            mGatt.close();
            mGatt = null;
        }
    }

    private boolean refreshDeviceCache(BluetoothGatt gatt) {
        try {
            BluetoothGatt localBluetoothGatt = gatt;
            Method localMethod = localBluetoothGatt.getClass().getMethod("refresh", new Class[0]);
            if (localMethod != null) {
                boolean bool = ((Boolean) localMethod.invoke(localBluetoothGatt, new Object[0])).booleanValue();
                return bool;
            }
        } catch (Exception localException) {
            Log.i("Simon", "An exception occured while refreshing device");
        }
        return false;
    }

    /**
     * 启用给定特征的指示
     *
     * @return true是请求已发送，如果其中一个参数为null或特性没有CCCD，则为false。
     */
    private void enableIndications(BluetoothGattCharacteristic characteristic) {
        final BluetoothGatt gatt = mGatt;
//        readCharacteristic(characteristic);
        int properties = characteristic.getProperties();
        if ((properties & BluetoothGattCharacteristic.PROPERTY_INDICATE) == 0)
            return;
        gatt.setCharacteristicNotification(characteristic, true);
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(OtaGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
        if (descriptor != null) {
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
            descriptorWriteQueue.add(descriptor);
            if (descriptorWriteQueue.size() == 1) {
                mGatt.writeDescriptor(descriptor);
            }
        }
    }

    private void enableNotifications(BluetoothGattCharacteristic characteristic, boolean isEnable) {
        BluetoothGatt gatt = mGatt;
        final int properties = characteristic.getProperties();
        if ((properties & BluetoothGattCharacteristic.PROPERTY_NOTIFY) == 0)
            return;

        gatt.setCharacteristicNotification(characteristic, isEnable);
        final BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(OtaGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));

        if (descriptor != null) {
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            descriptorWriteQueue.add(descriptor);
            //if there is only 1 item in the queue, then write it.  If more than 1, we handle asynchronously in the callback above
            if (descriptorWriteQueue.size() == 1) {
                mGatt.writeDescriptor(descriptor);
            }
        }
    }

    private void openToNotificationQueue(BluetoothGattCharacteristic characteristic, boolean isEnable) {
        BluetoothGatt gatt = mGatt;
        final int properties = characteristic.getProperties();
        if ((properties & BluetoothGattCharacteristic.PROPERTY_NOTIFY) == 0)
            return;
        gatt.setCharacteristicNotification(characteristic, isEnable);
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(OtaGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
        if (descriptor != null) {
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            descriptorWriteQueue.add(descriptor);
            //if there is only 1 item in the queue, then write it.  If more than 1, we handle asynchronously in the callback above
        }
    }

}
