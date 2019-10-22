package com.hellohuandian.apps.strategylibrary.charging.strategies;

import com.hellohuandian.apps.strategylibrary.strategies._data.BatteryData;

/**
 * Author:      Lee Yeung
 * Create Date: 2019-10-22
 * Description:
 */
public abstract class ChargingStrategy
{
    public abstract void charging(BatteryData batteryData);
}
