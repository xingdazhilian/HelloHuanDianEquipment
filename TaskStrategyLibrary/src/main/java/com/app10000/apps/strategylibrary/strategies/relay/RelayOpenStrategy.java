package com.app10000.apps.strategylibrary.strategies.relay;

import com.app10000.apps.controllerlibrary.DeviceIoAction;
import com.app10000.apps.strategylibrary.dispatchers.canExtension.CanDeviceIoAction;
import com.app10000.apps.strategylibrary.strategies._base.ProtocolStrategy;
import com.app10000.apps.utillibrary.StringFormatHelper;

import java.io.IOException;

/**
 * Author:      Lee Yeung
 * Create Date: 2019-08-22
 * Description: 继电器通路(继电器控制地址从0x15~0x1D)
 */
public class RelayOpenStrategy extends ProtocolStrategy
{
    private OnRelaySwitchAction onRelaySwitchAction;

    public RelayOpenStrategy(byte address)
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
        // TODO: 2019-09-27 先配置好结果ID
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

        final byte[] DATA = new byte[]{0x00, address, 0x00, (byte) 0x98, 0x06, 0x00, 0x00, 0x00, 0x55, 0x00, 0x22, 0x02, 0x22, 0x02, 0x00, 0x00};
        try
        {
            deviceIoAction.write(DATA);
            System.out.println("写入继电器：" + StringFormatHelper.getInstance().toHexString(DATA));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}