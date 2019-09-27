package com.hellohuandian.apps.strategylibrary.strategies.relay;

/**
 * Author:      Lee Yeung
 * Create Date: 2019-09-26
 * Description:
 */
public interface OnRelaySwitchAction
{
    void onSwitchSuccessed(byte address);

    void onSwitchFailed(byte address);
}
