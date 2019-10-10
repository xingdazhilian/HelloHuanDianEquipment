package com.hellohuandian.apps.equipment.modules.main.tabPages.monitor;

import android.os.Bundle;

import com.hellohuandian.apps.equipment.R;
import com.hellohuandian.apps.equipment._base.fragments.AppBaseFragment;
import com.hellohuandian.apps.equipment.modules.config.MachineVersionConfig;
import com.hellohuandian.apps.equipment.modules.main.tabPages.monitor.adapter.MonitorAdapter;
import com.hellohuandian.apps.equipment.modules.main.viewmodel.BatteryViewModel;
import com.hellohuandian.apps.equipment.widgets.SimpleItemDecoration;
import com.hellohuandian.apps.equipment.widgets.dialog.AppDialog;
import com.hellohuandian.apps.strategylibrary.config.MachineVersion;
import com.hellohuandian.apps.strategylibrary.dispatchers.DispatcherManager;
import com.hellohuandian.apps.strategylibrary.strategies._base.NodeStrategy;
import com.hellohuandian.apps.strategylibrary.strategies.pushRod.OnPushAction;
import com.hellohuandian.apps.strategylibrary.strategies.pushRod.PushRodStrategy;
import com.hellohuandian.apps.strategylibrary.strategies.relay.RelayCloseStrategy;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;

/**
 * Author:      Lee Yeung
 * Create Date: 2019-09-29
 * Description:
 */
public class MonitorFragment extends AppBaseFragment
{
    @BindView(R.id.rv_monitor)
    RecyclerView rvMonitor;
    private MonitorAdapter monitorAdapter;
    private AppDialog appDialog = new AppDialog(R.layout.layout_loading_dialog);

    private MonitorAdapter.OnOpenDoorAction onOpenDoorAction = address -> {
        // TODO: 2019-10-09 执行推杆操作
        pushRod(address);
    };

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

        BatteryViewModel batteryViewModel = ViewModelProviders.of(getActivity()).get(BatteryViewModel.class);
        if (batteryViewModel.batteryMonitorLiveData != null)
        {
            batteryViewModel.batteryMonitorLiveData.observe(this, batteryData -> {
                if (monitorAdapter != null)
                {
                    monitorAdapter.addElement(batteryData);
                }
            });
        }
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
            case MachineVersion.SC_1:
                monitorAdapter.setValues(4, 3, 10);
                break;
            case MachineVersion.SC_2:
            case MachineVersion.SC_3:
            case MachineVersion.SC_4:
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
                appDialog.dismiss();
            }

            @Override
            public void onPushFailed(byte address)
            {
                appDialog.dismiss();
            }
        });

        switch (MachineVersionConfig.getMachineVersion())
        {
            case MachineVersion.SC_1:
                pushNodeStrategy = pushRodStrategy;
                break;
            case MachineVersion.SC_2:
            case MachineVersion.SC_3:
            case MachineVersion.SC_4:
                pushNodeStrategy = pushRodStrategy.addPrevious(new RelayCloseStrategy((byte) (address - 0x05 + 0x15)));
                break;
        }

        if (pushNodeStrategy != null)
        {
            pushNodeStrategy.first()
                    .call(nodeStrategy -> DispatcherManager.getInstance().dispatch(nodeStrategy));
        }
    }
}
