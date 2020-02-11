package com.hellohuandian.apps.strategylibrary.charging;

import com.hellohuandian.apps.strategylibrary.charging.engines.ChargingEngine;
import com.hellohuandian.apps.strategylibrary.charging.engines.SC_3_ChargingEngine;
import com.hellohuandian.apps.strategylibrary.charging.engines.SC_5_1_ChargingEngine;
import com.hellohuandian.apps.strategylibrary.charging.engines.SC_5_ChargingEngine;
import com.hellohuandian.apps.strategylibrary.config.MachineVersion;
import com.hellohuandian.apps.strategylibrary.strategies.battery.BatteryInfo;

/**
 * Author:      Lee Yeung
 * Create Date: 2019-11-01
 * Description:
 */
class ChargingController
{
    private ChargingEngine chargingEngine;

    public void init(@MachineVersion int version)
    {
        switch (version)
        {
            case MachineVersion.SC_3:
                chargingEngine = new SC_3_ChargingEngine();
                break;
            case MachineVersion.SC_4:
            case MachineVersion.SC_5:
                chargingEngine = new SC_5_ChargingEngine();
                break;
            case MachineVersion.SC_5_1:
                chargingEngine = new SC_5_1_ChargingEngine();
                break;
        }
    }

    void controlCharging(BatteryInfo[] batteryInfos)
    {
        if (chargingEngine != null)
        {
//            chargingEngine.charging(batteryInfos);
        }
    }
}
