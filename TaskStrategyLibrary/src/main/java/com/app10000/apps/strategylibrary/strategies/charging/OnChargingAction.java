package com.app10000.apps.strategylibrary.strategies.charging;

/**
 * Author:      Lee Yeung
 * Create Date: 2019-11-18
 * Description:
 */
public interface OnChargingAction
{
    void onChargingSuccessed(byte address);

    void onChargingFailed(byte address);
}
