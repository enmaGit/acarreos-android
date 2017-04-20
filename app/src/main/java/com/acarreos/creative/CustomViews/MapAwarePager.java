package com.acarreos.creative.CustomViews;

import android.content.Context;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.SurfaceView;
import android.view.View;

/**
 * Created by EnmanuelPc on 09/09/2015.
 */
public class MapAwarePager extends ViewPager {
    public MapAwarePager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected boolean canScroll(View v, boolean checkV, int dx, int x,
                                int y) {
        if (v instanceof SurfaceView || v instanceof PagerTabStrip) {
            return (true);
        }

        return (super.canScroll(v, checkV, dx, x, y));
    }
}
