package client.NewElectric.app10000.widgets.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Author:      Lee Yeung
 * Create Date: 2019-10-09
 * Description:
 */
public class SimpleDialog extends BaseDialog
{
    private Unbinder unbinder;

    public SimpleDialog(int layoutId)
    {
        super(layoutId);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        if (unbinder != null)
        {
            unbinder.unbind();
        }
    }
}
