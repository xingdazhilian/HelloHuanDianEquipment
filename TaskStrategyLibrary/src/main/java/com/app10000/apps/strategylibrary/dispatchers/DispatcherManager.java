package com.app10000.apps.strategylibrary.dispatchers;

import com.app10000.apps.strategylibrary.config.MachineVersion;
import com.app10000.apps.strategylibrary.strategies._base.TaskStrategy;
import com.app10000.apps.strategylibrary.strategies._data.BatteryData;
import com.app10000.apps.strategylibrary.strategies.battery.BatteryDataStrategy;
import com.app10000.apps.strategylibrary.strategies.battery.BatteryInfoTable;
import com.app10000.apps.strategylibrary.strategies.battery.OnBatteryDataUpdate;
import com.app10000.apps.strategylibrary.strategies.lifes.DC_LifeStrategy;
import com.app10000.apps.strategylibrary.strategies.lifes.Pdu_LifeStrategy;
import com.app10000.apps.strategylibrary.strategies.upgrade.battery.BatteryUpgradeStrategy;

/**
 * Author:      Lee Yeung
 * Create Date: 2019-09-28
 * Description: 分发管理器，负责匹配策略分发
 */
public final class DispatcherManager
{
    private static final DispatcherManager DISPATCHER_MANAGER = new DispatcherManager();

    private TaskDispatcher taskDispatcher;
    private TaskDispatcher batteryUpgradeDispatcher;
    private OnBatteryDataUpdate mOnBatteryDataUpdate;

    private OnBatteryDataUpdate onBatteryDataUpdate = new OnBatteryDataUpdate()
    {
        @Override
        public void onUpdate(BatteryData batteryData)
        {
            if (mOnBatteryDataUpdate != null)
            {
                mOnBatteryDataUpdate.onUpdate(batteryData);
            }
        }
    };

    private DispatcherManager()
    {
    }

    public static DispatcherManager getInstance()
    {
        return DISPATCHER_MANAGER;
    }

    /**
     * 初始化
     *
     * @param version 机器版本
     */
    public void init(@MachineVersion int version)
    {
        byte startAddress = 0;
        int size = 0;
        switch (version)
        {
            case MachineVersion.SC_3:
                startAddress = 0x01;
                size = 12;
                taskDispatcher = SerialPortDispatcher.getInstance();
                // TODO: 2019-09-28 仓数地址1~12(0x01~0x0C)
                break;
            case MachineVersion.SC_4:
            case MachineVersion.SC_5:
                startAddress = 0x05;
                size = 9;
                // TODO: 2019-09-28 仓数地址1~9(0x05~0x0D)
                taskDispatcher = CanDispatcher.getInstance();
                batteryUpgradeDispatcher = SerialPortDispatcher.getInstance();
                ((CanDispatcher) taskDispatcher).initLifeStrategy(new Pdu_LifeStrategy());
                break;
            case MachineVersion.SC_6:
                startAddress = 0x01;
                size = 9;
                // TODO: 2019-09-28 仓数地址1~9(0x05~0x0D)
                taskDispatcher = CanDispatcher.getInstance();
                batteryUpgradeDispatcher = taskDispatcher;
                ((CanDispatcher) taskDispatcher).initLifeStrategy(new DC_LifeStrategy());
                break;
        }

        // TODO: 2019-09-29 设置观察策略任务
        if (taskDispatcher != null && size > 0)
        {
            BatteryInfoTable batteryInfoTable = new BatteryInfoTable();
            BatteryDataStrategy batteryDataStrategy;
            for (int i = 0; i < size; i++, startAddress++)
            {
                batteryDataStrategy = new BatteryDataStrategy(startAddress, version);
                batteryDataStrategy.setBatteryInfoTable(batteryInfoTable);
                batteryDataStrategy.setOnBatteryDataUpdate(onBatteryDataUpdate);
                taskDispatcher.watch(batteryDataStrategy);
            }
        }
    }

    public void setOnBatteryDataUpdate(OnBatteryDataUpdate onBatteryDataUpdate)
    {
        mOnBatteryDataUpdate = onBatteryDataUpdate;
    }

    public void start()
    {
        if (taskDispatcher != null)
        {
            taskDispatcher.start();
        }
    }

    public void stop()
    {
        if (taskDispatcher != null)
        {
            taskDispatcher.stop();
            SerialPortDispatcher.getInstance().stop();
        }
    }

    /**
     * 执行分发策略
     *
     * @param taskStrategy
     */
    public void dispatch(TaskStrategy taskStrategy)
    {
        if (taskStrategy instanceof BatteryUpgradeStrategy)
        {
            if (batteryUpgradeDispatcher != null)
            {
                BatteryUpgradeStrategy batteryUpgradeStrategy = (BatteryUpgradeStrategy) taskStrategy;
                final OnBatteryDataUpdate innerOnBatteryDataUpdate = batteryUpgradeStrategy.getOnBatteryDataUpdate();
                batteryUpgradeStrategy.setOnBatteryDataUpdate(new OnBatteryDataUpdate()
                {
                    @Override
                    public void onUpdate(BatteryData batteryData)
                    {
                        // TODO: 2019-10-23 升级信息先发送给监视器去执行过滤
                        if (onBatteryDataUpdate != null)
                        {
                            onBatteryDataUpdate.onUpdate(batteryData);
                        }
                        // TODO: 2019-10-23 执行真实的接口对象回调
                        if (innerOnBatteryDataUpdate != null)
                        {
                            innerOnBatteryDataUpdate.onUpdate(batteryData);
                        }
                    }
                });

                batteryUpgradeDispatcher.dispatch(taskStrategy);
            }
        } else
        {
            taskDispatcher.dispatch(taskStrategy);
        }
    }
}
