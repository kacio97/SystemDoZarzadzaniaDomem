package com.mainactivity.systemdozarzadzaniadomem.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EdgeEffect;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mainactivity.systemdozarzadzaniadomem.Adapters.MainActivityAdapter;
import com.mainactivity.systemdozarzadzaniadomem.Functionality.CreateNewDevice;
import com.mainactivity.systemdozarzadzaniadomem.Models.ServerDevice;
import com.mainactivity.systemdozarzadzaniadomem.R;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.*;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Objects;


public class MainActivity extends AppCompatActivity implements MqttCallback, Serializable, MainActivityAdapter.ItemClickListener {

    private static final String TAG = "ELO";
    ArrayList<ServerDevice> devices = new ArrayList<>();
    SharedPreferences preferences;
    private final String key = "Devices";
    MainActivityAdapter adapter;

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
        RecyclerView recyclerView = findViewById(R.id.rvServerDevices);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        String JSONstring = getPreferences(MODE_PRIVATE).getString(key, null);
        Type type = new TypeToken<ArrayList<ServerDevice>>() {
        }.getType();

        if (getPreferences(MODE_PRIVATE).contains(key)) {
            devices = new Gson().fromJson(JSONstring, type);
        }
        if (getIntent().hasExtra("newDevice")) {
            ServerDevice serverDevice = (ServerDevice) getIntent().getSerializableExtra("newDevice");
            addNewDevice(serverDevice);
        }


        adapter = new MainActivityAdapter(devices, getApplicationContext());
        adapter.setOnCLickListener(this);
        recyclerView.setAdapter(adapter);

//        temp = findViewById(R.id.temperatura);


    }

    @Override
    public void onItemClick(View view, int positon) {
        Toast.makeText(this, "Item: " + adapter.getItem(positon), Toast.LENGTH_SHORT).show();
    }

    public void addNewDevice(ServerDevice s) {

        SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();

        boolean exist = false;

        if (devices.isEmpty()) {
            devices.add(s);
            //TODO:
            String json = new Gson().toJson(devices);
            editor.putString(key, json);
            editor.commit();
            Log.d(TAG, "dodano urzadzenie " + s.toString());
            Toast msg = Toast.makeText(getApplicationContext(), "Udało się dodać nowe urządzenie", Toast.LENGTH_LONG);
            msg.show();
        } else {
            for (int i = 0; i < devices.size(); i++) {
                if (devices.get(i).getClientID().equals(s.getClientID())) {
                    exist = true;
                } else {
                    devices.add(s);
                    String json = new Gson().toJson(devices);
                    editor.putString(key, json);
                    editor.commit();
                    Log.d(TAG, "dodano urzadzenie " + s.toString());
                    Toast msg = Toast.makeText(getApplicationContext(), "Udało się dodać nowe urządzenie", Toast.LENGTH_LONG);
                    msg.show();
                    exist = false;
                    break;
                }
            }
        }

        if (exist) {
            Toast msg = Toast.makeText(getApplicationContext(), "Takie urządzenie już istnieje", Toast.LENGTH_LONG);
            String json = new Gson().toJson(devices);
            editor.putString(key, json);
            editor.commit();
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
