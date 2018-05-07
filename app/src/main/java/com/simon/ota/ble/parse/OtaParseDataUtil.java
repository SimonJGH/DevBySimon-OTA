package com.simon.ota.ble.parse;

import android.bluetooth.BluetoothGattCharacteristic;

import com.simon.ota.ble.OtaGattAttributes;
import com.simon.ota.ble.listener.OtaDeviceVersionListener;
import com.simon.ota.ble.listener.OtaPromoteListener;
import com.simon.ota.ble.listener.OtaSerialListener;
import com.simon.ota.ble.listener.OtaTemperatureListener;
import com.simon.ota.ble.util.OtaConvertUtil;
import com.simon.ota.ble.entity.OtaTemperatureInfo;

@SuppressWarnings("all")
public class OtaParseDataUtil {
    private OtaTemperatureListener mTemperatureListener;
    private OtaSerialListener mSerialListener;
    private OtaDeviceVersionListener mDeviceVersionListener;
    private OtaPromoteListener mPromoteListener;

    public void setRawDataParse(BluetoothGattCharacteristic characteristic) {
        String uuid = characteristic.getUuid().toString();
        byte[] data = characteristic.getValue();
        if (OtaGattAttributes.CURRENT_TEMPERATURE.equals(uuid)) {
            OtaTemperatureInfo currentTemperature = parseTemperature(data);
            if (mTemperatureListener != null)
                mTemperatureListener.onRealTemperatureData(currentTemperature);
        } else if (OtaGattAttributes.HISTORY_TEMPERATURE.equals(uuid)) {
            OtaTemperatureInfo historyTemperature = parseTemperature(data);
            if (mTemperatureListener != null)
                mTemperatureListener.onHistoryTemperatureData(historyTemperature);
        } else if (OtaGattAttributes.READ_SERIAL.equals(uuid)) {
            String serial = OtaConvertUtil.bytesToHexString(data);
            if (mSerialListener != null) {
                mSerialListener.onSerailData(serial);
            }
        } else if (OtaGattAttributes.DEVICE_VERSION.equals(uuid)) {
            String version = new String(characteristic.getValue());
            if (mDeviceVersionListener != null)
                mDeviceVersionListener.onSoftVersion(version);
        } else if (OtaGattAttributes.OTA_BODY.equals(uuid)) {
            if (mPromoteListener != null) {
                mPromoteListener.onPromotePositionListener(data);
            }
        }
    }

    public void setRawDataWrite(BluetoothGattCharacteristic characteristic) {
        String uuid = characteristic.getUuid().toString();
        if (uuid.equals(OtaGattAttributes.OTA_BODY)) {
            if (mPromoteListener != null) {
                mPromoteListener.onPromoteWriteSuccess();
            }
        } else if (uuid.equals(OtaGattAttributes.WRITE_SERIAL)) {
            if (mSerialListener != null) {
                mSerialListener.onSerialWriteSuccess();
            }
        } else if (OtaGattAttributes.OTA_HEADER.equals(uuid)) {
            if (mPromoteListener != null) {
                mPromoteListener.onPromoteHeaderWriteSuccess();
            }
        }

    }

    public void setTemperatureListener(OtaTemperatureListener temperatureListener) {
        this.mTemperatureListener = temperatureListener;
    }

    public void setSerialListener(OtaSerialListener serialListener) {
        this.mSerialListener = serialListener;
    }

    public void setDeviceVersionListener(OtaDeviceVersionListener deviceVersionListener) {
        this.mDeviceVersionListener = deviceVersionListener;
    }

    public void setPromoteListener(OtaPromoteListener promoteListener) {
        this.mPromoteListener = promoteListener;
    }

    private OtaTemperatureInfo parseTemperature(byte[] data) {
        int unit = data[0] & 0x01;//单位
        int timestampFlag = data[0] >> 1 & 0x01;//时间戳
        int locationInfoFlag = data[0] >> 2 & 0x01;//
        int a = data[4] & 0xff;
        double temperature = (double) (data[1] & 0xff | (data[2] & 0xff) << 8 | (data[3] & 0xff) << 16) / 100.0D;
        int year = (data[6] & 0xff) << 8 | data[5] & 0xff;
        int month = data[7] & 0xff;
        int day = data[8] & 0xff;
        int hour = data[9] & 0xff;
        int minute = data[10] & 0xff;
        int second = data[11] & 0xff;
        int generalBody = data[12] & 0xff;//固定值
        OtaTemperatureInfo info = new OtaTemperatureInfo(unit, generalBody, temperature, year, month, day, hour, minute, second);
        return info;
    }


}
