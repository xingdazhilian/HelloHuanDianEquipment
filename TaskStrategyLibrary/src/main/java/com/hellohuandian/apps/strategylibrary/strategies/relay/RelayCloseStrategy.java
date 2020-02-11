package com.hellohuandian.apps.strategylibrary.strategies.relay;

import com.hellohuandian.apps.controllerlibrary.DeviceIoAction;
import com.hellohuandian.apps.strategylibrary.dispatchers.canExtension.CanDeviceIoAction;
import com.hellohuandian.apps.strategylibrary.strategies._base.ProtocolStrategy;

import java.io.IOException;

/**
 * Author:      Lee Yeung
 * Create Date: 2019-08-22
 * Description: 关闭继电器策略
 */
public class RelayCloseStrategy extends ProtocolStrategy
{
    private OnRelaySwitchAction onRelaySwitchAction;

    public RelayCloseStrategy(byte address)
    {
        super(address);
    }

    public void setOnRelaySwitchAction(OnRelaySwitchAction onRelaySwitchAction)
    {
        this.onRelaySwitchAction = onRelaySwitchAction;
    }

    @Override
    protected void execute_sp(DeviceIoAction deviceIoAction)
    {

    }

    @Override
    protected void execute_can(final CanDeviceIoAction deviceIoAction)
    {
        // TODO: 2019-09-27 配置好结果ID
        final int resultId = 0x98 << 24 | (0x02 & 0xFF) << 16 | (0x00 & 0xFF) << 8 | (address & 0xFF);
        deviceIoAction.register(resultId, new NodeConsumer()
        {
            @Override
            public void onAccept(byte[] bytes)
            {
                if (onRelaySwitchAction != null)
                {
                    onRelaySwitchAction.onSwitchStatus(true);
                }
                deviceIoAction.unRegister(resultId);
            }
        });
        final byte[] DATA = new byte[]{0x00, address, 0x00, (byte) 0x98, 0x06, 0x00, 0x00, 0x00, (byte) 0xAA, 0x00, 0x22, 0x02, 0x00, 0x00,
                0x00, 0x00};
        try
        {
            deviceIoAction.write(DATA);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
