package com.mainactivity.systemdozarzadzaniadomem.Activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
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
import com.mainactivity.systemdozarzadzaniadomem.Models.TopicModel;
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
import java.util.HashMap;
import java.util.Map;

public class DeviceMainboardActivity extends AppCompatActivity implements MqttCallback, DeviceMainboardActivityAdapter.ItemClickListener {

    private static final String TAG = "DeviceMainBoardAcitivty";
    private static final String TAG_SERVER_CON = "ServerConnection";
    private static final int CREATE_MODULE_REQUEST_CODE = 1;
    private HashMap<String, TopicModel> topics = new HashMap<>();
//    private HashMap<String, String> values = new HashMap<>();


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
        Type type = new TypeToken<HashMap<String, TopicModel>>() {
        }.getType();

        if (getPreferences(MODE_PRIVATE).contains(key + deviceName)) {
            topics = new Gson().fromJson(JSONstring, type);
        }

//        for (int i = 0; i < topics.size(); i++) {
//            values.put(getHashMapKeyFromIndex(topics, i), "");
//        }

        //Jak aktualizować dane dla danego modułu ?
        //Tzn mamy jakiś kafel z tematem i czy aktualizacja jednego z TextView robie prawidłowo ?
        adapter = new DeviceMainboardActivityAdapter(this, topics);
        adapter.setOnClickListener(this);
        recyclerView.setAdapter(adapter);
        //TODO: AKTUALIZACJA INTERFEJSU OBIEKTY W HASHMAPIE DODAJA SiĘ NA POCZATEK STOSU WiEC MUSZE JAKOS AKTUALIZWoAC
        // ODPOWIEDNIO POZYCJE NA RECYCLERVIEW
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
                addNewTopic(data.getStringExtra("topicValue"), data.getStringExtra("topicTitle"));
                adapter.notifyDataSetChanged();
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
                            String t = getHashMapKeyFromIndex(topics, i);
                            client.subscribe(t, 1);
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

    private String getHashMapKeyFromIndex(HashMap<String, TopicModel> hashMap, int index) {
        String key = null;
        int pos = 0;
        for (Map.Entry<String, TopicModel> entry : hashMap.entrySet()) {
            if (index == pos) {
                key = entry.getKey();
            }
            pos++;
        }
        return key;
    }

    private void addNewTopic(String topicValue, String topicTitle) {

        if (topics.containsKey(topicValue)) {
            Toast.makeText(getApplicationContext(), "Taki temat już istnieje", Toast.LENGTH_SHORT).show();
        } else {
            TopicModel model = new TopicModel();
            model.setTopicName(topicTitle);
            model.setValue("");
            topics.put(topicValue, model);
            Toast.makeText(getApplicationContext(), "Dodano nowy temat", Toast.LENGTH_SHORT).show();
            updateTopicList(topics);
            adapter.notifyDataSetChanged();
        }
    }

    private void updateTopicList(HashMap<String, TopicModel> topics) {
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
        TopicModel model = topics.get(topic);
        model.setValue(message.toString());
        topics.put(topic, model);
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
        showPopupMenu(view, position);
    }

    private void showPopupMenu(View view, final int position) {
        PopupMenu menu = new PopupMenu(this, view);
        menu.inflate(R.menu.popup_menu_device_mainboard);
        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_edit: {
                        Toast.makeText(DeviceMainboardActivity.this, "MENU EDIT position " + position, Toast.LENGTH_SHORT).show();
                        //TODO: Implementacja edycji i usuwania kafelków, REMAKE przechowywania danych
                        // chodzi o arrayList z tematami za dużo nadmiernych operacji przenoszenia ZGUBI MNIE TO !!
//                        Intent intent = new Intent(getApplicationContext(), CreateModuleActivity.class);
//                        String topic = topicsAndValues.get(to)
//                        intent.putExtra("topicName", topic);
//                        startActivityForResult(intent, CREATE_MODULE_REQUEST_CODE);
                        return true;
                    }
                    case R.id.menu_delete: {
                        String key = getHashMapKeyFromIndex(topics,position);
                        adapter.removeItem(key, position);
                        topics.remove(key);
                        updateTopicList(topics);
                        adapter.notifyDataSetChanged();
                        Toast.makeText(DeviceMainboardActivity.this, "MENU DELETE position " + position, Toast.LENGTH_SHORT).show();
                        return true;
                    }
                    default:
                        return false;
                }
            }
        });
        menu.show();
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