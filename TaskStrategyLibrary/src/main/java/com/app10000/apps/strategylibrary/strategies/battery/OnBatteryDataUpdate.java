package com.app10000.apps.strategylibrary.strategies.battery;

import com.app10000.apps.strategylibrary.strategies._data.BatteryData;

/**
 * Author:      Lee Yeung
 * Create Date: 2019-09-17
 * Description:
 */
public interface OnBatteryDataUpdate<T extends BatteryData>
{
    void onUpdate(T batteryData);
}
