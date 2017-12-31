package com.example.user.cs496_002;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class ContactPopUpAcitivity extends AppCompatActivity {

    TextView nameText, phoneText, emailText, facebookText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contactpopup);

        nameText     = (TextView) findViewById(R.id.popup_text_name);
        phoneText    = (TextView) findViewById(R.id.popup_text_phone);
        emailText    = (TextView) findViewById(R.id.popup_text_email);
        facebookText = (TextView) findViewById(R.id.popup_text_facebook);

        nameText.setText(getIntent().getStringExtra("name"));
        phoneText.setText(getIntent().getStringExtra("phone"));
        emailText.setText(getIntent().getStringExtra("email"));
        facebookText.setText(getIntent().getStringExtra("facebook"));

    }

}
