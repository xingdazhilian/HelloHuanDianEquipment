package com.app10000.apps.strategylibrary.strategies.upgrade.dc;

import android.text.TextUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Author:      Lee Yeung
 * Create Date: 2019-12-20
 * Description: DC模块升级支持类，维持单个或者多个策略在单线程中的顺序执行。
 */
public final class DC_Upgrade_Supporter
{
    private static volatile DC_Upgrade_Supporter dc_upgrade_supporter;
    private HashMap<String, byte[]> upgradeDataMap = new HashMap<>();
    private LinkedList<UpgradeConsumer> upgradeConsumerLinkedList = new LinkedList<>();
    private volatile boolean isRun = true;

    private final Object lock = new Object();

    private DC_Upgrade_Supporter()
    {
        init();
    }

    public static DC_Upgrade_Supporter getInstance()
    {
        if (dc_upgrade_supporter == null)
        {
            synchronized (DC_Upgrade_Supporter.class)
            {
                if (dc_upgrade_supporter == null)
                {
                    dc_upgrade_supporter = new DC_Upgrade_Supporter();
                }
            }
        }

        return dc_upgrade_supporter;
    }

    private void init()
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                while (isRun)
                {
                    synchronized (lock)
                    {
                        UpgradeConsumer upgradeConsumer = upgradeConsumerLinkedList.poll();
                        if (upgradeConsumer != null)
                        {
                            if (!upgradeDataMap.containsKey(upgradeConsumer.getUpgradeFilePath()))
                            {
                                addData(upgradeConsumer.getUpgradeFilePath());
                            }
                            upgradeConsumer.accept(upgradeDataMap.get(upgradeConsumer.getUpgradeFilePath()));
                        } else
                        {
                            try
                            {
                                lock.wait();
                            }
                            catch (InterruptedException e)
                            {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }).start();
    }

    private void clear()
    {
        if (isRun)
        {
            synchronized (lock)
            {
                if (isRun)
                {
                    isRun = false;
                    upgradeConsumerLinkedList.clear();
                    upgradeDataMap.clear();
                    lock.notify();
                }
            }
        }
    }

    public static synchronized void stop()
    {
        if (dc_upgrade_supporter != null)
        {
            dc_upgrade_supporter.clear();
            dc_upgrade_supporter = null;
        }
    }

    public void register(UpgradeConsumer upgradeConsumer)
    {
        synchronized (lock)
        {
            if (upgradeConsumer != null && !TextUtils.isEmpty(upgradeConsumer.getUpgradeFilePath()))
            {
                upgradeConsumerLinkedList.add(upgradeConsumer);
                lock.notify();
            }
        }
    }

    private void addData(String filePath)
    {
        File file = new File(filePath);
        if (!(file != null && file.exists()))
        {
            System.out.println("pdu升级文件可能不存在！");
            return;
        }

        InputStream inputStream = null;
        try
        {
            inputStream = new FileInputStream(file);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }

        if (inputStream == null)
        {
            return;
        }

        int len = 0;
        try
        {
            len = inputStream.available();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        if (len <= 0)
        {
            return;
        }

        final byte[] DATA = new byte[len];
        try
        {
            inputStream.read(DATA);
            inputStream.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        upgradeDataMap.put(filePath, DATA);
    }
}
