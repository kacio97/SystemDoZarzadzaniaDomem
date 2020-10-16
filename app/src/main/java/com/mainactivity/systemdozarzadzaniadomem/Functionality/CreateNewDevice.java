package com.mainactivity.systemdozarzadzaniadomem.Functionality;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.mainactivity.systemdozarzadzaniadomem.Activities.MainActivity;
import com.mainactivity.systemdozarzadzaniadomem.Models.ServerDevice;
import com.mainactivity.systemdozarzadzaniadomem.R;

import java.io.Serializable;

public class CreateNewDevice extends AppCompatActivity implements Serializable {


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

        saveData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**
                 * ServerDevice obiekt służy do inizjalizacji nowego urządzenia listę urządzeń typu serwer
                 */
                ServerDevice device = new ServerDevice();

                device.setDeviceIP(adressIP.getText().toString());
                device.setDeviceName(deviceName.getText().toString());
                device.setClientID(clientID.getText().toString());
                device.setPort(port.getText().toString());

                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.putExtra("newDevice", device);
                startActivity(intent);
            }
        });
    }
}
