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

    String topicType = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_module);

        final EditText etTopicTitle = findViewById(R.id.etTopicText);
        final EditText etTopicValue = findViewById(R.id.etTopicValue);
        Button btSaveTopic = findViewById(R.id.btSaveTopic);

        if (getIntent().hasExtra("type")) {
            topicType = getIntent().getStringExtra("type");
        }

        btSaveTopic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String topicTitle = String.valueOf(etTopicTitle.getText());
                String topicValue = String.valueOf(etTopicValue.getText());

                Intent intent = new Intent(getApplicationContext(), DeviceMainboardActivity.class);
                intent.putExtra("topicTitle", topicTitle);
                intent.putExtra("topicValue", topicValue);
                intent.putExtra("topicType", topicType);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });

    }
}