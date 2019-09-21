package com.android_canbus_api.apps.strategylibrary._core.dispatchers;

import com.android_canbus_api.apps.controllerlibrary.CanDeviceController;
import com.android_canbus_api.apps.controllerlibrary.DeviceIoAction;
import com.android_canbus_api.apps.strategylibrary.strategies.pdu.LifeStrategy;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

import androidx.core.util.Consumer;

/**
 * Author:      Lee Yeung
 * Create Date: 2019-09-11
 * Description:
 */
public final class CanDispatcher extends ConcurrentLinkedQueue<TaskStrategy>
{
    private volatile boolean isLoop = true;
    private static final CanDispatcher CAN_DISPATCHER = new CanDispatcher();

    private final TaskStrategy lifeStrategy = new LifeStrategy((byte) 0x15);

    public static CanDispatcher getInstance()
    {
        return CAN_DISPATCHER;
    }

    public void loop()
    {
        CanDeviceController.getInstance().execute(new Consumer<DeviceIoAction>()
        {
            @Override
            public void accept(DeviceIoAction deviceIoAction)
            {
                finalRead(deviceIoAction);
                finalWrite(deviceIoAction);
            }
        });
    }

    private void finalRead(final DeviceIoAction deviceIoAction)
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                while (isLoop)
                {
                    try
                    {
                        // TODO: 2019-09-20 解析处理 
                        byte[] result = deviceIoAction.read();
                        

                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void finalWrite(final DeviceIoAction deviceIoAction)
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                while (isLoop)
                {
                    TaskStrategy ts = poll();
                    if (ts != null)
                    {
                        // TODO: 2019-09-19 分发主动can指令
                    }
                    // TODO: 2019-09-19 分发充电机生命帧指令
                    lifeStrategy.execute(deviceIoAction);
                }
            }
        }).start();
    }

    private void testPushRodStrategy()
    {

    }
}
