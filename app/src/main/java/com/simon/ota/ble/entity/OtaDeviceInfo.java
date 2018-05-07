package com.simon.ota.ble.entity;

import java.io.Serializable;

/**
 * Created by liu on 17/3/10.
 */

public class OtaDeviceInfo implements Comparable<OtaDeviceInfo>, Serializable {
    private boolean isAdjust;
    private  long time;
    private double temperature;
    private int rate;
    private int rssi;
    private String serialNum;
    private String mac;
    private boolean testState;

    public OtaDeviceInfo(int rssi, String serialNum, String mac) {
        this.rssi = rssi;
        this.serialNum = serialNum;
        this.mac = mac;

    }

    public OtaDeviceInfo(String mac, int rssi, int rate, double temperature, long time, boolean isAdjust) {
       this(mac, rssi, rate, temperature);
        this.time=time;
        this.isAdjust=isAdjust;
    }
    
    public OtaDeviceInfo(String mac, int rssi, int rate, double temperature) {
        this.mac = mac;
        this.rssi = rssi;
        this.rate = rate;
        this.temperature = temperature;
    }


    public String getSerialNum() {
        return serialNum;
    }

    public void setSerialNum(String serialNum) {
        this.serialNum = serialNum;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    @Override
    public int compareTo(OtaDeviceInfo o) {
        return o.getRssi() - rssi;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public int getRate() {
        return rate;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }

    public boolean isTestState() {
        return testState;
    }

    public void setTestState(boolean testState) {
        this.testState = testState;
    }

    public boolean isAdjust() {
        return isAdjust;
    }

    public void setAdjust(boolean adjust) {
        isAdjust = adjust;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
