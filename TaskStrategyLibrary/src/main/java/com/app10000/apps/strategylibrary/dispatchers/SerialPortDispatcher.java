package com.app10000.apps.strategylibrary.dispatchers;

import com.app10000.apps.controllerlibrary.DeviceIoAction;
import com.app10000.apps.controllerlibrary.SerialPortDeviceController;
import com.app10000.apps.strategylibrary.strategies._base.TaskStrategy;
import com.app10000.apps.strategylibrary.strategies.battery.BatteryDataStrategy;
import com.app10000.apps.strategylibrary.strategies.pushRod.OnPushAction;
import com.app10000.apps.strategylibrary.strategies.pushRod.PushRodStrategy;
import com.app10000.apps.strategylibrary.strategies.upgrade.battery.BatteryUpgradeStrategy;
import com.app10000.apps.strategylibrary.strategies.upgrade.battery.JieMinKe.JieMinKeBatteryUpgradeStrategy;

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
    private Thread serialPortDispatcherThread;

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
                    serialPortDispatcherThread = new Thread(new Runnable()
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
            clear();
            System.out.println("串口分发线程停止：" + isLoop);
            SerialPortDeviceController.getInstance().stop();

            if (serialPortDispatcherThread != null)
            {
                if (serialPortDispatcherThread.isAlive())
                {
                    try
                    {
                        serialPortDispatcherThread.interrupt();
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
                serialPortDispatcherThread = null;
            }
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
                "/HelloBMS19S_HW0101_FW0163_CRC62069E48.bin");

//        batteryUpgradeStrategy.setOnUpgradeProgress(new OnUpgradeProgress()
//        {
//            @Override
//            public void onUpgrade(BatteryUpgradeInfo batteryUpgradeInfo)
//            {
//                System.out.println("mapAddress:" + batteryUpgradeInfo.address);
//                System.out.println("statusFlag:" + batteryUpgradeInfo.statusFlag);
//                System.out.println("statusInfo:" + batteryUpgradeInfo.statusInfo);
//                System.out.println("currentPregress:" + batteryUpgradeInfo.currentPregress);
//                System.out.println("totalPregress:" + batteryUpgradeInfo.totalPregress);
//                if (batteryUpgradeInfo.totalPregress > 0)
//                {
//                    System.out.println((int) ((float) batteryUpgradeInfo.currentPregress / batteryUpgradeInfo.totalPregress * 100) + "%");
//                }
//            }
//        });
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
            public void onPushed(boolean isSuccessed)
            {

            }
        });
        dispatch(pushRodStrategy);
    }
}

