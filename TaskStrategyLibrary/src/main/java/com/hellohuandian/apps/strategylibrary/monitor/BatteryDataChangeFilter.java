package com.hellohuandian.apps.strategylibrary.monitor;

import android.text.TextUtils;

import com.hellohuandian.apps.strategylibrary.strategies._data.BatteryData;
import com.hellohuandian.apps.strategylibrary.strategies.battery.BatteryInfo;

import java.util.HashMap;

import androidx.core.util.Consumer;

/**
 * Author:      Lee Yeung
 * Create Date: 2019-10-19
 * Description:
 */
public class BatteryDataChangeFilter extends HashMap<BatteryData, String> implements Consumer<BatteryData>
{
    private Consumer<BatteryData> dataChangeConsumer;
    private Consumer<BatteryData> batteryLockedConsumer;

    public void setDataChangeConsumer(Consumer<BatteryData> dataChangeConsumer)
    {
        this.dataChangeConsumer = dataChangeConsumer;
    }

    public void setBatteryLockedConsumer(Consumer<BatteryData> batteryLockedConsumer)
    {
        this.batteryLockedConsumer = batteryLockedConsumer;
    }

    @Override
    public void accept(BatteryData batteryData)
    {
        if (batteryData != null)
        {
            if (dataChangeConsumer != null)
            {
                final String oldToString = get(batteryData);
                final String newToString = batteryData.toSimpleString();
                if (TextUtils.isEmpty(oldToString) || !oldToString.equals(newToString))
                {
                    put(batteryData, newToString);
                    dataChangeConsumer.accept(batteryData);
                }
            }
            if (batteryLockedConsumer != null && batteryData instanceof BatteryInfo)
            {
                BatteryInfo batteryInfo = (BatteryInfo) batteryData;
                // TODO: 2019-10-18 判断是否电池正确放好
                if ((batteryInfo.doorSideLockStatus & batteryInfo.doorBottomLockStatus) == 1)
                {
                    batteryLockedConsumer.accept(batteryData);
                }
            }
        }
    }
}
