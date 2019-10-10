package com.hellohuandian.apps.strategylibrary.strategies.upgrade.battery;

import com.hellohuandian.apps.controllerlibrary.DeviceIoAction;
import com.hellohuandian.apps.strategylibrary.strategies._base.BaseStrategy;

/**
 * Author:      Lee Yeung
 * Create Date: 2019-09-04
 * Description: 电池升级策略
 */
public abstract class BatteryUpgradeStrategy extends BaseStrategy
{
    protected final String filePath;

    protected OnUpgradeProgress onUpgradeProgress;

    public BatteryUpgradeStrategy(byte address, String filePath)
    {
        super(address);
        this.filePath = filePath;
    }

    public void setOnUpgradeProgress(OnUpgradeProgress onUpgradeProgress)
    {
        this.onUpgradeProgress = onUpgradeProgress;
    }

    /**
     * 升级
     *
     * @param deviceIoAction    设备iO操作，提供io功能
     * @param onUpgradeProgress 读写信息回调
     */
    protected abstract void upgrade(DeviceIoAction deviceIoAction, OnUpgradeProgress onUpgradeProgress);

    @Override
    public void execute(DeviceIoAction deviceIoAction)
    {
        upgrade(deviceIoAction, onUpgradeProgress);
    }
}
