package com.mainactivity.systemdozarzadzaniadomem.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.mainactivity.systemdozarzadzaniadomem.R;

/**
 * Acivity odpowiada za wpisanie i zebranie inforamcji dotyczących jakiegoś tematu.
 */
public class CreateModuleActivity extends AppCompatActivity {

    String topicType = "";
    String topicName = "";
    String key = "";
    Boolean edit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_module);

        final EditText etTopicTitle = findViewById(R.id.etTopicText);
        final EditText etTopicKey = findViewById(R.id.etTopicKey);
        Button btSaveTopic = findViewById(R.id.btSaveTopic);

        if (getIntent().hasExtra("topicType")) {
            topicType = getIntent().getStringExtra("topicType");
        }
        if(getIntent().hasExtra("topicName")) {
            topicName = getIntent().getStringExtra("topicName");
            etTopicTitle.setText(topicName);
        }
        if(getIntent().hasExtra("key")) {
            key = getIntent().getStringExtra("key");
            etTopicKey.setText(key);
            edit = true;
        }

        btSaveTopic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String topicTitle = String.valueOf(etTopicTitle.getText());
                String topicKey = String.valueOf(etTopicKey.getText());

                Intent intent = new Intent(getApplicationContext(), DeviceMainboardActivity.class);
                intent.putExtra("topicTitle", topicTitle);
                intent.putExtra("topicKey", topicKey);
                intent.putExtra("topicType", topicType);
                intent.putExtra("edit", edit);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });

    }
}