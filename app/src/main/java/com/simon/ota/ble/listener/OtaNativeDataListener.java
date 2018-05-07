package com.simon.ota.ble.listener;


import android.bluetooth.BluetoothGattCharacteristic;

@SuppressWarnings("all")
public interface OtaNativeDataListener {
    void onReceiveData(BluetoothGattCharacteristic characteristic);

    void onWriteData(BluetoothGattCharacteristic characteristic);
}
