package com.hellohuandian.apps.strategylibrary.strategies.pushRod;

import com.hellohuandian.apps.controllerlibrary.DeviceIoAction;
import com.hellohuandian.apps.controllerlibrary.DeviceIoController;
import com.hellohuandian.apps.strategylibrary._core.dispatchers.TaskStrategy;

/**
 * Author:      Lee Yeung
 * Create Date: 2019-08-28
 * Description: 推杆
 */
public class PushRodStrategy extends TaskStrategy
{
    public PushRodStrategy(byte address)
    {
        super(address);
    }

    @Override
    protected void execute(DeviceIoAction deviceIoAction)
    {

    }
}
