package com.app10000.apps.strategylibrary.strategies._base;

import com.app10000.apps.controllerlibrary.DeviceIoAction;
import com.app10000.apps.strategylibrary.dispatchers.canExtension.CanDeviceIoAction;

/**
 * Author:      Lee Yeung
 * Create Date: 2019-09-20
 * Description:
 */
public abstract class ProtocolStrategy extends NodeStrategy
{
    public ProtocolStrategy(byte address)
    {
        super(address);
    }

    public ProtocolStrategy(byte address, int machineVersion)
    {
        super(address,machineVersion);
    }

    @Override
    public void execute(DeviceIoAction deviceIoAction)
    {
        if (deviceIoAction == null)
        {
            return;
        }

        switch (deviceIoAction.ioProtocol())
        {
            case DeviceIoAction.Protocol.SERIAL_PORT:
                execute_sp(deviceIoAction);
                break;
            case DeviceIoAction.Protocol.CANBUS:
                if (deviceIoAction instanceof CanDeviceIoAction)
                {
                    execute_can((CanDeviceIoAction) deviceIoAction);
                }
                break;
        }
    }

    /**
     * 通过串口执行
     *
     * @param deviceIoAction
     */
    protected abstract void execute_sp(DeviceIoAction deviceIoAction);

    /**
     * 通知can执行
     *
     * @param deviceIoAction
     */
    protected abstract void execute_can(CanDeviceIoAction deviceIoAction);
}
