package com.hellohuandian.apps.strategylibrary.strategies.upgrade.dc;

import androidx.core.util.Consumer;

/**
 * Author:      Lee Yeung
 * Create Date: 2019-12-20
 * Description:
 */
public abstract class UpgradeConsumer implements Consumer<byte[]>
{
    private final String upgradeFilePath;

    public UpgradeConsumer(String upgradeFilePath)
    {
        this.upgradeFilePath = upgradeFilePath;
    }

    public String getUpgradeFilePath()
    {
        return upgradeFilePath;
    }
}
