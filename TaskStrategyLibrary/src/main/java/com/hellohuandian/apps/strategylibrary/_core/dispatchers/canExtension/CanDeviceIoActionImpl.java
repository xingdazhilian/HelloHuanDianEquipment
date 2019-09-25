package com.hellohuandian.apps.strategylibrary._core.dispatchers.canExtension;

import com.hellohuandian.apps.controllerlibrary.DeviceIoAction;
import com.hellohuandian.apps.utillibrary.StringFormatHelper;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import androidx.core.util.Consumer;

/**
 * Author:      Lee Yeung
 * Create Date: 2019-09-24
 * Description:
 */
public final class CanDeviceIoActionImpl extends ConcurrentHashMap<Integer, Consumer<byte[]>> implements CanDeviceIoAction
{
    private final DeviceIoAction deviceIoAction;
    private final int LEN_16 = 16;

    public CanDeviceIoActionImpl(DeviceIoAction deviceIoAction)
    {
        if (deviceIoAction == null)
        {
            throw new NullPointerException("deviceIoAction 空指针！！");
        }
        this.deviceIoAction = deviceIoAction;
    }

    /**
     * 解析分发
     */
    public void parseDispatch()
    {
        byte[] result = new byte[0];
        try
        {
            result = read();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        if (result != null && result.length == LEN_16)
        {
            // TODO: 2019-09-24 pdu是扩展帧，控制板相关是标准帧
            // TODO: 2019-09-24 先区分来自pdu还是控制板的数据
            final int frameId = (result[3] & 0xFF) << 24 | (result[2] & 0xFF) << 16 | (result[1] & 0xFF) << 8 | result[0];
            // TODO: 2019-09-24 初始化结果ID为帧ID
            int resultId = frameId;
            if (frameId >= 0x01 && frameId <= 0x0D)
            {
                //来自控制设备地址
                if (result[8] == 0)
                {
                    //推杆，映射控制设备唯一ID
                    resultId = (result[0] & 0xFF) << 16 | (result[8] & 0xFF) << 8 | result[11];
                } else if (result[8] >= 0x10)
                {
                    //数据包,映射到控制板地址作为ID
                    resultId = result[0];
                }
            }

            if (resultId > 0)
            {
                Consumer<byte[]> consumer = get(resultId);
                if (consumer != null)
                {
                    consumer.accept(result);
                }
            }
        }
    }

    @Override
    public void register(int id, Consumer<byte[]> consumer)
    {
        put(id, consumer);
    }

    @Override
    public void unRegister(int id)
    {
        remove(id);
    }

    @Override
    public void write(byte[] data) throws IOException
    {
        deviceIoAction.write(data);
    }

    @Override
    public byte[] read() throws IOException
    {
        return deviceIoAction.read();
    }

    @Override
    public int ioProtocol()
    {
        return deviceIoAction.ioProtocol();
    }
}