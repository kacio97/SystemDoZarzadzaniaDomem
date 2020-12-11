package com.mainactivity.systemdozarzadzaniadomem.Activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.mainactivity.systemdozarzadzaniadomem.R;

public class LedControlPanel extends AppCompatActivity {

//     EditText etRed;
//     EditText etGreen;
//     EditText etBlue;
//     Button btSetColor;
//     SeekBar sbColorRed;
//     SeekBar sbColorGreen;
//     SeekBar sbColorBlue;
//     TextView tvRedColorBox;
//     TextView tvGreenColorBox;
//     TextView tvBlueColorBox;

    int red = 0;
    int green = 0;
    int blue = 0;
    String color;
    int index = 0;
    boolean isOn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_led_control_panel);

        final EditText etRed = findViewById(R.id.etColorRed);
        final EditText etGreen = findViewById(R.id.etColorGreen);
        final EditText etBlue = findViewById(R.id.etColorBlue);

//        ZAMIENIAM TYP KLAWIATURY TYLKO NA CYFRY
        etRed.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        etRed.setTransformationMethod(new NumericKeyBoardTransformationMethod());

        etGreen.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        etGreen.setTransformationMethod(new NumericKeyBoardTransformationMethod());

        etBlue.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        etBlue.setTransformationMethod(new NumericKeyBoardTransformationMethod());

        final Button btSetColor = findViewById(R.id.btSetColor);
        final Button btSwitch = findViewById(R.id.btSwitch);
        final SeekBar sbColorRed = findViewById(R.id.sbRedColor);
        final SeekBar sbColorGreen = findViewById(R.id.sbGreenColor);
        final SeekBar sbColorBlue = findViewById(R.id.sbBlueColor);
        final TextView tvRedColorBox = findViewById(R.id.tvRedColorBox);
        final TextView tvGreenColorBox = findViewById(R.id.tvGreenColorBox);
        final TextView tvBlueColorBox = findViewById(R.id.tvBlueColorBox);
        final TextView tvFinalColor = findViewById(R.id.tvFinalColor);


        //USTAWIAM WARTOSCI POCZĄTKOWE dla seeekBarów
        if (getIntent().hasExtra("color")) {
            color = getIntent().getStringExtra("color");
            setLastRemeberedColors(color);
        }

        if(getIntent().hasExtra("ison")){
            isOn = getIntent().getBooleanExtra("ison",false);
        }

        if(isOn) {
            btSwitch.setText("Wyłącz");
        }else {
            btSwitch.setText("Włącz");
        }


        sbColorRed.setProgress(0);
        sbColorGreen.setProgress(0);
        sbColorBlue.setProgress(0);
        etRed.setText(String.valueOf(red));
        etGreen.setText(String.valueOf(green));
        etBlue.setText(String.valueOf(blue));
        sbColorRed.setProgress(red);
        sbColorGreen.setProgress(green);
        sbColorBlue.setProgress(blue);
        tvRedColorBox.setBackgroundColor(Color.rgb(red, 0, 0));
        tvGreenColorBox.setBackgroundColor(Color.rgb(0, green, 0));
        tvBlueColorBox.setBackgroundColor(Color.rgb(0, 0, blue));
        tvFinalColor.setBackgroundColor(Color.rgb(red, green, blue));

        etRed.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                Toast.makeText(LedControlPanel.this, "" + s, Toast.LENGTH_SHORT).show();
                StringBuilder sb = new StringBuilder(s);
//                String tmp = "";
                if (etRed.length() == 0) {
                    etRed.setText("0");
                    sbColorRed.setProgress(0, true);
                } else {
                    if (sb.charAt(0) == '0' && sb.length() > 1) {
                        sb.deleteCharAt(0);
//                        tmp = sb.toString();
                    }
                    sbColorRed.setProgress(Integer.parseInt(String.valueOf(sb)), true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                etRed.setSelection(setCursorOnPostion(s));
            }
        });

        etGreen.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                Toast.makeText(LedControlPanel.this, "" + s, Toast.LENGTH_SHORT).show();
                if (etGreen.length() == 0) {
                    etGreen.setText("0");
                    sbColorGreen.setProgress(0, true);
                } else {
                    sbColorGreen.setProgress(Integer.parseInt(String.valueOf(s)), true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                etGreen.setSelection(setCursorOnPostion(s));
            }
        });

        etBlue.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                Toast.makeText(LedControlPanel.this, "" + s, Toast.LENGTH_SHORT).show();
                if (etBlue.length() == 0) {
                    etBlue.setText("0");
                    sbColorBlue.setProgress(0, true);
                } else {
                    sbColorBlue.setProgress(Integer.parseInt(String.valueOf(s)), true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                etBlue.setSelection(setCursorOnPostion(s));
            }
        });


        sbColorRed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                String color = String.valueOf(seekBar.getProgress());
                etRed.setText(color);
                red = progress;
                tvRedColorBox.setBackgroundColor(Color.rgb(progress, 0, 0));
                tvFinalColor.setBackgroundColor(Color.rgb(red, green, blue));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        sbColorGreen.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                String color = String.valueOf(seekBar.getProgress());
                etGreen.setText(color);
                green = progress;
                tvGreenColorBox.setBackgroundColor(Color.rgb(0, progress, 0));
                tvFinalColor.setBackgroundColor(Color.rgb(red, green, blue));

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        sbColorBlue.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                String color = String.valueOf(seekBar.getProgress());
                etBlue.setText(color);
                blue = progress;
                tvBlueColorBox.setBackgroundColor(Color.rgb(0, 0, progress));
                tvFinalColor.setBackgroundColor(Color.rgb(red, green, blue));

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        btSetColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String r = String.valueOf(etRed.getText());
                String g = String.valueOf(etGreen.getText());
                String b = String.valueOf(etBlue.getText());

                Intent intent = new Intent(getApplicationContext(), DeviceMainboardActivity.class);
                intent.putExtra("colorRed", r);
                intent.putExtra("colorGreen", g);
                intent.putExtra("colorBlue", b);
                intent.putExtra("ison", true);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

//        TODO: Zrobic wlaczanie i wylaczanie LED
        btSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isOn) {
                    String r = String.valueOf(etRed.getText());
                    String g = String.valueOf(etGreen.getText());
                    String b = String.valueOf(etBlue.getText());

                    Intent intent = new Intent(getApplicationContext(), DeviceMainboardActivity.class);
                    intent.putExtra("colorRed", r);
                    intent.putExtra("colorGreen", g);
                    intent.putExtra("colorBlue", b);
                    intent.putExtra("ison", true);
                    setResult(RESULT_OK, intent);
                    finish();
                }
                else {
                    Intent intent = new Intent(getApplicationContext(), DeviceMainboardActivity.class);
                    intent.putExtra("colorRed", "0");
                    intent.putExtra("colorGreen", "0");
                    intent.putExtra("colorBlue", "0");
                    intent.putExtra("ison", false);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        });

    }

    /**
     * Klasa odpowiada za konwersje z hasła na ciąg znaków, wykorzystywana podczas wpisywania
     * TYLKO wartości cyfrowych dla EditText odpowiadającego za ustawianie koloru
     */
    private static class NumericKeyBoardTransformationMethod extends PasswordTransformationMethod {
        @Override
        public CharSequence getTransformation(CharSequence source, View view) {
            return source;
        }
    }


    /**
     * Metoda prawidłowo ustawia kursor podczas edycji pola tekstowego
     * (nie przesuwa za każdą edycją na początek)
     * @param s łańcuch znaków przekazany do analizy
     * @return zwraca index na którym ma się znaleźć kursor
     */
    private int setCursorOnPostion(Editable s) {
        int index = s.length();
        StringBuilder sb = new StringBuilder(s);
        if(sb.length() == 0) {
            index = 1;
        }
        if(sb.length() > 1 && sb.charAt(0) == '0') {
            sb.deleteCharAt(0);
            index = sb.length();
        }
        return index;
    }


    /**
     * Metoda ustawia ostatnio zapamiętany kolor
     * @param color wartość koloru w formacie 'rrr,ggg,bbb,'
     */
    private void setLastRemeberedColors(String color) {
        String tmp = "";
        for (int i = 0; i < color.length(); i++) {
            if (color.charAt(i) == ',' && index == 0) {
                red = Integer.parseInt(tmp);
                index++;
                tmp = "";
                continue;
            }
            if (color.charAt(i) == ',' && index == 1) {
                green = Integer.parseInt(tmp);
                index++;
                tmp = "";
                continue;
            }
            if (color.charAt(i) == ',' && index == 2) {
                blue = Integer.parseInt(tmp);
                index++;
                tmp = "";
                continue;
            }
            tmp += color.charAt(i);
        }
    }
}