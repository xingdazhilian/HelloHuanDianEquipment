package com.hellohuandian.apps.equipment.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.jakewharton.processphoenix.ProcessPhoenix;

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
            // TODO: 2019-10-15 重新启动整个app(Android rom存在bug，主线程和子线程运行状态的bug)
            ProcessPhoenix.triggerRebirth(context);
        }
    }
}
