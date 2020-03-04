package client.NewElectric.app10000.modules.launch;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import client.NewElectric.app10000._base.activities.AppBaseActivity;
import client.NewElectric.app10000.modules.main.MainActivity;

import com.orhanobut.logger.CsvFormatStrategy;
import com.orhanobut.logger.DiskLogAdapter;
import com.orhanobut.logger.FormatStrategy;
import com.orhanobut.logger.Logger;

import androidx.annotation.Nullable;
import client.NewElectric.app10000.R;

/**
 * Author:      Lee Yeung
 * Create Date: 2019-09-29
 * Description:
 */
public class LaunchActivity extends AppBaseActivity
{
    private final Handler handler = new Handler();

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
        handler.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                FormatStrategy formatStrategy = CsvFormatStrategy.newBuilder()
                        .tag("校验码")
                        .build();
                Logger.addLogAdapter(new DiskLogAdapter(formatStrategy));


                System.out.println("进入主页");
                finish();
                LaunchActivity.this.startActivity(new Intent(LaunchActivity.this, MainActivity.class));
            }
        }, 3000);
    }


    @Override
    protected void onStop()
    {
        super.onStop();
        handler.removeCallbacksAndMessages(null);
    }
}