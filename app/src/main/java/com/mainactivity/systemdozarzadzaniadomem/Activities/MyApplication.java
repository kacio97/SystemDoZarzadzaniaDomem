package com.mainactivity.systemdozarzadzaniadomem.Activities;

import android.app.Application;

import com.mainactivity.systemdozarzadzaniadomem.Models.ServerDevice;

import java.util.HashMap;

public class MyApplication extends Application {

    private static MyApplication instance;
    private final HashMap<String, ServerDevice> devices = new HashMap<>();

    public static MyApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        //TUDAJ COś MAM IMPLEMENTOWAć?
        instance = this;
    }

    public void setDevice(ServerDevice device) {
        devices.put(device.getDeviceName(), device);
    }

    public ServerDevice getDevice(String name) {
        return devices.get(name);
    }
}
