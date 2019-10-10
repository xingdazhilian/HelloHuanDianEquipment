package com.hellohuandian.apps.equipment.modules.main.tabPages.monitor.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hellohuandian.apps.equipment.R;
import com.hellohuandian.apps.equipment._base.adapter.BaseRecycleAdapter;
import com.hellohuandian.apps.strategylibrary.strategies.battery.BatteryData;

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
public class MonitorAdapter extends BaseRecycleAdapter<BatteryData, MonitorAdapter.ViewHolder>
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
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        return new ViewHolder(initItemView(R.layout.item_monitor_battery_card, parent));
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

    class ViewHolder extends BaseRecycleAdapter.BaseViewHolder<BatteryData> implements View.OnClickListener
    {
        @BindView(R.id.tv_position)
        TextView tvPosition;
        @BindView(R.id.tv_openDoor)
        TextView tvOpenDoor;
        @BindView(R.id.tv_id)
        TextView tvId;
        @BindView(R.id.tv_batterySimpleInfo)
        TextView tvBatterySimpleInfo;

        private BatteryData model;

        public ViewHolder(@NonNull View itemView)
        {
            super(itemView);
            ButterKnife.bind(this, itemView);
            tvOpenDoor.setOnClickListener(this);
        }

        @Override
        protected void update(BatteryData model, int position)
        {
            this.model = model;

            tvPosition.setText(String.format("%02d", position + 1));
            tvId.setText(TextUtils.isEmpty(model.batteryIdInfo) ? "----------------" : model.batteryIdInfo);
            tvBatterySimpleInfo.setText(model.str_batteryTotalVoltage + "\n"
                    + model.str_relativeCapatityPercent + "\n"
                    + model.str_realTimeCurrent + "\n"
                    + model.str_batteryTemperature + "\n"
                    + "sv" + model.str_softwareVersion + ",hv" + model.str_hardwareVersion);
        }

        @Override
        public void onClick(View v)
        {
            if (onOpenDoorAction != null)
            {
                onOpenDoorAction.onOpen(model.address);
            }
        }
    }
}
