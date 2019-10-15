package com.hellohuandian.apps.equipment.modules.main.tabPages.monitor.adapter;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hellohuandian.apps.equipment.R;
import com.hellohuandian.apps.equipment._base.adapter.BaseRecycleAdapter;
import com.hellohuandian.apps.strategylibrary.strategies._data.BatteryData;
import com.hellohuandian.apps.strategylibrary.strategies.battery.BatteryInfo;
import com.hellohuandian.apps.strategylibrary.strategies.upgrade.battery.BatteryUpgradeInfo;
import com.hellohuandian.apps.strategylibrary.strategies.upgrade.battery.BatteryUpgradeStrategyStatus;

import java.util.HashMap;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Author:      Lee Yeung
 * Create Date: 2019-09-29
 * Description:
 */
public class MonitorAdapter extends BaseRecycleAdapter<BatteryData, BaseRecycleAdapter.BaseViewHolder<BatteryData>>
{
    private final HashMap<Integer, Integer> indexMap = new HashMap<>();
    private int totalRowCount;//总行
    private int spanCount;//总列
    private int verticalSpacing;//分割线size

    public interface OnOpenDoorAction
    {
        void onOpen(byte address);
    }

    private OnOpenDoorAction onOpenDoorAction;

    public MonitorAdapter(Context context)
    {
        super(context);
    }

    public void setValues(int totalRowCount, int spanCount, int verticalSpacing)
    {
        this.totalRowCount = totalRowCount;
        this.spanCount = spanCount;
        this.verticalSpacing = verticalSpacing;
    }

    public void setOnOpenDoorAction(OnOpenDoorAction onOpenDoorAction)
    {
        this.onOpenDoorAction = onOpenDoorAction;
    }

    @Override
    public void addElement(BatteryData element)
    {
        if (element != null)
        {
            final int id = element.address;
            Integer index = indexMap.get(id);
            if (index == null)
            {
                int newIndex = getItemCount();
                indexMap.put(id, newIndex);
                super.addElement(element);
                notifyItemRangeChanged(newIndex, getItemCount());
            } else
            {
                if (index >= 0 && index < getItemCount())
                {
                    getDataArrayList().set(index, element);
                    notifyItemChanged(index);
                }
            }
        }
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        switch (viewType)
        {
            case BatteryData.BatteryDataType.UPGRADE:
                return new BatteryUpgradeViewHolder(initItemView(R.layout.item_monitor_battery_upgrade, parent));
            case BatteryData.BatteryDataType.INFO:
            default:
                return new BatteryInfoViewHolder(initItemView(R.layout.item_monitor_battery_card, parent));
        }
    }

    @Override
    public int getItemViewType(int position)
    {
        BatteryData batteryData = getItem(position);
        return batteryData != null ? batteryData.getBatteryDataType() : super.getItemViewType(position);
    }

    private View initItemView(@LayoutRes int itemId, @NonNull ViewGroup parent)
    {
        View itemView = getLayoutInflater().inflate(itemId, parent, false);
        ViewGroup.LayoutParams layoutParams = itemView.getLayoutParams();
        if (layoutParams instanceof ViewGroup.MarginLayoutParams)
        {
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) layoutParams;
            if (totalRowCount > 0 && spanCount > 0)
            {
                marginLayoutParams.height = (parent.getMeasuredHeight()
                        - verticalSpacing * (totalRowCount - 1)) / totalRowCount;
            }
        }
        return itemView;
    }

    class BatteryInfoViewHolder extends BaseViewHolder<BatteryData> implements View.OnClickListener
    {
        @BindView(R.id.tv_position)
        TextView tvPosition;
        @BindView(R.id.tv_openDoor)
        TextView tvOpenDoor;
        @BindView(R.id.tv_id)
        TextView tvId;
        @BindView(R.id.tv_batterySimpleInfo)
        TextView tvBatterySimpleInfo;

        private BatteryInfo modelBatteryInfo;

        public BatteryInfoViewHolder(@NonNull View itemView)
        {
            super(itemView);
            ButterKnife.bind(this, itemView);
            tvOpenDoor.setOnClickListener(this);
        }

        @Override
        protected void update(BatteryData model, int position)
        {
            if (model instanceof BatteryInfo)
            {
                modelBatteryInfo = (BatteryInfo) model;
                tvPosition.setText(String.format("%02d", position + 1));
                tvId.setText(!TextUtils.isEmpty(modelBatteryInfo.batteryIdInfo) ? modelBatteryInfo.batteryIdInfo : "");
                tvBatterySimpleInfo.setText(modelBatteryInfo.str_batteryTotalVoltage + "\n"
                        + modelBatteryInfo.str_relativeCapatityPercent + "\n"
                        + modelBatteryInfo.str_realTimeCurrent + "\n"
                        + modelBatteryInfo.str_batteryTemperature + "\n"
                        + "sv" + modelBatteryInfo.softwareVersion + ",hv" + modelBatteryInfo.hardwareVersion + "\n"
                        + "BMS-" + modelBatteryInfo._BMS_manufacturer);
            }
        }

        @Override
        public void onClick(View v)
        {
            if (onOpenDoorAction != null && modelBatteryInfo != null)
            {
                onOpenDoorAction.onOpen(modelBatteryInfo.address);
            }
        }
    }

    class BatteryUpgradeViewHolder extends BaseViewHolder<BatteryData>
    {
        @BindView(R.id.tv_position)
        TextView tvPosition;
        @BindView(R.id.pb_statusBar)
        ProgressBar pbStatusBar;
        @BindView(R.id.tv_percent)
        TextView tvPercent;
        @BindView(R.id.tv_statusInfo)
        TextView tvStatusInfo;

        private BatteryUpgradeInfo modelBatteryUpgradeInfo;

        public BatteryUpgradeViewHolder(@NonNull View itemView)
        {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @Override
        protected void update(BatteryData model, int position)
        {
            if (model instanceof BatteryUpgradeInfo)
            {
                tvPosition.setText(String.format("%02d", position + 1));
                modelBatteryUpgradeInfo = (BatteryUpgradeInfo) model;
                if (modelBatteryUpgradeInfo.totalPregress > 0)
                {
                    int percent = (int) ((float) modelBatteryUpgradeInfo.currentPregress / modelBatteryUpgradeInfo.totalPregress * 100);
                    if (pbStatusBar.getProgress() != percent)
                    {
                        tvPercent.setText(percent + "%");
                        pbStatusBar.setProgress(percent);
                        System.out.println(percent + "%");
                    }
                }
                if (modelBatteryUpgradeInfo.statusFlag == BatteryUpgradeStrategyStatus.FAILED)
                {
                    tvStatusInfo.setTextColor(Color.RED);
                } else if (modelBatteryUpgradeInfo.statusFlag == BatteryUpgradeStrategyStatus.SUCCESSED)
                {
                    tvStatusInfo.setTextColor(Color.GREEN);
                }
                tvStatusInfo.setText(modelBatteryUpgradeInfo.statusInfo);
            }
        }
    }
}
