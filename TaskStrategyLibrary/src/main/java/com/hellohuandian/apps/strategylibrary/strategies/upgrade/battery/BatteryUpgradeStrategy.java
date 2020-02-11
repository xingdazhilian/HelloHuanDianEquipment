package com.hellohuandian.apps.strategylibrary.strategies.upgrade.battery;

import com.hellohuandian.apps.controllerlibrary.DeviceIoAction;
import com.hellohuandian.apps.strategylibrary.dispatchers.canExtension.CanDeviceIoAction;
import com.hellohuandian.apps.strategylibrary.strategies._base.ProtocolStrategy;
import com.hellohuandian.apps.strategylibrary.strategies.battery.OnBatteryDataUpdate;

/**
 * Author:      Lee Yeung
 * Create Date: 2019-09-04
 * Description: 电池升级策略
 */
public abstract class BatteryUpgradeStrategy extends ProtocolStrategy
{
    protected final String filePath;
    protected String idCode;
    protected String bmsHardwareVersion;
    protected String crcValue;

    protected OnBatteryDataUpdate onBatteryDataUpdate;

    public BatteryUpgradeStrategy(byte address, String filePath)
    {
        super(address);
        this.filePath = filePath;
    }

    /**
     * @param idCode             ID码，要求写入前两位
     * @param bmsHardwareVersion 十六进制数据
     */
    public void setIdCodeAndBmsHardwareVersion(String idCode, String bmsHardwareVersion, String crcValue)
    {
        this.idCode = idCode;
        this.bmsHardwareVersion = bmsHardwareVersion;
        this.crcValue = crcValue;
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
        super.execute(deviceIoAction);
        // TODO: 2019-10-10 升级完成之后执行下一个策略
        nextCall();
    }


    @Override
    protected void execute_sp(DeviceIoAction deviceIoAction)
    {
        upgrade(deviceIoAction, onBatteryDataUpdate);
    }

    @Override
    protected void execute_can(CanDeviceIoAction deviceIoAction)
    {
        upgrade_can(deviceIoAction, onBatteryDataUpdate);
    }

    protected void upgrade_can(CanDeviceIoAction deviceIoAction, OnBatteryDataUpdate onBatteryDataUpdate)
    {
    }
}
