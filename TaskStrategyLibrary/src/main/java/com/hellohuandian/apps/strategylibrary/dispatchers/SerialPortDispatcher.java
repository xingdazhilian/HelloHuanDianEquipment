package com.hellohuandian.apps.strategylibrary.dispatchers;

import com.hellohuandian.apps.controllerlibrary.DeviceIoAction;
import com.hellohuandian.apps.controllerlibrary.SerialPortDeviceController;
import com.hellohuandian.apps.strategylibrary.strategies._base.TaskStrategy;
import com.hellohuandian.apps.strategylibrary.strategies.battery.BatteryDataStrategy;
import com.hellohuandian.apps.strategylibrary.strategies.pushRod.OnPushAction;
import com.hellohuandian.apps.strategylibrary.strategies.pushRod.PushRodStrategy;
import com.hellohuandian.apps.strategylibrary.strategies.upgrade.battery.BatteryUpgradeStrategy;
import com.hellohuandian.apps.strategylibrary.strategies.upgrade.battery.JieMinKe.JieMinKeBatteryUpgradeStrategy;
import com.hellohuandian.apps.strategylibrary.strategies.upgrade.battery.OnUpgradeProgress;

import java.util.ArrayList;

import androidx.core.util.Consumer;

/**
 * Author:      Lee Yeung
 * Create Date: 2019-09-11
 * Description: 串口通讯分发器
 */
final class SerialPortDispatcher extends TaskDispatcher<TaskStrategy>
{
    private int sn;
    private volatile boolean isLoop;
    private static final SerialPortDispatcher SERIAL_PORT_DISPATCHER = new SerialPortDispatcher();
    private final ArrayList<TaskStrategy> watchList = new ArrayList<>();

    private SerialPortDispatcher()
    {
    }

    public static SerialPortDispatcher getInstance()
    {
        return SERIAL_PORT_DISPATCHER;
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
                    Thread serialPortDispatcherThread = new Thread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            while (isLoop)
                            {
                                TaskStrategy ts = poll();
                                if (ts != null)
                                {
                                    execute(ts);
                                } else
                                {
                                    if (watchList.isEmpty())
                                    {
                                        // TODO: 2019-10-10 针对在can通讯下进行电池升级任务，任务结束后，队列没有新任务，同时没有观察策略，就执行停止操作
                                        stop();
                                    } else
                                    {
                                        // TODO: 2019-09-16 执行485观察策略
                                        watch();
                                    }
                                }
                            }
                        }
                    });
                    serialPortDispatcherThread.setName("Thread-SerialPortDispatcher");
                    serialPortDispatcherThread.start();
                    System.out.println("串口分发线程启动isLoop：" + isLoop);
                }
            }
        }
    }

    private void execute(final TaskStrategy taskStrategy)
    {
        if (taskStrategy != null)
        {
            SerialPortDeviceController.getInstance().execute(new Consumer<DeviceIoAction>()
            {
                @Override
                public void accept(DeviceIoAction deviceIoAction)
                {
                    taskStrategy.execute(deviceIoAction);
                }
            });
        }
    }

    @Override
    protected void stop()
    {
        if (isLoop)
        {
            isLoop = false;
            System.out.println("串口分发线程停止isLoop：" + isLoop);
        }
    }

    @Override
    protected void watch(TaskStrategy taskStrategy)
    {
        watchList.add(taskStrategy);
    }

    @Override
    protected void dispatch(final TaskStrategy taskStrategy)
    {
        if (taskStrategy != null)
        {
            if (!isLoop)
            {
                start();
            }
            add(taskStrategy);
        }
    }

    private void watch()
    {
        if (!watchList.isEmpty())
        {
            sn = sn % watchList.size();
            dispatch(watchList.get(sn++));
        }
    }

    private void testBatteryInfoStrategy()
    {
        // TODO: 2019-09-18 测试数据包
        dispatch(new BatteryDataStrategy((byte) 0x05));
    }

    private void testBatteryUpgradeStrategy()
    {
        // TODO: 2019-09-18 测试升级
        //        BatteryUpgradeStrategy batteryUpgradeStrategy = new JieMinKeBatteryUpgradeStrategy((byte) 0x05, "/sdcard" +
        //                "/HelloBMS19S_HW0101_FW0161_CRC399D90C3_BT00000000.bin");
        //        BatteryUpgradeStrategy batteryUpgradeStrategy = new NuoWanBatteryUpgradeStrategy((byte) 0x05, "/sdcard" +
        //                "/
        BatteryUpgradeStrategy batteryUpgradeStrategy = new JieMinKeBatteryUpgradeStrategy((byte) 0x05, "/sdcard/Download" +
                "/HelloBMS19S_HW0101_FW0164_CRCDBE1851D.bin");

        batteryUpgradeStrategy.setOnUpgradeProgress(new OnUpgradeProgress()
        {
            @Override
            public void onUpgrade(byte mapAddress, byte statusFlag, String statusInfo, long currentPregress, long totalPregress)
            {
                System.out.println("mapAddress:" + mapAddress);
                System.out.println("statusFlag:" + statusFlag);
                System.out.println("statusInfo:" + statusInfo);
                System.out.println("currentPregress:" + currentPregress);
                System.out.println("totalPregress:" + totalPregress);
                if (totalPregress > 0)
                {
                    System.out.println((int) ((float) currentPregress / totalPregress * 100) + "%");
                }
            }
        });
        dispatch(batteryUpgradeStrategy);
    }

    private void testPushRodStrategy()
    {
        try
        {
            System.out.println("10秒后开始推杆");
            Thread.sleep(10000);
            System.out.println("开始推杆...");
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        PushRodStrategy pushRodStrategy = new PushRodStrategy((byte) 0x05);
        pushRodStrategy.setOnPushAction(new OnPushAction()
        {
            @Override
            public void onPushSuccessed(byte address)
            {
                System.out.println("推杆成功");
            }

            @Override
            public void onPushFailed(byte address)
            {
                System.out.println("推杆失败");
            }
        });
        dispatch(pushRodStrategy);
    }
}

