package com.mainactivity.systemdozarzadzaniadomem.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.mainactivity.systemdozarzadzaniadomem.R;

public class CreateModuleActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_module);

        final EditText etTopic = findViewById(R.id.etTopicText);
        Button btSaveTopic = findViewById(R.id.btSaveTopic);

        btSaveTopic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String topic = String.valueOf(etTopic.getText());

                //TODO: Zaimplementować startActivityForResult ! POCZYTAć !
                Intent intent = new Intent(getApplicationContext(), DeviceMainboardActivity.class);
                intent.putExtra("topicText", topic);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });

    }
}