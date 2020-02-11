package com.hellohuandian.apps.strategylibrary.charging.engines;

import com.hellohuandian.apps.strategylibrary.strategies.battery.BatteryInfo;

/**
 * Author:      Lee Yeung
 * Create Date: 2019-11-05
 * Description:
 */
public abstract class ChargingEngine
{
    public abstract void charging(BatteryInfo[] batteryInfos);
}
