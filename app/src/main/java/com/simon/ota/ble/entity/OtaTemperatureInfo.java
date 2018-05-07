package com.simon.ota.ble.entity;

@SuppressWarnings("all")
public class OtaTemperatureInfo {
    int unit;


    int errorRateState;
    double temperature;
    int year, month, day, hour, min, second;


    public int getUnit() {
        return unit;
    }

    public void setUnit(int unit) {
        this.unit = unit;
    }

    public int getErrorRateState() {
        return errorRateState;
    }

    public void setErrorRateState(int errorRateState) {
        this.errorRateState = errorRateState;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public int getSecond() {
        return second;
    }

    public void setSecond(int second) {
        this.second = second;
    }

    public OtaTemperatureInfo(int unit, int errorRateState, double temperature, int year, int month, int day, int hour, int min, int second) {
        this.unit = unit;
        this.errorRateState = errorRateState;
        this.temperature = temperature;
        this.year = year;
        this.month = month;
        this.day = day;
        this.hour = hour;
        this.min = min;
        this.second = second;
    }


    public OtaTemperatureInfo() {
    }
}
