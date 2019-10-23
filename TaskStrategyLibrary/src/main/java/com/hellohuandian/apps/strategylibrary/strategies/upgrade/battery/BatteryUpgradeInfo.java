package com.hellohuandian.apps.strategylibrary.strategies.upgrade.battery;

import com.hellohuandian.apps.strategylibrary.strategies._data.BatteryData;

/**
 * Author:      Lee Yeung
 * Create Date: 2019-10-11
 * Description: 电池升级信息类
 */
public class BatteryUpgradeInfo extends BatteryData
{
    public @BatteryUpgradeStrategyStatus
    int statusFlag;
    public String statusInfo;
    public long currentPregress;
    public long totalPregress;

    public BatteryUpgradeInfo(byte address)
    {
        super(address);
    }

    @Override
    public int getBatteryDataType()
    {
        return BatteryDataType.UPGRADE;
    }

    @Override
    public String toSimpleString()
    {
        return statusInfo;
    }
}
