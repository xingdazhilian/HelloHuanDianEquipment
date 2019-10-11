package com.hellohuandian.apps.equipment.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.hellohuandian.apps.equipment.modules.launch.LaunchActivity;

/**
 * Author:      Lee Yeung
 * Create Date: 2019-10-11
 * Description:
 */
public class RebootReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED))
        {
            context.startActivity(new Intent(context, LaunchActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }
    }
}
