package com.example.walletjournal.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

/**
 * A FrameLayout that also recognizes a horizontal swipe across its whole
 * area — used by StatsActivity so 分類/趨勢/排行 can be switched by swiping
 * left/right, in addition to tapping the segmented control.
 *
 * A touch is only claimed for the swipe once its horizontal movement has
 * both cleared the touch-slop and clearly outpaced its vertical movement
 * (the same disambiguation RecyclerView/ViewPager use internally), so plain
 * taps and the RecyclerViews' own vertical scrolling inside each panel are
 * left untouched. A touch starting inside a horizontally-scrolling child
 * (see setHorizontalScrollExclusionView, e.g. 趨勢's rv_trend) is excluded
 * from swipe detection for its whole gesture — both this view and that
 * child would otherwise claim the exact same horizontal drag.
 */
public class SwipeableFrameLayout extends FrameLayout {

    /** Callback for a completed left/right swipe. */
    public interface OnSwipeListener {
        void onSwipeLeft();

        void onSwipeRight();
    }

    private static final int FLING_MIN_DISTANCE_DP = 60;
    private static final int FLING_MIN_VELOCITY_DP = 200;

    private final int touchSlop;
    private final int flingMinDistance;
    private final int flingMinVelocity;
    private final GestureDetector gestureDetector;

    private OnSwipeListener listener;
    private View horizontalScrollExclusionView;
    private float downX;
    private float downY;
    private boolean intercepting;
    private boolean excludedForThisGesture;

    public SwipeableFrameLayout(Context context) {
        this(context, null);
    }

    public SwipeableFrameLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        float density = context.getResources().getDisplayMetrics().density;
        flingMinDistance = Math.round(FLING_MIN_DISTANCE_DP * density);
        flingMinVelocity = Math.round(FLING_MIN_VELOCITY_DP * density);

        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e1 == null || listener == null) {
                    return false;
                }
                float dx = e2.getX() - e1.getX();
                float dy = e2.getY() - e1.getY();
                if (Math.abs(dx) > flingMinDistance
                        && Math.abs(dx) > Math.abs(dy)
                        && Math.abs(velocityX) > flingMinVelocity) {
                    if (dx < 0) {
                        listener.onSwipeLeft();
                    } else {
                        listener.onSwipeRight();
                    }
                    return true;
                }
                return false;
            }
        });
    }

    public void setOnSwipeListener(OnSwipeListener listener) {
        this.listener = listener;
    }

    /**
     * A touch that starts inside this view is left entirely to it (e.g. a
     * horizontally-scrolling RecyclerView) — never claimed for the tab-switch
     * swipe, since both would otherwise fight over the same horizontal drag.
     */
    public void setHorizontalScrollExclusionView(@Nullable View view) {
        this.horizontalScrollExclusionView = view;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                downX = ev.getX();
                downY = ev.getY();
                intercepting = false;
                excludedForThisGesture = isInsideExclusionView(ev);
                break;
            case MotionEvent.ACTION_MOVE:
                if (!excludedForThisGesture && !intercepting) {
                    float dx = ev.getX() - downX;
                    float dy = ev.getY() - downY;
                    if (Math.abs(dx) > touchSlop && Math.abs(dx) > Math.abs(dy)) {
                        intercepting = true;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                intercepting = false;
                break;
            default:
                break;
        }
        return intercepting;
    }

    private boolean isInsideExclusionView(MotionEvent ev) {
        if (horizontalScrollExclusionView == null || horizontalScrollExclusionView.getVisibility() != View.VISIBLE) {
            return false;
        }
        int[] location = new int[2];
        horizontalScrollExclusionView.getLocationOnScreen(location);
        float rawX = ev.getRawX();
        float rawY = ev.getRawY();
        return rawX >= location[0] && rawX <= location[0] + horizontalScrollExclusionView.getWidth()
                && rawY >= location[1] && rawY <= location[1] + horizontalScrollExclusionView.getHeight();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        if (event.getActionMasked() == MotionEvent.ACTION_UP
                || event.getActionMasked() == MotionEvent.ACTION_CANCEL) {
            intercepting = false;
        }
        return true;
    }
}
