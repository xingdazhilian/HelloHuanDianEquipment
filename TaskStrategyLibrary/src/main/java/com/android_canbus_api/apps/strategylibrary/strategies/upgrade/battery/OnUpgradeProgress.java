package com.android_canbus_api.apps.strategylibrary.strategies.upgrade.battery;

/**
 * Author:      Lee Yeung
 * Create Date: 2019-09-07
 * Description:
 */
public interface OnUpgradeProgress
{
    void onUpgrade(byte mapAddress, byte statusFlag, String statusInfo, long currentPregress, long totalPregress);
}
