package com.example.naruto.guidetosettingboot;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * @Purpose 具有遮罩层的按钮，按下后可以在文本上方显示遮罩层，无需selector,支持圆角和描边，无需自定义shape
 * @Author Naruto Yang
 * @CreateDate ${2018-06-07}
 * @Note
 */
public class MaskLayerButton extends AppCompatButton {
    private final static int DEFAULT_MASK_LAYER_COLOR = 0x21000000;
    private boolean isOnPress = false;//是否处于按下状态
    private boolean isNeedMaskLayer;//是否需要遮罩层
    private int maskLayerColor;//遮罩层颜色
    private CustomViewHelper customViewHelper;
    private Context context;

    public MaskLayerButton(Context context) {
        super(context);
        this.context = context;
    }

    public MaskLayerButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init(attrs);
    }

    public MaskLayerButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init(attrs);
    }

    public void init(AttributeSet attrs) {
        customViewHelper = new CustomViewHelper(context, "MaskLayerButton", this);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.MaskLayerButton);
        //从TypedArray中取出对应的值来为要设置的属性赋值
        isNeedMaskLayer = ta.getBoolean(R.styleable.MaskLayerButton_needMaskLayer, true);
        maskLayerColor = ta.getColor(R.styleable.MaskLayerButton_maskLayerColor, DEFAULT_MASK_LAYER_COLOR);
        customViewHelper.getAttrs(ta);
        ta.recycle();
    }

    @Override
    public void onDrawForeground(Canvas canvas) {
        super.onDrawForeground(canvas);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        Paint paint = new Paint();
        customViewHelper.setRadiusByPercent();
        customViewHelper.makeBackgroundRoundRect(paint, canvas);
        super.onDraw(canvas);
        customViewHelper.drawStroke(paint, canvas);
        if (isNeedMaskLayer) {
            if (isOnPress) {
                customViewHelper.drawMaskLayer(paint, canvas, maskLayerColor);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isNeedMaskLayer) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    isOnPress = true;
                    invalidate();
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (!isTouchOnView(event)) {
                        resetOnPressState();
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_OUTSIDE:
                    resetOnPressState();
                    break;
            }
        }
        return super.onTouchEvent(event);
    }

    /**
     * 触摸点是否在当前控件内(圆角问题暂不考虑)
     *
     * @param event
     * @return
     */
    public boolean isTouchOnView(MotionEvent event) {
        //当前手指坐标
        float eventRawX = event.getRawX();
        float eventRawY = event.getRawY();

        //获取控件在屏幕的位置
        int[] location = new int[2];
        this.getLocationOnScreen(location);

        //控件相对于屏幕的坐标
        int x = location[0];
        int y = location[1];

        return (eventRawX >= x && eventRawX <= x + getWidth()) && (eventRawY >= y && eventRawY <= y + getHeight());
    }

    /**
     * 重置状态
     */
    public void resetOnPressState() {
        if (isOnPress) {
            isOnPress = false;
            invalidate();
        }
    }

    public int getMaskLayerColor() {
        return maskLayerColor;
    }

    public void setMaskLayerColor(int maskLayerColor) {
        this.maskLayerColor = maskLayerColor;
    }

    public boolean isNeedMaskLayer() {
        return isNeedMaskLayer;
    }

    public void setNeedMaskLayer(boolean needMaskLayer) {
        isNeedMaskLayer = needMaskLayer;
    }

}
