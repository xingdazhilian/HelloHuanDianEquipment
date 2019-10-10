package com.hellohuandian.apps.equipment.modules.main.tabPages.home;

import android.os.Bundle;

import com.hellohuandian.apps.equipment.R;
import com.hellohuandian.apps.equipment._base.fragments.AppBaseFragment;
import com.hellohuandian.apps.equipment.modules.main.viewmodel.BatteryViewModel;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

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
