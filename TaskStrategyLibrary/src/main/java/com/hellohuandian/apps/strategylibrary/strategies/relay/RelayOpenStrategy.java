package com.hellohuandian.apps.strategylibrary.strategies.relay;

import com.hellohuandian.apps.controllerlibrary.DeviceIoAction;
import com.hellohuandian.apps.strategylibrary.strategies._base.BaseStrategy;

/**
 * Author:      Lee Yeung
 * Create Date: 2019-08-22
 * Description: 继电器通路(继电器控制地址从0x15~0x1D)
 */
public class RelayOpenStrategy extends BaseStrategy
{
    private OnRelaySwitchAction onRelaySwitchAction;
    private final byte[] data = new byte[]{0x00, address, 0x00, (byte) 0x98, 0x06, 0x00, 0x00, 0x00, 0x55, 0x00, 0x22, 0x02, 0x22, 0x02, 0x00, 0x00};

    public RelayOpenStrategy(byte address)
    {
        super(address);
    }

    public void setOnRelaySwitchAction(OnRelaySwitchAction onRelaySwitchAction)
    {
        this.onRelaySwitchAction = onRelaySwitchAction;
    }

    @Override
    protected void execute(DeviceIoAction deviceIoAction)
    {

    }
}