package com.example.walletjournal.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;

/**
 * A simple donut (ring) chart drawn with Canvas arcs — no charting library.
 * Each segment's sweep angle is proportional to its percent (0-100), drawn
 * clockwise starting from the top.
 */
public class DonutChartView extends View {

    public static class Segment {
        public final float percent;
        public final int color;

        public Segment(float percent, int color) {
            this.percent = percent;
            this.color = color;
        }
    }

    private final List<Segment> segments = new ArrayList<>();
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF bounds = new RectF();
    private final float strokeWidthPx;

    public DonutChartView(Context context) {
        this(context, null);
    }

    public DonutChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        strokeWidthPx = 28f * getResources().getDisplayMetrics().density;
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(strokeWidthPx);
        paint.setStrokeCap(Paint.Cap.BUTT);
    }

    public void setSegments(List<Segment> newSegments) {
        segments.clear();
        if (newSegments != null) {
            segments.addAll(newSegments);
        }
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float inset = strokeWidthPx / 2f;
        bounds.set(inset, inset, w - inset, h - inset);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (segments.isEmpty() || bounds.isEmpty()) {
            return;
        }
        float startAngle = -90f;
        for (Segment segment : segments) {
            float sweep = segment.percent / 100f * 360f;
            if (sweep <= 0f) {
                continue;
            }
            paint.setColor(segment.color);
            canvas.drawArc(bounds, startAngle, sweep, false, paint);
            startAngle += sweep;
        }
    }
}
