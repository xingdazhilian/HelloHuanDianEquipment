package com.app10000.apps.strategylibrary.charging;

import com.app10000.apps.strategylibrary.config.MachineVersion;
import com.app10000.apps.strategylibrary.strategies.battery.BatteryInfo;

/**
 * Author:      Lee Yeung
 * Create Date: 2019-10-12
 * Description: 充电单元
 */
public class ChargingUnit
{
    private ChargingTable chargingTable = new ChargingTable();

    public void init(@MachineVersion int version)
    {
        chargingTable.init(version);
    }

    public void charging(BatteryInfo batteryData)
    {
        chargingTable.recordBatteryInfo(batteryData);
    }
}
