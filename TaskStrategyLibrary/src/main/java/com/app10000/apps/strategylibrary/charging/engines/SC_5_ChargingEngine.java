package com.app10000.apps.strategylibrary.charging.engines;

import android.os.SystemClock;

import com.app10000.apps.strategylibrary.dispatchers.DispatcherManager;
import com.app10000.apps.strategylibrary.strategies._base.NodeStrategy;
import com.app10000.apps.strategylibrary.strategies.battery.BatteryInfo;
import com.app10000.apps.strategylibrary.strategies.charging.ChargingStrategy;
import com.app10000.apps.strategylibrary.strategies.relay.OnRelaySwitchAction;
import com.app10000.apps.strategylibrary.strategies.relay.RelayCloseStrategy;
import com.app10000.apps.strategylibrary.strategies.relay.RelayOpenStrategy;

import androidx.core.util.Consumer;

/**
 * Author:      Lee Yeung
 * Create Date: 2019-11-05
 * Description:
 */
public class SC_5_ChargingEngine extends ChargingEngine
{
    private ChargingStrategy chargingStrategy1 = new ChargingStrategy((byte) (0x05 - 0x05 + 0x15));
    private ChargingStrategy chargingStrategy2 = new ChargingStrategy((byte) (0x08 - 0x05 + 0x15));
    private ChargingStrategy chargingStrategy3 = new ChargingStrategy((byte) (0x0B - 0x05 + 0x15));
    private long chargingTime1;
    private long chargingTime2;
    private long chargingTime3;

    public SC_5_ChargingEngine()
    {
        init();
    }

    private void init()
    {

    }

    @Override
    public void charging(BatteryInfo[] batteryInfos)
    {
        if (batteryInfos != null && batteryInfos.length == 9)
        {
            chargingCase_05_06_07(batteryInfos[0], batteryInfos[1], batteryInfos[2]);
            chargingCase_08_09_0A(batteryInfos[3], batteryInfos[4], batteryInfos[5]);
            chargingCase_0B_0C_0D(batteryInfos[6], batteryInfos[7], batteryInfos[8]);
        }
    }

    private void chargingCase_05_06_07(BatteryInfo _05BatteryInfo, BatteryInfo _06BatteryInfo, BatteryInfo _07BatteryInfo)
    {

        if (SystemClock.elapsedRealtime() < chargingTime1)
        {
            // TODO: 2019-11-18 充电时间还没到
            return;
        }

        // TODO: 2019-11-19 断点，关闭继电器
        RelayCloseStrategy relayCloseStrategy = new RelayCloseStrategy(chargingStrategy1.getAddress());
        relayCloseStrategy.setOnRelaySwitchAction(new OnRelaySwitchAction()
        {
            @Override
            public void onSwitchStatus(boolean isSuccessed)
            {
                chargingTime1 = 0;
            }
        });
        chargingStrategy1.addNext(relayCloseStrategy)
                .first()
                .call(new Consumer<NodeStrategy>()
                {
                    @Override
                    public void accept(NodeStrategy nodeStrategy)
                    {
                        DispatcherManager.getInstance().dispatch(nodeStrategy);
                    }
                });

        if (_05BatteryInfo != null && _05BatteryInfo.isLocked())
        {
            // TODO: 2019-11-18 先关闭当前正在充电的舱门
            chargingStrategy1.setBatterySpecification('0');
            //            chargingStrategy1.addNext(new RelayCloseStrategy(chargingStrategy1.a)

            final byte address = (byte) (_05BatteryInfo.address - 0x05 + 0x15);


            //可能已经休眠
            if (_05BatteryInfo.batteryTotalVoltage == 0)
            {
                chargingStrategy1.setAddress(address);
                chargingStrategy1.setBatterySpecification('M');
                chargingStrategy1.addPrevious(new RelayOpenStrategy(address));
                chargingStrategy1.first().call(new Consumer<NodeStrategy>()
                {
                    @Override
                    public void accept(NodeStrategy nodeStrategy)
                    {
                        DispatcherManager.getInstance().dispatch(nodeStrategy);
                    }
                });

                // TODO: 2019-11-13 充电1分钟
                chargingTime1 = SystemClock.elapsedRealtime() + 60 * 1000;
            } else if (_05BatteryInfo.relativeCapatityPercent < 80)
            {
                chargingStrategy1.setAddress(address);
                chargingStrategy1.setBatterySpecification(_05BatteryInfo.batteryIdInfo.charAt(0));
                chargingStrategy1.addPrevious(new RelayOpenStrategy(address));
                chargingStrategy1.first().call(new Consumer<NodeStrategy>()
                {
                    @Override
                    public void accept(NodeStrategy nodeStrategy)
                    {
                        DispatcherManager.getInstance().dispatch(nodeStrategy);
                    }
                });
                // TODO: 2019-11-13 充电15分钟
                chargingTime1 = SystemClock.elapsedRealtime() + 3 * 60 * 1000;
            }
        }
    }

    private void chargingCase_08_09_0A(BatteryInfo _08BatteryInfo, BatteryInfo _09BatteryInfo, BatteryInfo _0A_BatteryInfo)
    {

    }

    private void chargingCase_0B_0C_0D(BatteryInfo _0B_BatteryInfo, BatteryInfo _0C_BatteryInfo, BatteryInfo _0D_BatteryInfo)
    {

    }
}
