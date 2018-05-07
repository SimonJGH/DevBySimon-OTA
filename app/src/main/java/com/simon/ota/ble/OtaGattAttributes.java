package com.simon.ota.ble;

import java.util.HashMap;

@SuppressWarnings("all")
public class OtaGattAttributes {
    private static HashMap<String, String> attributes = new HashMap<String, String>();
    public static String HEART_RATE_MEASUREMENT = "00002a37-0000-1000-8000-00805f9b34fb";
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";

    /**
     * 温度服务
     */
    public static String TEMPERATURE_SERVICE = "00001809-0000-1000-8000-00805f9b34fb";

    /**
     * 设备服务
     */
    public static String DEVICE_SERVICE = "0000180a-0000-1000-8000-00805f9b34fb";

    /**
     * 电池服务
     */
    public static String BATTERY_SERVICE = "0000180f-0000-1000-8000-00805f9b34fb";

    public static String OTA_SERVICE = "f000ffc0-0451-4000-b000-000000000000";
    public static String OTA_HEADER = "f000ffc1-0451-4000-b000-000000000000";
    public static String OTA_BODY = "f000ffc2-0451-4000-b000-000000000000";


    public static String CURRENT_TEMPERATURE = "00002a1e-0000-1000-8000-00805f9b34fb";
    public static String HISTORY_TEMPERATURE = "00002a1c-0000-1000-8000-00805f9b34fb";
    public static String READ_SERIAL = "00002a25-0000-1000-8000-00805f9b34fb";//read
    public static String WRITE_SERIAL = "00002a71-0000-1000-8000-00805f9b34fb";//read
    public static String TEMPERATURE_UNIT = "00002a75-0000-1000-8000-00805f9b34fb";
    public static String ERROR_RATE = "00002a76-0000-1000-8000-00805f9b34fb";
    public static String DEVICE_VERSION = "00002a28-0000-1000-8000-00805f9b34fb";
//    public static String ERROR_RATE = "00002a76-0000-1000-8000-00805f9b34fb";

    public static String TEMPERATURE_TIME = "00002a2b-0000-1000-8000-00805f9b34fb";
    public static String LCD = "00002a74-0000-1000-8000-00805f9b34fb";
    public static String USER_ID = "00002a73-0000-1000-8000-00805f9b34fb";
    public static String SEND = "00002a70-0000-1000-8000-00805f9b34fb";


}
