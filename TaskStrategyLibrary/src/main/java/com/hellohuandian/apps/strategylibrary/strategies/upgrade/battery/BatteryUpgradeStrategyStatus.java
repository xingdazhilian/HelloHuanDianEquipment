package com.hellohuandian.apps.strategylibrary.strategies.upgrade.battery;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.IntDef;

/**
 * Author:      Lee Yeung
 * Create Date: 2019-10-11
 * Description:
 */
@IntDef({BatteryUpgradeStrategyStatus.WAITTING,
        BatteryUpgradeStrategyStatus.BOOT_LOADER_MODE,
        BatteryUpgradeStrategyStatus.INIT_FIRMWARE_DATA,
        BatteryUpgradeStrategyStatus.WRITE_DATA,
        BatteryUpgradeStrategyStatus.ACTION_BMS,
        BatteryUpgradeStrategyStatus.FAILED,
        BatteryUpgradeStrategyStatus.SUCCESSED,
})
@Retention(RetentionPolicy.SOURCE)
public @interface BatteryUpgradeStrategyStatus
{
    int WAITTING = 2;//等待485重置
    int BOOT_LOADER_MODE = 4;//进入BootLoader模式
    int INIT_FIRMWARE_DATA = 5;//初始化固件数据
    int WRITE_DATA = 6;//写入升级数据
    int ACTION_BMS = 7;//立即激活新BMS程序
    int FAILED = 8;//升级失败
    int SUCCESSED = 9;//升级成功
}
