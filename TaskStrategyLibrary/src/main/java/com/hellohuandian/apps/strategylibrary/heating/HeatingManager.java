package com.hellohuandian.apps.strategylibrary.heating;

import com.hellohuandian.apps.strategylibrary._core.FuncManager;

/**
 * Author:      Lee Yeung
 * Create Date: 2019-10-12
 * Description: 加热管理器
 */
public class HeatingManager extends FuncManager
{
    private static final HeatingManager HEATING_MANAGER = new HeatingManager();

    private HeatingManager()
    {
    }

    public static HeatingManager getInstance()
    {
        return HEATING_MANAGER;
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
