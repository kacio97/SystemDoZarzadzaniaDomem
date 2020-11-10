package com.mainactivity.systemdozarzadzaniadomem.Activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
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

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

public class DeviceMainboardActivity extends AppCompatActivity implements MqttCallback, DeviceMainboardActivityAdapter.ItemClickListener {

    private static final String TAG = "DeviceMainBoardAcitivty";
    private static final String TAG_SERVER_CON = "ServerConnection";
    private static final int CREATE_MODULE_REQUEST_CODE = 1;
    private ArrayList<String> topics = new ArrayList<>();
    private final HashMap<String, String> topicsAndValues = new HashMap<>();

    MqttAndroidClient client;
    FloatingActionButton actionButton;
    DeviceMainboardActivityAdapter adapter;
    private final String key = "Topics";
    private String topicValue;
    private String deviceName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_mainboard);
        RecyclerView recyclerView = findViewById(R.id.rvTopics);
        int numberOfColumn = 2;
        recyclerView.setLayoutManager(new GridLayoutManager(this, numberOfColumn));
        deviceName = getIntent().getStringExtra("device");

        String JSONstring = getPreferences(MODE_PRIVATE).getString(key + deviceName, null);
        Type type = new TypeToken<ArrayList<String>>() {
        }.getType();

        if (getPreferences(MODE_PRIVATE).contains(key + deviceName)) {
            topics = new Gson().fromJson(JSONstring, type);
        }

        for (int i = 0; i < topics.size(); i++) {
            topicsAndValues.put(topics.get(i), "");
        }

        //Jak aktualizować dane dla danego modułu ?
        //Tzn mamy jakiś kafel z tematem i czy aktualizacja jednego z TextView robie prawidłowo ?
        adapter = new DeviceMainboardActivityAdapter(this, topics, topicsAndValues);
        adapter.setOnClickListener(this);
        recyclerView.setAdapter(adapter);

        actionButton = findViewById(R.id.fabAddNewModule);
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), CreateModuleActivity.class);
                startActivityForResult(intent, CREATE_MODULE_REQUEST_CODE);
            }
        });

        ServerDevice s = MyApplication.getInstance().getDevice(getIntent().getStringExtra("device"));
        client = new MqttAndroidClient(this, "tcp://" + s.getDeviceIP(), s.getClientID());

        /*
         * Metoda służąca do nasłuchiwania:
         * wiadomość dotarła jest gotowa do przetworzenia
         * czy połączenie zostało zerwane
         * wiadomosc zostala wyslana do serwera
         * */
        client.setCallback(this);
        connectClient();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CREATE_MODULE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                addNewTopic(data.getStringExtra("topicText"));
                adapter.notifyDataSetChanged();
//                Intent intent = getIntent();
//                finish();
//                startActivity(intent);
            }
        }
    }

    private void connectClient() {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(false);
        try {
            client.connect(options, null, new IMqttActionListener() {
                @SuppressLint("RestrictedApi")
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d(TAG_SERVER_CON, "UDALO SIE POLACZYC ");
                    Toast.makeText(getApplicationContext(), "Nawiązano połączenie z serwerem " + client.getServerURI(), Toast.LENGTH_LONG).show();
                    actionButton.setVisibility(View.VISIBLE);

                    //Połączenie jest ale czy subskrybcja tematów jest okej ? Bo to chyba nie tak powinno być.
                    //Myślałem że w pętli może by pykło ale po przemyśleniu to chyba średni pomysł
                    for (int i = 0; i < topics.size(); i++) {
//                        subscribeTopic(topics.get(i), asyncActionToken, i);
                        try {
                            client.subscribe(topics.get(i), 1);
                        } catch (MqttException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @SuppressLint("RestrictedApi")
                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.d(TAG_SERVER_CON, "NIE UDALO SIE POLACZYC " + exception.getMessage());
                    Toast.makeText(getApplicationContext(), "Nie udało się nawiązać połączenia z serwerem " + client.getServerURI(), Toast.LENGTH_LONG).show();
                    actionButton.setVisibility(View.INVISIBLE);
                }

            });

        } catch (MqttException e) {
            e.printStackTrace();
        }

    }

    private void addNewTopic(String topic) {
        boolean exist = false;

        if (topics.isEmpty()) {
            topics.add(topic);
            Toast.makeText(getApplicationContext(), "Dodano nowy temat", Toast.LENGTH_SHORT).show();
            updateTopicList(topics);
        } else {
            for (int i = 0; i < topics.size(); i++) {
                if (topics.get(i).equals(topic)) {
                    exist = true;
                    break;
                }
            }
            if (exist) {
                Toast.makeText(getApplicationContext(), "Taki temat już istnieje", Toast.LENGTH_SHORT).show();
            } else {
                topics.add(topic);
                Toast.makeText(getApplicationContext(), "Dodano nowy temat", Toast.LENGTH_SHORT).show();
                updateTopicList(topics);
            }
        }
    }

    private void updateTopicList(ArrayList<String> topics) {
        SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
        String json = new Gson().toJson(topics);
        editor.clear();
        editor.putString(key + deviceName, json);
        editor.commit();
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
        for (int i = 0; i < topics.size(); i++) {
            topicsAndValues.put(topic, message.toString());
        }
        adapter.notifyDataSetChanged();
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


//    private void subscribeTopic(String topic, IMqttToken asyncActionToken, int position) {
//        try {
//            client.subscribe(topic, 1, null, new IMqttActionListener() {
//                @Override
//                public void onSuccess(IMqttToken asyncActionToken) {
//                    IMqttMessageListener mqttMessageListener = new IMqttMessageListener() {
//                        @Override
//                        public void messageArrived(String topic, MqttMessage message) throws Exception {
//                            Log.d(TAG, "Pobieram Dane " + topic);
//                            // Niby tutaj sobie to przekazuje do adaptera ale nie jestem przekonany że tak to ma wyglądać
//                            // Według mnie po prostu znikają mi pewne intenty i tracę część informacji do ustalania co to za
//                            //urządzenie i jakie posiada tematy, więc wszystko sprowadza się do tego Application.class
//                            // którego nie bardzo wiem jak zastosować bo samo to o co w tym chodzi to mniej więcej rozumiem
//                            topicValue = message.toString();
//
//                        }
//                    };
//                }
//
//                @Override
//                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
//
//                }
//            });
//        } catch (MqttException e) {
//            e.printStackTrace();
//        }
//    }

}