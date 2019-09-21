package com.android_canbus_api.apps.strategylibrary.strategies.relay;

import com.android_canbus_api.apps.controllerlibrary.DeviceIoAction;
import com.android_canbus_api.apps.strategylibrary.strategies._base.BaseStrategy;

/**
 * Author:      Lee Yeung
 * Create Date: 2019-08-22
 * Description:
 */
public class RelayCloseStrategy extends BaseStrategy
{
    private final byte[] data = new byte[]{0x00, address, 0x00, (byte) 0x98, 0x06, 0x00, 0x00, 0x00, (byte) 0xAA, 0x00, 0x22, 0x02, 0x00, 0x00,
            0x00, 0x00};

    public RelayCloseStrategy(byte address)
    {
        super(address);
    }

    @Override
    protected void execute(DeviceIoAction deviceIoAction)
    {

    }
}
