package com.hellohuandian.apps.equipment.services;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.hellohuandian.apps.equipment.modules.config.MachineVersionConfig;
import com.hellohuandian.apps.strategylibrary._core.ScManager;
import com.hellohuandian.apps.strategylibrary.config.MachineVersion;
import com.hellohuandian.apps.strategylibrary.monitor.BatteriesMonitor;
import com.hellohuandian.apps.strategylibrary.strategies.battery.BatteryData;
import com.hellohuandian.apps.strategylibrary.strategies.battery.OnBatteryDataUpdate;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import androidx.annotation.Nullable;

/**
 * Author:      Lee Yeung
 * Create Date: 2019-09-29
 * Description:
 */
public class StrategyService extends Service implements OnBatteryDataUpdate
{
    private Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            if (msg.obj instanceof BatteryData)
            {
//                System.out.println("电池数据：" + msg.obj.toString());
                BatteryWatcherRegisters.onWatch(((BatteryData) msg.obj));
            }
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        BatteriesMonitor.getInstance().setOnBatteryDataUpdate(this);
        ScManager.getInstance().init(MachineVersionConfig.getMachineVersion());
        ScManager.getInstance().start();
    }

    @Override
    public void onUpdate(BatteryData batteryData)
    {
        handler.removeMessages(batteryData.address);
        Message message = handler.obtainMessage(batteryData.address);
        message.obj = batteryData;
        handler.sendMessage(message);
    }

    public static class BatteryWatcherRegisters
    {
        private static final Set<BatteryWatcher> batteryWatchers = new HashSet<>();

        static void onWatch(BatteryData batteryData)
        {
            Iterator<BatteryWatcher> infoWatcherIterator = batteryWatchers.iterator();
            while (infoWatcherIterator.hasNext())
            {
                infoWatcherIterator.next().onWatch(batteryData);
            }
        }

        public static void register(BatteryWatcher batteryWatcher)
        {
            batteryWatchers.add(batteryWatcher);
        }

        public static void unRegister(BatteryWatcher batteryWatcher)
        {
            batteryWatchers.remove(batteryWatcher);
        }
    }
}