package com.android_canbus_api.apps.controllerlibrary;

import java.io.IOException;

import android_serialport_api.SerialPortConfig;
import android_serialport_api.SerialPortDevice;
import android_serialport_api.SerialPortIoAction;
import androidx.core.util.Consumer;

/**
 * Author:      Lee Yeung
 * Create Date: 2019-08-28
 * Description: 串口设备控制器
 */
public final class SerialPortDeviceController implements Consumer<SerialPortIoAction>, DeviceIoAction
{
    private static final SerialPortDeviceController DEVICE_IO_CONTROLLER = new SerialPortDeviceController();

    private SerialPortDevice serialPortDevice;
    private SerialPortIoAction serialPortIoAction;
    private Consumer<DeviceIoAction> deviceIoActionConsumer;

    public SerialPortDeviceController()
    {
        try
        {
            serialPortDevice = new SerialPortDevice(new SerialPortConfig());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static SerialPortDeviceController getInstance()
    {
        return DEVICE_IO_CONTROLLER;
    }

    public <T extends Consumer<DeviceIoAction>> void execute(T deviceIoActionConsumer)
    {
        if (deviceIoActionConsumer != null)
        {
            synchronized (this)
            {
                if (serialPortDevice != null)
                {
                    this.deviceIoActionConsumer = deviceIoActionConsumer;
                    serialPortDevice.doIoAction(this);
                }
            }
        }
    }

    @Override
    public void accept(SerialPortIoAction serialPortIoAction)
    {
        if (serialPortIoAction != null && deviceIoActionConsumer != null)
        {
            this.serialPortIoAction = serialPortIoAction;
            deviceIoActionConsumer.accept(this);
        }
    }

    @Override
    public void write(byte[] data) throws IOException
    {
        serialPortIoAction.write(data);
    }

    @Override
    public byte[] read() throws IOException
    {
        return serialPortIoAction.read();
    }

    @Override
    public int ioProtocol()
    {
        return DeviceIoAction.SERIAL_PORT;
    }
}
