package com.zgkx.change;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.chtj.keepalive.FBaseDaemon;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}