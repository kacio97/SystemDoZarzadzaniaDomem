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
    ArrayList<ServerDevice> devices = new ArrayList<>();
    SharedPreferences preferences;
    private final String key = "Devices";
    MainActivityAdapter adapter;
    CoordinatorLayout coordinatorLayout;
    RecyclerView recyclerView;


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
        recyclerView = findViewById(R.id.rvServerDevices);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        coordinatorLayout = findViewById(R.id.coordinatorLayout);




        String JSONstring = getPreferences(MODE_PRIVATE).getString(key, null);
        Type type = new TypeToken<ArrayList<ServerDevice>>() {
        }.getType();

        if (getPreferences(MODE_PRIVATE).contains(key)) {
            devices = new Gson().fromJson(JSONstring, type);
        }

        if (getIntent().hasExtra("newDevice")) {
            ServerDevice serverDevice = (ServerDevice) getIntent().getSerializableExtra("newDevice");
            addNewDevice(serverDevice);
        } else if (getIntent().hasExtra("editedDevice")) {
            ServerDevice serverDevice = (ServerDevice) getIntent().getSerializableExtra("editedDevice");
            int position = getIntent().getIntExtra("position", -1);
            updateDevice(serverDevice, position);
        }


        adapter = new MainActivityAdapter(devices, getApplicationContext());
        adapter.setOnCLickListener(this);
        recyclerView.setAdapter(adapter);

        swipeToDeleteAndUndo();

//        temp = findViewById(R.id.temperatura);


    }

    private void swipeToDeleteAndUndo() {
        SwipieToDeleteCallback swipieToDeleteCallback = new SwipieToDeleteCallback(this) {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                final int position = viewHolder.getAdapterPosition();
                final ServerDevice item = adapter.getItem(position);
                adapter.removeItem(position);

                Snackbar snackbar = Snackbar.make(coordinatorLayout, "Usunięto element", Snackbar.LENGTH_LONG);

                snackbar.setAction("undo", new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        adapter.restoreItem(item, position);
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

    @Override
    public void onItemClick(View view, int positon) {
        Toast.makeText(this, "Nawiązuję połączenie z " + adapter.getItemName(positon) + " " + positon, Toast.LENGTH_SHORT).show();
        ServerDevice device = adapter.getItem(positon);
        Intent intent = new Intent(getApplicationContext(), DeviceMainboardActivity.class);
        intent.putExtra("device", device);
        startActivity(intent);
    }

    @Override
    public void onLongItemClick(View view, int position) {
        Toast.makeText(this, "Edycja urządzenia " + adapter.getItemName(position) + " " + position, Toast.LENGTH_SHORT).show();
        ServerDevice device = adapter.getItem(position);
        Intent intent = new Intent(getApplicationContext(), CreateNewDevice.class);
        intent.putExtra("oldDevice", device);
        intent.putExtra("position", position);
        startActivity(intent);
    }


    public void addNewDevice(ServerDevice s) {

        SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();

        boolean exist = false;

        if (devices.isEmpty()) {
            devices.add(s);
            String json = new Gson().toJson(devices);
            editor.putString(key, json);
            editor.commit();
            Log.d(TAG, "dodano urzadzenie " + s.toString());
           Toast.makeText(getApplicationContext(), "Udało się dodać nowe urządzenie", Toast.LENGTH_LONG).show();

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
                    Toast.makeText(getApplicationContext(), "Udało się dodać nowe urządzenie", Toast.LENGTH_LONG).show();
                    exist = false;
                    break;
                }
            }
        }

        if (exist) {
           Toast.makeText(getApplicationContext(), "Takie urządzenie już istnieje", Toast.LENGTH_LONG).show();
            String json = new Gson().toJson(devices);
            editor.putString(key, json);
            editor.commit();
        }
    }

    public void updateDevice(ServerDevice s, int position) {
        SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();

        devices.get(position).setClientID(s.getClientID());
        devices.get(position).setDeviceIP(s.getDeviceIP());
        devices.get(position).setDeviceName(s.getDeviceName());
        devices.get(position).setPort(s.getPort());

        String json = new Gson().toJson(devices);
        editor.putString(key, json);
        editor.commit();
        Log.d(TAG, "Edytowano urządzenie " + s.toString());
        Toast.makeText(getApplicationContext(), "Udało się edytować wybrane urządzenie", Toast.LENGTH_LONG).show();
    }

    }
