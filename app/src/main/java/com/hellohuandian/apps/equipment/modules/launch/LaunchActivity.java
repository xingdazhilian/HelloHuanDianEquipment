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
    private int launchFlag;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
        launchFlag = getIntent().getIntExtra("launchFlag", 1);
        System.out.println("启动页launchFlag:" + launchFlag);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        if (launchFlag == 0)
        {
            launchFlag += 1;
            outState.putInt("launchFlag", launchFlag);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);
        launchFlag = savedInstanceState.getInt("launchFlag");
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if (launchFlag == 1)
        {
            new Handler().postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    finish();
                    LaunchActivity.this.startActivity(new Intent(LaunchActivity.this, MainActivity.class));
                }
            }, 3000);
        }
    }
}
