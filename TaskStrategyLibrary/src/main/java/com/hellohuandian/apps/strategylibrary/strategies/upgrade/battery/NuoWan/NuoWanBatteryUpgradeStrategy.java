package com.hellohuandian.apps.strategylibrary.strategies.upgrade.battery.NuoWan;

import android.text.TextUtils;

import com.hellohuandian.apps.controllerlibrary.DeviceIoAction;
import com.hellohuandian.apps.strategylibrary.dispatchers.canExtension.CanDeviceIoAction;
import com.hellohuandian.apps.strategylibrary.strategies.battery.OnBatteryDataUpdate;
import com.hellohuandian.apps.strategylibrary.strategies.upgrade.battery.BatteryUpgradeInfo;
import com.hellohuandian.apps.strategylibrary.strategies.upgrade.battery.BatteryUpgradeStrategy;
import com.hellohuandian.apps.strategylibrary.strategies.upgrade.battery.BatteryUpgradeStrategyStatus;
import com.hellohuandian.apps.utillibrary.StringFormatHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Author:      Lee Yeung
 * Create Date: 2019-09-04
 * Description:诺万电池升级程序,该代码程序不负责升级文件校验！！！
 */
public class NuoWanBatteryUpgradeStrategy extends BatteryUpgradeStrategy
{
    class Lock
    {
        public volatile boolean isContinue;
        public int dataLen;
        public int dataSize;
        public byte[] flagBytes;
        public byte cmdFlag;
        public final ConcurrentHashMap<Byte, Boolean> cmdFlagMap = new ConcurrentHashMap<>();
    }

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

    public NuoWanBatteryUpgradeStrategy(byte address, String filePath)
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
    public void upgrade(final DeviceIoAction deviceIoAction, final OnBatteryDataUpdate onBatteryDataUpdate)
    {
        if (deviceIoAction == null || onBatteryDataUpdate == null)
        {
            return;
        }
        upgrade_485(deviceIoAction, onBatteryDataUpdate);
    }

    public void upgrade_485(DeviceIoAction deviceIoAction, OnBatteryDataUpdate onBatteryDataUpdate)
    {
        if (deviceIoAction == null || onBatteryDataUpdate == null)
        {
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
            System.out.println("线程" + Thread.currentThread().getName());
            sleep(5000);
            deviceIoAction.read();
            // TODO: 2019-09-05 激活485转发
            System.out.println("激活485转发" + StringFormatHelper.getInstance().toHexString(_485));
            deviceIoAction.write(_485);
            // TODO: 2019-09-05 读取一次串口数据
            sleep(500);
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
                System.out.println("ID码十六进制：" + StringFormatHelper.getInstance().toHexString(result));
                if (!(!TextUtils.isEmpty(resultIdCode) && resultIdCode.startsWith(idCode)))
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
            System.out.println("请求BootLoader模式：" + StringFormatHelper.getInstance().toHexString(bootLoaderMode));
            final long startTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - startTime < 30 * 1000)//超过30S失败
            {
                deviceIoAction.write(bootLoaderMode);

                sleep(500);
                result = deviceIoAction.read();
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

                final int binDataCrc = crc32(binData);
                System.out.println("计算后的CRC：" + binDataCrc);
                if (hexToInt(crcValue) != binDataCrc)
                {
                    batteryUpgradeInfo.statusFlag = BatteryUpgradeStrategyStatus.FAILED;
                    batteryUpgradeInfo.statusInfo = "CRC不匹配!" + StringFormatHelper.getInstance().toHexString(result);
                    onBatteryDataUpdate.onUpdate(batteryUpgradeInfo);
                    sleep(10 * 1000);
                    return;
                }

                // TODO: 2019-09-02 CRC32 校验码,低字节在前，高字节在后。
                firmwareInfo[12] = (byte) (binDataCrc & 0xFF);
                firmwareInfo[13] = (byte) (binDataCrc >> 8 & 0xFF);
                firmwareInfo[14] = (byte) (binDataCrc >> 16 & 0xFF);
                firmwareInfo[15] = (byte) (binDataCrc >> 24 & 0xFF);

                sum = calculateSum(firmwareInfo, 1, 20);
                firmwareInfo[21] = (byte) (sum & 0xFF);
                firmwareInfo[22] = (byte) (sum >> 8 & 0xFF);
                System.out.println("新固件信息写入：" + StringFormatHelper.getInstance().toHexString(firmwareInfo));
                deviceIoAction.write(firmwareInfo);
                sleep(2000);// TODO: 2019-10-14 诺万厂商建议2S
                result = deviceIoAction.read();
                batteryUpgradeInfo.totalPregress = totalFrameSize;
                if (result != null && result.length > 6 && result[4] == (byte) 0xF6 && result[5] == 0x00)
                {
                    batteryUpgradeInfo.statusFlag = BatteryUpgradeStrategyStatus.INIT_FIRMWARE_DATA;
                    batteryUpgradeInfo.statusInfo = "新固件信息发送成功:" + StringFormatHelper.getInstance().toHexString(result);
                    onBatteryDataUpdate.onUpdate(batteryUpgradeInfo);
                } else
                {
                    batteryUpgradeInfo.statusFlag = BatteryUpgradeStrategyStatus.FAILED;
                    batteryUpgradeInfo.statusInfo = "新固件信息发送失败:" + StringFormatHelper.getInstance().toHexString(result);
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

                    deviceIoAction.write(lastData);
                    sleep(3000);
                    result = deviceIoAction.read();
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

                deviceIoAction.write(activationBMS);
                sleep(100);
                result = deviceIoAction.read();
                batteryUpgradeInfo.currentPregress = totalFrameSize;
                batteryUpgradeInfo.statusFlag = BatteryUpgradeStrategyStatus.ACTION_BMS;
                batteryUpgradeInfo.statusInfo = "正在激活...";
                onBatteryDataUpdate.onUpdate(batteryUpgradeInfo);
                System.out.println("激活读：" + StringFormatHelper.getInstance().toHexString(result));

                sleep(500);
                if (result != null && result.length > 6 && result[4] == (byte) 0xF4 && result[5] == 0x00)
                {
                    batteryUpgradeInfo.statusFlag = BatteryUpgradeStrategyStatus.SUCCESSED;
                    batteryUpgradeInfo.statusInfo = "激活成功!";
                } else
                {
                    batteryUpgradeInfo.statusFlag = BatteryUpgradeStrategyStatus.FAILED;
                    batteryUpgradeInfo.statusInfo = "激活失败!";
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
            batteryUpgradeInfo.statusFlag = BatteryUpgradeStrategyStatus.FAILED;
            batteryUpgradeInfo.statusInfo = "升级IO异常\n" + e.getLocalizedMessage();
            batteryUpgradeInfo.currentPregress = snTemp;
            batteryUpgradeInfo.totalPregress = totalFrameSizeTemp;
            onBatteryDataUpdate.onUpdate(batteryUpgradeInfo);
        }

        sleep(10 * 1000);
        // TODO: 2019-09-20 超时10S电池自动使用新程序,同时推出485转发模式
        onBatteryDataUpdate.onUpdate(batteryUpgradeInfo);
    }


    @Override
    protected void upgrade_can(final CanDeviceIoAction deviceIoAction, final OnBatteryDataUpdate onBatteryDataUpdate)
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                run_can(deviceIoAction, onBatteryDataUpdate, new Lock());
            }
        }).start();
    }

    protected void run_can(CanDeviceIoAction deviceIoAction, OnBatteryDataUpdate onBatteryDataUpdate, final Lock lock)
    {

        // TODO: 2020-01-15 结束升级，发送通讯命令
        //        final byte[] fi3nishUpgrade = new byte[]{0x65, address, (byte) 0xA3, (byte) 0x98,
        //                0x01,
        //                0x00, 0x00, 0x00,
        //                (byte) 0xAA, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        //        try
        //        {
        //            deviceIoAction.write(fi3nishUpgrade);
        //        }
        //        catch (IOException e)
        //        {
        //            e.printStackTrace();
        //        }
        //        System.out.println("~~结束升级");
        //        if(true)return;

        onBatteryDataUpdate = mapOnBatteryDataUpdateImpl(onBatteryDataUpdate);

        short snTemp = 0;
        int totalFrameSizeTemp = 0;
        int sum;

        try
        {
            byte[] result = null;
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

            // TODO: 2020-01-13 电池升级命令帧0xA3
            final byte[] updradeModeData = new byte[]{0x65, address, (byte) 0xA3, (byte) 0x98,
                    0x01,
                    0x00, 0x00, 0x00,
                    0x55, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
            deviceIoAction.write(updradeModeData);

            // TODO: 2020-01-12 定义头帧数据PF:0xA0
            final byte[] startData = new byte[]{0x65, address, (byte) 0xA0, (byte) 0x98,
                    0x04,
                    0x00, 0x00, 0x00,
                    (byte) 0xAA, 0x55, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
            // TODO: 2020-01-12 定义数据帧PF:0xA1
            final byte[] data = new byte[]{0x65, address, (byte) 0xA1, (byte) 0x98,
                    0x08,
                    0x00, 0x00, 0x00,
                    0x55, (byte) 0xAA, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
            // TODO: 2020-01-12 定义尾帧数据PF:0xA2
            final byte[] lastData = new byte[]{0x65, address, (byte) 0xA2, (byte) 0x98,
                    0x02,
                    0x00, 0x00, 0x00,
                    0x55, (byte) 0xAA, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};


            // TODO: 2020-01-13 注册回调对象
            final int startDataResultId = (startData[3] & 0xFF) << 24 | (startData[2] & 0xFF) << 16 | (startData[0] & 0xFF) << 8 | startData[1] & 0xFF;
            deviceIoAction.register(startDataResultId, new NodeConsumer(false)
            {
                @Override
                public void onAccept(byte[] bytes)
                {
                    System.out.println("~~电池升级头：" + bytes[12]);
                    if (bytes != null && bytes.length == 16)
                    {
                        if (lock.cmdFlagMap != null && !lock.cmdFlagMap.containsKey(bytes[12]))
                        {
                            lock.cmdFlag = bytes[12];
                            lock.dataSize = 0;
                            lock.dataLen = ((bytes[11] & 0xFF) << 8) + (bytes[10] & 0xFF);
                            if (lock.dataLen > 0)
                            {
                                lock.flagBytes = new byte[lock.dataLen];
                            }
                            System.out.println("~~电池升级头帧：" + StringFormatHelper.getInstance().toHexString(bytes));
                        }
                    }
                }
            });

            final int dataResultId = (data[3] & 0xFF) << 24 | (data[2] & 0xFF) << 16 | (data[0] & 0xFF) << 8 | data[1];
            deviceIoAction.register(dataResultId, new NodeConsumer(false)
            {
                @Override
                public void onAccept(byte[] bytes)
                {
                    if (bytes != null && bytes.length == 16)
                    {
                        if (lock.cmdFlagMap != null && !lock.cmdFlagMap.containsKey(lock.cmdFlag))
                        {
                            System.out.println("~~电池升级数据帧：" + StringFormatHelper.getInstance().toHexString(bytes));
                            if (lock.flagBytes != null)
                            {
                                final int len = (bytes[4] & 0xFF) - 1;
                                System.arraycopy(bytes, 9, lock.flagBytes, lock.dataSize, len);
                                lock.dataSize += len;
                            }
                        }
                    }
                }
            });

            final int lastDataResultId = (lastData[3] & 0xFF) << 24 | (lastData[2] & 0xFF) << 16 | (lastData[0] & 0xFF) << 8 | lastData[1] & 0xFF;
            deviceIoAction.register(lastDataResultId, new NodeConsumer(false)
            {
                @Override
                public void onAccept(byte[] bytes)
                {
                    if (bytes != null && bytes.length == 16)
                    {
                        if ((bytes[8] & 0xFF) == 0x55 && (bytes[9] & 0xFF) == 0xAA)
                        {
                            if (lock.cmdFlag == bytes[10] && lock.cmdFlagMap != null && !lock.cmdFlagMap.containsKey(lock.cmdFlag))
                            {
                                if (lock.cmdFlag != (byte) 0xF7)
                                {
                                    lock.cmdFlagMap.put(lock.cmdFlag, true);
                                }
                                System.out.println("~~电池升级尾帧：" + StringFormatHelper.getInstance().toHexString(bytes));
                                lock.isContinue = true;
                                if (lock.flagBytes != null)
                                {
                                    System.out.println("~~电池结果总帧：" + StringFormatHelper.getInstance().toHexString(lock.flagBytes));
                                }

                                synchronized (lock)
                                {
                                    lock.notify();
                                }
                            }
                        }
                    }
                }
            });

            //设置长度信息
            //            startData[10] = (byte) (bootLoaderMode.length & 0xFF);
            //            startData[11] = (byte) ((bootLoaderMode.length >> 8) & 0xFF);

            // TODO: 2019-09-05 进入BootLoader模式
            sum = calculateSum(bootLoaderMode, 1, 16);
            bootLoaderMode[17] = (byte) (sum & 0xFF);
            bootLoaderMode[18] = (byte) (sum >> 8 & 0xFF);
            System.out.println("~~请求BootLoader模式：" + StringFormatHelper.getInstance().toHexString(bootLoaderMode));
            final long startTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - startTime < 30 * 1000)//超过30S失败
            {
                convertData(startData, data, lastData, bootLoaderMode, deviceIoAction);
                sleep(500);
                if (lock.flagBytes != null && lock.flagBytes.length > 6 && lock.flagBytes[4] == (byte) 0xF1 && lock.flagBytes[5] == 0x00)
                {
                    System.out.println("~~成功break");
                    break;
                } else
                {
                    if (lock.cmdFlagMap != null)
                    {
                        lock.cmdFlagMap.remove(lock.cmdFlag);
                    }
                    System.out.println("~~没有成功清除");
                }
            }
            if (lock.flagBytes != null && lock.flagBytes.length > 6 && lock.flagBytes[4] == (byte) 0xF1 && lock.flagBytes[5] == 0x00)
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

                final int binDataCrc = crc32(binData);
                System.out.println("~~计算后的CRC：" + binDataCrc);
                if (hexToInt(crcValue) != binDataCrc)
                {
                    batteryUpgradeInfo.statusFlag = BatteryUpgradeStrategyStatus.FAILED;
                    batteryUpgradeInfo.statusInfo = "CRC不匹配!" + StringFormatHelper.getInstance().toHexString(result);
                    onBatteryDataUpdate.onUpdate(batteryUpgradeInfo);
                    sleep(10 * 1000);
                    return;
                }

                // TODO: 2019-09-02 CRC32 校验码,低字节在前，高字节在后。
                firmwareInfo[12] = (byte) (binDataCrc & 0xFF);
                firmwareInfo[13] = (byte) (binDataCrc >> 8 & 0xFF);
                firmwareInfo[14] = (byte) (binDataCrc >> 16 & 0xFF);
                firmwareInfo[15] = (byte) (binDataCrc >> 24 & 0xFF);

                sum = calculateSum(firmwareInfo, 1, 20);
                firmwareInfo[21] = (byte) (sum & 0xFF);
                firmwareInfo[22] = (byte) (sum >> 8 & 0xFF);
                System.out.println("~~新固件信息写入：" + StringFormatHelper.getInstance().toHexString(firmwareInfo));

                convertData(startData, data, lastData, firmwareInfo, deviceIoAction);
                synchronized (lock)
                {
                    try
                    {
                        lock.wait();
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }

                batteryUpgradeInfo.totalPregress = totalFrameSize;
                if (lock.flagBytes != null && lock.flagBytes.length > 6 && lock.flagBytes[4] == (byte) 0xF6 && lock.flagBytes[5] == 0x00)
                {
                    batteryUpgradeInfo.statusFlag = BatteryUpgradeStrategyStatus.INIT_FIRMWARE_DATA;
                    batteryUpgradeInfo.statusInfo = "新固件信息发送成功:" + StringFormatHelper.getInstance().toHexString(result);
                    onBatteryDataUpdate.onUpdate(batteryUpgradeInfo);
                } else
                {
                    batteryUpgradeInfo.statusFlag = BatteryUpgradeStrategyStatus.FAILED;
                    batteryUpgradeInfo.statusInfo = "新固件信息发送失败:" + StringFormatHelper.getInstance().toHexString(result);
                    onBatteryDataUpdate.onUpdate(batteryUpgradeInfo);
                    sleep(10 * 1000);
                    return;
                }

                System.out.println("~~新固件信息发送成功");

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

                    System.out.println("~~第" + sn + "包：" + StringFormatHelper.getInstance().toHexString(firmwareData));
                    convertData(startData, data, lastData, firmwareData, deviceIoAction);
                    offset++;

                    synchronized (lock)
                    {
                        try
                        {
                            lock.wait();
                        }
                        catch (InterruptedException e)
                        {
                            e.printStackTrace();
                        }
                    }

                    sleep(500);

                    batteryUpgradeInfo.currentPregress = sn;
                    if (lock.flagBytes != null && lock.flagBytes.length > 6 && lock.flagBytes[4] == (byte) 0xF7 && lock.flagBytes[5] == 0x00)
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
                    byte[] lastFrameData = new byte[lastLen];
                    System.arraycopy(firmwareData, 0, lastFrameData, 0, 7);
                    System.arraycopy(binData, offset, lastFrameData, 7, len - offset);
                    // TODO: 2019-09-03 填充长度
                    lastFrameData[3] = (byte) ((3 + len - offset) & 0xFF);

                    int end = lastLen - 4 - 1;
                    sum = calculateSum(lastFrameData, 1, end);
                    lastFrameData[++end] = (byte) (sum & 0xFF);
                    lastFrameData[++end] = (byte) (sum >> 8 & 0xFF);
                    lastFrameData[++end] = 0x0D;
                    lastFrameData[++end] = 0x0A;

                    System.out.println("~~第" + sn + "包：" + StringFormatHelper.getInstance().toHexString(firmwareData));
                    convertData(startData, data, lastData, lastFrameData, deviceIoAction);
                    synchronized (lock)
                    {
                        try
                        {
                            lock.wait();
                        }
                        catch (InterruptedException e)
                        {
                            e.printStackTrace();
                        }
                    }

                    if (lock.flagBytes != null && lock.flagBytes.length > 6 && lock.flagBytes[4] == (byte) 0xF7 && lock.flagBytes[5] == 0x00)
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

                sleep(3000);

                sum = calculateSum(activationBMS, 1, 5);
                activationBMS[6] = (byte) (sum & 0xFF);
                activationBMS[7] = (byte) (sum >> 8 & 0xFF);

                convertData(startData, data, lastData, activationBMS, deviceIoAction);
                synchronized (lock)
                {
                    try
                    {
                        lock.wait();
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }

                batteryUpgradeInfo.currentPregress = totalFrameSize;
                batteryUpgradeInfo.statusFlag = BatteryUpgradeStrategyStatus.ACTION_BMS;
                batteryUpgradeInfo.statusInfo = "正在激活...";
                onBatteryDataUpdate.onUpdate(batteryUpgradeInfo);
                System.out.println("~~激活读：" + StringFormatHelper.getInstance().toHexString(result));

                if (lock.flagBytes != null && lock.flagBytes.length > 6 && lock.flagBytes[4] == (byte) 0xF4 && lock.flagBytes[5] == 0x00)
                {
                    batteryUpgradeInfo.statusFlag = BatteryUpgradeStrategyStatus.SUCCESSED;
                    batteryUpgradeInfo.statusInfo = "激活成功!";
                } else
                {
                    batteryUpgradeInfo.statusFlag = BatteryUpgradeStrategyStatus.FAILED;
                    batteryUpgradeInfo.statusInfo = "激活失败!";
                }
            }

            // TODO: 2020-01-15 结束升级，发送通讯命令
            final byte[] finishUpgrade = new byte[]{0x65, address, (byte) 0xA3, (byte) 0x98,
                    0x01,
                    0x00, 0x00, 0x00,
                    (byte) 0xAA, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
            deviceIoAction.write(finishUpgrade);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            batteryUpgradeInfo.statusFlag = BatteryUpgradeStrategyStatus.FAILED;
            batteryUpgradeInfo.statusInfo = "升级IO异常\n" + e.getLocalizedMessage();
            batteryUpgradeInfo.currentPregress = snTemp;
            batteryUpgradeInfo.totalPregress = totalFrameSizeTemp;
            onBatteryDataUpdate.onUpdate(batteryUpgradeInfo);
        }

        System.out.println("~~激活执行完成");

        // TODO: 2019-09-20 超时10S电池自动使用新程序,同时推出485转发模式
        onBatteryDataUpdate.onUpdate(batteryUpgradeInfo);
    }

    private void convertData(byte[] startData, byte[] data, byte[] lastData, byte[] srcData, CanDeviceIoAction deviceIoAction) throws IOException
    {
        startData[10] = (byte) (srcData.length & 0xFF);
        startData[11] = (byte) ((srcData.length >> 8) & 0xFF);

        deviceIoAction.write(startData);
        System.out.println("~~数据：" + StringFormatHelper.getInstance().toHexString(startData));
        sleep(20);

        int count = srcData.length / 7;
        int frameSn = 1;
        for (int i = 0, pos = 0; i < count; i++, frameSn = frameSn++ % 255 > 0 ? frameSn : 1, pos += 7)
        {
            data[8] = (byte) frameSn;
            System.arraycopy(srcData, pos, data, 9, 7);
            deviceIoAction.write(data);
            System.out.println("~~数据：" + StringFormatHelper.getInstance().toHexString(data));
            sleep(20);
        }

        if (srcData.length % 7 > 0)
        {
            int pos = srcData.length - srcData.length % 7;
            int len_ = srcData.length % 7;
            data[8] = (byte) frameSn;
            System.arraycopy(srcData, pos, data, 9, len_);
            deviceIoAction.write(data);
            System.out.println("~~数据：" + StringFormatHelper.getInstance().toHexString(data));
            sleep(20);
        }
        deviceIoAction.write(lastData);
        System.out.println("~~数据：" + StringFormatHelper.getInstance().toHexString(lastData));
    }
}
