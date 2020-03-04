package client.NewElectric.app10000.modules.main.tabPages.home;

import android.os.Bundle;

import client.NewElectric.app10000._base.fragments.AppBaseFragment;
import client.NewElectric.app10000.modules.main.viewmodel.BatteryViewModel;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;
import client.NewElectric.app10000.R;

/**
 * Author:      Lee Yeung
 * Create Date: 2019-09-29
 * Description:
 */
public class HomeFragment extends AppBaseFragment
{
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        BatteryViewModel batteryViewModel = ViewModelProviders.of(getActivity()).get(BatteryViewModel.class);
        if (batteryViewModel.batteryMonitorLiveData != null)
        {
            batteryViewModel.batteryMonitorLiveData.observe(this, batteryData -> {

            });
        }
    }

    @Override
    protected int getLayoutID()
    {
        return R.layout.fragment_home;
    }
}
