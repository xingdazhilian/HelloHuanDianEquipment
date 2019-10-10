package com.hellohuandian.apps.equipment.modules.launch;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.hellohuandian.apps.equipment.R;
import com.hellohuandian.apps.equipment._base.activities.AppBaseActivity;
import com.hellohuandian.apps.equipment.modules.main.MainActivity;

import androidx.annotation.Nullable;

/**
 * Author:      Lee Yeung
 * Create Date: 2019-09-29
 * Description:
 */
public class LaunchActivity extends AppBaseActivity
{
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        new Handler().postDelayed(() -> startActivity(new Intent(LaunchActivity.this, MainActivity.class)), 3000);
    }

}
