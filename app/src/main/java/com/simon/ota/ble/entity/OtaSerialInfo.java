package com.simon.ota.ble.entity;

@SuppressWarnings("all")
public class OtaSerialInfo {
    private String mac;
    private String serial;
    private long date;

    public OtaSerialInfo(String mac, String serial, long date) {
        this.mac = mac;
        this.serial = serial;
        this.date = date;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    @Override
    public String toString() {
        return mac + " = " + serial;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }
}
