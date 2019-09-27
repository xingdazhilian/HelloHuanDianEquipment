package com.hellohuandian.apps.strategylibrary._core.dispatchers;

import com.hellohuandian.apps.controllerlibrary.DeviceIoAction;
import com.hellohuandian.apps.controllerlibrary.SerialPortDeviceController;
import com.hellohuandian.apps.strategylibrary.strategies.battery.BatteryDataStrategy;
import com.hellohuandian.apps.strategylibrary.strategies.pushRod.OnPushAction;
import com.hellohuandian.apps.strategylibrary.strategies.pushRod.PushRodStrategy;
import com.hellohuandian.apps.strategylibrary.strategies.upgrade.battery.BatteryUpgradeStrategy;
import com.hellohuandian.apps.strategylibrary.strategies.upgrade.battery.JieMinKe.JieMinKeBatteryUpgradeStrategy;
import com.hellohuandian.apps.strategylibrary.strategies.upgrade.battery.NuoWan.NuoWanBatteryUpgradeStrategy;
import com.hellohuandian.apps.strategylibrary.strategies.upgrade.battery.OnUpgradeProgress;

import java.util.concurrent.ConcurrentLinkedQueue;

import androidx.core.util.Consumer;

/**
 * Author:      Lee Yeung
 * Create Date: 2019-09-11
 * Description: 串口数据分发器
 */
public final class SerialPortDispatcher extends ConcurrentLinkedQueue<TaskStrategy>
{
    private static final SerialPortDispatcher SERIAL_PORT_DISPATCHER = new SerialPortDispatcher();

    public static SerialPortDispatcher getInstance()
    {
        return SERIAL_PORT_DISPATCHER;
    }

    public void dispatch(final TaskStrategy taskStrategy)
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

    public void loop()
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                while (true)
                {
                    TaskStrategy ts = poll();
                    if (ts != null)
                    {
                        dispatch(ts);
                    } else
                    {
                        // TODO: 2019-09-16 执行485策略任务
                        testBatteryUpgradeStrategy();
                        break;
                    }
                }
            }
        }).start();
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
        BatteryUpgradeStrategy batteryUpgradeStrategy = new NuoWanBatteryUpgradeStrategy((byte) 0x05, "/sdcard" +
                "/19_V255.bin");

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

