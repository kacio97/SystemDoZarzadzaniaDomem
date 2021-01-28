package com.mainactivity.systemdozarzadzaniadomem.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mainactivity.systemdozarzadzaniadomem.Adapters.MainActivityAdapter;
import com.mainactivity.systemdozarzadzaniadomem.Functionality.SwipieToDeleteCallback;
import com.mainactivity.systemdozarzadzaniadomem.Models.ServerDevice;
import com.mainactivity.systemdozarzadzaniadomem.R;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements Serializable, MainActivityAdapter.ItemClickListener {

    private static final String TAG = "MainActivity";
    private final String key = "Devices";
    private static final int CREATE_NEW_DEVICE_REQUEST_CODE = 1;
    private static final int UPDATE_DEVICE_REQUEST_CODE = 2;
    ArrayList<ServerDevice> devices = new ArrayList<>();
    SharedPreferences preferences;
    MainActivityAdapter adapter;
    CoordinatorLayout coordinatorLayout;
    RecyclerView recyclerView;

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.addNewDevice: {
                Intent intent = new Intent(getApplicationContext(), CreateNewDevice.class);
                startActivityForResult(intent, CREATE_NEW_DEVICE_REQUEST_CODE);
//                return true;
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
        recyclerView = findViewById(R.id.rvServerDevices);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        coordinatorLayout = findViewById(R.id.coordinatorLayout);


        String JSONstring = getPreferences(MODE_PRIVATE).getString(key, null);
        Type type = new TypeToken<ArrayList<ServerDevice>>() {
        }.getType();

        if (getPreferences(MODE_PRIVATE).contains(key)) {
            devices = new Gson().fromJson(JSONstring, type);
        }

//        if (getIntent().hasExtra("newDevice")) {
//
//        } else if (getIntent().hasExtra("editedDevice")) {
//
//        }


        adapter = new MainActivityAdapter(devices, getApplicationContext());
        adapter.setOnCLickListener(this);
        recyclerView.setAdapter(adapter);

        swipeToDeleteAndUndo();

    }


    /**
     * Metoda która uruchamia się i zbiera dane po przejściu z zakończonego Activity
     * @param requestCode Kod zwrócony z innego activity (Określa jaka czynność była wykonana)
     * @param resultCode Kod wynikowy (Czy rezultat jest prawidłowy)
     * @param data Dane przesłane przez intent
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CREATE_NEW_DEVICE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                ServerDevice serverDevice = (ServerDevice) data.getSerializableExtra("newDevice");
                addNewDevice(serverDevice);
                //ODSWIEZANIE ACTIVITY :)
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }
        } else if (requestCode == UPDATE_DEVICE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                ServerDevice serverDevice = (ServerDevice) data.getSerializableExtra("editedDevice");
                int position = data.getIntExtra("position", -1);
                updateDevice(serverDevice, position);
                //ODSWIEZANIE ACTIVITY :)
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }
        }
    }

    /**
     * Metoda która pozwala na usuwanie urządzeń oraz przywracanie ich w określonym czasie.
     * Wywołanie metody następuje podczas przesunięcia elementu RecyclerView w lewo, czynność
     * ta wykonywana jest wewnątrz metody onSwiped()
     */
    private void swipeToDeleteAndUndo() {
        SwipieToDeleteCallback swipieToDeleteCallback = new SwipieToDeleteCallback(this) {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                final int position = viewHolder.getAdapterPosition();
                final ServerDevice item = adapter.getItem(position);
                adapter.removeItem(position);
                updateDeviceList(devices);

                Snackbar snackbar = Snackbar.make(coordinatorLayout, "Usunięto element", Snackbar.LENGTH_LONG);

                snackbar.setAction("undo", new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        adapter.restoreItem(item, position);
                        updateDeviceList(devices);
                        recyclerView.scrollToPosition(position);
                    }
                });

                snackbar.setActionTextColor(Color.CYAN);
                snackbar.show();
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipieToDeleteCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }


    /**
     * Metoda określa zachowanie podczas pojedyńczego naciśnięcia na dany element recycler view
     * @param view Obecny widok
     * @param positon pozycja która określa wartość na jakiej znajduje się element
     */
    @Override
    public void onItemClick(View view, int positon) {
        Toast.makeText(this, "Nawiązuję połączenie z " + adapter.getItemName(positon) + " " + positon, Toast.LENGTH_SHORT).show();
        ServerDevice device = adapter.getItem(positon);
        MyApplication.getInstance().setDevice(device);
        Intent intent = new Intent(getApplicationContext(), DeviceMainboardActivity.class);
        intent.putExtra("device", device.getDeviceName());
        startActivity(intent);
    }


    /**
     * Metoda określa zachowanie podczas długiego przytrzymania (naciśnięcia) na danym elemencie recycler view
     * @param view Obecny widok
     * @param position pozycja która określa wartość na jakiej znajduje się element
     */
    @Override
    public void onLongItemClick(View view, int position) {
        Toast.makeText(this, "Edycja urządzenia " + adapter.getItemName(position) + " " + position, Toast.LENGTH_SHORT).show();
        ServerDevice device = adapter.getItem(position);
        Intent intent = new Intent(getApplicationContext(), CreateNewDevice.class);
        intent.putExtra("oldDevice", device);
        intent.putExtra("position", position);
        startActivityForResult(intent, UPDATE_DEVICE_REQUEST_CODE);
    }


    /**
     * Metoda która odpowiada za dodawanie nowego urządzenia (tj. serwer MQTT) do listy urządzeń.
     * @param s Obiekt ten przechowuje informacje na temat serwera do którego będziemy próbowali
     *          nawiązać połączenie.
     */
    public void addNewDevice(ServerDevice s) {
        boolean exist = false;

        if (devices.isEmpty()) {
            devices.add(s);
            updateDeviceList(devices);
            Log.d(TAG, "dodano urzadzenie " + s.toString());
            Toast.makeText(getApplicationContext(), "Udało się dodać nowe urządzenie", Toast.LENGTH_LONG).show();
        } else {
            for (int i = 0; i < devices.size(); i++) {
                if (devices.get(i).getClientID().equals(s.getClientID())) {
                    exist = true;
                    break;
                }
            }
            if (exist) {
                Toast.makeText(getApplicationContext(), "Takie urządzenie już istnieje", Toast.LENGTH_LONG).show();
//                updateDeviceList(devices); // TODO: CZY jest sens to aktualizować ?
            } else {
                devices.add(s);
                updateDeviceList(devices);
                Log.d(TAG, "dodano urzadzenie " + s.toString());
                Toast.makeText(getApplicationContext(), "Udało się dodać nowe urządzenie", Toast.LENGTH_LONG).show();
                updateDeviceList(devices);
            }
        }
    }

    /**
     * Aktualizacja informacji dotyczących konkretnego serwera podczas zmiany jego edycji.
     * @param s Obiekt ten przechowuje informacje na temat serwera MQTT do którego będziemy próbowali
     *          nawiązać połączenie.
     * @param position Pozycja elementu znajdującego się na liście
     */
    public void updateDevice(ServerDevice s, int position) {
        devices.get(position).setClientID(s.getClientID());
        devices.get(position).setDeviceIP(s.getDeviceIP());
        devices.get(position).setDeviceName(s.getDeviceName());
        devices.get(position).setPort(s.getPort());

        updateDeviceList(devices);

        Log.d(TAG, "Edytowano urządzenie " + s.toString());
        Toast.makeText(getApplicationContext(), "Udało się edytować wybrane urządzenie", Toast.LENGTH_LONG).show();
    }


    /**
     * Aktualizacja danych dotycząca zmian które wystąpiły w liście serwerów MQTT np. edycja/usuniecie/dodanie
     * Dane przechowywane są w pamięci telefonu i odtwarzane podczas uruchamiania activity
     * @param devices Lista serwerów/urządzeń
     */
    private void updateDeviceList(ArrayList<ServerDevice> devices) {
        SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
        String json = new Gson().toJson(devices);
        editor.clear();
        editor.putString(key, json);
        editor.commit();
    }

}
