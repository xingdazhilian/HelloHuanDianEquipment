package com.app10000.apps.strategylibrary.strategies.upgrade.pdu;

import android.os.SystemClock;
import android.text.TextUtils;

import com.app10000.apps.controllerlibrary.DeviceIoAction;
import com.app10000.apps.strategylibrary.dispatchers.canExtension.CanDeviceIoAction;
import com.app10000.apps.strategylibrary.strategies._base.ProtocolStrategy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Author:      Lee Yeung
 * Create Date: 2019-12-02
 * Description: pdu升级策略 地址固定为(byte) 0x88
 */
public class PduUpgradeStrategy extends ProtocolStrategy
{
    private String filePath;

    class Lock
    {
        public byte sn;
        public boolean isContinue;
    }

    public PduUpgradeStrategy()
    {
        super((byte) 0x88);
    }

    @Override
    protected void execute_sp(DeviceIoAction deviceIoAction)
    {

    }

    @Override
    protected void execute_can(final CanDeviceIoAction deviceIoAction)
    {
        if (TextUtils.isEmpty(filePath))
        {
            System.out.println("pdu升级文件路径为空！");
            return;
        }

        File file = new File(filePath);
        if (!(file != null && file.exists()))
        {
            System.out.println("pdu升级文件可能不存在！");
            return;
        }

        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                runPduUpgrade(deviceIoAction, new Lock());
            }
        }).start();
    }

    public void setPduUpgradeFilePath(String filePath)
    {
        this.filePath = filePath;
    }

    private void runPduUpgrade(final CanDeviceIoAction deviceIoAction, final Lock lock)
    {
        // TODO: 2019-12-02 1:请求pdu升级模式 -》[AB][3D][0F][88][05][00][00][00][01][F0][00][31][AA][00][00][00]
        final int updradeModeResultId = 0x88 << 24 | 0x0F << 16 | 0x3D << 8 | 0xAB;
        deviceIoAction.registerTimeOut(updradeModeResultId, SystemClock.elapsedRealtime() + 2000);
        deviceIoAction.register(updradeModeResultId, new NodeConsumer()
        {
            @Override
            public void onAccept(byte[] bytes)
            {
                deviceIoAction.unRegister(updradeModeResultId);
                synchronized (lock)
                {
                    lock.isContinue = bytes != null && bytes.length == 16 && bytes[11] == 0x31;
                    System.out.println("pdu升级模式:" + (lock.isContinue ? "ok" : "no"));
                    lock.notify();
                }
            }
        });

        // TODO: 2019-12-05 ===========================================================
        // TODO: 2019-12-03 请求升级模式指令
        final byte[] updradeModeData = new byte[]{0x3F, (byte) 0xAF, 0x0D, (byte) 0x88,
                0x08,
                0x00, 0x00, 0x00,
                0x01, (byte) 0xF0, 0x00, 0x30, 0x55, 0x00, 0x00, 0x00};
        synchronized (lock)
        {
            try
            {
                deviceIoAction.write(updradeModeData);
                lock.wait();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        if (!lock.isContinue)
        {
            return;
        }

        // TODO: 2019-12-02 2: 进入升级模式后，上位机把整个 bin 文件拆分开，每整包数据是 2k 字节数据，2k 的数据 也是拆成每帧报文，携带 7 个字节发送。
        File file = new File(filePath);
        InputStream inputStream = null;
        try
        {
            inputStream = new FileInputStream(file);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }

        if (inputStream == null)
        {
            return;
        }

        int len = 0;
        try
        {
            len = inputStream.available();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        if (len <= 0)
        {
            return;
        }

        // TODO: 2019-12-05 计算总2K包数量
        final int packetCount = len / 2048 + (len % 2048 > 0 ? 1 : 0);
        // TODO: 2019-12-05 建立链接指令
        final byte[] upgradeConnectData = new byte[]{0x3F, (byte) 0xAF, 0x1D, (byte) 0x88,
                0x08,
                0x00, 0x00, 0x00,
                0x10,//命令标志16(10H)请求发送
                (byte) (packetCount & 0xFF), (byte) (packetCount >> 8 & 0xFF), (byte) (packetCount >> 16 & 0xFF), (byte) (packetCount >> 24 & 0xFF),//本包程序字节数
                (byte) (packetCount & 0xFF), (byte) (packetCount >> 8 & 0xFF),//本包帧数
                (byte) 0xFF//预留
        };

        // TODO: 2019-12-05 数据单帧指令 
        final byte[] upgradeFrameData = new byte[]{0x3F, (byte) 0xAF, 0x2D, (byte) 0x88,
                0x08,
                0x00, 0x00, 0x00,
                0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        // TODO: 2019-12-05 单包结束指令
        final byte[] framePacketFinish = new byte[]{0x3F, (byte) 0xAF, 0x1D, (byte) 0x88,
                0x08,
                0x00, 0x00, 0x00,
                0x13,//报文发送结束
                0x00, 0x00, 0x00, 0x00,//本包程序字节数
                0x00, 0x00,//CRC-16
                0x00};

        try
        {
            final int availableLen = inputStream.available();
            if (availableLen <= 0)
            {
                return;
            }

            //创建升级包文件数组
            final byte[] frameDataByes = new byte[availableLen];
            inputStream.read(frameDataByes);
            inputStream.close();

            int elementCount = availableLen;
            int currentReadLen;
            int packetLen = 0;
            int start = 0;
            int packetSn = 0;

            while (elementCount > 0)
            {
                packetSn++;
                lock.isContinue = false;
                // TODO: 2019-12-05 ===========================================================
                // TODO: 2019-12-03  3.每次都要重新建立链接
                final int updradeConnectResultId = 0x88 << 24 | 0x1F << 16 | 0x3D << 8 | 0xAB;
                deviceIoAction.registerTimeOut(updradeConnectResultId, SystemClock.elapsedRealtime() + 2000);
                deviceIoAction.register(updradeConnectResultId, new NodeConsumer()
                {
                    @Override
                    public void onAccept(byte[] bytes)
                    {
                        deviceIoAction.unRegister(updradeConnectResultId);
                        synchronized (lock)
                        {
                            lock.isContinue = bytes != null && bytes.length == 16 && bytes[8] == 0x11;
                            System.out.println("pdu升级链接：" + (lock.isContinue ? "ok" : "no"));
                            if (lock.isContinue)
                            {
                                lock.sn = bytes[11];//下一个要发送的数据包编号
                            }
                            lock.notify();
                        }
                    }
                });
                synchronized (lock)
                {
                    try
                    {
                        deviceIoAction.write(upgradeConnectData);
                        lock.wait();
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
                if (!lock.isContinue)
                {
                    return;
                }

                byte sn = lock.sn;
                //当前可读取长度
                currentReadLen = elementCount >= 2048 ? 2048 : elementCount;
                elementCount -= currentReadLen;
                // TODO: 2019-12-05 ===========================================================
                // TODO: 2019-12-05 循环处理单包整除7的数据
                for (int i = 0, count = currentReadLen / 7; i < count; i++, start += 7)
                {
                    upgradeFrameData[4] = 0x08;
                    sn = sn % 256 == 0 ? 0 : sn;
                    upgradeFrameData[8] = sn++;
                    System.arraycopy(frameDataByes, start, upgradeFrameData, 9, 7);
                    Thread.sleep(10);
                    deviceIoAction.write(upgradeFrameData);
                }
                // TODO: 2019-12-05 ===========================================================
                // TODO: 2019-12-05 处理单包不能被7整除，剩余长度的数据 
                final int lastLen = currentReadLen % 7;
                if (lastLen > 0)
                {
                    upgradeFrameData[4] = (byte) (1 + lastLen);
                    upgradeFrameData[8] = sn;

                    System.arraycopy(frameDataByes, start, upgradeFrameData, 9, lastLen);
                    deviceIoAction.write(upgradeFrameData);
                    start += lastLen;
                }

                // TODO: 2019-12-05 ===========================================================
                // TODO: 2019-12-04 5.发送单包数据帧结束指令
                framePacketFinish[9] = (byte) (currentReadLen & 0xFF);
                framePacketFinish[10] = (byte) (currentReadLen >> 8 & 0xFF);
                framePacketFinish[11] = (byte) (currentReadLen >> 16 & 0xFF);
                framePacketFinish[12] = (byte) (currentReadLen >> 24 & 0xFF);

                //计算单包CRC值
                packetLen += currentReadLen;
                short crc16 = crc16(frameDataByes, packetLen - currentReadLen, packetLen);
                framePacketFinish[13] = (byte) (crc16 & 0xFF);
                framePacketFinish[14] = (byte) (crc16 >> 8 & 0xFF);

                final int psn = packetSn;
                final int framePacketFinishResultId = 0x88 << 24 | 0x3F << 16 | 0x3D << 8 | 0xAB;
                deviceIoAction.registerTimeOut(framePacketFinishResultId, SystemClock.elapsedRealtime() + 2000);
                deviceIoAction.register(framePacketFinishResultId, new NodeConsumer()
                {
                    @Override
                    public void onAccept(byte[] bytes)
                    {
                        deviceIoAction.unRegister(framePacketFinishResultId);
                        synchronized (lock)
                        {
                            lock.isContinue = bytes != null && bytes.length == 16 && (bytes[8] & 0xFF) == 0xAA;
                            System.out.println("pdu单包结束结果：" + (lock.isContinue ? "ok" : "no"));
                            System.out.println("pdu第" + psn + "包完成");
                            lock.notify();
                        }
                    }
                });

                synchronized (lock)
                {
                    deviceIoAction.write(framePacketFinish);
                    lock.wait();
                }

                if (!lock.isContinue)
                {
                    return;
                }
            }

            // TODO: 2019-12-05 ===========================================================
            // TODO: 2019-12-04 6.发送整包结束报文指令
            final int framePacketFinishResultId = 0x88 << 24 | 0x5F << 16 | 0x3D << 8 | 0xAB;
            deviceIoAction.registerTimeOut(framePacketFinishResultId, SystemClock.elapsedRealtime() + 2000);
            deviceIoAction.register(framePacketFinishResultId, new NodeConsumer()
            {
                @Override
                public void onAccept(byte[] bytes)
                {
                    deviceIoAction.unRegister(framePacketFinishResultId);
                    synchronized (lock)
                    {
                        lock.isContinue = bytes != null && bytes.length == 16 && (bytes[8] & 0xFF) == 0x0E;
                        System.out.println("pdu整包结束结果：" + (lock.isContinue ? "ok" : "no"));
                        lock.notify();
                    }
                }
            });

            final short crc16 = crc16(frameDataByes, 0, availableLen);
            final byte[] allPacketFinish = new byte[]{0x3F, (byte) 0xAF, 0x4D, (byte) 0x88,
                    0x08,
                    0x00, 0x00, 0x00,
                    (byte) (availableLen & 0xFF), (byte) (availableLen >> 8 & 0xFF), (byte) (availableLen >> 16 & 0xFF), (byte) (availableLen >> 24 & 0xFF),//本包程序字节数
                    (byte) (crc16 & 0xFF), (byte) (crc16 >> 8 & 0xFF),//CRC-16
                    (byte) 0xFF, (byte) 0xFF};

            synchronized (lock)
            {
                deviceIoAction.write(allPacketFinish);
                lock.wait();
            }

            if (!lock.isContinue)
            {
                return;
            }
            System.out.println("PDU升级完成！！！");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}





