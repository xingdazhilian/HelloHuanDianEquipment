package com.hellohuandian.apps.strategylibrary.strategies.charging;

import com.hellohuandian.apps.controllerlibrary.DeviceIoAction;
import com.hellohuandian.apps.strategylibrary.dispatchers.canExtension.CanDeviceIoAction;
import com.hellohuandian.apps.strategylibrary.strategies._base.ProtocolStrategy;
import com.hellohuandian.apps.utillibrary.StringFormatHelper;

import java.io.IOException;

/**
 * Author:      Lee Yeung
 * Create Date: 2019-10-25
 * Description: 充电策略
 * <p>
 * CAN数据充电
 * 3.3.2 安卓板设置该终端的电压电流帧
 * 传输循环率:2S周期发送/改变时立即发送 数据长度:5字节
 * 默认优先值:6
 * PF:01H
 * PS(目标地址):充电机终端 SA(源地址):安卓板 默认优先值:6
 * <p>
 * 起始位/字节   长度      SPN定义         说明
 * 1            2字节     需求电压(V)     数据分辨率:0.1V/位，0V偏移量
 * 3            2字节     需求电流(A)     数据分辨率:0.1A/位，-400A偏移量
 * 5            1字节     充电模式        0x01:恒压充电，0x02恒流充电
 * <p>
 * 设置电压和电流举例:安卓板下发给0x15终端的电压为60V，电流为10A，模式为恒流充电 帧ID 数据域
 * 18010500 58 02 3C 0F 02
 */
public class ChargingStrategy extends ProtocolStrategy
{
    private short v = (short) (65.5 * 10);
    private short a = (400 - 10) * 10;

    private OnChargingAction onChargingAction;

    public ChargingStrategy(byte address)
    {
        super(address);
    }

    public void setOnChargingAction(OnChargingAction onChargingAction)
    {
        this.onChargingAction = onChargingAction;
    }

    public void setAddress(byte address)
    {
        this.address = address;
    }

    public byte getAddress()
    {
        return address;
    }

    public void setBatterySpecification(char batterySpecification)
    {
        switch (batterySpecification)
        {
            case 'I':
                v = (short) (54.6 * 10);
                a = (400 - 16) * 10;
                break;
            case 'M':
                v = (short) (65.5 * 10);
                a = (400 - 16) * 10;
                break;
            case '0':
            case 0:
                a = 0;
                break;
        }
    }

    @Override
    protected void execute_sp(DeviceIoAction deviceIoAction)
    {

    }

    @Override
    protected void execute_can(final CanDeviceIoAction deviceIoAction)
    {
        final int resultId = 0x98 << 24 | (0x02 & 0xFF) << 16 | (0x00 & 0xFF) << 8 | (address & 0xFF);
        deviceIoAction.register(resultId, new NodeConsumer()
        {
            @Override
            public void onAccept(byte[] bytes)
            {
                if (onChargingAction != null)
                {
                    if (bytes != null && bytes.length == 16 && ((bytes[8] & 0xFF) == 0xFF))
                    {
                        // TODO: 2019-11-18 充电成功
                        onChargingAction.onChargingSuccessed(address);
                    } else
                    {
                        onChargingAction.onChargingFailed(address);
                    }
                }
                System.out.println("充电结果：" + StringFormatHelper.getInstance().toHexString(bytes));
                deviceIoAction.unRegister(resultId);
            }
        });

        final byte[] DATA = new byte[]{0x00, address, 0x01, (byte) 0x98, 0x06, 0x00, 0x00, 0x00,
                (byte) (v & 0xFF), (byte) ((v >> 8) & 0xFF), (byte) (a & 0xFF), (byte) ((a >> 8) & 0xFF), 0x02,
                0x02, 0x00, 0x00};
        try
        {
            deviceIoAction.write(DATA);
            System.out.println("充电下发：" + StringFormatHelper.getInstance().toHexString(DATA));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
