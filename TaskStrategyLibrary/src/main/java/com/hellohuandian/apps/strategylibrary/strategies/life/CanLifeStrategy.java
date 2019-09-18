package com.hellohuandian.apps.strategylibrary.strategies.life;

import com.hellohuandian.apps.controllerlibrary.DeviceIoAction;
import com.hellohuandian.apps.controllerlibrary.DeviceIoController;
import com.hellohuandian.apps.strategylibrary.strategies._base.BaseStrategy;

/**
 * Author:      Lee Yeung
 * Create Date: 2019-08-22
 * Description: 生命帧策略
 */
public class CanLifeStrategy extends BaseStrategy
{
    private byte address = 0x00;

    private final byte[] data = new byte[]{0x00, address, 0x05, (byte) 0x98, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

    public CanLifeStrategy(byte address)
    {
        super(address);
    }


    @Override
    protected void execute(DeviceIoAction deviceIoAction)
    {

    }
}
