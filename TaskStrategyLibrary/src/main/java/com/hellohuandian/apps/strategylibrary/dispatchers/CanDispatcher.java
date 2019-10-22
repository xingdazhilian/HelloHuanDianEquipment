package com.hellohuandian.apps.strategylibrary.dispatchers;

import com.hellohuandian.apps.controllerlibrary.CanDeviceController;
import com.hellohuandian.apps.controllerlibrary.DeviceIoAction;
import com.hellohuandian.apps.strategylibrary.dispatchers.canExtension.CanDeviceIoActionImpl;
import com.hellohuandian.apps.strategylibrary.strategies._base.NodeStrategy;
import com.hellohuandian.apps.strategylibrary.strategies._base.TaskStrategy;
import com.hellohuandian.apps.strategylibrary.strategies._data.BatteryData;
import com.hellohuandian.apps.strategylibrary.strategies.battery.BatteryDataStrategy;
import com.hellohuandian.apps.strategylibrary.strategies.battery.BatteryInfoTable;
import com.hellohuandian.apps.strategylibrary.strategies.battery.OnBatteryDataUpdate;
import com.hellohuandian.apps.strategylibrary.strategies.pdu.LifeStrategy;
import com.hellohuandian.apps.strategylibrary.strategies.pushRod.OnPushAction;
import com.hellohuandian.apps.strategylibrary.strategies.pushRod.PushRodStrategy;
import com.hellohuandian.apps.strategylibrary.strategies.relay.OnRelaySwitchAction;
import com.hellohuandian.apps.strategylibrary.strategies.relay.RelayCloseStrategy;
import com.hellohuandian.apps.strategylibrary.strategies.relay.RelayOpenStrategy;

import androidx.core.util.Consumer;

/**
 * Author:      Lee Yeung
 * Create Date: 2019-09-11
 * Description: can通讯分发器
 */
final class CanDispatcher extends TaskDispatcher<TaskStrategy>
{
    private volatile boolean isLoop;
    private static final CanDispatcher CAN_DISPATCHER = new CanDispatcher();
    private final TaskStrategy pduLifeStrategy = new LifeStrategy((byte) 0x15);
    private Thread canReadThread;
    private Thread canWriteThread;

    private CanDispatcher()
    {
    }

    public static CanDispatcher getInstance()
    {
        return CAN_DISPATCHER;
    }

    @Override
    protected void start()
    {
        if (!isLoop)
        {
            synchronized (this)
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
        }
    }

    @Override
    protected void stop()
    {
        if (isLoop)
        {
            isLoop = false;

            if (canReadThread != null)
            {
                if (canReadThread.isAlive())
                {
                    try
                    {
                        canReadThread.interrupt();
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
                canReadThread = null;
            }

            if (canWriteThread != null)
            {
                if (canWriteThread.isAlive())
                {
                    try
                    {
                        canWriteThread.interrupt();
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
                canWriteThread = null;
            }

            System.out.println("CAN分发线程停止isLoop：" + isLoop);
        }
    }

    @Override
    protected void watch(TaskStrategy taskStrategy)
    {
        dispatch(taskStrategy);
    }

    @Override
    protected void dispatch(TaskStrategy taskStrategy)
    {
        if (taskStrategy != null)
        {
            add(taskStrategy);
        }
    }

    private void CanDeviceIoActionImplWrapping(CanDeviceIoActionImpl deviceIoAction)
    {
        finalRead(deviceIoAction);
        finalWrite(deviceIoAction);
    }

    private void finalRead(final CanDeviceIoActionImpl canDeviceIoAction)
    {
        canReadThread = new Thread(new Runnable()
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
        });
        canReadThread.setName("Thread_CanRead");
        canReadThread.start();
    }

    private void finalWrite(final CanDeviceIoActionImpl deviceIoAction)
    {
        canWriteThread = new Thread(new Runnable()
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
                        ts.execute(deviceIoAction);
                    }
                    // TODO: 2019-09-19 分发充电机生命帧指令
                    pduLifeStrategy.execute(deviceIoAction);
                }
            }
        });
        canWriteThread.setName("Thread_CanWrite");
        canWriteThread.start();
    }

    private void testRelayOpenStrategy()
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    Thread.sleep(5000);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
                System.out.println("准备通路继电器...");
                RelayOpenStrategy relayOpenStrategy = new RelayOpenStrategy((byte) 0x15);
                relayOpenStrategy.setOnRelaySwitchAction(new OnRelaySwitchAction()
                {
                    @Override
                    public void onSwitchSuccessed(byte address)
                    {
                        System.out.println("继电器通路");
                    }

                    @Override
                    public void onSwitchFailed(byte address)
                    {

                    }
                });
                relayOpenStrategy.call(new Consumer<NodeStrategy>()
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
