package com.example.user.cs496_002;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class GridPopupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imagepopup);

        Intent intent = getIntent();

        ImageView imageView = (ImageView) findViewById(R.id.popup_imgview);

        imageView.setImageURI(Uri.fromFile(new File(Environment.getExternalStorageDirectory().toString() + "/CS496_caches/" + intent.getStringExtra("id"))));

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

}
