package com.app10000.apps.strategylibrary.strategies.upgrade.battery.BoQiang;

import android.text.TextUtils;

import com.app10000.apps.controllerlibrary.DeviceIoAction;
import com.app10000.apps.strategylibrary.strategies.battery.OnBatteryDataUpdate;
import com.app10000.apps.strategylibrary.strategies.upgrade.battery.BatteryUpgradeInfo;
import com.app10000.apps.strategylibrary.strategies.upgrade.battery.BatteryUpgradeStrategy;
import com.app10000.apps.strategylibrary.strategies.upgrade.battery.BatteryUpgradeStrategyStatus;
import com.app10000.apps.utillibrary.StringFormatHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Author:      Lee Yeung
 * Create Date: 2019-09-04
 * Description: 博强电池升级程序,该代码程序升级文件校验！！！
 */
public class BoQiangBatteryUpgradeStrategy extends BatteryUpgradeStrategy
{
    //需要进入485转发，下标0是对应的控制板地址，需要设置电池对应的控制板地址
    private byte[] _485 = new byte[]{address, 0x05, 0x00, 0x0B, 0x00, 0x01, 0x00, 0x00};

    //电池ID码
    static final byte[] BATTERY_ID_CODE = {0x3A, 0x16, (byte) 0x7E, 0x01, 0x00, 0x00, 0x00, 0x0D, 0x0A};

    //BMS版本信息
    static final byte[] VERSION = {0x3A, 0x16, 0x7F, 0x01, 0x00, 0x00, 0x00, 0x0D, 0x0A};

    //进入bootLoader模式
    private byte[] bootLoaderMode = new byte[]{0x3A, 0x16, (byte) 0xF0, 0x0D,//长度信息是子命令和数据内容的长度
            (byte) 0xF1, 0x4A, 0x4D, 0x4B, 0x2D, 0x42, 0x4D, 0x53, 0x2D, 0x42, 0x4C, 0x30, 0x30, 0x00, 0x00, 0x0D, 0x0A};

    //发送新固件信息(0xF6)
    private byte[] firmwareInfo = new byte[]{0x3A, 0x16, (byte) 0xF0, 0x11, (byte) 0xF6, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
            , 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x0D, 0x0A};

    //发送新固件数据(0xF7)
    private byte[] firmwareData = new byte[]{0x3A, 0x16, (byte) 0xF0, (byte) 0x83, (byte) 0xF7, 0x00, 0x00,//1~2当前固件数据帧号。范围:0~(总帧数-1)。
            //固件数据每帧最长数据为 128 字节。
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x0D, 0x0A};

    //立即激活新程序，不激活的话，在所有数据帧写完之10S后电池自动激活
    private byte[] activationBMS = new byte[]{0x3A, 0x16, (byte) 0xF0, 0x02, (byte) 0xF4, 0x00, 0x00, 0x00, 0x0D, 0x0A};

    private final BatteryUpgradeInfo batteryUpgradeInfo = new BatteryUpgradeInfo(address);

    public BoQiangBatteryUpgradeStrategy(byte address, String filePath)
    {
        super(address, filePath);
    }

    private OnBatteryDataUpdate mapOnBatteryDataUpdateImpl(final OnBatteryDataUpdate onBatteryDataUpdate)
    {
        return new OnBatteryDataUpdate<BatteryUpgradeInfo>()
        {
            private final OnBatteryDataUpdate innerOnBatteryDataUpdate = onBatteryDataUpdate;

            @Override
            public void onUpdate(BatteryUpgradeInfo batteryUpgradeInfo)
            {
                if (innerOnBatteryDataUpdate != null)
                {
                    innerOnBatteryDataUpdate.onUpdate(batteryUpgradeInfo);
                    System.out.println(batteryUpgradeInfo.statusInfo);
                    if (batteryUpgradeInfo.statusFlag == BatteryUpgradeStrategyStatus.FAILED)
                    {
                        sleep(2000);
                    }
                }
            }
        };
    }

    @Override
    public void upgrade(DeviceIoAction deviceIoAction, OnBatteryDataUpdate onBatteryDataUpdate)
    {
        if (deviceIoAction == null || onBatteryDataUpdate == null)
        {
            sleep(10 * 1000);
            return;
        }
        onBatteryDataUpdate = mapOnBatteryDataUpdateImpl(onBatteryDataUpdate);

        short snTemp = 0;
        int totalFrameSizeTemp = 0;
        int sum;

        short crc = crc16(_485, 0, 6);
        _485[_485.length - 2] = (byte) (crc & 0xFF);
        _485[_485.length - 1] = (byte) (crc >> 8 & 0xFF);

        try
        {
            byte[] result = null;
            sleep(5000);
            // TODO: 2019-09-05 激活485转发
            deviceIoAction.read();
            System.out.println("激活485转发" + StringFormatHelper.getInstance().toHexString(_485));
            deviceIoAction.write(_485);
            // TODO: 2019-09-05 读取一次串口数据
            sleep(1000);
            result = deviceIoAction.read();
            if (result != null && result.length > 0)
            {
                batteryUpgradeInfo.statusFlag = BatteryUpgradeStrategyStatus.WAITTING;
                batteryUpgradeInfo.statusInfo = "激活485转发成功:" + StringFormatHelper.getInstance().toHexString(result);
                onBatteryDataUpdate.onUpdate(batteryUpgradeInfo);
            } else
            {
                batteryUpgradeInfo.statusFlag = BatteryUpgradeStrategyStatus.FAILED;
                batteryUpgradeInfo.statusInfo = "激活485转发失败:" + StringFormatHelper.getInstance().toHexString(result);
                onBatteryDataUpdate.onUpdate(batteryUpgradeInfo);
                sleep(10 * 1000);
                return;
            }

            if (TextUtils.isEmpty(filePath))
            {
                batteryUpgradeInfo.statusFlag = BatteryUpgradeStrategyStatus.FAILED;
                batteryUpgradeInfo.statusInfo = "升级文件路径为空";
                onBatteryDataUpdate.onUpdate(batteryUpgradeInfo);
                sleep(10 * 1000);
                return;
            }
            File file = new File(filePath);
            if (!(file != null && file.exists()))
            {
                batteryUpgradeInfo.statusFlag = BatteryUpgradeStrategyStatus.FAILED;
                batteryUpgradeInfo.statusInfo = "升级文件不存在:" + file.getAbsolutePath();
                onBatteryDataUpdate.onUpdate(batteryUpgradeInfo);
                sleep(10 * 1000);
                return;
            }

            InputStream inputStream = null;
            try
            {
                inputStream = new FileInputStream(file);
            }
            catch (FileNotFoundException e)
            {
                e.printStackTrace();
                batteryUpgradeInfo.statusFlag = BatteryUpgradeStrategyStatus.FAILED;
                batteryUpgradeInfo.statusInfo = "升级文件没有找到!\n" + e.getLocalizedMessage();
                onBatteryDataUpdate.onUpdate(batteryUpgradeInfo);
                sleep(10 * 1000);
                return;
            }

            if (inputStream == null)
            {
                batteryUpgradeInfo.statusFlag = BatteryUpgradeStrategyStatus.FAILED;
                batteryUpgradeInfo.statusInfo = "升级文件流为null!";
                onBatteryDataUpdate.onUpdate(batteryUpgradeInfo);
                sleep(10 * 1000);
                return;
            }

            if (TextUtils.isEmpty(idCode) || idCode.length() < 2)
            {
                batteryUpgradeInfo.statusFlag = BatteryUpgradeStrategyStatus.FAILED;
                batteryUpgradeInfo.statusInfo = "传入的ID码为空或者长度小于2!";
                onBatteryDataUpdate.onUpdate(batteryUpgradeInfo);
                sleep(10 * 1000);
                return;
            }

            sum = calculateSum(BATTERY_ID_CODE, 1, 4);
            BATTERY_ID_CODE[5] = (byte) (sum & 0xFF);
            BATTERY_ID_CODE[6] = (byte) (sum >> 8 & 0xFF);
            System.out.println("请求ID码：" + StringFormatHelper.getInstance().toHexString(BATTERY_ID_CODE));
            deviceIoAction.write(BATTERY_ID_CODE);
            sleep(200);
            result = deviceIoAction.read();
            if (result != null && result.length >= 20)
            {
                StringBuilder stringBuilder = new StringBuilder();
                for (int i = 4, len = result.length - 4; i < len; stringBuilder.append((char) (result[i] & 0xFF)), i++)
                    ;
                final String resultIdCode = stringBuilder.toString();
                System.out.println("ID码：" + resultIdCode);
                if (!(idCode.charAt(0) == resultIdCode.charAt(0) && idCode.charAt(1) == resultIdCode.charAt(1)))
                {
                    batteryUpgradeInfo.statusFlag = BatteryUpgradeStrategyStatus.FAILED;
                    batteryUpgradeInfo.statusInfo = "电池类型和升级包不匹配!";
                    onBatteryDataUpdate.onUpdate(batteryUpgradeInfo);
                    sleep(10 * 1000);
                    return;
                }
            } else
            {
                batteryUpgradeInfo.statusFlag = BatteryUpgradeStrategyStatus.FAILED;
                batteryUpgradeInfo.statusInfo = "电池ID码错误!" + StringFormatHelper.getInstance().toHexString(result);
                onBatteryDataUpdate.onUpdate(batteryUpgradeInfo);
                sleep(10 * 1000);
                return;
            }

            if (TextUtils.isEmpty(bmsHardwareVersion))
            {
                batteryUpgradeInfo.statusFlag = BatteryUpgradeStrategyStatus.FAILED;
                batteryUpgradeInfo.statusInfo = "BMS硬件版本为空!";
                onBatteryDataUpdate.onUpdate(batteryUpgradeInfo);
                sleep(10 * 1000);
                return;
            } else
            {
                sum = calculateSum(VERSION, 1, 4);
                VERSION[5] = (byte) (sum & 0xFF);
                VERSION[6] = (byte) (sum >> 8 & 0xFF);
                System.out.println("请求版本：" + StringFormatHelper.getInstance().toHexString(VERSION));
                deviceIoAction.write(VERSION);
                sleep(200);
                result = deviceIoAction.read();
                if (result.length >= 6)
                {
                    final String hv = Byte.toString((byte) (result[6] & 0xFF));
                    System.out.println("BMS硬件版本：" + hv);
                    if (!hv.equals(bmsHardwareVersion))
                    {
                        batteryUpgradeInfo.statusFlag = BatteryUpgradeStrategyStatus.FAILED;
                        batteryUpgradeInfo.statusInfo = "BMS硬件版本不匹配!" + StringFormatHelper.getInstance().toHexString(result);
                        onBatteryDataUpdate.onUpdate(batteryUpgradeInfo);
                        sleep(10 * 1000);
                        return;
                    }
                } else
                {
                    batteryUpgradeInfo.statusFlag = BatteryUpgradeStrategyStatus.FAILED;
                    batteryUpgradeInfo.statusInfo = "BMS硬件版本错误!" + StringFormatHelper.getInstance().toHexString(result);
                    onBatteryDataUpdate.onUpdate(batteryUpgradeInfo);
                    sleep(10 * 1000);
                    return;
                }
            }

            // TODO: 2019-09-08 比较CRC校验码
            if (TextUtils.isEmpty(crcValue))
            {
                batteryUpgradeInfo.statusFlag = BatteryUpgradeStrategyStatus.FAILED;
                batteryUpgradeInfo.statusInfo = "文件包CRC为空!" + StringFormatHelper.getInstance().toHexString(result);
                onBatteryDataUpdate.onUpdate(batteryUpgradeInfo);
                sleep(10 * 1000);
                return;
            }

            // TODO: 2019-09-05 进入BootLoader模式
            sum = calculateSum(bootLoaderMode, 1, 16);
            bootLoaderMode[17] = (byte) (sum & 0xFF);
            bootLoaderMode[18] = (byte) (sum >> 8 & 0xFF);
            final long startTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - startTime < 30 * 1000)//超过30S失败
            {
                deviceIoAction.write(bootLoaderMode);
                sleep(100);
                result = deviceIoAction.read();
                System.out.println("博强BootLoader返回：" + StringFormatHelper.getInstance().toHexString(result));
                if (result != null && result.length > 6 && result[4] == (byte) 0xF1 && result[5] == 0x00)
                {
                    break;
                }
            }

            if (result != null && result.length > 6 && result[4] == (byte) 0xF1 && result[5] == 0x00)
            {
                batteryUpgradeInfo.statusFlag = BatteryUpgradeStrategyStatus.BOOT_LOADER_MODE;
                batteryUpgradeInfo.statusInfo = "进入BootLoader模式成功:" + StringFormatHelper.getInstance().toHexString(result);
                onBatteryDataUpdate.onUpdate(batteryUpgradeInfo);
            } else
            {
                batteryUpgradeInfo.statusFlag = BatteryUpgradeStrategyStatus.FAILED;
                batteryUpgradeInfo.statusInfo = "进入BootLoader模式失败:" + StringFormatHelper.getInstance().toHexString(result);
                onBatteryDataUpdate.onUpdate(batteryUpgradeInfo);
                sleep(10 * 1000);
                return;
            }

            // TODO: 2019-09-05 初始化固件数据
            int len = 0;
            try
            {
                len = inputStream.available();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            byte[] binData = null;
            if (len > 0)
            {
                binData = new byte[len];
                try
                {
                    inputStream.read(binData);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }

                final int binDataCrc = crc32(binData);
                if (hexToInt(crcValue) != binDataCrc)
                {
                    batteryUpgradeInfo.statusFlag = BatteryUpgradeStrategyStatus.FAILED;
                    batteryUpgradeInfo.statusInfo = "CRC不匹配!" + StringFormatHelper.getInstance().toHexString(result);
                    onBatteryDataUpdate.onUpdate(batteryUpgradeInfo);
                    sleep(10 * 1000);
                    return;
                }

                firmwareInfo[7] = (byte) (len & 0xFF);
                firmwareInfo[8] = (byte) (len >> 8 & 0xFF);
                firmwareInfo[9] = (byte) (len >> 16 & 0xFF);
                // TODO:总帧数,低字节在前，高字节在后。
                int totalFrameSize = len / 128;
                if (len % 128 > 0)
                {
                    totalFrameSize += 1;
                }
                totalFrameSizeTemp = totalFrameSize;

                firmwareInfo[10] = (byte) (totalFrameSize & 0xFF);
                firmwareInfo[11] = (byte) (totalFrameSize >> 8 & 0xFF);
                // TODO: 2019-09-02 CRC32 校验码,低字节在前，高字节在后。
                firmwareInfo[12] = (byte) (binDataCrc & 0xFF);
                firmwareInfo[13] = (byte) (binDataCrc >> 8 & 0xFF);
                firmwareInfo[14] = (byte) (binDataCrc >> 16 & 0xFF);
                firmwareInfo[15] = (byte) (binDataCrc >> 24 & 0xFF);

                sum = calculateSum(firmwareInfo, 1, 20);
                firmwareInfo[21] = (byte) (sum & 0xFF);
                firmwareInfo[22] = (byte) (sum >> 8 & 0xFF);
                System.out.println("新固件信息写入:" + StringFormatHelper.getInstance().toHexString(firmwareInfo));
                deviceIoAction.write(firmwareInfo);
                sleep(5000);
                result = deviceIoAction.read();
                batteryUpgradeInfo.totalPregress = totalFrameSize;
                if (result != null && result.length > 6 && result[4] == (byte) 0xF6 && result[5] == 0x00)
                {
                    batteryUpgradeInfo.statusFlag = BatteryUpgradeStrategyStatus.INIT_FIRMWARE_DATA;
                    batteryUpgradeInfo.statusInfo = "新固件信息发送成功:" + StringFormatHelper.getInstance().toHexString(result);
                    System.out.println(batteryUpgradeInfo.statusInfo);
                    onBatteryDataUpdate.onUpdate(batteryUpgradeInfo);
                } else
                {
                    batteryUpgradeInfo.statusFlag = BatteryUpgradeStrategyStatus.FAILED;
                    batteryUpgradeInfo.statusInfo = "新固件信息发送失败:" + StringFormatHelper.getInstance().toHexString(result);
                    System.out.println(batteryUpgradeInfo.statusInfo);
                    onBatteryDataUpdate.onUpdate(batteryUpgradeInfo);
                    sleep(10 * 1000);
                    return;
                }

                // TODO: 2019-09-02 开始循环写入数据帧
                int loopCount = 1;
                short sn = 0;
                int offset = 0;
                for (; loopCount < totalFrameSize; loopCount++, sn++, offset += 127)
                {
                    // TODO: 2019-09-02 帧号
                    firmwareData[5] = (byte) (sn & 0xFF);
                    firmwareData[6] = (byte) (sn >> 8 & 0xFF);
                    // TODO: 2019-09-02 数据帧
                    System.arraycopy(binData, offset, firmwareData, 7, 128);
                    sum = calculateSum(firmwareData, 1, 134);
                    firmwareData[135] = (byte) (sum & 0xFF);
                    firmwareData[136] = (byte) (sum >> 8 & 0xFF);

                    deviceIoAction.write(firmwareData);
                    offset++;
                    sleep(500);
                    result = deviceIoAction.read();
                    batteryUpgradeInfo.currentPregress = sn;
                    if (result != null && result.length > 6 && result[4] == (byte) 0xF7 && result[5] == 0x00)
                    {
                        batteryUpgradeInfo.statusFlag = BatteryUpgradeStrategyStatus.WRITE_DATA;
                        batteryUpgradeInfo.statusInfo = "发送" + sn + "条成功";
                        onBatteryDataUpdate.onUpdate(batteryUpgradeInfo);
                    } else
                    {
                        batteryUpgradeInfo.statusFlag = BatteryUpgradeStrategyStatus.FAILED;
                        batteryUpgradeInfo.statusInfo = "发送" + sn + "条失败! 错误数据：" + StringFormatHelper.getInstance().toHexString(result);
                        onBatteryDataUpdate.onUpdate(batteryUpgradeInfo);
                        sleep(10 * 1000);
                        return;
                    }
                }

                if (loopCount == totalFrameSize)
                {
                    // TODO: 2019-09-02 处理最后一帧数据
                    // TODO: 2019-09-02 帧号
                    firmwareData[5] = (byte) (sn & 0xFF);
                    firmwareData[6] = (byte) (sn >> 8 & 0xFF);

                    // TODO: 2019-09-03 最后一帧数据长度
                    final int lastLen = 7 + len - offset + 4;
                    byte[] lastData = new byte[lastLen];
                    System.arraycopy(firmwareData, 0, lastData, 0, 7);
                    System.arraycopy(binData, offset, lastData, 7, len - offset);
                    // TODO: 2019-09-03 填充长度
                    lastData[3] = (byte) ((3 + len - offset) & 0xFF);

                    int end = lastLen - 4 - 1;
                    sum = calculateSum(lastData, 1, end);
                    lastData[++end] = (byte) (sum & 0xFF);
                    lastData[++end] = (byte) (sum >> 8 & 0xFF);
                    lastData[++end] = 0x0D;
                    lastData[++end] = 0x0A;

                    System.out.println("最后一针写：" + StringFormatHelper.getInstance().toHexString(lastData));
                    deviceIoAction.write(lastData);
                    sleep(5000);
                    result = deviceIoAction.read();
                    System.out.println("最后一针读：" + StringFormatHelper.getInstance().toHexString(result));
                    if (result != null && result.length > 6 && result[4] == (byte) 0xF7 && result[5] == 0x00)
                    {
                        batteryUpgradeInfo.statusFlag = BatteryUpgradeStrategyStatus.WRITE_DATA;
                        batteryUpgradeInfo.statusInfo = "发送" + sn + "条成功";
                        onBatteryDataUpdate.onUpdate(batteryUpgradeInfo);
                    } else
                    {
                        batteryUpgradeInfo.statusFlag = BatteryUpgradeStrategyStatus.FAILED;
                        batteryUpgradeInfo.statusInfo = "发送" + sn + "条失败! 错误数据：" + StringFormatHelper.getInstance().toHexString(result);
                        onBatteryDataUpdate.onUpdate(batteryUpgradeInfo);
                        sleep(10 * 1000);
                        return;
                    }
                }

                sum = calculateSum(activationBMS, 1, 5);
                activationBMS[6] = (byte) (sum & 0xFF);
                activationBMS[7] = (byte) (sum >> 8 & 0xFF);

                System.out.println("激活写：" + StringFormatHelper.getInstance().toHexString(activationBMS));
                deviceIoAction.write(activationBMS);
                sleep(200);
                result = deviceIoAction.read();
                if (result != null && result.length > 6 && result[4] == (byte) 0xF4 && result[5] == 0x00)
                {
                    batteryUpgradeInfo.currentPregress = totalFrameSize;
                    batteryUpgradeInfo.statusFlag = BatteryUpgradeStrategyStatus.ACTION_BMS;
                    batteryUpgradeInfo.statusInfo = "正在激活...";
                    onBatteryDataUpdate.onUpdate(batteryUpgradeInfo);
                    sleep(500);
                    System.out.println("激活读：" + StringFormatHelper.getInstance().toHexString(result));
                    // TODO: 2019-09-20 超时10S电池自动使用新程序,同时推出485转发模式
                    batteryUpgradeInfo.statusInfo = "激活成功!";
                    batteryUpgradeInfo.statusFlag = BatteryUpgradeStrategyStatus.SUCCESSED;
                } else
                {
                    batteryUpgradeInfo.statusFlag = BatteryUpgradeStrategyStatus.FAILED;
                    batteryUpgradeInfo.statusInfo = "激活失败:" + StringFormatHelper.getInstance().toHexString(result);
                    onBatteryDataUpdate.onUpdate(batteryUpgradeInfo);
                    sleep(10 * 1000);
                    return;
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            batteryUpgradeInfo.statusFlag = BatteryUpgradeStrategyStatus.FAILED;
            batteryUpgradeInfo.statusInfo = "升级IO异常\n" + e.getLocalizedMessage();
            batteryUpgradeInfo.currentPregress = snTemp;
            batteryUpgradeInfo.totalPregress = totalFrameSizeTemp;
            onBatteryDataUpdate.onUpdate(batteryUpgradeInfo);
        }

        sleep(10 * 1000);
        onBatteryDataUpdate.onUpdate(batteryUpgradeInfo);
    }
}
