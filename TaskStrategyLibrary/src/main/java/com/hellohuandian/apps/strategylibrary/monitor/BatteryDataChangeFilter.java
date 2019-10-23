package com.hellohuandian.apps.strategylibrary.monitor;

import android.text.TextUtils;

import com.hellohuandian.apps.strategylibrary.strategies._data.BatteryData;
import com.hellohuandian.apps.strategylibrary.strategies.battery.BatteryInfo;

import java.util.concurrent.ConcurrentHashMap;

import androidx.core.util.Consumer;

/**
 * Author:      Lee Yeung
 * Create Date: 2019-10-19
 * Description:
 */
public class BatteryDataChangeFilter extends ConcurrentHashMap<BatteryData, String> implements Consumer<BatteryData>
{
    private Consumer<BatteryData> dataChangeConsumer;
    private Consumer<BatteryData> batteryLockedConsumer;
    private Consumer<BatteryData> batteryUpgradeConsumer;

    public void setDataChangeConsumer(Consumer<BatteryData> dataChangeConsumer)
    {
        this.dataChangeConsumer = dataChangeConsumer;
    }

    public void setBatteryLockedConsumer(Consumer<BatteryData> batteryLockedConsumer)
    {
        this.batteryLockedConsumer = batteryLockedConsumer;
    }

    public void setBatteryUpgradeConsumer(Consumer<BatteryData> batteryUpgradeConsumer)
    {
        this.batteryUpgradeConsumer = batteryUpgradeConsumer;
    }

    @Override
    public void accept(BatteryData batteryData)
    {
        if (batteryData != null)
        {
            switch (batteryData.getBatteryDataType())
            {
                case BatteryData.BatteryDataType.UPGRADE:
                    // TODO: 2019-10-23 此时已经是485操作，在串口线程执行
                    if (containsKey(batteryData))
                    {
                        remove(batteryData);
                    }
                    if (batteryUpgradeConsumer != null)
                    {
                        batteryUpgradeConsumer.accept(batteryData);
                    }
                    break;
                case BatteryData.BatteryDataType.INFO:
                    // TODO: 2019-10-23 CAN只读线程
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
                    break;
            }
        }
    }
}
