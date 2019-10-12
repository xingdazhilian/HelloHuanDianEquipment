package com.hellohuandian.apps.equipment.modules.main.tabPages.monitor;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.hellohuandian.apps.equipment.R;
import com.hellohuandian.apps.equipment._base.fragments.AppBaseFragment;
import com.hellohuandian.apps.equipment.modules.config.MachineVersionConfig;
import com.hellohuandian.apps.equipment.modules.main.tabPages.monitor.adapter.MonitorAdapter;
import com.hellohuandian.apps.equipment.modules.main.viewmodel.BatteryViewModel;
import com.hellohuandian.apps.equipment.services.StrategyService;
import com.hellohuandian.apps.equipment.widgets.SimpleItemDecoration;
import com.hellohuandian.apps.equipment.widgets.dialog.AppDialog;
import com.hellohuandian.apps.strategylibrary.config.MachineVersion;
import com.hellohuandian.apps.strategylibrary.dispatchers.DispatcherManager;
import com.hellohuandian.apps.strategylibrary.strategies._base.NodeStrategy;
import com.hellohuandian.apps.strategylibrary.strategies.activation.Action485;
import com.hellohuandian.apps.strategylibrary.strategies.pushRod.OnPushAction;
import com.hellohuandian.apps.strategylibrary.strategies.pushRod.PushRodStrategy;
import com.hellohuandian.apps.strategylibrary.strategies.relay.RelayCloseStrategy;
import com.hellohuandian.apps.strategylibrary.strategies.upgrade.battery.BatteryUpgradeInfo;
import com.hellohuandian.apps.strategylibrary.strategies.upgrade.battery.BatteryUpgradeStrategy;
import com.hellohuandian.apps.strategylibrary.strategies.upgrade.battery.JieMinKe.JieMinKeBatteryUpgradeStrategy;
import com.hellohuandian.apps.strategylibrary.strategies.upgrade.battery.OnUpgradeProgress;

import androidx.annotation.Nullable;
import androidx.core.util.Consumer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.OnClick;

/**
 * Author:      Lee Yeung
 * Create Date: 2019-09-29
 * Description:
 */
public class MonitorFragment extends AppBaseFragment implements View.OnClickListener
{
    @BindView(R.id.rv_monitor)
    RecyclerView rvMonitor;
    private MonitorAdapter monitorAdapter;
    private AppDialog appDialog = new AppDialog(R.layout.layout_loading_dialog);
    private MonitorAdapter.OnOpenDoorAction onOpenDoorAction = address -> {
        // TODO: 2019-10-09 执行推杆操作
        pushRod(address);
    };

    private BatteryViewModel batteryViewModel;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        monitorAdapter = new MonitorAdapter(getContext());
        monitorAdapter.setOnOpenDoorAction(onOpenDoorAction);
        setMachineVersion(MachineVersionConfig.getMachineVersion());
        rvMonitor.setLayoutManager(new GridLayoutManager(getContext(), 3));
        rvMonitor.addItemDecoration(new SimpleItemDecoration());
        rvMonitor.setAdapter(monitorAdapter);

        batteryViewModel = ViewModelProviders.of(getActivity()).get(BatteryViewModel.class);
        if (batteryViewModel.batteryMonitorLiveData != null)
        {
            batteryViewModel.batteryMonitorLiveData.observe(this, batteryData -> {
                if (monitorAdapter != null)
                {
                    monitorAdapter.addElement(batteryData);
                }
            });
        }
//        testBatteryUpgradeStrategy();
    }

    @Override
    protected int getLayoutID()
    {
        return R.layout.fragment_monitor;
    }

    public void setMachineVersion(@MachineVersion int version)
    {
        switch (version)
        {
            case MachineVersion.SC_3:
                monitorAdapter.setValues(4, 3, 10);
                break;
            case MachineVersion.SC_4:
            case MachineVersion.SC_5:
                monitorAdapter.setValues(3, 3, 10);
                break;
        }
    }

    private void pushRod(byte address)
    {
        if (appDialog.isShowing())
        {
            return;
        }
        appDialog.show(getActivity().getSupportFragmentManager(), "show");

        NodeStrategy pushNodeStrategy = null;
        PushRodStrategy pushRodStrategy = new PushRodStrategy(address);
        pushRodStrategy.setOnPushAction(new OnPushAction()
        {
            @Override
            public void onPushSuccessed(byte address)
            {
                System.out.println("推杆成功");
                appDialog.dismiss();
            }

            @Override
            public void onPushFailed(byte address)
            {
                System.out.println("推杆失败");
                appDialog.dismiss();
            }
        });

        switch (MachineVersionConfig.getMachineVersion())
        {
            case MachineVersion.SC_3:
                pushNodeStrategy = pushRodStrategy;
                break;
            case MachineVersion.SC_4:
            case MachineVersion.SC_5:
                pushNodeStrategy = pushRodStrategy.addPrevious(new RelayCloseStrategy((byte) (address - 0x05 + 0x15)));
                break;
        }

        if (pushNodeStrategy != null)
        {
            pushNodeStrategy.first()
                    .call(nodeStrategy -> DispatcherManager.getInstance().dispatch(nodeStrategy));
        }
    }

    private void testBatteryUpgradeStrategy()
    {
        // TODO: 2019-10-11 升级过程中其他控制操作必须暂停 
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                System.out.println("准备升级");
                try
                {
                    Thread.sleep(5000);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
                System.out.println("开始升级");
                BatteryUpgradeStrategy batteryUpgradeStrategy = new JieMinKeBatteryUpgradeStrategy((byte) 0x05, "/sdcard/Download" +
                        "/HelloBMS19S_HW0101_FW0163_CRC62069E48.bin");
                batteryUpgradeStrategy.setOnUpgradeProgress(new OnUpgradeProgress()
                {
                    @Override
                    public void onUpgrade(BatteryUpgradeInfo batteryUpgradeInfo)
                    {
                        if (batteryViewModel != null)
                        {
                            batteryViewModel.batteryMonitorLiveData.postValue(batteryUpgradeInfo);
                        }
                    }
                });

                batteryUpgradeStrategy.addNext(new Action485((byte) 0x05))
                        .first()
                        .call(new Consumer<NodeStrategy>()
                        {
                            @Override
                            public void accept(NodeStrategy nodeStrategy)
                            {
                                DispatcherManager.getInstance().dispatch(nodeStrategy);
                            }
                        });

            }
        }).start();
    }

    @OnClick({R.id.tv_exitApp})
    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.tv_exitApp:
                getActivity().stopService(new Intent(getActivity(), StrategyService.class));
                getActivity().finish();
                break;
        }
    }
}
