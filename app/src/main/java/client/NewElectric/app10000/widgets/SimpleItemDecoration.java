package client.NewElectric.app10000.widgets;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

/**
 * Author:      Lee Yeung
 * Create Date: 2019-10-08
 * Description:
 */
public class SimpleItemDecoration extends RecyclerView.ItemDecoration
{
    private int mHorizontalSpacing = 10;
    private int mVerticalSpacing = 10;
    private boolean mIncludeEdge = false;

    public SimpleItemDecoration()
    {
    }

    public SimpleItemDecoration(int mHorizontalSpacing, int mVerticalSpacing, boolean mIncludeEdge)
    {
        this.mHorizontalSpacing = mHorizontalSpacing;
        this.mVerticalSpacing = mVerticalSpacing;
        this.mIncludeEdge = mIncludeEdge;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state)
    {
        super.getItemOffsets(outRect, view, parent, state);

        int position = parent.getChildAdapterPosition(view);
        if (parent.getLayoutManager() instanceof GridLayoutManager)
        {
            GridLayoutManager layoutManager = (GridLayoutManager) parent.getLayoutManager();
            int spanCount = layoutManager.getSpanCount();
            int column = position % spanCount;
            getGridItemOffsets(outRect, position, column, spanCount);
        } else if (parent.getLayoutManager() instanceof StaggeredGridLayoutManager)
        {
            StaggeredGridLayoutManager layoutManager = (StaggeredGridLayoutManager) parent.getLayoutManager();
            int spanCount = layoutManager.getSpanCount();
            StaggeredGridLayoutManager.LayoutParams lp = (StaggeredGridLayoutManager.LayoutParams) view.getLayoutParams();
            int column = lp.getSpanIndex();
            getGridItemOffsets(outRect, position, column, spanCount);
        } else if (parent.getLayoutManager() instanceof LinearLayoutManager)
        {
            outRect.left = mHorizontalSpacing;
            outRect.right = mHorizontalSpacing;
            if (mIncludeEdge)
            {
                if (position == 0)
                {
                    outRect.top = mVerticalSpacing;
                }
                outRect.bottom = mVerticalSpacing;
            } else
            {
                if (position > 0)
                {
                    outRect.top = mVerticalSpacing;
                }
            }
        }
    }

    private void getGridItemOffsets(Rect outRect, int position, int column, int spanCount)
    {
        if (mIncludeEdge)
        {
            outRect.left = mHorizontalSpacing * (spanCount - column) / spanCount;
            outRect.right = mHorizontalSpacing * (column + 1) / spanCount;
            if (position < spanCount)
            {
                outRect.top = mVerticalSpacing;
            }
            outRect.bottom = mVerticalSpacing;
        } else
        {
            outRect.left = mHorizontalSpacing * column / spanCount;
            outRect.right = mHorizontalSpacing * (spanCount - 1 - column) / spanCount;
            if (position >= spanCount)
            {
                outRect.top = mVerticalSpacing;
            }
        }
    }
}