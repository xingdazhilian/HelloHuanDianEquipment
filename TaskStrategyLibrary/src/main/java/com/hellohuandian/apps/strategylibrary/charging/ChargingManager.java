package com.hellohuandian.apps.strategylibrary.charging;

import com.hellohuandian.apps.strategylibrary._core.FuncManager;

/**
 * Author:      Lee Yeung
 * Create Date: 2019-10-12
 * Description: 充电管理器
 */
public class ChargingManager extends FuncManager
{
    private static final ChargingManager CHARGING_MANAGER = new ChargingManager();
    private ChargingManager()
    {
    }

    public static ChargingManager getInstance()
    {
        return CHARGING_MANAGER;
    }

    @Override
    public void start()
    {

    }

    @Override
    public void stop()
    {

    }
}
