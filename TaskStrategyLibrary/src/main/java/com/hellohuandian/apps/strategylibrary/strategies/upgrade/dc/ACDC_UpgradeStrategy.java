package com.hellohuandian.apps.strategylibrary.strategies.upgrade.dc;

import android.os.SystemClock;

import com.hellohuandian.apps.controllerlibrary.DeviceIoAction;
import com.hellohuandian.apps.strategylibrary.dispatchers.canExtension.CanDeviceIoAction;
import com.hellohuandian.apps.utillibrary.StringFormatHelper;

/**
 * Author:      Lee Yeung
 * Create Date: 2019-12-10
 * Description: 赛德PDU升级策略
 */
public class ACDC_UpgradeStrategy extends DC_UpgradeStrategy
{
    public ACDC_UpgradeStrategy(byte address)
    {
        super(address);
    }

    class Lock
    {
        public volatile boolean isContinue;
        //传输地址
        public volatile int transmissionAddress;
        //传输长度
        public volatile short transmissionLen;

        public byte[] flagBytes;
    }

    @Override
    protected void execute_sp(DeviceIoAction deviceIoAction)
    {

    }

    @Override
    protected void execute_can(final CanDeviceIoAction deviceIoAction)
    {
        DC_Upgrade_Supporter.getInstance().register(new UpgradeConsumer(upgradeFilePath)
        {
            @Override
            public void accept(byte[] bytes)
            {
                try
                {
                    runPduUpgrade(bytes, deviceIoAction, new ACDC_UpgradeStrategy.Lock());

                    //                    final byte open = 0x55;
                    //                    final byte[] openAcdc = new byte[]{0x65, 0x51, 0x10, (byte) 0x98,
                    //                            0x01,
                    //                            0x00, 0x00, 0x00,
                    //                            open, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                    ////                    for (int addr = 0x51; addr <= 0x53; addr++)
                    ////                    {
                    ////                        openAcdc[1] = (byte) addr;
                    //                        System.out.println("ACDC开机：" + StringFormatHelper.getInstance().toHexString(openAcdc));
                    //
                    //                        deviceIoAction.write(openAcdc);
                    ////                        sleep(100);
                    ////                    }

                    if (next() != null)
                    {
                        nextCall();
                    } else
                    {
                        DC_Upgrade_Supporter.stop();
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
    }

    private void runPduUpgrade(final byte[] DATA, final CanDeviceIoAction deviceIoAction, final ACDC_UpgradeStrategy.Lock lock) throws Exception
    {
        // TODO: 2019-12-25 ACDC全部关机
        final byte close = (byte) 0xAA;
        final byte[] closeAcdc = new byte[]{0x65, 0x00, 0x10, (byte) 0x98,
                0x01,
                0x00, 0x00, 0x00,
                close, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        for (int addr = 0x51; addr <= 0x53; addr++)
        {
            sleep(200);
            closeAcdc[1] = (byte) addr;
            System.out.println("ACDC关机：" + StringFormatHelper.getInstance().toHexString(closeAcdc));
            deviceIoAction.write(closeAcdc);
        }

        // TODO: 2019-12-11 ===========================1：发送命令让总线从正常模式切换至升级模式================================
        // TODO: 2019-12-11 (先选择设置模式，模式值为升级模式,注意：0xFF是广播地址为了让所有ACDC模块全部挂起并且让出总线负载)
        final byte[] selectUpgradeModeData = new byte[]{0x65, (byte) 0xFF, 0x13, (byte) 0x98,
                0x02,
                0x00, 0x00, 0x00,
                0x02, 0x02, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        System.out.println("ACDC升级步骤1：");
        deviceIoAction.write(selectUpgradeModeData);
        sleep(100);

        // TODO: 2019-12-11 ===========================2：上位机发送给下位机从APP切换至boot命令================================
        // TODO: 2019-12-11 (设置指定地址对应的ACDC模块进入升级模式之后，需要对其重启boot才能生效，注意：只操作重新选定的地址)
        final byte[] selectBootModeData = new byte[]{0x65, (byte) (address & 0xFF), 0x13, (byte) 0x98,
                0x02,
                0x00, 0x00, 0x00,
                0x01, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        System.out.println("ACDC升级步骤2：");
        deviceIoAction.write(selectBootModeData);
        sleep(100);

        final byte mapAddress = (byte) (address & 0x0F);

        // TODO: 2019-12-11 ===========================3：注册下位机是否收到连接帧回调================================
        // TODO: 2019-12-11 下位机立即回复上位机的连接帧请求,只是请求，还没有确认
        final int connectingRequestResultId = (0x9CA << 20) + (0x01 << 19) + ((androidAddress & 0xFF) << 11) + ((mapAddress & 0xFF) << 3) + (0x03 << 0);
        deviceIoAction.registerTimeOut(connectingRequestResultId, SystemClock.elapsedRealtime() + 5 * 1000);
        deviceIoAction.register(connectingRequestResultId, new NodeConsumer(false)
        {
            @Override
            public void onAccept(byte[] bytes)
            {
                if (bytes != null)
                {
                    System.out.println("ACDC升级步骤4下位机返回：");
                    lock.isContinue = (bytes[8] & 0xFF) == 0x00 && (bytes[9] & 0xFF) == 0xF0;
                    if (lock.isContinue)
                    {
                        System.arraycopy(bytes, 0, lock.flagBytes, 0, lock.flagBytes.length);
                        deviceIoAction.unRegister(connectingRequestResultId);
                    }
                }
                synchronized (lock)
                {
                    lock.notify();
                }
            }
        });

        // TODO: 2019-12-11上位机开始50ms周期发送请求连接帧，是否超时达到200ms
        final int connectingId = (0x9CA << 20) + (0x01 << 19) + ((mapAddress & 0xFF) << 11) + ((androidAddress & 0xFF) << 3) + (0x03 << 0);
        final byte[] connectingFrameData = new byte[]{(byte) (connectingId & 0xFF), (byte) ((connectingId >> 8) & 0xFF), (byte) ((connectingId >> 16) & 0xFF), (byte) ((connectingId >> 24) & 0xFF),
                0x00,
                0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

        int count = 4;
        while (count-- > 0)
        {
            System.out.println("ACDC升级步骤3：");
            deviceIoAction.write(connectingFrameData);
            sleep(50);
            if (lock.isContinue)
            {
                break;
            }
        }
        if (!lock.isContinue)
        {
            synchronized (lock)
            {
                lock.wait(5 * 1000);
            }
        }
        if (!lock.isContinue)
        {
            // TODO: 2019-12-12 下位机立即回复上位机的连接帧请求超时
            System.out.println("下位机立即回复上位机的连接帧请求超时");
            // TODO: 2019-12-16 解除挂起ACDC
            selectUpgradeModeData[9] = 0x01;
            System.out.println("ACDC解除挂起");
            deviceIoAction.write(selectUpgradeModeData);

            sleep(5000);

            // TODO: 2019-12-25 ACDC全部开机
            final byte open = 0x55;
            final byte[] openAcdc = new byte[]{0x65, 0x00, 0x10, (byte) 0x98,
                    0x01,
                    0x00, 0x00, 0x00,
                    open, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
            for (int addr = 0x51; addr <= 0x53; addr++)
            {
                sleep(200);
                openAcdc[1] = (byte) addr;
                System.out.println("ACDC开机：" + StringFormatHelper.getInstance().toHexString(openAcdc));
                deviceIoAction.write(openAcdc);
            }
            return;
        }

        // TODO: 2019-12-11 ========================先注册升级请求传输帧===================================
        // TODO: 2019-12-11  升级开始，模块请求升级信息0x1c8f000b(超时时间为15S，15S升级失败，返回)
        final int _1c8_ResultId = (0x9C8 << 20) + (0x01 << 19) + ((androidAddress & 0xFF) << 11) + ((mapAddress & 0xFF) << 3) + (0x03 << 0);
        deviceIoAction.register(_1c8_ResultId, new NodeConsumer(false)
        {
            @Override
            public void onAccept(byte[] bytes)
            {
                System.out.println("ACDC升级头");
                synchronized (lock)
                {
                    /*
                        0x00:块回应 ack 响应
                        0x01:数据丢失
                        0x02:最后一块响应
                        0x03:非法的固件程序
                        0x04:设备写固件丢失
                    */
                    if (bytes != null && bytes.length == 16)
                    {
                        lock.isContinue = (bytes[8] & 0xFF) == 0x00 && (bytes[9] & 0xFF) == 0x00;
                        lock.transmissionAddress = (bytes[10] & 0xFF) << 24 | (bytes[11] & 0xFF) << 16 | (bytes[12] & 0xFF) << 8 | (bytes[13] & 0xFF);
                        lock.transmissionLen = (short) ((bytes[14] & 0xFF) << 8 | (bytes[15] & 0xFF));
                    }
                    lock.notify();
                }
            }
        });
        final int _1c9_ResultId = (0x9C9 << 20) + (0x01 << 19) + ((androidAddress & 0xFF) << 11) + ((mapAddress & 0xFF) << 3) + (0x03 << 0);
        deviceIoAction.register(_1c9_ResultId, new NodeConsumer(false)
        {
            @Override
            public void onAccept(byte[] bytes)
            {
                System.out.println("ACDC升级段");
                synchronized (lock)
                {
                    /*
                        0x00:块回应 ack 响应
                        0x01:数据丢失
                        0x02:最后一块响应
                        0x03:非法的固件程序
                        0x04:设备写固件丢失
                    */
                    if (bytes != null && bytes.length == 16)
                    {
                        if ((bytes[8] & 0xFF) == 0x00 && (bytes[9] & 0xFF) == 0x02)
                        {
                            deviceIoAction.unRegister(_1c9_ResultId);
                            deviceIoAction.unRegister(_1c8_ResultId);
                            System.out.println("升级成功");
                        } else
                        {
                            lock.isContinue = (bytes[8] & 0xFF) == 0x00 && (bytes[9] & 0xFF) == 0x00;
                            lock.transmissionAddress = (bytes[10] & 0xFF) << 24 | (bytes[11] & 0xFF) << 16 | (bytes[12] & 0xFF) << 8 | (bytes[13] & 0xFF);
                            lock.transmissionLen = (short) ((bytes[14] & 0xFF) << 8 | (bytes[15] & 0xFF));
                        }
                    }
                    lock.notify();
                }
            }
        });

        // TODO: 2019-12-11 ========================下位机立即回复上位机的连接帧确认===================================
        // TODO: 2019-12-11上位机响应确认连接oK命令(注意是确认连接ok)
        lock.isContinue = false;
        int _connected_Frame_Id = (0x9CA << 20) + (0x01 << 19) + ((mapAddress & 0xFF) << 11) + ((androidAddress & 0xFF) << 3) + (0x03 << 0);
        final byte[] connectedFrameData = new byte[]{(byte) (_connected_Frame_Id & 0xFF), (byte) ((_connected_Frame_Id >> 8) & 0xFF), (byte) ((_connected_Frame_Id >> 16) & 0xFF), (byte) ((_connected_Frame_Id >> 24) & 0xFF),
                0x08,
                0x00, 0x00, 0x00,
                (byte) 0x80, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        synchronized (lock)
        {
            if (lock.flagBytes != null && lock.flagBytes.length == 16)
            {
                System.arraycopy(lock.flagBytes, 9, connectedFrameData, 9, 7);
            }

            System.out.println("ACDC升级步骤5：");
            deviceIoAction.write(connectedFrameData);
            lock.wait(5 * 1000);
        }
        if (!lock.isContinue)
        {
            deviceIoAction.unRegister(_1c8_ResultId);
            deviceIoAction.unRegister(_1c9_ResultId);
            // TODO: 2019-12-16 解除挂起ACDC
            selectUpgradeModeData[9] = 0x01;
            System.out.println("ACDC解除挂起：");
            deviceIoAction.write(selectUpgradeModeData);

            sleep(5000);

            // TODO: 2019-12-25 ACDC全部开机
            final byte open = 0x55;
            final byte[] openAcdc = new byte[]{0x65, 0x00, 0x10, (byte) 0x98,
                    0x01,
                    0x00, 0x00, 0x00,
                    open, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
            for (int addr = 0x51; addr <= 0x53; addr++)
            {
                sleep(200);
                openAcdc[1] = (byte) addr;
                System.out.println("ACDC开机：" + StringFormatHelper.getInstance().toHexString(openAcdc));
                deviceIoAction.write(openAcdc);
            }
            return;
        }

        // TODO: 2019-12-11上位机发送数据(0x1d0~0x1ef)
        int _transmission_Frame_Id = (0x9D0 << 20) + (0x01 << 19) + ((mapAddress & 0xFF) << 11) + ((androidAddress & 0xFF) << 3) + (0x03 << 0);
        final byte[] transmissionFrameData = new byte[]{(byte) (_transmission_Frame_Id & 0xFF), (byte) ((_transmission_Frame_Id >> 8) & 0xFF), (byte) ((_transmission_Frame_Id >> 16) & 0xFF), (byte) ((_transmission_Frame_Id >> 24) & 0xFF),
                0x00,
                0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        while (true)
        {
            synchronized (lock)
            {
                short sn = 0;
                int len;
                while ((len = lock.transmissionLen >= 8 ? 8 : lock.transmissionLen % 8) > 0)
                {
                    sn = sn % 256 == 0 ? 0 : sn;
                    transmissionFrameData[3] = (byte) (0x9D + (sn >> 4));
                    transmissionFrameData[2] = (byte) (0x08 + ((sn & 0x0F) << 4));
                    sn++;

                    transmissionFrameData[4] = (byte) len;
                    System.arraycopy(DATA, lock.transmissionAddress, transmissionFrameData, 8, len);
                    System.out.println("ACDC数据地址：" + lock.transmissionAddress);
                    deviceIoAction.write(transmissionFrameData);

                    lock.transmissionAddress += len;
                    lock.transmissionLen -= len;
                }

                lock.isContinue = false;
                lock.wait();

                if (!lock.isContinue)
                {
                    break;
                }
            }
        }

        deviceIoAction.unRegister(_1c8_ResultId);
        deviceIoAction.unRegister(_1c9_ResultId);

        System.out.println("ACDC升级成功");

        // TODO: 2019-12-16 解除挂起ACDC
        selectUpgradeModeData[9] = 0x01;
        deviceIoAction.write(selectUpgradeModeData);
        System.out.println("ACDC解除挂起：");
        CONTOUNT = CONTOUNT + 1;
        System.out.println("ACDC成功升级数量" + CONTOUNT);

        sleep(5000);

        // TODO: 2019-12-25 ACDC全部开机
        final byte open = 0x55;
        final byte[] openAcdc = new byte[]{0x65, 0x00, 0x10, (byte) 0x98,
                0x01,
                0x00, 0x00, 0x00,
                open, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        for (int addr = 0x51; addr <= 0x53; addr++)
        {
            sleep(200);
            openAcdc[1] = (byte) addr;
            System.out.println("ACDC开机：" + StringFormatHelper.getInstance().toHexString(openAcdc));
            deviceIoAction.write(openAcdc);
        }

        sleep(5000);
    }

    static int CONTOUNT = 0;
}


