package com.hellohuandian.apps.equipment;

import android.os.Bundle;

import com.hellohuandian.apps.strategylibrary._core.dispatchers.CanDispatcher;
import com.hellohuandian.apps.strategylibrary._core.dispatchers.SerialPortDispatcher;

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
