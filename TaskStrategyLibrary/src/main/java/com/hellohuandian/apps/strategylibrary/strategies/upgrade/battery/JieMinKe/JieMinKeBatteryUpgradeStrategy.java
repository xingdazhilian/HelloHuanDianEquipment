package com.hellohuandian.apps.strategylibrary.strategies.upgrade.battery.JieMinKe;

import android.text.TextUtils;

import com.hellohuandian.apps.controllerlibrary.DeviceIoAction;
import com.hellohuandian.apps.strategylibrary.strategies.upgrade.battery.BatteryUpgradeStatus;
import com.hellohuandian.apps.strategylibrary.strategies.upgrade.battery.BatteryUpgradeStrategy;
import com.hellohuandian.apps.strategylibrary.strategies.upgrade.battery.OnUpgradeProgress;
import com.hellohuandian.apps.utillibrary.StringFormatHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Author:      Lee Yeung
 * Create Date: 2019-09-04
 * Description: 捷敏科电池升级程序,该代码程序升级文件校验！！！
 */
public class JieMinKeBatteryUpgradeStrategy extends BatteryUpgradeStrategy
{
    //需要进入485转发，下标0是对应的控制板地址，需要设置电池对应的控制板地址
    private byte[] _485 = new byte[]{address, 0x05, 0x00, 0x0B, 0x00, 0x01, 0x00, 0x00};

    //电池信息指令
    private byte[] BMS_cmd = new byte[]{0x3A, 0x16, (byte) 0xF0, 0x02, (byte) 0xF2, 0x00, 0x00, 0x00, 0x0D, 0x0A};

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

    public JieMinKeBatteryUpgradeStrategy(byte address, String filePath)
    {
        super(address, filePath);
    }

    @Override
    public void upgrade(DeviceIoAction deviceIoAction, OnUpgradeProgress onUpgradeProgress)
    {
        if (deviceIoAction == null)
        {
            return;
        }
        if (TextUtils.isEmpty(filePath))
        {
            onUpgradeProgress.onUpgrade(address, BatteryUpgradeStatus.FAILED, "升级文件路径为空", 0, 0);
            return;
        }
        File file = new File(filePath);
        System.out.println(file.getAbsolutePath());
        if (!(file != null && file.exists()))
        {
            onUpgradeProgress.onUpgrade(address, BatteryUpgradeStatus.FAILED, "升级文件不存在!", 0, 0);
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
            onUpgradeProgress.onUpgrade(address, BatteryUpgradeStatus.FAILED, "升级文件没有找到!\n" + e.getLocalizedMessage(), 0, 0);
        }

        if (inputStream == null)
        {
            onUpgradeProgress.onUpgrade(address, BatteryUpgradeStatus.FAILED, "升级文件流为null!", 0, 0);
            return;
        }

        short snTemp = 0;
        int totalFrameSizeTemp = 0;

        short crc = crc16(_485, 0, 6);
        _485[_485.length - 2] = (byte) (crc & 0xFF);
        _485[_485.length - 1] = (byte) (crc >> 8 & 0xFF);

        try
        {
            byte[] result = null;

            // TODO: 2019-09-05 激活485转发
            sleep(10 * 1000);
            System.out.println("》" + StringFormatHelper.getInstance().toHexString(_485));
            deviceIoAction.write(_485);
            // TODO: 2019-09-05 读取一次串口数据
            sleep(200);
            result = deviceIoAction.read();
            System.out.println("《" + StringFormatHelper.getInstance().toHexString(result));
            if (result != null && result.length > 0)
            {
                onUpgradeProgress.onUpgrade(address, BatteryUpgradeStatus.WAITTING, "激活485转发成功!", 0, 0);
            } else
            {
                onUpgradeProgress.onUpgrade(address, BatteryUpgradeStatus.FAILED, "激活485转发失败!", 0, 0);
                return;
            }

            int sum;

            // TODO: 2019-09-05 进入BootLoader模式
            sum = calculateSum(bootLoaderMode, 1, 16);
            bootLoaderMode[17] = (byte) (sum & 0xFF);
            bootLoaderMode[18] = (byte) (sum >> 8 & 0xFF);
            final long startTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - startTime < 30 * 1000)//超过30S失败
            {
                deviceIoAction.write(bootLoaderMode);

                sleep(500);
                result = deviceIoAction.read();
                if (result != null && result.length > 0)
                {
                    break;
                }
            }
            if (result != null && result.length > 0)
            {
                onUpgradeProgress.onUpgrade(address, BatteryUpgradeStatus.BOOT_LOADER_MODE, "进入BootLoader模式成功!", 0, 0);
            } else
            {
                onUpgradeProgress.onUpgrade(address, BatteryUpgradeStatus.FAILED, "进入BootLoader模式失败!", 0, 0);
                return;
            }

            // TODO: 2019-09-17 判断电池是否符合捷敏科电池升级要求
            sum = calculateSum(BMS_cmd, 1, 5);
            BMS_cmd[6] = (byte) (sum & 0xFF);
            BMS_cmd[7] = (byte) (sum >> 8 & 0xFF);
            System.out.println("》" + StringFormatHelper.getInstance().toHexString(BMS_cmd));
            deviceIoAction.write(BMS_cmd);
            sleep(200);
            result = deviceIoAction.read();
            System.out.println("《" + StringFormatHelper.getInstance().toHexString(result));
            if (result != null && result.length > 30)
            {
                if ((result[27] & 0xFF) != 0)
                {
                    onUpgradeProgress.onUpgrade(address, BatteryUpgradeStatus.FAILED, "BMS厂商不匹配!", 0, 0);
                    return;
                }
                if ((result[28] & 0xFF) != 0)
                {
                    onUpgradeProgress.onUpgrade(address, BatteryUpgradeStatus.FAILED, "电池厂商不匹配!", 0, 0);
                    return;
                }
                if ((result[29] & 0xFF) != 0)
                {
                    onUpgradeProgress.onUpgrade(address, BatteryUpgradeStatus.FAILED, "电芯类型不匹配!", 0, 0);
                    return;
                }
                if ((result[30] & 0xFF) != 0)
                {
                    onUpgradeProgress.onUpgrade(address, BatteryUpgradeStatus.FAILED, "电池型号不匹配!", 0, 0);
                    return;
                }
            } else
            {
                onUpgradeProgress.onUpgrade(address, BatteryUpgradeStatus.FAILED, "电池信息无效!", 0, 0);
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
                final int binDataCrc = crc32(binData);
                firmwareInfo[12] = (byte) (binDataCrc & 0xFF);
                firmwareInfo[13] = (byte) (binDataCrc >> 8 & 0xFF);
                firmwareInfo[14] = (byte) (binDataCrc >> 16 & 0xFF);
                firmwareInfo[15] = (byte) (binDataCrc >> 24 & 0xFF);

                sum = calculateSum(firmwareInfo, 1, 20);
                firmwareInfo[21] = (byte) (sum & 0xFF);
                firmwareInfo[22] = (byte) (sum >> 8 & 0xFF);
                deviceIoAction.write(firmwareInfo);
                sleep(1000);
                result = deviceIoAction.read();
                if (result != null && result.length > 0)
                {
                    onUpgradeProgress.onUpgrade(address, BatteryUpgradeStatus.INIT_FIRMWARE_DATA, "新固件信息发送成功!", 0, totalFrameSize);
                } else
                {
                    onUpgradeProgress.onUpgrade(address, BatteryUpgradeStatus.FAILED, "新固件信息发送失败!", 0, totalFrameSize);
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
                    if (result != null && result.length > 0)
                    {
                        onUpgradeProgress.onUpgrade(address, BatteryUpgradeStatus.WRITE_DATA, "发送" + sn + "条成功", sn, totalFrameSize);
                    } else
                    {
                        onUpgradeProgress.onUpgrade(address, BatteryUpgradeStatus.FAILED, "发送" + sn + "条失败", sn, totalFrameSize);
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

                    deviceIoAction.write(lastData);
                    sleep(5000);
                    result = deviceIoAction.read();
                    if (result != null && result.length > 0)
                    {
                        onUpgradeProgress.onUpgrade(address, BatteryUpgradeStatus.WRITE_DATA, "发送" + sn + "条成功", sn, totalFrameSize);
                    } else
                    {
                        onUpgradeProgress.onUpgrade(address, BatteryUpgradeStatus.FAILED, "发送" + sn + "条失败", sn, totalFrameSize);
                        return;
                    }
                }


                sum = calculateSum(activationBMS, 1, 5);
                activationBMS[6] = (byte) (sum & 0xFF);
                activationBMS[7] = (byte) (sum >> 8 & 0xFF);
                onUpgradeProgress.onUpgrade(address, BatteryUpgradeStatus.ACTION_BMS, "开始激活...", totalFrameSize, totalFrameSize);
                deviceIoAction.write(activationBMS);
                sleep(100);
                result = deviceIoAction.read();
                if (result != null && result.length > 0)
                {
                    onUpgradeProgress.onUpgrade(address, BatteryUpgradeStatus.SUCCESSED, "激活成功", totalFrameSize, totalFrameSize);
                } else
                {
                    // TODO: 2019-09-20 超时10S电池自动使用新程序
                    sleep(10 * 1000);
                    onUpgradeProgress.onUpgrade(address, BatteryUpgradeStatus.SUCCESSED, "激活成功", sn, totalFrameSize);
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
            onUpgradeProgress.onUpgrade(address, BatteryUpgradeStatus.FAILED, "升级IO异常\n" + e.getLocalizedMessage(), snTemp, totalFrameSizeTemp);
        }
    }
}