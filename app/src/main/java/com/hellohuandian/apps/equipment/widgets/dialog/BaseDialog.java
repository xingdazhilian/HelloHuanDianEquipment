package com.hellohuandian.apps.equipment.widgets.dialog;

import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

/**
 * Author:      Lee Yeung
 * Create Date: 2019-10-09
 * Description:
 */
public class BaseDialog extends DialogFragment
{
    private int layoutId;
    private View self;

    public interface OnShowListener
    {
        void onShow();
    }

    private OnShowListener onShowListener;

    public BaseDialog(@LayoutRes int layoutId)
    {
        this.layoutId = layoutId;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        Dialog dialog = getDialog();
        if (dialog != null)
        {
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setDimAmount(0f); //暗度为0
            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(false);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            {
                // 解决全屏时状态栏变黑
                dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            }
            dialog.setOnKeyListener((dialog1, keyCode, event) -> keyCode == KeyEvent.KEYCODE_BACK);
        }

        if (self == null)
        {
            self = inflater.inflate(layoutId, container, false);
        } else
        {
            if (self.getParent() != null)
            {
                ((ViewGroup) self.getParent()).removeView(self);
            }
        }

        return self;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        if (getDialog() != null)
        {
            try
            {
                getDialog().setOnDismissListener(null);
                getDialog().setOnCancelListener(null);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public boolean isShowing()
    {
        return getDialog() != null && getDialog().isShowing();
    }

    @Override
    public void show(FragmentManager manager, String tag)
    {
        if (!isShowing())
        {
            super.show(manager, tag);
            if (onShowListener != null)
            {
                onShowListener.onShow();
            }
        }
    }

    @Override
    public void dismiss()
    {
        if (isShowing())
        {
            super.dismiss();
        }
    }

    @Override
    public void dismissAllowingStateLoss()
    {
        if (isShowing())
        {
            super.dismissAllowingStateLoss();
        }
    }

    public void setOnShowListener(OnShowListener onShowListener)
    {
        this.onShowListener = onShowListener;
    }

    protected void setDialogSize(int width, int height)
    {
        Dialog dialog = getDialog();
        if (dialog != null)
        {
            dialog.getWindow().setLayout(width, height);
        }
    }
}
