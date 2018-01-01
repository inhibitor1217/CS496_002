package com.example.user.cs496_002;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private TabPagerAdapter adapter;

    private final int MY_PERMISSIONS_REQUEST_INTERNET = 42;
    private final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 45;

    public static boolean INTERNET_ALLOWED = false;
    public static boolean READ_CONTACTS_ALLOWED = false;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // request permission for internet usage
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, MY_PERMISSIONS_REQUEST_INTERNET);
        } else {
            INTERNET_ALLOWED = true;
        }

        // request permission for reading contact information from local device
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, MY_PERMISSIONS_REQUEST_READ_CONTACTS);
        } else {
            READ_CONTACTS_ALLOWED = true;
        }

        viewPager = (ViewPager) findViewById(R.id.pager);
        TabPagerAdapter adapter = new TabPagerAdapter(getSupportFragmentManager());

        adapter.addFragment(new TabFragment1(), "연락처");
        adapter.addFragment(new TabFragment2(), "사진첩");
        adapter.addFragment(new TabFragment3(), "TAB C");

        viewPager.setAdapter(adapter);

        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode) {
            case MY_PERMISSIONS_REQUEST_INTERNET:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    INTERNET_ALLOWED = true;
                }
                else {
                    Toast.makeText(this, "서버 접속을 위해 [설정]>[애플리케이션 관리]에서 인터넷 접속 권한을 활성화 해주세요.", Toast.LENGTH_SHORT).show();
                }
                break;
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    READ_CONTACTS_ALLOWED = true;
                }
                else {
                    Toast.makeText(this, "서버 접속을 위해 [설정]>[애플리케이션 관리]에서 연락처 접근 권한을 활성화 해주세요.", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
    }
}