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
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

//TODO: odswiezanie na onSwipe w dol lub ewentulanie auto-refresh dla GUI jak to zrobic ?
public class DeviceMainboardActivity extends AppCompatActivity implements DeviceMainboardActivityAdapter.ItemClickListener {

    private static final String TAG = "DeviceMainBoardAcitivty";
    private static final String TAG_SERVER_CON = "ServerConnection";
    private static final int CREATE_MODULE_REQUEST_CODE = 1;
    private static final int SET_LED_COLOR = 2;
    private HashMap<String, TopicModel> topics = new HashMap<>();
    RecyclerView recyclerView;
    IMqttToken iMqttToken;

    MqttAndroidClient client;
    FloatingActionButton actionButton;
    DeviceMainboardActivityAdapter adapter;
    private final String key = "Topics";
    private String deviceName;
    private boolean isOn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_mainboard);
        int numberOfColumn = 2;
        recyclerView = findViewById(R.id.rvTopics);
        recyclerView.setLayoutManager(new GridLayoutManager(this, numberOfColumn));
        deviceName = getIntent().getStringExtra("device");


         //* Przywracanie stanu danych po uruchomieniu activity
        String JSONstring = getPreferences(MODE_PRIVATE).getString(key + deviceName, null);
        Type type = new TypeToken<HashMap<String, TopicModel>>() {
        }.getType();

        if (getPreferences(MODE_PRIVATE).contains(key + deviceName)) {
            topics = new Gson().fromJson(JSONstring, type);
        }


        adapter = new DeviceMainboardActivityAdapter(this, topics);
        adapter.setOnClickListener(this);
        recyclerView.setAdapter(adapter);
        //TODO: AKTUALIZACJA INTERFEJSU OBIEKTY W HASHMAPIE DODAJA SiĘ NA POCZATEK STOSU WiEC MUSZE JAKOS AKTUALIZWoAC
        // ODPOWIEDNIO POZYCJE NA RECYCLERVIEW
        actionButton = findViewById(R.id.fabAddNewModule);
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PopupMenu menu = new PopupMenu(getApplicationContext(), v);
                menu.inflate(R.menu.popup_menu_device_mainboard_add_topic);

                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.menu_text: {
//                                Toast.makeText(DeviceMainboardActivity.this, "MENU TEXT", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getApplicationContext(), CreateModuleActivity.class);
                                intent.putExtra("topicType", "text");
                                startActivityForResult(intent, CREATE_MODULE_REQUEST_CODE);
                                return true;
                            }
                            case R.id.menu_LED: {
                                Intent intent = new Intent(getApplicationContext(), CreateModuleActivity.class);
                                intent.putExtra("topicType", "led");
                                startActivityForResult(intent, CREATE_MODULE_REQUEST_CODE);
                                return true;
                            }
                            case R.id.menu_button: {
                                Intent intent = new Intent(getApplicationContext(), CreateModuleActivity.class);
                                intent.putExtra("topicType", "button");
                                startActivityForResult(intent, CREATE_MODULE_REQUEST_CODE);
                                return true;
                            }
                            default:
                                return false;
                        }
                    }
                });
                menu.show();
            }
        });

        ServerDevice s = MyApplication.getInstance().getDevice(getIntent().getStringExtra("device"));
        client = new MqttAndroidClient(this, "tcp://" + s.getDeviceIP(), s.getClientID());
        connectClient();
    }

    /**
     * Metoda która uruchamia się i zbiera dane po przejściu z zakończonego Activity
     *
     * @param requestCode Kod zwrócony z innego activity (Określa jaka czynność była wykonana)
     * @param resultCode  Kod wynikowy (Czy rezultat jest prawidłowy)
     * @param data        Dane przesłane przez intent
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CREATE_MODULE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                addNewTopic(data.getStringExtra("topicKey"), data.getStringExtra("topicTitle"), data.getStringExtra("topicType"), data.getBooleanExtra("edit", false));
                adapter.notifyDataSetChanged();
            }
        }
        if (requestCode == SET_LED_COLOR) {
            if (resultCode == RESULT_OK) {
                String r = data.getStringExtra("colorRed");
                String g = data.getStringExtra("colorGreen");
                String b = data.getStringExtra("colorBlue");

                int red = (255 - Integer.parseInt(r)) * 4;
                int green = (255 - Integer.parseInt(g)) * 4;
                int blue = (255 - Integer.parseInt(b)) * 4;

                String message = red + "," + green + "," + blue + ",";
                MqttMessage mqttMessage = new MqttMessage(message.getBytes());
                try {
                    mqttMessage.setRetained(true);
                    client.publish("ledOutput", mqttMessage);
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Metoda odpowiada za połączenie z serwerem MQTT oraz subskrybcje tematów po udanym połączeniu
     * Jeżeli połączenie nie powiedzie się zostanie uruchomiona ponowna próba połączenia metoda onFailure()
     */
    private void connectClient() {
        /*
         * Metoda służąca do nasłuchiwania:
         * wiadomość dotarła jest gotowa do przetworzenia
         * czy połączenie zostało zerwane
         * wiadomosc zostala wyslana do serwera
         * */
        client.setCallback(new MqttCallbackExtended() {
            @SuppressLint("RestrictedApi")
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                if (reconnect) {
                    Log.d(TAG, "reconnect: ");
                    Toast.makeText(DeviceMainboardActivity.this, "Ponownie polaczono z serwerem: " + serverURI, Toast.LENGTH_SHORT).show();
                    subscribe();
                    actionButton.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.VISIBLE);
                } else {
                    Log.d(TAG, "first connect: ");
                    Toast.makeText(DeviceMainboardActivity.this, "Polaczono z serwerem: " + serverURI, Toast.LENGTH_SHORT).show();
                    actionButton.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.VISIBLE);
                }
            }

            @SuppressLint("RestrictedApi")
            @Override
            public void connectionLost(Throwable cause) {
                Toast.makeText(getApplicationContext(), "Utracono połączenie z serwerem connectionLost " + client.getServerURI(), Toast.LENGTH_SHORT).show();
                actionButton.setVisibility(View.INVISIBLE);
                recyclerView.setVisibility(View.GONE);
                unsubscribe();
                Toast.makeText(getApplicationContext(), "Ponowna próba połączenia z serwerem connectionLost ", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Log.d(TAG, topic);
                if (topic.equals("ison")) {
                    String tmp = "";
                    String msg = message.toString();
                    int index = 0;
                    int red = 0;
                    int green = 0;
                    int blue = 0;

                    for (int i = 0; i < msg.length(); i++) {
                        if (msg.charAt(i) == ',' && index == 0) {
                            red = Integer.parseInt(tmp);
                            index++;
                            tmp = "";
                            continue;
                        }
                        if (msg.charAt(i) == ',' && index == 1) {
                            green = Integer.parseInt(tmp);
                            index++;
                            tmp = "";
                            continue;
                        }
                        if (msg.charAt(i) == ',' && index == 2) {
                            blue = Integer.parseInt(tmp);
                            index++;
                            tmp = "";
                            continue;
                        }
                        tmp += msg.charAt(i);
                    }

//            Jezeli ktorys z kolorow jest wiekszy od 0 to LED sa wlaczone
                    isOn = (red < 1020) || (green < 1020) || (blue < 1020);
                    if (isOn) {
                        red = 255 - (red / 4);
                        blue = 255 - (blue / 4);
                        green = 255 - (green / 4);
                        TopicModel model = topics.get("ledOutput");
                        model.setValue(red + "," + green + "," + blue + ",");
                        topics.put("ledOutput", model);
                    }
                } else if (topic.equals("light")) {
                    String msg = message.toString();
                    TopicModel model = topics.get("lightOut");

                    if (msg.equals("off")) {
                        model.setValue("Włącz");
                        topics.put("lightOut", model);
                    } else {
                        model.setValue("Wyłącz");
                        topics.put("lightOut", model);
                    }
                } else {
                    TopicModel model = topics.get(topic);
                    model.setValue(message.toString());
                    topics.put(topic, model);
                }
                updateTopicList(topics);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                Log.d(TAG, "deliveryComplete");
                Toast.makeText(getApplicationContext(), "Wysłano wiadomość", Toast.LENGTH_SHORT).show();
            }
        });

        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setConnectionTimeout(3);
        options.setCleanSession(false);

        try {
            client.connect(options, null, new IMqttActionListener() {
                @SuppressLint("RestrictedApi")
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d(TAG, "UDALO SIE POLACZYC ");
//                    Toast.makeText(getApplicationContext(), "Nawiązano połączenie z serwerem " + client.getServerURI(), Toast.LENGTH_SHORT).show();
//                    actionButton.setVisibility(View.VISIBLE);
//                    recyclerView.setVisibility(View.VISIBLE);
                    subscribe();
                }

                @SuppressLint("RestrictedApi")
                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.d(TAG, "NIE UDALO SIE POLACZYC " + exception.getMessage());
                    if (exception.getMessage().equals("Operacja nawiązywania połączenia jest już w toku")) {

                    } else if (exception.getMessage().equals("Połączenie z klientem zostało nawiązane")) {
                        actionButton.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.VISIBLE);
                    } else {
                        Toast.makeText(getApplicationContext(), "Nie udało się nawiązać połączenia z serwerem onFailure" + client.getServerURI(), Toast.LENGTH_SHORT).show();
                        actionButton.setVisibility(View.INVISIBLE);
                        recyclerView.setVisibility(View.GONE);
                        Toast.makeText(getApplicationContext(), "Ponowna próba połączenia z serwerem onFailure", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /**
     * Metoda subskrybuje wszystkie tematy z hashmapy topics
     */
    public void subscribe() {
        for (int i = 0; i < topics.size(); i++) {
//                        subscribeTopic(topics.get(i), asyncActionToken, i);
            try {
                String t = getHashMapKeyByIndex(topics, i);
                if (t.equals("ledOutput")) {
                    client.subscribe("ison", 1);
                } else if (t.equals("lightOut")) {
                    client.subscribe("light", 1);
                } else {
                    client.subscribe(t, 1);
                }

            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Metoda odsubskrybowywyje wszystkie tematy z hashmapy topics (przy zamykaniu activity)
     */
    public void unsubscribe() {
        for (int i = 0; i < topics.size(); i++) {
            try {
                String t = getHashMapKeyByIndex(topics, i);
                if (t.equals("ledOutput")) {
                    client.unsubscribe("ison");
                } else if (t.equals("lightOut")) {
                    client.unsubscribe("light");
                } else {
                    client.unsubscribe(t);
                    Log.d(TAG, "unsubcsribe: " + t);
                }

            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Metoda wykonuje czynności jakie zostały zapisane po wciśnieciu fizycznego guzika telefonu wstecz.
     * Metoda zamyka nam activity
     */
    @Override
    public void onBackPressed() {
//        TODO: zamykanie polaczenia
        unsubscribe();
        int count = 0;
        try {
            client.disconnect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
        Toast.makeText(this, "Zakończono połączenie z serwerem", Toast.LENGTH_SHORT).show();
        client.close();
        client.unregisterResources();
        Log.d(TAG, "finish activity: ");
    }

    /**
     * Metoda która pozwala wyciągnąć wartość klucza z hashmapy pod konkretnym indexem
     *
     * @param hashMap Mapa przechowująca wartości
     * @param index   Index z którego ma zostać wyciągnięta wartość
     * @return Klucz
     */
    private String getHashMapKeyByIndex(HashMap<String, TopicModel> hashMap, int index) {
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

    /**
     * Metoda która odpowiada za dodawanie nowych tematów do hashMapy, tematy te są wykorzystywane
     * do nasłuchiwania MQTT callback
     *
     * @param key        Klucz (do nasłuchiwania)
     * @param topicTitle Nazwa tematu (wyświetlana)
     * @param topicType  Typ tematu
     */
    private void addNewTopic(String key, String topicTitle, String topicType, Boolean edit) {

        if (topics.containsKey(key)) {
            Toast.makeText(getApplicationContext(), "Taki temat już istnieje", Toast.LENGTH_SHORT).show();
        } else {
            if (!edit) {
                TopicModel model = new TopicModel();
                model.setTopicName(topicTitle);
                if (topicType.equals("button")) {
                    model.setValue("Włącz");
                } else if (topicType.equals("led")) {
                    model.setValue("0,0,0,");
                } else {
                    model.setValue("n/A");
                }
                model.setTypeOfTopic(topicType);
                topics.put(key, model);
                Toast.makeText(getApplicationContext(), "Dodano nowy temat", Toast.LENGTH_SHORT).show();
                updateTopicList(topics);
                adapter.notifyDataSetChanged();
            } else {
                TopicModel model = new TopicModel();
                model.setTopicName(topicTitle);
                model.setValue("");
                model.setTypeOfTopic(topicType);
                topics.put(key, model);
                Toast.makeText(getApplicationContext(), "Zaktualizowano temat", Toast.LENGTH_SHORT).show();
                updateTopicList(topics);
                adapter.notifyDataSetChanged();
            }

        }
    }

    /**
     * Aktualizacja danych dotycząca zmian które wystąpiły w liście tematów np. edycja/usuniecie/dodanie
     * Dane przechowywane są w pamięci telefonu i odtwarzane podczas uruchamiania activity
     *
     * @param topics Lista tematów dla MQTT serwer
     */
    private void updateTopicList(HashMap<String, TopicModel> topics) {
        SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
        String json = new Gson().toJson(topics);
        editor.clear();
        editor.putString(key + deviceName, json);
        editor.commit();
    }

    @Override
    public void onItemClick(View view, int position) {
        String key = getHashMapKeyByIndex(topics, position);

        if (topics.get(key).getTypeOfTopic().equals("led")) {
            Intent intent = new Intent(getApplicationContext(), LedControlPanel.class);
            intent.putExtra("color", topics.get(key).getValue());
            intent.putExtra("ison", isOn);
            startActivityForResult(intent, SET_LED_COLOR);
        } else if (topics.get(key).getTypeOfTopic().equals("button")) {
            String message;
            if (topics.get(key).getValue().equals("Włącz")) {
                topics.get(key).setValue("Wyłącz");
                message = "on";
            } else {
                topics.get(key).setValue("Włącz");
                message = "off";
            }

            MqttMessage mqttMessage = new MqttMessage(message.getBytes());
            try {
                mqttMessage.setRetained(false);
                client.publish("lightOutput", mqttMessage);
            } catch (MqttException e) {
                e.printStackTrace();
            }

            updateTopicList(topics);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onLongItemClick(View view, int position) {
        showPopupMenu(view, position);
    }

    /**
     * Metoda odpowiadająca za wyświetlanie popUp menu podczas długiego przytrzymania elementu
     * z recycler view
     *
     * @param view     Widok
     * @param position pozycja danego elementu
     */
    private void showPopupMenu(View view, final int position) {
        PopupMenu menu = new PopupMenu(this, view);
        menu.inflate(R.menu.popup_menu_device_mainboard);
        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_edit: {
//                        Toast.makeText(DeviceMainboardActivity.this, "MENU EDIT position " + position, Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(), CreateModuleActivity.class);
                        String key = getHashMapKeyByIndex(topics, position);
                        String topicName = topics.get(key).getTopicName();
                        String topicType = topics.get(key).getTypeOfTopic();
                        intent.putExtra("topicName", topicName);
                        intent.putExtra("key", key);
                        intent.putExtra("topicType", topicType);
                        adapter.removeItem(key, position);
                        topics.remove(key);
                        updateTopicList(topics);
//                        adapter.notifyDataSetChanged();
                        startActivityForResult(intent, CREATE_MODULE_REQUEST_CODE);
                        return true;
                    }
                    case R.id.menu_delete: {
                        String key = getHashMapKeyByIndex(topics, position);
                        adapter.removeItem(key, position);
                        topics.remove(key);
                        updateTopicList(topics);
                        adapter.notifyDataSetChanged();
//                        Toast.makeText(DeviceMainboardActivity.this, "MENU DELETE position " + position, Toast.LENGTH_SHORT).show();
                        return true;
                    }
                    default:
                        return false;
                }
            }
        });
        menu.show();
    }
}