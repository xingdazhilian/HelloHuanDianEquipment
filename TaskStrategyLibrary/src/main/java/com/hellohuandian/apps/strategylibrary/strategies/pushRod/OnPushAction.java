package com.hellohuandian.apps.strategylibrary.strategies.pushRod;

/**
 * Author:      Lee Yeung
 * Create Date: 2019-09-18
 * Description:
 */
public interface OnPushAction
{
    void onPushSuccessed(byte address);

    void onPushFailed(byte address);
}
