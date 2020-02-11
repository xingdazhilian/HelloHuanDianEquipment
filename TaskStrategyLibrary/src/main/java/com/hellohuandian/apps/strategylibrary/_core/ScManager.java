package com.hellohuandian.apps.strategylibrary._core;

import com.hellohuandian.apps.strategylibrary.config.MachineVersion;
import com.hellohuandian.apps.strategylibrary.dispatchers.DispatcherManager;

/**
 * Author:      Lee Yeung
 * Create Date: 2019-09-28
 * Description: SC是充电柜的缩写(来自文档)
 * 负责初始化串口和can通讯操作
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
        System.out.println("ScManager停止" + Thread.currentThread().getName());
        DispatcherManager.getInstance().stop();
    }
}
