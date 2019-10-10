package com.hellohuandian.apps.strategylibrary._core;

import com.hellohuandian.apps.strategylibrary.config.MachineVersion;
import com.hellohuandian.apps.strategylibrary.dispatchers.DispatcherManager;

/**
 * Author:      Lee Yeung
 * Create Date: 2019-09-28
 * Description:
 */
public final class ScManager
{
    private static final ScManager DISPATCHER_MANAGER = new ScManager();

    private ScManager()
    {
    }

    public static ScManager getInstance()
    {
        return DISPATCHER_MANAGER;
    }

    public void init(@MachineVersion int version)
    {
        DispatcherManager.getInstance().init(version);
    }

    public void start()
    {
        DispatcherManager.getInstance().start();
    }

    public void stop()
    {
        DispatcherManager.getInstance().stop();
    }
}
