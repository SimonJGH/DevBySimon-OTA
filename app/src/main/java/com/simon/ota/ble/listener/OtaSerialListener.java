package com.simon.ota.ble.listener;

@SuppressWarnings("all")
public interface OtaSerialListener {
    void onSerailData(String serial);
    void onSerialWriteSuccess();
}
