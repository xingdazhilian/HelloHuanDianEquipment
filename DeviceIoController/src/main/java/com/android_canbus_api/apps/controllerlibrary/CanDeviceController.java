package com.android_canbus_api.apps.controllerlibrary;

import com.android_canbus_api.CanIoAction;
import com.android_canbus_api.CanbusDevice;

import java.io.IOException;

import androidx.core.util.Consumer;

/**
 * Author:      Lee Yeung
 * Create Date: 2019-09-19
 * Description: can总线设备控制器
 */
public class CanDeviceController implements Consumer<CanIoAction>, DeviceIoAction
{
    private static final CanDeviceController CAN_DEVICE_CONTROLLER = new CanDeviceController();

    private CanbusDevice canbusDevice;
    private CanIoAction canIoAction;
    private Consumer<DeviceIoAction> deviceIoActionConsumer;

    public CanDeviceController()
    {
        canbusDevice = new CanbusDevice();
    }

    public static CanDeviceController getInstance()
    {
        return CAN_DEVICE_CONTROLLER;
    }

    public synchronized <T extends Consumer<DeviceIoAction>> void execute(T deviceIoActionConsumer)
    {
        if (deviceIoActionConsumer != null)
        {
            if (canbusDevice != null)
            {
                this.deviceIoActionConsumer = deviceIoActionConsumer;
                canbusDevice.doIoAction(this);
            }
        }
    }

    @Override
    public void accept(CanIoAction canIoAction)
    {
        if (canIoAction != null && deviceIoActionConsumer != null)
        {
            this.canIoAction = canIoAction;
            deviceIoActionConsumer.accept(this);
        }
    }

    @Override
    public void write(byte[] data) throws IOException
    {
        canIoAction.write(data);
    }

    @Override
    public byte[] read() throws IOException
    {
        return canIoAction.read();
    }

    @Override
    public int ioProtocol()
    {
        return DeviceIoAction.CANBUS;
    }
}
