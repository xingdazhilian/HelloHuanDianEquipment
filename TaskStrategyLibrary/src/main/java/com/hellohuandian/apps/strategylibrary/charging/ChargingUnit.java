package com.hellohuandian.apps.strategylibrary.charging;

import com.hellohuandian.apps.strategylibrary.charging.strategies.ChargingStrategy;
import com.hellohuandian.apps.strategylibrary.strategies._data.BatteryData;

/**
 * Author:      Lee Yeung
 * Create Date: 2019-10-12
 * Description: 充电
 */
public class ChargingUnit
{
    private ChargingStrategy chargingStrategy;

    public void setChargingStrategy(ChargingStrategy chargingStrategy)
    {
        this.chargingStrategy = chargingStrategy;
    }

    public void charging(BatteryData batteryData)
    {
        if (chargingStrategy != null)
        {
            chargingStrategy.charging(batteryData);
        }
    }
}
