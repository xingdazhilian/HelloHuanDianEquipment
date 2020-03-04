package client.NewElectric.app10000.services;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;

import client.NewElectric.app10000.modules.config.MachineVersionConfig;

import com.app10000.apps.strategylibrary._core.ScManager;
import com.app10000.apps.strategylibrary.monitor.BatteriesMonitor;
import com.app10000.apps.strategylibrary.strategies._data.BatteryData;
import com.app10000.apps.strategylibrary.strategies.battery.OnBatteryDataUpdate;

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

        BatteriesMonitor.getInstance().addOnBatteryDataUpdate(this);
        BatteriesMonitor.getInstance().init(MachineVersionConfig.getMachineVersion());
        BatteriesMonitor.getInstance().start();
        ScManager.getInstance().init(MachineVersionConfig.getMachineVersion());
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                ScManager.getInstance().start();
                System.out.println("策略服务启动");
            }
        }).start();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        ScManager.getInstance().stop();
        BatteriesMonitor.getInstance().stop();
    }

    @Override
    public void onUpdate(BatteryData batteryData)
    {
        if (isMainThread())
        {
            BatteryWatcherRegisters.onWatch(batteryData);
        } else
        {
            handler.removeMessages(batteryData.address);
            Message message = handler.obtainMessage(batteryData.address);
            message.obj = batteryData;
            handler.sendMessage(message);
        }
    }

    public boolean isMainThread()
    {
        return Looper.getMainLooper() == Looper.myLooper();
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
