package com.app10000.apps.strategylibrary.charging;

import com.app10000.apps.strategylibrary.config.MachineVersion;
import com.app10000.apps.strategylibrary.strategies.battery.BatteryInfo;

/**
 * Author:      Lee Yeung
 * Create Date: 2019-11-05
 * Description:
 */
class ChargingTable
{
    private ChargingController chargingController = new ChargingController();

    private BatteryInfo[] batteryInfos;
    private int differenceValue;

    void init(@MachineVersion int version)
    {
        switch (version)
        {
            case MachineVersion.SC_3:
                //控制板地址去映射到数组下标
                differenceValue = 1;
                //初始化对应舱门数量的数组
                batteryInfos = new BatteryInfo[12];
                break;
            case MachineVersion.SC_4:
            case MachineVersion.SC_5:
            case MachineVersion.SC_5_1:
                differenceValue = 5;
                batteryInfos = new BatteryInfo[9];
                break;
        }
        chargingController.init(version);
    }

    void recordBatteryInfo(BatteryInfo batteryData)
    {
        if (batteryInfos != null && batteryInfos != null)
        {
            final int index = batteryData.address - differenceValue;
            if (index < batteryInfos.length)
            {
                batteryInfos[index] = batteryData;
                chargingController.controlCharging(batteryInfos);
            }
        }
    }
}
