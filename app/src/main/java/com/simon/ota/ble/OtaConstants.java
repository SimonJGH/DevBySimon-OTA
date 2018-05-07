package com.simon.ota.ble;

@SuppressWarnings("all")
public class OtaConstants {
    public static final String DEVICENAME = "Care-1314";
    public static final String OTANAME = "cinfor oad";

    public class TemperatureUnit {
        public static final int C = 0;
        public static final int F = 1;


        public TemperatureUnit() {
        }
    }

    public static final int CODE_ERROR = 3;
    public static final int DEVICETYPE = 13;


    public class ConnectState {
        public static final int STATE_NONE = 0;
        public static final int STATE_CONNECTING = 1;
        public static final int STATE_CONNECTED = 2;
        public static final int STATE_DISCONNECTED = 3;
        public static final int STATE_FAILE = 4;
        public static final int STATE_SUPPORT_SERVICE_SUCCEED = 5;    //获取实时数据
        public static final int STATE_SUPPORT_SERVICE_FAILE = 6;
        public static final int STATE_SUPPORT_OTA = 7;
    }


}
