package com.mainactivity.systemdozarzadzaniadomem.Activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mainactivity.systemdozarzadzaniadomem.Adapters.DeviceMainboardActivityAdapter;
import com.mainactivity.systemdozarzadzaniadomem.Models.ServerDevice;
import com.mainactivity.systemdozarzadzaniadomem.R;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.ArrayList;

public class DeviceMainboardActivity extends AppCompatActivity implements MqttCallback, DeviceMainboardActivityAdapter.ItemClickListener {

    private static final String TAG = "DeviceMainBoardAcitivty";
    private static final String TAG_SERVER_CON = "ServerConnection";
    private ArrayList<String> topics = new ArrayList<>();
    MqttAndroidClient client;
    FloatingActionButton actionButton;
    DeviceMainboardActivityAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_mainboard);
        RecyclerView recyclerView = findViewById(R.id.rvTopics);
        int numberOfColumn = 2;
        recyclerView.setLayoutManager(new GridLayoutManager(this, numberOfColumn));

        topics.add("temperatura");
        topics.add("ciśnienie");
        topics.add("Wilgotność");
        topics.add("Wysokość NPM");
        adapter = new DeviceMainboardActivityAdapter(this, topics);
        adapter.setOnClickListener(this);
        recyclerView.setAdapter(adapter);
        actionButton = findViewById(R.id.fabAddNewModule);

        if (getIntent().hasExtra("device")) {
            ServerDevice serverDevice = (ServerDevice) getIntent().getSerializableExtra("device");
            client = new MqttAndroidClient(this, "tcp://" + serverDevice.getDeviceIP(), serverDevice.getClientID());
            /*
             * Metoda służąca do nasłuchiwania:
             * wiadomość dotarła jest gotowa do przetworzenia
             * czy połączenie zostało zerwane
             * wiadomosc zostala wyslana do serwera
             * */
            client.setCallback(this);
            try {
                client.connect(new MqttConnectOptions(), null, new IMqttActionListener() {
                    @SuppressLint("RestrictedApi")
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        Log.d(TAG_SERVER_CON, "UDALO SIE POLACZYC ");
                        Toast.makeText(getApplicationContext(), "Nawiązano połączenie z serwerem " + client.getServerURI(), Toast.LENGTH_LONG).show();
                        actionButton.setVisibility(View.VISIBLE);
                    }

                    @SuppressLint("RestrictedApi")
                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        Log.d(TAG_SERVER_CON, "NIE UDALO SIE POLACZYC " + exception.getMessage());
                        Toast.makeText(getApplicationContext(), "Nie udało się nawiązać połączenia z serwerem " + client.getServerURI(), Toast.LENGTH_LONG).show();
                        actionButton.setVisibility(View.VISIBLE);
                    }

                });

            } catch (MqttException e) {
                e.printStackTrace();
            }

        }
    }


    @SuppressLint("RestrictedApi")
    @Override
    public void connectionLost(Throwable cause) {
        Log.d(TAG, "connectionLost");
        Toast.makeText(getApplicationContext(), "Utracono połączenie z serwerem " + client.getServerURI(), Toast.LENGTH_SHORT).show();
        actionButton.setVisibility(View.INVISIBLE);
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

    @Override
    public void onItemClick(View view, int position) {

    }

    @Override
    public void onLongItemClick(View view, int position) {

    }


                            /*try {
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
    }*/

}