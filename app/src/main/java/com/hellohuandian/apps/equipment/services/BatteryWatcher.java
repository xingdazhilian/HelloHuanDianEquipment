package com.hellohuandian.apps.equipment.services;

import com.hellohuandian.apps.strategylibrary.strategies.battery.BatteryData;

/**
 * Author:      Lee Yeung
 * Create Date: 2019-09-29
 * Description:
 */
public interface BatteryWatcher
{
    void onWatch(BatteryData batteryData);
}
