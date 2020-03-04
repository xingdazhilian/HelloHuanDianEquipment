package client.NewElectric.app10000.services;

import com.app10000.apps.strategylibrary.strategies._data.BatteryData;

/**
 * Author:      Lee Yeung
 * Create Date: 2019-09-29
 * Description:
 */
public interface BatteryWatcher
{
    void onWatch(BatteryData batteryData);
}
