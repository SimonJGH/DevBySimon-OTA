package com.simon.ota.ble.listener;

@SuppressWarnings("all")
public interface OtaPromoteListener {
    void onPromotePositionListener(byte[] datas);

    void onPromoteWriteSuccess();

    void onPromoteHeaderWriteSuccess();
}
