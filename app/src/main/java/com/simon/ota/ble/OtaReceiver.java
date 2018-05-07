package com.simon.ota.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.simon.ota.ble.listener.OtaBluetoothListener;

@SuppressWarnings("all")
public class OtaReceiver extends BroadcastReceiver {

    private OtaBluetoothListener mBluetoothListener;

    public OtaReceiver(OtaBluetoothListener bluetoothListener) {
        mBluetoothListener = bluetoothListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        switch (action) {
            case BluetoothAdapter.ACTION_STATE_CHANGED:
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                int preState = intent.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_STATE, 0);
//                mBluetoothListener.onActionStateChanged(preState, state);
                break;
            case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                if (mBluetoothListener != null)
                    mBluetoothListener.onActionDiscoveryStateChanged(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
                break;
            case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                break;
            case BluetoothDevice.ACTION_FOUND:
//                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//                    short rssi =(short)( intent.getShortExtra(BluetoothDevice.EXTRA_RSSI,(short) 0));
//                    CIMDevice cimDevice=new CIMDevice(device.getName(),device.getAddress(),rssi);
//                    if (cimDevice.getDeviceType()!= DeviceType.UNKNOWN) {
//                        if (!cimDevice.isSupportBLE())
//                            mBluetoothListener.onActionDeviceFound(cimDevice);
//                    }
                break;
            case BluetoothAdapter.ACTION_SCAN_MODE_CHANGED:
                int scanMode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, 0);
                int preScanMode = intent.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_SCAN_MODE, 0);
                if (mBluetoothListener != null)
                    mBluetoothListener.onActionScanModeChanged(preScanMode, scanMode);
                break;
            default:
                break;
        }
    }
}
