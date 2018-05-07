package com.simon.ota.ble.listener;

import com.simon.ota.ble.entity.OtaTemperatureInfo;

@SuppressWarnings("all")
public interface OtaTemperatureListener {
    void onRealTemperatureData(OtaTemperatureInfo info);
    void onHistoryTemperatureData(OtaTemperatureInfo info);
}
