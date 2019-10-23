package com.hellohuandian.apps.strategylibrary.strategies.upgrade.battery;

import com.hellohuandian.apps.controllerlibrary.DeviceIoAction;
import com.hellohuandian.apps.strategylibrary.strategies._base.NodeStrategy;
import com.hellohuandian.apps.strategylibrary.strategies.battery.OnBatteryDataUpdate;

/**
 * Author:      Lee Yeung
 * Create Date: 2019-09-04
 * Description: 电池升级策略
 */
public abstract class BatteryUpgradeStrategy extends NodeStrategy
{
    protected final String filePath;

    protected OnBatteryDataUpdate onBatteryDataUpdate;

    public BatteryUpgradeStrategy(byte address, String filePath)
    {
        super(address);
        this.filePath = filePath;
    }

    public void setOnBatteryDataUpdate(OnBatteryDataUpdate onBatteryDataUpdate)
    {
        this.onBatteryDataUpdate = onBatteryDataUpdate;
    }

    public OnBatteryDataUpdate getOnBatteryDataUpdate()
    {
        return onBatteryDataUpdate;
    }

    /**
     * 升级
     *
     * @param deviceIoAction      设备iO操作，提供io功能
     * @param onBatteryDataUpdate 读写信息回调
     */
    protected abstract void upgrade(DeviceIoAction deviceIoAction, OnBatteryDataUpdate onBatteryDataUpdate);

    @Override
    public void execute(DeviceIoAction deviceIoAction)
    {
        upgrade(deviceIoAction, onBatteryDataUpdate);
        // TODO: 2019-10-10 升级完成之后执行下一个策略
        nextCall();
    }
}
