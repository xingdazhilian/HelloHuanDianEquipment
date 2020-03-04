package com.app10000.apps.strategylibrary.strategies.lifes;

import com.app10000.apps.controllerlibrary.DeviceIoAction;

import java.io.IOException;

/**
 * Author:      Lee Yeung
 * Create Date: 2019-08-22
 * Description: DCDC模块生命帧
 */
public final class DC_LifeStrategy extends LifeStrategy
{
    private final byte PF = 0x07;
    private final byte PS = address;//PS目标地址
    private final byte SA = 0x65;//SA：Android的地址固定
    private byte sn;
    // TODO: 2019-08-23 长度必须是16协议规则
    private final byte[] DATA = new byte[]{SA, PS, PF, (byte) 0x98,
            0x01,//数据长度
            0x00, 0x00, 0x00,//远程帧，错误帧，过载帧
            sn,//递增序号：0~255(从0开始发送，发送一帧+1， 超过255继续从0开始)
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};//填充剩余，满足16位

    public DC_LifeStrategy()
    {
        this((byte) 0xFF);//广播所有
    }

    public DC_LifeStrategy(byte address)
    {
        super(address);
    }

    @Override
    public void execute(DeviceIoAction deviceIoAction)
    {
        if (deviceIoAction == null)
        {
            return;
        }
        sn = sn % 256 == 0 ? 0 : sn;
        DATA[8] = sn++;
        try
        {
            deviceIoAction.write(DATA);
            sleep(1000);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
