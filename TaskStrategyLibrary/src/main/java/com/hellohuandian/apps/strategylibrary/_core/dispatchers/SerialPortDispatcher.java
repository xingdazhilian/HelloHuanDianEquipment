package com.hellohuandian.apps.strategylibrary._core.dispatchers;

import com.hellohuandian.apps.controllerlibrary.DeviceIoAction;
import com.hellohuandian.apps.controllerlibrary.DeviceIoController;
import com.hellohuandian.apps.strategylibrary.strategies.upgrade.battery.JieMinKe.JieMinKeBatteryUpgradeStrategy;
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
            DeviceIoController.getInstance().execute(new Consumer<DeviceIoAction>()
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
        while (true)
        {
            TaskStrategy ts = poll();
            if (ts != null)
            {
                dispatch(ts);
            } else
            {
                // TODO: 2019-09-16 执行485策略任务
                //dispatch(new BatteryInfoStrategy((byte) 0x05));

                JieMinKeBatteryUpgradeStrategy jieMinKeBatteryUpgradeStrategy = new JieMinKeBatteryUpgradeStrategy((byte) 0x05, "/sdcard" +
                        "/HelloBMS19S_HW0101_FW0161_CRC399D90C3_BT00000000.bin");
                jieMinKeBatteryUpgradeStrategy.setOnUpgradeProgress(new OnUpgradeProgress()
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
                            System.out.println(((float) currentPregress / currentPregress) + "%");
                        }
                    }
                });
                dispatch(jieMinKeBatteryUpgradeStrategy);
                break;
            }
        }
    }
}
