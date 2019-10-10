package com.hellohuandian.apps.equipment.widgets.dialog;

import android.os.Bundle;
import android.view.View;

import com.hellohuandian.apps.equipment.R;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

/**
 * Author:      Lee Yeung
 * Create Date: 2019-10-09
 * Description:
 */
public class AppDialog extends SimpleDialog
{
    public AppDialog(int layoutId)
    {
        super(layoutId);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.AppDialog);
    }

    public void setOnClickListener(View.OnClickListener onClickListener, int... ids)
    {
        if (onClickListener != null)
        {
            View view;
            for (int id : ids)
            {
                view = getView().findViewById(id);
                if (view != null)
                {
                    view.setOnClickListener(onClickListener);
                }
            }
        }
    }
}
