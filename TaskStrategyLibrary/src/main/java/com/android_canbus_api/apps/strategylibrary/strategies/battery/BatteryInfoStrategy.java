package com.android_canbus_api.apps.strategylibrary.strategies.battery;

import com.android_canbus_api.apps.controllerlibrary.DeviceIoAction;
import com.android_canbus_api.apps.strategylibrary.strategies._base.BaseStrategy;
import com.android_canbus_api.apps.utillibrary.StringFormatHelper;

import java.io.IOException;

/**
 * Author:      Lee Yeung
 * Create Date: 2019-08-28
 * Description: 电池信息策略
 */
public final class BatteryInfoStrategy extends BaseStrategy
{
    // TODO: 2019-09-16 数据包指令，必须进行CRC校验
    private final byte[] packageData = {0x00, 0x02, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

    private final StringBuilder stringBuilder = new StringBuilder();
    byte[] _16bytes = new byte[16];

    private BatteryInfo batteryInfo = new BatteryInfo();
    private OnBatteryInfoUpdate onBatteryInfoUpdate;

    public BatteryInfoStrategy(byte address)
    {
        super(address);
        packageData[0] = address;
    }

    public void setOnBatteryInfoUpdate(OnBatteryInfoUpdate onBatteryInfoUpdate)
    {
        batteryInfo = onBatteryInfoUpdate != null ? new BatteryInfo() : null;
        this.onBatteryInfoUpdate = onBatteryInfoUpdate;
    }

    @Override
    public void execute(DeviceIoAction deviceIoAction)
    {
        if (deviceIoAction == null)
        {
            return;
        }

        short crc16 = crc16(packageData, 0, 6);
        packageData[packageData.length - 2] = (byte) (crc16 & 0xFF);
        packageData[packageData.length - 1] = (byte) (crc16 >> 8 & 0xFF);

        byte[] result = null;

        try
        {
            deviceIoAction.write(packageData);
            sleep(3000);
            result = deviceIoAction.read();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        System.out.println("综合数据读取：" + StringFormatHelper.getInstance().toHexString(result));

        // TODO: 2019-09-17 判断数据长度大于长度值位下标，然后通过判断长度值和真个byte[]对比是否一致 (长度值是整个数据包的长度)
        if (result != null && result.length > 3)
        {
            final int len = result.length;
            if ((result[2] & 0xFF) == len)
            {
                final short crc = crc16(result, 0, result.length - 2);
                // TODO: 2019-09-17 检验CRC
                if ((crc & 0xFF) == (result[result.length - 2] & 0xFF) && (crc >> 8) == result[result.length - 1])
                {
                    parse(result);
                    if (onBatteryInfoUpdate != null)
                    {
                        onBatteryInfoUpdate.onUpdate(batteryInfo);
                    }
                }
            }
        }
        sleep(20000);
    }

    /**
     * 解析说明见：
     * 安卓板和下位机通讯协议修改版V09_增加BMS转发_温度.docx
     *
     * @param bytes
     */
    private void parse(byte[] bytes)
    {
        if (batteryInfo != null)
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
            test1(bytes);
            test2(bytes);
        }
    }

    /**
     * 解析门锁状态
     *
     * @param bytes
     */
    private void parseDoorLockStatus(byte[] bytes)
    {
        byte doorLockStatus = bytes[3];
        System.out.println("门锁状态：" + (doorLockStatus == 0 ? "关闭" : "打开"));
    }

    /**
     * 解析电机状态
     *
     * @param bytes
     */
    private void parseElectricMachineStatus(byte[] bytes)
    {
        byte electricMachineStatus = bytes[4];
        System.out.println("电机状态：" + (electricMachineStatus == 0 ? "关闭" : "打开"));
    }

    /**
     * 解析电池温度
     *
     * @param bytes
     */
    private void parseBatteryTemperature(byte[] bytes)
    {
        int value = 0;
        for (int i = 5, j = 0; i <= 8; value |= ((bytes[i] & 0xFF) << (j * 8)), i++, j++) ;
        value -= 2731;
        float temperature = (float) value / 10;
        System.out.println("温度：" + temperature + "度");
    }

    /**
     * 解析电压
     *
     * @param bytes
     */
    private void parseBatteryTotalVoltage(byte[] bytes)
    {
        int value = 0;
        for (int i = 9, j = 0; i <= 12; value |= ((bytes[i] & 0xFF) << (j * 8)), i++, j++) ;
        System.out.println("电压：" + value + "mV");

    }

    /**
     * 解析电流
     *
     * @param bytes
     */
    private void parseRealTimeCurrent(byte[] bytes)
    {
        int value = 0;
        for (int i = 13, j = 0; i <= 16; value |= ((bytes[i] & 0xFF) << (j * 8)), i++, j++) ;
        if (value >= 0x4e20)
        {
            value = (~value + 1);
        }
        System.out.println("电流：" + value + "mA");
    }

    /**
     * 解析电池相对容量百分比
     *
     * @param bytes
     */
    private void parseRelativeCapatityPercent(byte[] bytes)
    {
        int value = 0;
        for (int i = 17, j = 0; i <= 18; value |= ((bytes[i] & 0xFF) << (j * 8)), i++, j++) ;
        System.out.println("电池相对容量百分比：" + value + "%");
    }

    /**
     * 电池绝对容量百分比
     *
     * @param bytes
     */
    private void parseAbsoluteCapatityPercent(byte[] bytes)
    {
        int value = 0;
        for (int i = 19, j = 0; i <= 20; value |= ((bytes[i] & 0xFF) << (j * 8)), i++, j++) ;
        System.out.println("电池绝对容量百分比：" + value + "%");
    }

    /**
     * 解析电池剩余容量
     *
     * @param bytes
     */
    private void parseRemainingCapatity(byte[] bytes)
    {
        int value = 0;
        for (int i = 21, j = 0; i <= 22; value |= ((bytes[i] & 0xFF) << (j * 8)), i++, j++) ;
        System.out.println("电池剩余容量：" + value + "mAh");
    }

    /**
     * 解析电池满充容量
     *
     * @param bytes
     */
    private void parseFullCapatity(byte[] bytes)
    {
        int value = 0;
        for (int i = 23, j = 0; i <= 24; value |= ((bytes[i] & 0xFF) << (j * 8)), i++, j++) ;
        System.out.println("电池满充容量：" + value + "mAh");
    }

    /**
     * 解析电池循环次数
     *
     * @param bytes
     */
    private void parseLoopCount(byte[] bytes)
    {
        int value = 0;
        for (int i = 25, j = 0; i <= 26; value |= ((bytes[i] & 0xFF) << (j * 8)), i++, j++) ;
        System.out.println("电池循环次数：" + value + "次");
    }

    /**
     * 解析电芯电压1-7节
     *
     * @param bytes
     */
    private void parseBatteryVoltage_1_7(byte[] bytes)
    {
        stringBuilder.setLength(0);
        for (int i = 27; i < 33; stringBuilder.append(bytes[i] & 0xFF).append("-"), i++) ;
        stringBuilder.append(bytes[stringBuilder.length()] & 0xFF);
        System.out.println("电芯电压1-7节:" + stringBuilder.toString());
    }

    /**
     * 解析电芯电压8-15节
     *
     * @param bytes
     */
    private void parseBatteryVoltage_8_15(byte[] bytes)
    {
        stringBuilder.setLength(0);
        for (int i = 41; i < 47; stringBuilder.append(bytes[i] & 0xFF).append("-"), i++) ;
        stringBuilder.append(bytes[stringBuilder.length()] & 0xFF);
        System.out.println("电芯电压8-15节:" + stringBuilder.toString());
    }

    /**
     * 解析电池健康百分比
     *
     * @param bytes
     */
    private void parseSOH(byte[] bytes)
    {
        int value = 0;
        for (int i = 99, j = 0; i <= 100; value |= ((bytes[i] & 0xFF) << (j * 8)), i++, j++) ;
        System.out.println("电池健康百分比：" + value + "%");
    }


    /**
     * 解析电池ID信息
     *
     * @param bytes
     */
    private void parseBatteryIdInfo(byte[] bytes)
    {
        stringBuilder.setLength(0);
        System.arraycopy(bytes, 57, _16bytes, 0, _16bytes.length);
        for (int i = 0, len = _16bytes.length; i < len; stringBuilder.append((char) (_16bytes[i] & 0xFF)), i++) ;
        System.out.println("电池ID信息:" + stringBuilder.toString());



    }

    /**
     * 解析版本
     *
     * @param bytes
     */
    private void parseVersion(byte[] bytes)
    {
        int v1 = bytes[77] & 0xFF;
        int v2 = bytes[78] & 0xFF;

        System.out.println("软件版本:" + v1);
        System.out.println("硬件版本：" + v2);
    }

    /**
     * 解析控制板温度
     *
     * @param bytes
     */
    private void parseControlPanelTemperature(byte[] bytes)
    {
        int value = bytes[97] & 0xFF;
        System.out.println("控制板温度：" + value + "度");
    }

    /**
     * 解析控制板版本
     *
     * @param bytes
     */
    private void parseControlPanelVersion(byte[] bytes)
    {
        int value = bytes[55];
        System.out.println("控制板版本：" + value);
    }

    private void test1(byte[] bytes)
    {
        int value = 0;
        for (int i = 101, j = 0; i <= 104; value |= ((bytes[i] & 0xFF) << (j * 8)), i++, j++) ;
        value -= 2731;
        float temperature = (float) value / 10;
        System.out.println("温度1：" + temperature + "度");
    }

    private void test2(byte[] bytes)
    {
        int value = 0;
        for (int i = 105, j = 0; i <= 108; value |= ((bytes[i] & 0xFF) << (j * 8)), i++, j++) ;
        value -= 2731;
        float temperature = (float) value / 10;
        System.out.println("电温度2：" + temperature + "度");
    }
}
