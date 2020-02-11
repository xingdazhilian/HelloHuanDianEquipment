package com.hellohuandian.apps.strategylibrary.monitor;

import android.text.TextUtils;

import com.hellohuandian.apps.strategylibrary.strategies._data.BatteryData;

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

    public void setDataChangeConsumer(Consumer<BatteryData> dataChangeConsumer)
    {
        this.dataChangeConsumer = dataChangeConsumer;
    }

    @Override
    public void accept(BatteryData batteryData)
    {
        if (dataChangeConsumer != null && batteryData != null)
        {
            switch (batteryData.getBatteryDataType())
            {
                case BatteryData.BatteryDataType.UPGRADE:
                    // TODO: 2019-10-23 此时已经是485操作，在串口线程执行
                    if (containsKey(batteryData))
                    {
                        remove(batteryData);
                    }
                    dataChangeConsumer.accept(batteryData);
                    break;
                case BatteryData.BatteryDataType.INFO:
                    // TODO: 2019-10-25 数据变化回调
                    final String oldToString = get(batteryData);
                    final String newToString = batteryData.toSimpleString();
                    if (TextUtils.isEmpty(oldToString) || !oldToString.equals(newToString))
                    {
                        put(batteryData, newToString);
                        dataChangeConsumer.accept(batteryData);
                    }
                    break;
            }
        }
    }
}
