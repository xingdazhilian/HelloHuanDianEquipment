package client.NewElectric.app10000.widgets.dialog;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import client.NewElectric.app10000.R;

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
        setStyle(STYLE_NO_FRAME, R.style.AppDialog);
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
