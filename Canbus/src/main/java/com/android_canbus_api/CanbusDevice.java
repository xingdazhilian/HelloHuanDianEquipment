package com.android_canbus_api;

import java.io.IOException;

import androidx.core.util.Consumer;

/**
 * Author:      Lee Yeung
 * Create Date: 2019-09-18
 * Description:
 */
public final class CanbusDevice extends Canbus
{
    private CanIoAction canIoAction = new CanIoAction()
    {
        @Override
        public void write(byte[] data) throws IOException
        {
            CanbusDevice.this.write(data);
        }

        @Override
        public byte[] read() throws IOException
        {
            return CanbusDevice.this.read();
        }
    };

    public CanbusDevice()
    {
        init();
    }

    private void init()
    {
        //can初始化
        A_RootCmd.execRootCmd("ip link set can0 down");
        A_RootCmd.execRootCmd("ip link set can0 type can loopback off triple-sampling on");
        A_RootCmd.execRootCmd("ip link set can0 type can bitrate 125000 loopback off triple-sampling on");
        A_RootCmd.execRootCmd("ip link set can0 up");
        openCan();
    }

    public final <T extends Consumer<CanIoAction>> void doIoAction(T ioConsumer)
    {
        if (ioConsumer != null)
        {
            ioConsumer.accept(canIoAction);
        }
    }
}
