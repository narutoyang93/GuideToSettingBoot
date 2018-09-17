package com.example.naruto.guidetosettingboot;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

/**
 * @Purpose
 * @Author Naruto Yang
 * @CreateDate 2018/9/15 0015
 * @Note
 */
public class RoundRectRelativeLayout extends RelativeLayout {
    private Context context;
    private CustomViewHelper customViewHelper;

    public RoundRectRelativeLayout(Context context) {
        super(context);
    }

    public RoundRectRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init(attrs);
    }

    public RoundRectRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init(attrs);
        if (Build.VERSION.SDK_INT < 18) {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
    }

    public void init(AttributeSet attrs) {
        setWillNotDraw(false);
        customViewHelper = new CustomViewHelper(context, "RoundRectRelativeLayout", this);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.RoundRectRelativeLayout);
        //从TypedArray中取出对应的值来为要设置的属性赋值
        customViewHelper.getAttrs(ta);
        ta.recycle();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        Paint paint = new Paint();
        customViewHelper.setRadiusByPercent();
        customViewHelper.makeBackgroundRoundRect(paint, canvas);
        customViewHelper.drawStroke(paint, canvas);
        customViewHelper.makeToRoundRect(canvas);
        super.onDraw(canvas);
    }
}
