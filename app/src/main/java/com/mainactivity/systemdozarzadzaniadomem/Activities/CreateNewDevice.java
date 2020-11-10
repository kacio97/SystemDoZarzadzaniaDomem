package com.mainactivity.systemdozarzadzaniadomem.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.mainactivity.systemdozarzadzaniadomem.Models.ServerDevice;
import com.mainactivity.systemdozarzadzaniadomem.R;

import java.io.Serializable;

public class CreateNewDevice extends AppCompatActivity implements Serializable {
    boolean oldDevice = false;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_device);

        final EditText deviceName = findViewById(R.id.etDeviceName);
        final EditText adressIP = findViewById(R.id.etIpAdress);
        final EditText clientID = findViewById(R.id.etClientID);
        final EditText port = findViewById(R.id.etPort);
        ImageButton saveData = findViewById(R.id.ibSaveData);


        if (getIntent().hasExtra("oldDevice")) {
            ServerDevice serverDevice = (ServerDevice) getIntent().getSerializableExtra("oldDevice");
            adressIP.setText(serverDevice.getDeviceIP());
            deviceName.setText(serverDevice.getDeviceName());
            clientID.setText(serverDevice.getClientID());
            port.setText(serverDevice.getPort());
            oldDevice = true;
        }

        saveData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**
                 * ServerDevice obiekt służy do inizjalizacji nowego urządzenia listę urządzeń typu serwer
                 */


                if (oldDevice) {
                    ServerDevice serverDevice = (ServerDevice) getIntent().getSerializableExtra("oldDevice");
                    serverDevice.setDeviceIP(adressIP.getText().toString());
                    serverDevice.setDeviceName(deviceName.getText().toString());
                    serverDevice.setClientID(clientID.getText().toString());
                    serverDevice.setPort(port.getText().toString());
                    Intent intent = new Intent();
                    intent.putExtra("editedDevice", serverDevice);
                    intent.putExtra("position", getIntent().getSerializableExtra("position"));
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                } else {
                    ServerDevice device = new ServerDevice();
                    device.setDeviceIP(adressIP.getText().toString());
                    device.setDeviceName(deviceName.getText().toString());
                    device.setClientID(clientID.getText().toString());
                    device.setPort(port.getText().toString());
                    Intent intent = new Intent();
                    intent.putExtra("newDevice", device);
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                }


            }
        });
    }


}
