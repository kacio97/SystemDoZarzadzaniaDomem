package com.mainactivity.systemdozarzadzaniadomem.Models;

import java.io.Serializable;

/**
 * Klasa reprezentująca model dla serwera MQTT
 */
public class ServerDevice implements Serializable {

    private String deviceName;
    private String deviceIP;
    private String port;
    private String clientID;

    public ServerDevice() {
    }

    public ServerDevice(String deviceName, String deviceIP, String port, String clientID) {
        this.deviceName = deviceName;
        this.deviceIP = deviceIP;
        this.port = port;
        this.clientID = clientID;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceIP() {
        return deviceIP;
    }

    public void setDeviceIP(String deviceIP) {
        this.deviceIP = deviceIP;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getClientID() {
        return clientID;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }


}
