package com.example.user.cs496_002;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class ContactPopUpAcitivity extends AppCompatActivity {

    TextView nameText, phoneText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contactpopup);

        nameText = (TextView) findViewById(R.id.popup_text_name);
        phoneText = (TextView) findViewById(R.id.popup_text_phone);

        nameText.setText(getIntent().getStringExtra("name"));
        phoneText.setText(getIntent().getStringExtra("phone"));

    }

}
