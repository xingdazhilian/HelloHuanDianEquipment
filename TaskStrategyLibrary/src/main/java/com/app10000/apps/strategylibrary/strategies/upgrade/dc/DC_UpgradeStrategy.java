package com.app10000.apps.strategylibrary.strategies.upgrade.dc;

import com.app10000.apps.strategylibrary.strategies._base.ProtocolStrategy;

/**
 * Author:      Lee Yeung
 * Create Date: 2019-12-20
 * Description:
 */
public abstract class DC_UpgradeStrategy extends ProtocolStrategy
{
    protected final byte androidAddress = (byte) 0xE0;//android上位机地址

    protected String upgradeFilePath;

    public DC_UpgradeStrategy(byte address)
    {
        super(address);
    }

    public void setUpgradeFilePath(String upgradeFilePath)
    {
        this.upgradeFilePath = upgradeFilePath;
    }
}
