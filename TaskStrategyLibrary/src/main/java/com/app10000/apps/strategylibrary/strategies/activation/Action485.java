package com.app10000.apps.strategylibrary.strategies.activation;

import com.app10000.apps.controllerlibrary.DeviceIoAction;
import com.app10000.apps.strategylibrary.dispatchers.canExtension.CanDeviceIoAction;
import com.app10000.apps.strategylibrary.strategies._base.ProtocolStrategy;

import java.io.IOException;

/**
 * Author:      Lee Yeung
 * Create Date: 2019-10-11
 * Description: 485激活
 */
public class Action485 extends ProtocolStrategy
{
    public Action485(byte address)
    {
        super(address);
    }

    @Override
    protected void execute_sp(DeviceIoAction deviceIoAction)
    {
        byte[] _485 = new byte[]{address, 0x05, 0x00, 0x0B, 0x00, 0x01, 0x00, 0x00};
        short crc = crc16(_485, 0, 6);
        _485[_485.length - 2] = (byte) (crc & 0xFF);
        _485[_485.length - 1] = (byte) (crc >> 8 & 0xFF);
        try
        {
            deviceIoAction.write(_485);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    protected void execute_can(CanDeviceIoAction deviceIoAction)
    {
        byte[] _485 = new byte[]{address, 0x00, 0x00, 0x00, 0x08, 0x00, 0x00, 0x00, address, 0x05, 0x00, 0x0B, 0x00, 0x01, 0x00, 0x00};
        short crc = crc16(_485, 8, 14);
        _485[_485.length - 2] = (byte) (crc & 0xFF);
        _485[_485.length - 1] = (byte) (crc >> 8 & 0xFF);

        try
        {
            deviceIoAction.write(_485);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
