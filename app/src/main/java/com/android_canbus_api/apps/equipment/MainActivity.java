package com.android_canbus_api.apps.equipment;

import android.os.Bundle;

import com.android_canbus_api.apps.strategylibrary._core.dispatchers.CanDispatcher;
import com.android_canbus_api.apps.strategylibrary._core.dispatchers.SerialPortDispatcher;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        SerialPortDispatcher.getInstance().loop();
        CanDispatcher.getInstance().loop();
    }
}
