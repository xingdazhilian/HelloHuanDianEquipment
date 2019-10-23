package com.hellohuandian.apps.strategylibrary.monitor;

import com.hellohuandian.apps.strategylibrary.charging.ChargingUnit;
import com.hellohuandian.apps.strategylibrary.dispatchers.DispatcherManager;
import com.hellohuandian.apps.strategylibrary.heating.HeatingUnit;
import com.hellohuandian.apps.strategylibrary.strategies._data.BatteryData;
import com.hellohuandian.apps.strategylibrary.strategies.battery.OnBatteryDataUpdate;

import java.util.HashSet;
import java.util.Iterator;

import androidx.core.util.Consumer;

/**
 * Author:      Lee Yeung
 * Create Date: 2019-09-28
 * Description: 负责电池信息通知和任务调度分发
 */
public final class BatteriesMonitor implements OnBatteryDataUpdate
{
    private final HashSet<OnBatteryDataUpdate> onBatteryDataUpdateHashSet = new HashSet<>();

    private static final BatteriesMonitor BATTERIES_MONITOR = new BatteriesMonitor();

    public static BatteriesMonitor getInstance()
    {
        return BATTERIES_MONITOR;
    }

    private final BatteryDataChangeFilter batteryDataChangeFilter = new BatteryDataChangeFilter();

    private final HeatingUnit heatingUnit = new HeatingUnit();
    private final ChargingUnit chargingUnit = new ChargingUnit();

    private BatteriesMonitor()
    {
        batteryDataChangeFilter.setDataChangeConsumer(new Consumer<BatteryData>()
        {
            @Override
            public void accept(BatteryData batteryData)
            {
                onUpdateDispatch(batteryData);
            }
        });
        batteryDataChangeFilter.setBatteryLockedConsumer(new Consumer<BatteryData>()
        {
            @Override
            public void accept(BatteryData batteryData)
            {
                chargingUnit.charging(batteryData);
            }
        });
        batteryDataChangeFilter.setBatteryUpgradeConsumer(new Consumer<BatteryData>()
        {
            @Override
            public void accept(BatteryData batteryData)
            {
                onUpdateDispatch(batteryData);
            }
        });
    }

    public void addOnBatteryDataUpdate(OnBatteryDataUpdate onBatteryDataUpdate)
    {
        if (onBatteryDataUpdate != null)
        {
            onBatteryDataUpdateHashSet.add(onBatteryDataUpdate);
        }
    }

    private void onUpdateDispatch(BatteryData batteryData)
    {
        if (onBatteryDataUpdateHashSet != null)
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

    public void start()
    {
        DispatcherManager.getInstance().setOnBatteryDataUpdate(this);
    }

    public void stop()
    {
        onBatteryDataUpdateHashSet.clear();
    }

    @Override
    public void onUpdate(BatteryData batteryData)
    {
        batteryDataChangeFilter.accept(batteryData);
    }
}
