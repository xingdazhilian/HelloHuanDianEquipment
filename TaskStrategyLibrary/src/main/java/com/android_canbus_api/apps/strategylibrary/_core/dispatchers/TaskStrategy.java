package com.android_canbus_api.apps.strategylibrary._core.dispatchers;

import com.android_canbus_api.apps.controllerlibrary.DeviceIoAction;

/**
 * Author:      Lee Yeung
 * Create Date: 2019-09-16
 * Description: 任务策略，提供策略执行入口和必须的读写操作
 */
public abstract class TaskStrategy
{
    protected byte address;

    public TaskStrategy(byte address)
    {
        this.address = address;
    }

    protected abstract void execute(DeviceIoAction deviceIoAction);
}
