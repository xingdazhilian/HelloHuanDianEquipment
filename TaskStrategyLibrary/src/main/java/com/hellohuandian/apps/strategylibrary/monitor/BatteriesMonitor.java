package com.hellohuandian.apps.strategylibrary.monitor;

import com.hellohuandian.apps.strategylibrary.dispatchers.DispatcherManager;
import com.hellohuandian.apps.strategylibrary.strategies._data.BatteryData;
import com.hellohuandian.apps.strategylibrary.strategies.battery.OnBatteryDataUpdate;

import java.util.HashSet;
import java.util.Iterator;

/**
 * Author:      Lee Yeung
 * Create Date: 2019-09-28
 * Description: 负责电池信息通知和任务调度分发
 */
public final class BatteriesMonitor
{
    private final HashSet<OnBatteryDataUpdate> onBatteryDataUpdateHashSet = new HashSet<>();
    private volatile boolean isRun;

    private static final BatteriesMonitor BATTERIES_MONITOR = new BatteriesMonitor();

    private BatteriesMonitor()
    {
        isRun = true;
        DispatcherManager.getInstance().setOnBatteryDataUpdate(new OnBatteryDataUpdate()
        {
            @Override
            public void onUpdate(BatteryData batteryData)
            {
                // TODO: 2019-09-28 回调给上层
                if (isRun && onBatteryDataUpdateHashSet != null)
                {
                    Iterator<OnBatteryDataUpdate> onBatteryDataUpdateIterator = onBatteryDataUpdateHashSet.iterator();
                    while (onBatteryDataUpdateIterator.hasNext())
                    {
                        OnBatteryDataUpdate onBatteryDataUpdate = onBatteryDataUpdateIterator.next();
                        if (onBatteryDataUpdate != null)
                        {
                            onBatteryDataUpdate.onUpdate(batteryData);
                        }
                    }
                }
            }
        });
    }

    public static BatteriesMonitor getInstance()
    {
        return BATTERIES_MONITOR;
    }

    public void addOnBatteryDataUpdate(OnBatteryDataUpdate onBatteryDataUpdate)
    {
        if (onBatteryDataUpdate != null)
        {
            onBatteryDataUpdateHashSet.add(onBatteryDataUpdate);
        }
    }

    public void stop()
    {
        isRun = false;
        onBatteryDataUpdateHashSet.clear();
    }
}
