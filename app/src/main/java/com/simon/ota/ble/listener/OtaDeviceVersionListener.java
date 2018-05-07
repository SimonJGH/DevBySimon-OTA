package com.simon.ota.ble.listener;

@SuppressWarnings("all")
public interface OtaDeviceVersionListener {
    void onSoftVersion(String version);
}
