package com.hellohuandian.apps.strategylibrary.strategies.pushRod;

import com.hellohuandian.apps.controllerlibrary.DeviceIoAction;
import com.hellohuandian.apps.strategylibrary._core.dispatchers.canExtension.CanDeviceIoAction;
import com.hellohuandian.apps.strategylibrary.strategies._base.ProtocolStrategy;

import java.io.IOException;

/**
 * Author:      Lee Yeung
 * Create Date: 2019-08-28
 * Description: 推杆策略
 */
public class PushRodStrategy extends ProtocolStrategy
{
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
        final byte[] PUSH_ROD = {address, 0x05, 0x00, 0x09, 0x00, 0x03, 0x00, 0x00};
        short crc = crc16(PUSH_ROD, 0, 6);
        PUSH_ROD[PUSH_ROD.length - 2] = (byte) (crc & 0xFF);
        PUSH_ROD[PUSH_ROD.length - 1] = (byte) (crc >> 8 & 0xFF);

        try
        {
            deviceIoAction.write(PUSH_ROD);
            // TODO: 2019-09-18 发生推送指令之后，10秒后开始读取结果
            sleep(10 * 1000);//此处10S是实际测试大约的推杆收缩动作结束完成的时间
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
    protected void execute_can(final CanDeviceIoAction deviceIoAction)
    {
        sleep(2000);
        final byte[] PUSH_ROD = {address, 0x00, 0x00, 0x00, 0x08, 0x00, 0x00, 0x00, address, 0x05, 0x00, 0x09, 0x00, 0x03, 0x00, 0x00};
        // TODO: 2019-09-21 只对数据内容做crc填充
        short crc = crc16(PUSH_ROD, 8, 14);
        PUSH_ROD[PUSH_ROD.length - 2] = (byte) (crc & 0xFF);
        PUSH_ROD[PUSH_ROD.length - 1] = (byte) (crc >> 8 & 0xFF);
        try
        {
            deviceIoAction.write(PUSH_ROD);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        // TODO: 2019-09-23 生成唯一ID，格式： PUSH_ROD[0] << 16 | PUSH_ROD[8] << 8 | PUSH_ROD[11]
        final int id = (address & 0xFF) << 16 | PUSH_ROD[11];
        deviceIoAction.register(id, new NodeConsumer()
        {
            @Override
            public void onAccept(byte[] bytes)
            {
                int resultId = 0;
                if (bytes != null && bytes.length == 16)
                {
                    resultId = (bytes[0] & 0xFF) << 16 | (bytes[8] & 0xFF) << 8 | PUSH_ROD[11];
                }
                if (onPushAction != null)
                {
                    if (resultId == id)
                    {
                        onPushAction.onPushSuccessed(address);
                    } else
                    {
                        onPushAction.onPushFailed(address);
                    }
                }
                deviceIoAction.unRegister(id);
            }
        });
    }

}
