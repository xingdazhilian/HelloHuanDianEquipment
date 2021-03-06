package client.NewElectric.app10000.modules.main;

import android.content.Intent;
import android.os.Bundle;

import client.NewElectric.app10000._base.activities.AppBaseActivity;
import client.NewElectric.app10000.modules.main.viewmodel.BatteryViewModel;
import client.NewElectric.app10000.services.StrategyService;

import androidx.lifecycle.ViewModelProviders;
import client.NewElectric.app10000.R;

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
