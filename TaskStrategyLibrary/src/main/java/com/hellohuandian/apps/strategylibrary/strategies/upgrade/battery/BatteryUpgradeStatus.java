package com.hellohuandian.apps.strategylibrary.strategies.upgrade.battery;

/**
 * Author:      Lee Yeung
 * Create Date: 2019-09-05
 * Description: 升级状态
 */
public final class BatteryUpgradeStatus
{
    public static final byte WAITTING = 2;//等待485重置
    public static final byte BOOT_LOADER_MODE = 4;//进入BootLoader模式
    public static final byte INIT_FIRMWARE_DATA = 5;//初始化固件数据
    public static final byte WRITE_DATA = 6;//写入升级数据
    public static final byte ACTION_BMS = 7;//立即激活新BMS程序
    public static final byte FAILED = 8;//升级失败
    public static final byte SUCCESSED = 9;//升级成功
}