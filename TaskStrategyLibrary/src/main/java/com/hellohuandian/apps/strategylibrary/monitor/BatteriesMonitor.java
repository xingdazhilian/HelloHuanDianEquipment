package com.hellohuandian.apps.strategylibrary.monitor;

import com.hellohuandian.apps.strategylibrary.dispatchers.DispatcherManager;
import com.hellohuandian.apps.strategylibrary.strategies.battery.BatteryData;
import com.hellohuandian.apps.strategylibrary.strategies.battery.OnBatteryDataUpdate;

/**
 * Author:      Lee Yeung
 * Create Date: 2019-09-28
 * Description: 负责电池信息通知和任务调度分发
 */
public final class BatteriesMonitor
{
    private OnBatteryDataUpdate onBatteryDataUpdate;

    private static final BatteriesMonitor BATTERIES_MONITOR = new BatteriesMonitor();

    private BatteriesMonitor()
    {
        DispatcherManager.getInstance().setOnBatteryDataUpdate(new OnBatteryDataUpdate()
        {
            @Override
            public void onUpdate(BatteryData batteryData)
            {
                // TODO: 2019-09-28 回调给上层
                if (onBatteryDataUpdate != null)
                {
                    onBatteryDataUpdate.onUpdate(batteryData);
                }
            }
        });
    }

    public static BatteriesMonitor getInstance()
    {
        return BATTERIES_MONITOR;
    }

    public void setOnBatteryDataUpdate(OnBatteryDataUpdate onBatteryDataUpdate)
    {
        this.onBatteryDataUpdate = onBatteryDataUpdate;
    }
}
