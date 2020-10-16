package com.mainactivity.systemdozarzadzaniadomem.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.mainactivity.systemdozarzadzaniadomem.Functionality.CreateNewDevice;
import com.mainactivity.systemdozarzadzaniadomem.Models.ServerDevice;
import com.mainactivity.systemdozarzadzaniadomem.R;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements MqttCallback, Serializable {

    private static final String TAG = "ELO";
    //TODO: Zrobić sharedPreferences dla ArrayList<ServiceDevice>
    private ArrayList<ServerDevice> devices = new ArrayList<>();

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.addNewDevice: {
                Intent intent = new Intent(getApplicationContext(), CreateNewDevice.class);
                startActivity(intent);
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);
        return true;
    }


//    TextView temp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if (getIntent().hasExtra("newDevice")) {
            ServerDevice serverDevice = (ServerDevice) getIntent().getSerializableExtra("newDevice");
            addNewDevice(serverDevice);
        }

//        temp = findViewById(R.id.temperatura);


    }

    public void addNewDevice(ServerDevice s) {

        boolean exist = false;
        for (int i = 0; i <= devices.size(); i++) {
            if (devices.isEmpty()) {
                devices.add(s);
                Toast msg = Toast.makeText(getApplicationContext(), "Udało się dodać nowe urządzenie", Toast.LENGTH_LONG);
                msg.show();
                exist = false;
                break;
            }

            if (devices.get(i).getClientID().equals(s.getClientID())) {
                exist = true;
            } else {
                devices.add(s);
                Toast msg = Toast.makeText(getApplicationContext(), "Udało się dodać nowe urządzenie", Toast.LENGTH_LONG);
                msg.show();
                exist = false;
                break;

            }
        }

        if (exist) {
            Toast msg = Toast.makeText(getApplicationContext(), "Takie urządzenie już istnieje", Toast.LENGTH_LONG);
            msg.show();
        }
    }

    public void serwerConnection() {
        try {
            String clientId = MqttClient.generateClientId();

            MqttAndroidClient client = new MqttAndroidClient(this, "tcp://192.168.1.20:1883", clientId);
            /*
             * Metoda służąca do nasłuchiwania:
             * wiadomość dotarła jest gotowa do przetworzenia
             * czy połączenie zostało zerwane
             * wiadomosc zostala wyslana do serwera
             * */
            client.setCallback(this);


            client.connect(new MqttConnectOptions(), null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d(TAG, "UDALO SIE POLACZYC ");
                    try {
                        asyncActionToken.getClient().subscribe("temperatura", 1, null, new IMqttActionListener() {
                            @Override
                            public void onSuccess(IMqttToken asyncActionToken) {
                                IMqttMessageListener mqttMessageListener = new IMqttMessageListener() {
                                    @Override
                                    public void messageArrived(String topic, MqttMessage message) throws Exception {
                                        Log.d(TAG, "Pobieram Dane " + topic);
//                                        temp.setText(message.toString());
                                    }
                                };
                            }

                            @Override
                            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {

                            }
                        });
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.d(TAG, "NIE UDALO SIE POLACZYC " + exception.getMessage());
                }

            });


        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        Log.d(TAG, "connectionLost");
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        Log.d(TAG, topic);
        String temperatura = message.toString();
//        temp.setText(temperatura);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        Log.d(TAG, "deliveryComplete");
    }
}
