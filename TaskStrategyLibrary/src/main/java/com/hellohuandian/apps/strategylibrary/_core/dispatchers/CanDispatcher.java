package com.hellohuandian.apps.strategylibrary._core.dispatchers;

import com.hellohuandian.apps.controllerlibrary.CanDeviceController;
import com.hellohuandian.apps.controllerlibrary.DeviceIoAction;
import com.hellohuandian.apps.strategylibrary._core.dispatchers.canExtension.CanDeviceIoActionImpl;
import com.hellohuandian.apps.strategylibrary.strategies._base.NodeStrategy;
import com.hellohuandian.apps.strategylibrary.strategies.battery.BatteryData;
import com.hellohuandian.apps.strategylibrary.strategies.battery.BatteryDataStrategy;
import com.hellohuandian.apps.strategylibrary.strategies.battery.BatteryInfoTable;
import com.hellohuandian.apps.strategylibrary.strategies.battery.OnBatteryDataUpdate;
import com.hellohuandian.apps.strategylibrary.strategies.pdu.LifeStrategy;
import com.hellohuandian.apps.strategylibrary.strategies.pushRod.OnPushAction;
import com.hellohuandian.apps.strategylibrary.strategies.pushRod.PushRodStrategy;
import com.hellohuandian.apps.strategylibrary.strategies.relay.OnRelaySwitchAction;
import com.hellohuandian.apps.strategylibrary.strategies.relay.RelayCloseStrategy;

import java.util.concurrent.ConcurrentLinkedQueue;

import androidx.core.util.Consumer;

/**
 * Author:      Lee Yeung
 * Create Date: 2019-09-11
 * Description:
 */
public final class CanDispatcher extends ConcurrentLinkedQueue<TaskStrategy>
{
    private volatile boolean isLoop;
    private static final CanDispatcher CAN_DISPATCHER = new CanDispatcher();

    private final TaskStrategy lifeStrategy = new LifeStrategy((byte) 0x15);

    public static CanDispatcher getInstance()
    {
        return CAN_DISPATCHER;
    }

    public synchronized void loop()
    {
        if (!isLoop)
        {
            isLoop = true;
            CanDeviceController.getInstance().execute(new Consumer<DeviceIoAction>()
            {
                @Override
                public void accept(DeviceIoAction deviceIoAction)
                {
                    CanDeviceIoActionImplWrapping(new CanDeviceIoActionImpl(deviceIoAction));
                }
            });
        }
    }

    private void CanDeviceIoActionImplWrapping(CanDeviceIoActionImpl deviceIoAction)
    {
        finalRead(deviceIoAction);
        finalWrite(deviceIoAction);
    }

    private void finalRead(final CanDeviceIoActionImpl canDeviceIoAction)
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                while (isLoop)
                {
                    // TODO: 2019-09-20 解析处理
                    canDeviceIoAction.parseDispatch();
                }
            }
        }).start();
    }

    private void finalWrite(final CanDeviceIoActionImpl deviceIoAction)
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                //                initBatteryDataStrategies(deviceIoAction);

                while (isLoop)
                {
                    TaskStrategy ts = poll();
                    if (ts != null)
                    {
                        // TODO: 2019-09-19 分发主动can指令
                        ts.execute(deviceIoAction);
                    }
                    // TODO: 2019-09-19 分发充电机生命帧指令
                    lifeStrategy.execute(deviceIoAction);
                }
            }
        }).start();

        testPushRodStrategy();
    }

    private void initBatteryDataStrategies(final CanDeviceIoActionImpl deviceIoAction)
    {
        BatteryInfoTable batteryInfoTable = new BatteryInfoTable();
        TaskStrategy taskStrategy;
        for (int i = 5; i <= 5; i++)
        {
            taskStrategy = new BatteryDataStrategy((byte) i);
            ((BatteryDataStrategy) taskStrategy).setBatteryInfoTable(batteryInfoTable);
            ((BatteryDataStrategy) taskStrategy).setOnBatteryDataUpdate(new OnBatteryDataUpdate()
            {
                @Override
                public void onUpdate(BatteryData batteryData)
                {
                    System.out.println(batteryData.toString());
                }
            });
            taskStrategy.execute(deviceIoAction);
        }
    }

    private void testPushRodStrategy()
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    Thread.sleep(10000);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
                RelayCloseStrategy relayCloseStrategy = new RelayCloseStrategy((byte) 0x15);
                relayCloseStrategy.setOnRelaySwitchAction(new OnRelaySwitchAction()
                {
                    @Override
                    public void onSwitchSuccessed(byte address)
                    {
                        System.out.println("继电器短路成功：" + address);
                    }

                    @Override
                    public void onSwitchFailed(byte address)
                    {
                        System.out.println("继电器短路失败：" + address);
                    }
                });
                PushRodStrategy pushRodStrategy = new PushRodStrategy((byte) 0x05);
                pushRodStrategy.setOnPushAction(new OnPushAction()
                {
                    @Override
                    public void onPushSuccessed(byte address)
                    {
                        System.out.println("推杆执行完成：" + address);
                    }

                    @Override
                    public void onPushFailed(byte address)
                    {
                        System.out.println("推杆执行失败：" + address);
                    }
                });
                relayCloseStrategy.addNext(pushRodStrategy)
                        .first().call(new Consumer<NodeStrategy>()
                {
                    @Override
                    public void accept(NodeStrategy nodeStrategy)
                    {
                        add(nodeStrategy);
                    }
                });

            }
        }).start();
    }
}
