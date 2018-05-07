package com.simon.ota.ble.listener;


import com.simon.ota.ble.entity.OtaDeviceInfo;


public interface OtaScanDeviceListener {
    public void onPreScanDevice();

    public void onScanFinished();

    public void onScanningExecute(OtaDeviceInfo device);

}
