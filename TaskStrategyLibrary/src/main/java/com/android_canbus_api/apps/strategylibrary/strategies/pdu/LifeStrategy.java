package com.android_canbus_api.apps.strategylibrary.strategies.pdu;

import com.android_canbus_api.apps.controllerlibrary.DeviceIoAction;
import com.android_canbus_api.apps.strategylibrary.strategies._base.BaseStrategy;

import java.io.IOException;

/**
 * Author:      Lee Yeung
 * Create Date: 2019-08-22
 * Description: PDU充电机生命帧策略需要间隔1S发送一次(厂商规定最长间隔是15S之内必须发送生命帧)
 */
public final class LifeStrategy extends BaseStrategy
{
    // TODO: 2019-09-19 充电机生命帧地址，规定只发送给该地址即可
    private final byte PF = 0x05;//指令的类型，生命帧，还是打开继电器，还是要关闭继电器
    private final byte PS = address;//PS目标地址
    private final byte SA = 0x00;//SA：Android的地址固定
    private byte sn;
    // TODO: 2019-08-23 长度必须是16协议规则
    private final byte[] data = new byte[]{SA, PS, PF, (byte) 0x98,//SA, PS, PF, (byte) 0x98固定顺序(如果是充电机发给Android的则SA和PS是反的注意！)：源地址，目标地址，指令类型，98固定值
            0x01,//数据长度
            0x00, 0x00, 0x00,//远程帧，错误帧，过载帧
            sn,//递增序号：0~255(从0开始发送，发送一帧+1， 超过255继续从0开始)
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};//填充剩余，满足16位

    public LifeStrategy(byte address)
    {
        super(address);
    }

    @Override
    protected void execute(DeviceIoAction deviceIoAction)
    {
        if (deviceIoAction == null)
        {
            return;
        }
        sn = sn % 256 == 0 ? 0 : sn;
        data[8] = sn++;
        try
        {
            deviceIoAction.write(data);
            sleep(1000);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
