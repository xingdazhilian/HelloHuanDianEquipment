package com.hellohuandian.apps.equipment.modules.main;

import android.content.Intent;
import android.os.Bundle;

import com.hellohuandian.apps.equipment.R;
import com.hellohuandian.apps.equipment._base.activities.AppBaseActivity;
import com.hellohuandian.apps.equipment.modules.main.viewmodel.BatteryViewModel;
import com.hellohuandian.apps.equipment.services.StrategyService;

import androidx.lifecycle.ViewModelProviders;

/**
 * 管理主页和电池监视器窗口
 */
public class MainActivity extends AppBaseActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewModelProviders.of(this).get(BatteryViewModel.class);
        startService(new Intent(this, StrategyService.class));
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        stopService(new Intent(this, StrategyService.class));
    }
}
