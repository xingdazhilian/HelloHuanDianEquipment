package com.hellohuandian.apps.strategylibrary.strategies._data;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.IntDef;

/**
 * Author:      Lee Yeung
 * Create Date: 2019-09-17
 * Description: 电池数据基本类
 */
public abstract class BatteryData
{
    @IntDef({BatteryDataType.INFO, BatteryDataType.UPGRADE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface BatteryDataType
    {
        int INFO = 10;
        int UPGRADE = 20;
    }

    public final byte address;

    public BatteryData(byte address)
    {
        this.address = address;
    }

    public abstract @BatteryDataType int getBatteryDataType();
}
