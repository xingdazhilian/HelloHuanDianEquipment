package com.hellohuandian.apps.strategylibrary.config;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.IntDef;

/**
 * Author:      Lee Yeung
 * Create Date: 2019-09-28
 * Description: 定义机器版本
 */
@IntDef({MachineVersion.SC_1, MachineVersion.SC_2, MachineVersion.SC_3, MachineVersion.SC_4})
@Retention(RetentionPolicy.SOURCE)
public @interface MachineVersion
{
    int SC_1 = 1;//一代：通讯485，没有PDU(充电机：电池=12：12)
    int SC_2 = 2;//二代：通讯CAN，有PDU(充电机：电池=1：3)
    int SC_3 = 3;//三代：通讯CAN，有PDU(充电机：电池=1：3)
    int SC_4 = 4;//四代：通讯CAN，有PDU(充电机：电池=1：1)
}
