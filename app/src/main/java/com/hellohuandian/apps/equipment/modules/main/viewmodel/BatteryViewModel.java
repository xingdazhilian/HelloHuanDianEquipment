package com.hellohuandian.apps.equipment.modules.main.viewmodel;

import android.app.Application;

import com.hellohuandian.apps.equipment.services.BatteryWatcher;
import com.hellohuandian.apps.equipment.services.StrategyService;
import com.hellohuandian.apps.strategylibrary.strategies._data.BatteryData;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

/**
 * Author:      Lee Yeung
 * Create Date: 2019-09-29
 * Description:
 */
public class BatteryViewModel extends AndroidViewModel implements BatteryWatcher
{
    public final MutableLiveData<BatteryData> batteryMonitorLiveData = new MutableLiveData<>();

    public BatteryViewModel(@NonNull Application application)
    {
        super(application);
        StrategyService.BatteryWatcherRegisters.register(this);
    }

    @Override
    protected void onCleared()
    {
        StrategyService.BatteryWatcherRegisters.unRegister(this);
    }

    @Override
    public void onWatch(BatteryData batteryData)
    {
        batteryMonitorLiveData.setValue(batteryData);
    }
}
