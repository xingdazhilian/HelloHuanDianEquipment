package com.app10000.apps.strategylibrary.config;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.IntDef;

/**
 * Author:      Lee Yeung
 * Create Date: 2019-09-28
 * Description: 定义机器版本
 */
@IntDef({MachineVersion.SC_3, MachineVersion.SC_4, MachineVersion.SC_5, MachineVersion.SC_5_1, MachineVersion.SC_6})
@Retention(RetentionPolicy.SOURCE)
public @interface MachineVersion
{
    int SC_3 = 3;//三代：通讯485，没有PDU(充电机：电池=12：12)
    int SC_4 = 4;//四代：通讯CAN，有PDU(充电机：电池=1：3)
    int SC_5 = 5;//五代：通讯CAN，有PDU(充电机：电池=1：3)
    int SC_5_1 = 6;//五代：通讯CAN，有PDU(充电机：电池=1：3),增加一个充电机
    int SC_6 = 7;//
}
