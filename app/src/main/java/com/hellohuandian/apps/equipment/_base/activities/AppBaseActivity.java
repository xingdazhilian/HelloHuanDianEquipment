package com.hellohuandian.apps.equipment._base.activities;

import android.view.View;

import com.hellohuandian.apps.BaseLibrary.activities.FunctionActivity;

import butterknife.ButterKnife;

/**
 * Author:      Lee Yeung
 * Create Date: 2019-08-07
 * Description:
 */
public class AppBaseActivity extends FunctionActivity
{
    @Override
    public void setContentView(View view)
    {
        super.setContentView(view);
        ButterKnife.bind(this);
    }
}
