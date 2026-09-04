package com.example.walletjournal.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

/**
 * A FrameLayout that always measures itself as a square — side length equal
 * to whichever of its available width/height is smaller (capped at
 * MAX_SIZE_DP so it doesn't blow up on tall/wide containers).
 *
 * Used to hold StatsActivity's donut chart: that panel now gives the chart
 * only half the screen's height (see activity_stats.xml), which on shorter
 * screens is less than the chart's old fixed 200dp, and a plain FrameLayout
 * clips a child bigger than itself — the ring got cut off top/bottom. This
 * shrinks the chart to whatever square actually fits instead.
 */
public class SquareFrameLayout extends FrameLayout {

    private static final int MAX_SIZE_DP = 220;

    public SquareFrameLayout(Context context) {
        this(context, null);
    }

    public SquareFrameLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int maxSizePx = Math.round(MAX_SIZE_DP * getResources().getDisplayMetrics().density);
        int size = Math.min(
                Math.min(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec)),
                maxSizePx);
        int squareSpec = MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY);
        super.onMeasure(squareSpec, squareSpec);
        setMeasuredDimension(size, size);
    }
}
