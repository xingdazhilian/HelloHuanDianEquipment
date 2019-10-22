package com.hellohuandian.apps.strategylibrary.strategies.battery;

import android.text.TextUtils;

import com.hellohuandian.apps.strategylibrary.strategies._data.BatteryData;

import androidx.annotation.NonNull;

/**
 * Author:      Lee Yeung
 * Create Date: 2019-10-11
 * Description: 电池数据信息类
 */
public class BatteryInfo extends BatteryData
{
    //侧微动锁
    public byte doorSideLockStatus;
    //底微动锁
    public byte doorBottomLockStatus;
    public byte electricMachineStatus;
    public float batteryTemperature;
    public int batteryTotalVoltage;
    public int realTimeCurrent;
    public int relativeCapatityPercent;
    public int absoluteCapatityPercent;
    public int remainingCapatity;
    public int fullCapatity;
    public int loopCount;
    public String batteryVoltage_1_7;
    public String batteryVoltage_8_15;
    public int soh;
    public String batteryIdInfo;
    public int softwareVersion;
    public int hardwareVersion;
    public int controlPanelTemperature;
    public int controlPanelVersion;

    public String str_doorSideLockStatus = "无电池";
    public String str_doorBottomLockStatus = "无电池";
    public String str_electricMachineStatus;
    public String str_batteryTemperature = "--°C";
    public String str_batteryTotalVoltage = "0mV";
    public String str_realTimeCurrent = "0mA";
    public String str_relativeCapatityPercent = "0%";
    public String str_absoluteCapatityPercent = "0%";
    public String str_remainingCapatity = "0mAh";
    public String str_fullCapatity = "0mAh";
    public String str_loopCount = "0";
    //    public String batteryVoltage_1_7;
    //    public String batteryVoltage_8_15;
    public String str_soh;
    //    public String manufacturer;
    public String str_controlPanelTemperature = "--°C";

    // TODO: 2019-09-26 附加的字段
    public String _battery_capacity_specification;//电池容量规格
    public String _BMS_manufacturer;//BMS生产厂家
    public String _pack_manufacturer;//Pack生产厂家
    public String _production_line_identification_code;//生产线识别码
    public String _battery_manufacturer;//电芯生产厂家
    public String _year;
    public String _month;
    public String _day;

    private int lockedChangeFlag;

    public BatteryInfo(byte address)
    {
        super(address);
    }

    @Override
    public int getBatteryDataType()
    {
        return BatteryDataType.INFO;
    }

    public void setDoorSideLockStatus(byte doorSideLockStatus)
    {
        this.doorSideLockStatus = doorSideLockStatus;
        str_doorSideLockStatus = doorSideLockStatus == 1 ? "有电池" : "无电池";
    }

    public void setDoorBottomLockStatus(byte doorBottomLockStatus)
    {
        this.doorBottomLockStatus = doorBottomLockStatus;
        str_doorBottomLockStatus = doorBottomLockStatus == 1 ? "有电池" : "无电池";
    }

    void setElectricMachineStatus(byte electricMachineStatus)
    {
        this.electricMachineStatus = electricMachineStatus;
        str_electricMachineStatus = electricMachineStatus == 0 ? "关闭" : "打开";
    }

    void setBatteryTemperature(float batteryTemperature)
    {
        this.batteryTemperature = batteryTemperature;
        str_batteryTemperature = batteryTemperature + "°C";
    }

    void setBatteryTotalVoltage(int batteryTotalVoltage)
    {
        this.batteryTotalVoltage = batteryTotalVoltage;
        str_batteryTotalVoltage = batteryTotalVoltage + "mV";
    }

    void setRealTimeCurrent(int realTimeCurrent)
    {
        this.realTimeCurrent = realTimeCurrent;
        str_realTimeCurrent = realTimeCurrent + "mA";
    }

    void setRelativeCapatityPercent(int relativeCapatityPercent)
    {
        this.relativeCapatityPercent = relativeCapatityPercent;
        str_relativeCapatityPercent = relativeCapatityPercent + "%";
    }

    void setAbsoluteCapatityPercent(int absoluteCapatityPercent)
    {
        this.absoluteCapatityPercent = absoluteCapatityPercent;
        str_absoluteCapatityPercent = absoluteCapatityPercent + "%";
    }

    void setRemainingCapatity(int remainingCapatity)
    {
        this.remainingCapatity = remainingCapatity;
        str_remainingCapatity = remainingCapatity + "mAh";
    }

    void setFullCapatity(int fullCapatity)
    {
        this.fullCapatity = fullCapatity;
        str_fullCapatity = fullCapatity + "mAh";
    }

    void setLoopCount(int loopCount)
    {
        this.loopCount = loopCount;
        str_loopCount = loopCount + "次";
    }

    void setBatteryVoltage_1_7(String batteryVoltage_1_7)
    {
        this.batteryVoltage_1_7 = batteryVoltage_1_7;
    }

    void setBatteryVoltage_8_15(String batteryVoltage_8_15)
    {
        this.batteryVoltage_8_15 = batteryVoltage_8_15;
    }

    void setSoh(int soh)
    {
        this.soh = soh;
        str_soh = soh + "%";
    }

    void setBatteryIdInfo(String batteryIdInfo)
    {
        this.batteryIdInfo = !TextUtils.isEmpty(batteryIdInfo) ? batteryIdInfo.trim() : batteryIdInfo;
    }

    void setSoftwareVersion(int softwareVersion)
    {
        this.softwareVersion = softwareVersion;
    }

    void setHardwareVersion(int hardwareVersion)
    {
        this.hardwareVersion = hardwareVersion;
    }

    void setControlPanelTemperature(int controlPanelTemperature)
    {
        this.controlPanelTemperature = controlPanelTemperature;
        str_controlPanelTemperature = controlPanelTemperature + "°C";
    }

    void setControlPanelVersion(int controlPanelVersion)
    {
        this.controlPanelVersion = controlPanelVersion;
    }

    void reset()
    {
        setDoorSideLockStatus((byte) 0);
        setDoorBottomLockStatus((byte) 0);
        str_electricMachineStatus = "关闭";
        str_batteryTemperature = "--°C";
        str_batteryTotalVoltage = "0mV";
        str_realTimeCurrent = "0mA";
        str_relativeCapatityPercent = "0%";
        str_absoluteCapatityPercent = "0%";
        str_remainingCapatity = "0mAh";
        str_fullCapatity = "0mAh";
        str_loopCount = "0";
        //    public String batteryVoltage_1_7;
        //    public String batteryVoltage_8_15;
        str_soh = "0%";
        //    public String manufacturer;
        softwareVersion = 0;
        hardwareVersion = 0;
        str_controlPanelTemperature = "--°C";
        batteryIdInfo = "";
    }

    @NonNull
    @Override
    public String toString()
    {
        return
                "电机状态：" + str_electricMachineStatus + "\n"
                        + "电池温度：" + str_batteryTemperature + "\n"
                        + "电池电压：" + str_batteryTotalVoltage + "\n"
                        + "电池电流：" + str_realTimeCurrent + "\n"
                        + "电池相对容量百分比：" + str_relativeCapatityPercent + "\n"
                        + "电池剩余容量：" + str_remainingCapatity + "\n"
                        + "电池满充容量：" + str_fullCapatity + "\n"
                        + "电池循环次数：" + str_loopCount + "\n"
                        + "电芯电压1-7节：" + batteryVoltage_1_7 + "\n"
                        + "电芯电压8-15节：" + batteryVoltage_8_15 + "\n"
                        + "电池健康百分比：" + str_soh + "\n"
                        + "电池ID信息：" + batteryIdInfo + "\n"
                        + "电池软件版本：" + softwareVersion + "\n"
                        + "电池硬件版本：" + hardwareVersion + "\n"
                        + "控制板温度：" + str_controlPanelTemperature + "\n"
                        + "控制板版本：" + controlPanelVersion + "\n"
                        + "电池容量规格：" + _battery_capacity_specification + "\n"
                        + "BMS生产厂家：" + _BMS_manufacturer + "\n"
                        + "Pack生产厂家：" + _pack_manufacturer + "\n"
                        + "生产线识别码：" + _production_line_identification_code + "\n"
                        + "电芯生产厂家：" + _battery_manufacturer + "\n"
                        + "生产日期：" + _year + "-" + _month + "-" + _day;
    }

    @Override
    public String toSimpleString()
    {
        return _BMS_manufacturer + batteryTotalVoltage + remainingCapatity + realTimeCurrent
                + batteryTemperature + softwareVersion + doorSideLockStatus + doorBottomLockStatus;
    }

    public boolean isLocked()
    {
        return (doorSideLockStatus & doorBottomLockStatus) == 1;
    }

    public boolean isLockedChange()
    {
        if ((doorSideLockStatus & doorBottomLockStatus) != lockedChangeFlag)
        {
            lockedChangeFlag = doorSideLockStatus & doorBottomLockStatus;
            return true;
        }
        return false;
    }
}
