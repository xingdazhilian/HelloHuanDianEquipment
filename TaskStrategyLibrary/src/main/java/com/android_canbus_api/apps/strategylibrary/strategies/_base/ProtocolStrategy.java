package com.android_canbus_api.apps.strategylibrary.strategies._base;

import com.android_canbus_api.apps.controllerlibrary.DeviceIoAction;

/**
 * Author:      Lee Yeung
 * Create Date: 2019-09-20
 * Description:
 */
public abstract class ProtocolStrategy extends BaseStrategy
{
    public ProtocolStrategy(byte address)
    {
        super(address);
    }

    @Override
    protected final void execute(DeviceIoAction deviceIoAction)
    {
        if (deviceIoAction == null)
        {
            return;
        }

        switch (deviceIoAction.ioProtocol())
        {
            case DeviceIoAction.SERIAL_PORT:
                execute_sp(deviceIoAction);
                break;
            case DeviceIoAction.CANBUS:
                execute_can(deviceIoAction);
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
    protected abstract void execute_can(DeviceIoAction deviceIoAction);
}
