package com.android_canbus_api.apps.strategylibrary.strategies.pushRod;

import com.android_canbus_api.apps.controllerlibrary.DeviceIoAction;
import com.android_canbus_api.apps.strategylibrary.strategies._base.ProtocolStrategy;

import java.io.IOException;

/**
 * Author:      Lee Yeung
 * Create Date: 2019-08-28
 * Description: 推杆
 */
public class PushRodStrategy extends ProtocolStrategy
{
    private final byte[] PUSH_ROD = {address, 0x05, 0x00, 0x09, 0x00, 0x03, 0x00, 0x00};

    private OnPushAction onPushAction;

    public PushRodStrategy(byte address)
    {
        super(address);
    }

    public void setOnPushAction(OnPushAction onPushAction)
    {
        this.onPushAction = onPushAction;
    }

    @Override
    protected void execute_sp(DeviceIoAction deviceIoAction)
    {
        short crc = crc16(PUSH_ROD, 0, 6);
        PUSH_ROD[PUSH_ROD.length - 2] = (byte) (crc & 0xFF);
        PUSH_ROD[PUSH_ROD.length - 1] = (byte) (crc >> 8 & 0xFF);

        try
        {
            deviceIoAction.write(PUSH_ROD);
            // TODO: 2019-09-18 发生推送指令之后，10秒后开始读取结果
            sleep(10 * 1000);
            byte[] result = deviceIoAction.read();
            if (result != null && result.length > 0)
            {
                // TODO: 2019-09-18 收缩推杆正常完成
                if (onPushAction != null)
                {
                    onPushAction.onPushSuccessed(address);
                }
            } else
            {
                // TODO: 2019-09-18 收缩推杆可能失败，继续等待2S，再次失败认为推杆故障
                sleep(2 * 1000);
                result = deviceIoAction.read();
                if (result != null && result.length > 0)
                {
                    // TODO: 2019-09-18 推杆成功
                    if (onPushAction != null)
                    {
                        onPushAction.onPushSuccessed(address);
                    }
                } else
                {
                    // TODO: 2019-09-18 推杆失败，可能存在故障
                    if (onPushAction != null)
                    {
                        onPushAction.onPushFailed(address);
                    }
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    protected void execute_can(DeviceIoAction deviceIoAction)
    {

    }

}
