package com.simon.ota;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

/**
 * oad升级
 * Created by Administrator on 2018/4/17.
 */

public class UpgadeView extends View {
    private Paint mPaint;
    private int widthPixels;
    private int heightPixels;
    private float mProgress;

    private int startTemperature = 35;
    private int endTemperature = 42;
    private int paddingTop = 100;

    public UpgadeView(Context context) {
        this(context, null);
    }

    public UpgadeView(Context context,  AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public UpgadeView(Context context,  AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        widthPixels = displayMetrics.widthPixels;
        heightPixels = displayMetrics.heightPixels;
    }

    public void setProgress(int progress) {
        this.mProgress = progress;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPaint.setColor(Color.YELLOW);
        float bottom = (float) (heightPixels * (1 - 0.3));
        float top = (float) (heightPixels * (1 - 0.72));
        float v = (bottom - top) / 100;
        canvas.drawRect(0, bottom - v * mProgress, widthPixels, bottom, mPaint);
        mPaint.setColor(Color.BLACK);
    }
}
