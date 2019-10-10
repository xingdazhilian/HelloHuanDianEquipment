package com.hellohuandian.apps.strategylibrary.strategies._base;

import com.hellohuandian.apps.controllerlibrary.DeviceIoAction;

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

    public abstract void execute(DeviceIoAction deviceIoAction);
}






