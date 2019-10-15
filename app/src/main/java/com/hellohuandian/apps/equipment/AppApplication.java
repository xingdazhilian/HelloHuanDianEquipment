package com.hellohuandian.apps.equipment;

import android.app.Application;

import com.jakewharton.processphoenix.ProcessPhoenix;

import io.reactivex.functions.Consumer;
import io.reactivex.plugins.RxJavaPlugins;

/**
 * Author:      Lee Yeung
 * Create Date: 2019-09-29
 * Description:
 */
public class AppApplication extends Application
{
    @Override
    public void onCreate()
    {
        super.onCreate();
        //        setRxJavaErrorHandler();

        if (ProcessPhoenix.isPhoenixProcess(getApplicationContext()))
        {
            return;
        }
    }

    private void setRxJavaErrorHandler()
    {
        RxJavaPlugins.setErrorHandler(new Consumer<Throwable>()
        {
            @Override
            public void accept(Throwable throwable) throws Exception
            {
                System.out.println("错误了");
            }
        });
    }
}
