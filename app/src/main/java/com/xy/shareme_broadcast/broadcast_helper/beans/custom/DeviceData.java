package com.xy.shareme_broadcast.broadcast_helper.beans.custom;

import java.io.Serializable;

public class DeviceData implements Serializable {
    private static final long serialVersionUID = 1L;
    private String device;
    private String token;

    // Important Constructor without Arguments
    public DeviceData() {
    }

    public DeviceData(String token) {
        this.setDevice("android");
        this.setToken(token);
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
