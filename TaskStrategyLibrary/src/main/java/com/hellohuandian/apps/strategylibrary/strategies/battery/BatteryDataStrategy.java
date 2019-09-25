package com.hellohuandian.apps.strategylibrary.strategies.battery;

import com.hellohuandian.apps.controllerlibrary.DeviceIoAction;
import com.hellohuandian.apps.strategylibrary._core.dispatchers.canExtension.CanDeviceIoAction;
import com.hellohuandian.apps.strategylibrary.strategies._base.ProtocolStrategy;
import com.hellohuandian.apps.utillibrary.StringFormatHelper;

import java.io.IOException;

import androidx.core.util.Consumer;

/**
 * Author:      Lee Yeung
 * Create Date: 2019-09-24
 * Description: 电池数据包策略
 */
public class BatteryDataStrategy extends ProtocolStrategy
{
    private final StringBuilder stringBuilder = new StringBuilder();
    private final byte[] _16bytes = new byte[16];
    private final BatteryData batteryData = new BatteryData();
    private OnBatteryDataUpdate onBatteryDataUpdate;

    public BatteryDataStrategy(byte address)
    {
        super(address);
    }

    public void setOnBatteryDataUpdate(OnBatteryDataUpdate onBatteryDataUpdate)
    {
        this.onBatteryDataUpdate = onBatteryDataUpdate;
    }

    @Override
    protected void execute_sp(DeviceIoAction deviceIoAction)
    {
        final byte[] DATA = {address, 0x02, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        short crc = crc16(DATA, 0, 6);
        DATA[DATA.length - 2] = (byte) (crc & 0xFF);
        DATA[DATA.length - 1] = (byte) (crc >> 8 & 0xFF);

        try
        {
            deviceIoAction.write(DATA);
            sleep(200);
            // TODO: 2019-09-24 解析电池数据包
            parse(deviceIoAction.read());
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    protected void execute_can(CanDeviceIoAction deviceIoAction)
    {
        // TODO: 2019-09-25 虽然默认控制板会上报数据包信息，但是为了功能结构统一，必须执行
        final byte[] DATA = {address, 0x00, 0x00, 0x00, 0x08, 0x00, 0x00, 0x00, address, 0x02, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        // TODO: 2019-09-21 只对数据内容做crc填充
        short crc = crc16(DATA, 8, 14);
        DATA[DATA.length - 2] = (byte) (crc & 0xFF);
        DATA[DATA.length - 1] = (byte) (crc >> 8 & 0xFF);
        try
        {
            deviceIoAction.write(DATA);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        final int resultId = address;
        deviceIoAction.register(resultId, new Consumer<byte[]>()
        {
            private byte[] result;
            private int realLen;
            private int position;
            private final int start = 9;
            private boolean isReceive = true;
            private int nextFrameSn;

            @Override
            public void accept(byte[] bytes)
            {
                if (bytes != null && bytes.length == 16)
                {
                    if (bytes[8] == 0x10)
                    {
                        realLen = bytes[11];

                        if (!(result != null && result.length == realLen))
                        {
                            result = new byte[realLen];
                        }
                        isReceive = true;
                    }

                    if (isReceive && bytes[8] >= 0x10 && result != null && result.length == realLen)
                    {
                        final int coypLen = bytes.length - start;
                        System.arraycopy(bytes, start, result, position, coypLen);
                        position += coypLen;

                        if (position >= realLen)
                        {
                            position = 0;
                            // TODO: 2019-09-24 数据包收集完毕
                            parse(result);
                        }
                    }
                }
            }
        });
    }

    /**
     * 解析说明见：
     * 安卓板和下位机通讯协议修改版V09_增加BMS转发_温度.docx
     *
     * @param bytes
     */
    private void parse(byte[] bytes)
    {
        System.out.println(address + "号读取：" + StringFormatHelper.getInstance().toHexString(bytes));
        batteryData.reset();

        if (bytes != null && bytes.length > 0)
        {
            parseDoorLockStatus(bytes);
            parseElectricMachineStatus(bytes);
            parseBatteryTemperature(bytes);
            parseBatteryTotalVoltage(bytes);
            parseRealTimeCurrent(bytes);
            parseRelativeCapatityPercent(bytes);
            parseAbsoluteCapatityPercent(bytes);
            parseRemainingCapatity(bytes);
            parseFullCapatity(bytes);
            parseLoopCount(bytes);
            parseBatteryVoltage_1_7(bytes);
            parseBatteryVoltage_8_15(bytes);
            parseSOH(bytes);
            parseBatteryIdInfo(bytes);
            parseVersion(bytes);
            parseControlPanelTemperature(bytes);
            parseControlPanelVersion(bytes);
        }

        if (onBatteryDataUpdate != null)
        {
            onBatteryDataUpdate.onUpdate(batteryData);
        }
    }

    /**
     * 解析门锁状态
     *
     * @param bytes
     */
    private void parseDoorLockStatus(byte[] bytes)
    {
        if (bytes.length > 3)
        {
            byte doorLockStatus = bytes[3];
            System.out.println("门锁状态：" + (doorLockStatus == 0 ? "关闭" : "打开"));
        }
    }

    /**
     * 解析电机状态
     *
     * @param bytes
     */
    private void parseElectricMachineStatus(byte[] bytes)
    {
        if (bytes.length > 4)
        {
            byte electricMachineStatus = bytes[4];
            System.out.println("电机状态：" + (electricMachineStatus == 0 ? "关闭" : "打开"));
        }
    }

    /**
     * 解析电池温度
     *
     * @param bytes
     */
    private void parseBatteryTemperature(byte[] bytes)
    {
        if (bytes.length > 8)
        {
            int value = 0;
            for (int i = 5, j = 0; i <= 8; value |= ((bytes[i] & 0xFF) << (j * 8)), i++, j++) ;
            value -= 2731;
            float temperature = (float) value / 10;
            System.out.println("温度：" + temperature + "度");
        }
    }

    /**
     * 解析电压
     *
     * @param bytes
     */
    private void parseBatteryTotalVoltage(byte[] bytes)
    {
        if (bytes.length > 12)
        {
            int value = 0;
            for (int i = 9, j = 0; i <= 12; value |= ((bytes[i] & 0xFF) << (j * 8)), i++, j++) ;
            System.out.println("电压：" + value + "mV");
        }
    }

    /**
     * 解析电流
     *
     * @param bytes
     */
    private void parseRealTimeCurrent(byte[] bytes)
    {
        if (bytes.length > 16)
        {
            int value = 0;
            for (int i = 13, j = 0; i <= 16; value |= ((bytes[i] & 0xFF) << (j * 8)), i++, j++) ;
            if (value >= 0x4e20)
            {
                value = (~value + 1);
            }
            System.out.println("电流：" + value + "mA");
        }
    }

    /**
     * 解析电池相对容量百分比
     *
     * @param bytes
     */
    private void parseRelativeCapatityPercent(byte[] bytes)
    {
        if (bytes.length > 18)
        {
            int value = 0;
            for (int i = 17, j = 0; i <= 18; value |= ((bytes[i] & 0xFF) << (j * 8)), i++, j++) ;
            System.out.println("电池相对容量百分比：" + value + "%");
        }
    }

    /**
     * 电池绝对容量百分比
     *
     * @param bytes
     */
    private void parseAbsoluteCapatityPercent(byte[] bytes)
    {
        if (bytes.length > 20)
        {
            int value = 0;
            for (int i = 19, j = 0; i <= 20; value |= ((bytes[i] & 0xFF) << (j * 8)), i++, j++) ;
            System.out.println("电池绝对容量百分比：" + ((float) value / 100) + "%");
        }
    }

    /**
     * 解析电池剩余容量
     *
     * @param bytes
     */
    private void parseRemainingCapatity(byte[] bytes)
    {
        if (bytes.length > 22)
        {
            int value = 0;
            for (int i = 21, j = 0; i <= 22; value |= ((bytes[i] & 0xFF) << (j * 8)), i++, j++) ;
            System.out.println("电池剩余容量：" + value + "mAh");
        }
    }

    /**
     * 解析电池满充容量
     *
     * @param bytes
     */
    private void parseFullCapatity(byte[] bytes)
    {
        if (bytes.length > 24)
        {
            int value = 0;
            for (int i = 23, j = 0; i <= 24; value |= ((bytes[i] & 0xFF) << (j * 8)), i++, j++) ;
            System.out.println("电池满充容量：" + value + "mAh");
        }
    }

    /**
     * 解析电池循环次数
     *
     * @param bytes
     */
    private void parseLoopCount(byte[] bytes)
    {
        if (bytes.length > 26)
        {
            int value = 0;
            for (int i = 25, j = 0; i <= 26; value |= ((bytes[i] & 0xFF) << (j * 8)), i++, j++) ;
            System.out.println("电池循环次数：" + value + "次");
        }
    }

    /**
     * 解析电芯电压1-7节
     *
     * @param bytes
     */
    private void parseBatteryVoltage_1_7(byte[] bytes)
    {
        stringBuilder.setLength(0);
        if (bytes.length >= 33)
        {
            for (int i = 27; i < 33; stringBuilder.append(bytes[i] & 0xFF).append("-"), i++) ;
            stringBuilder.append(bytes[stringBuilder.length()] & 0xFF);
            System.out.println("电芯电压1-7节:" + stringBuilder.toString());
        }
    }

    /**
     * 解析电芯电压8-15节
     *
     * @param bytes
     */
    private void parseBatteryVoltage_8_15(byte[] bytes)
    {
        stringBuilder.setLength(0);
        if (bytes.length >= 47)
        {
            for (int i = 41; i < 47; stringBuilder.append(bytes[i] & 0xFF).append("-"), i++) ;
            stringBuilder.append(bytes[stringBuilder.length()] & 0xFF);
            System.out.println("电芯电压8-15节:" + stringBuilder.toString());
        }
    }

    /**
     * 解析电池健康百分比
     *
     * @param bytes
     */
    private void parseSOH(byte[] bytes)
    {
        if (bytes.length > 100)
        {
            int value = 0;
            for (int i = 99, j = 0; i <= 100; value |= ((bytes[i] & 0xFF) << (j * 8)), i++, j++) ;
            System.out.println("电池健康百分比：" + value + "%");
        }
    }


    /**
     * 解析电池ID信息
     *
     * @param bytes
     */
    private void parseBatteryIdInfo(byte[] bytes)
    {
        stringBuilder.setLength(0);
        if (bytes.length > 57 + _16bytes.length)
        {
            System.arraycopy(bytes, 57, _16bytes, 0, _16bytes.length);
            for (int i = 0, len = _16bytes.length; i < len; stringBuilder.append((char) (_16bytes[i] & 0xFF)), i++) ;
            System.out.println("电池ID信息:" + stringBuilder.toString());
        }
    }

    /**
     * 解析版本
     *
     * @param bytes
     */
    private void parseVersion(byte[] bytes)
    {
        if (bytes.length > 78)
        {
            int v1 = bytes[77] & 0xFF;
            int v2 = bytes[78] & 0xFF;

            System.out.println("软件版本:" + v1);
            System.out.println("硬件版本：" + v2);
        }
    }

    /**
     * 解析控制板温度
     *
     * @param bytes
     */
    private void parseControlPanelTemperature(byte[] bytes)
    {
        if (bytes.length > 97)
        {
            int value = bytes[97] & 0xFF;
            System.out.println("控制板温度：" + value + "度");
        }
    }

    /**
     * 解析控制板版本
     *
     * @param bytes
     */
    private void parseControlPanelVersion(byte[] bytes)
    {
        if (bytes.length > 55)
        {
            int value = bytes[55];
            System.out.println("控制板版本：" + value);
        }
    }

    private void test1(byte[] bytes)
    {
        if (bytes.length > 104)
        {
            int value = 0;
            for (int i = 101, j = 0; i <= 104; value |= ((bytes[i] & 0xFF) << (j * 8)), i++, j++) ;
            value -= 2731;
            float temperature = (float) value / 10;
            System.out.println("温度1：" + temperature + "度");
        }
    }

    private void test2(byte[] bytes)
    {
        if (bytes.length > 108)
        {
            int value = 0;
            for (int i = 105, j = 0; i <= 108; value |= ((bytes[i] & 0xFF) << (j * 8)), i++, j++) ;
            value -= 2731;
            float temperature = (float) value / 10;
            System.out.println("电温度2：" + temperature + "度");
        }
    }
}
