package com.simon.ota.ble.entity;

@SuppressWarnings("all")
public class OtaHeaderInfo {
    private int ver;
    private int len;

    public int getVer() {
        return ver;
    }

    public void setVer(int ver) {
        this.ver = ver;
    }

    public int getLen() {
        return len;
    }

    public void setLen(int len) {
        this.len = len;
    }

    public OtaHeaderInfo(int ver, int len) {
        this.ver = ver;
        this.len = len;
    }
}
