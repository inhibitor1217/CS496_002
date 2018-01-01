package com.example.user.cs496_002;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

public class GridPopupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imagepopup);

        Intent intent = getIntent();
        int position = intent.getExtras().getInt("position");

        ImageView imageView = (ImageView) findViewById(R.id.popup_imgview);

        // set image using data inside intent

    }

}
