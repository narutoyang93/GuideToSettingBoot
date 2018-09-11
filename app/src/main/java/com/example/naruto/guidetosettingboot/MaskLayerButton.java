package com.example.naruto.guidetosettingboot;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
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
    private final static int DEFAULT_STROKE_WIDTH = 1;//单位：dp
    private final static int DEFAULT_STROKE_DASH_SIZE = 3;//默认虚线间隔，单位：dp
    private final static int STROKE_TYPE_SOLID = 0;//实线
    private final static int STROKE_TYPE_DASH = 1;//虚线
    private Context context;
    private boolean isOnPress = false;//是否处于按下状态
    private boolean isNeedMaskLayer;//是否需要遮罩层
    private int maskLayerColor;//遮罩层颜色
    private float strokeWidth;//描边画笔宽度//单位：px
    private int strokeColor;//描边颜色
    private int radius;//圆角半径
    private int strokeType;//描边类型
    private int strokeDashSize;//虚线间隔
    private float constraintRadiusWithWidth_percent;//圆角半径相对于控件宽度的比例
    private float constraintRadiusWithHeight_percent;//圆角半径相对于控件高度的比例

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
        /**
         * 通过这个方法，将attrs.xml中定义的declare-styleable的所有属性的值存储到TypedArray中
         */
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.MaskLayerButton);
        //从TypedArray中取出对应的值来为要设置的属性赋值
        radius = ta.getDimensionPixelSize(R.styleable.MaskLayerButton_radius, 0);
        isNeedMaskLayer = ta.getBoolean(R.styleable.MaskLayerButton_needMaskLayer, true);
        maskLayerColor = ta.getColor(R.styleable.MaskLayerButton_maskLayerColor, DEFAULT_MASK_LAYER_COLOR);
        strokeWidth = ta.getDimensionPixelSize(R.styleable.MaskLayerButton_strokeWidth, dip2px(DEFAULT_STROKE_WIDTH));
        strokeColor = ta.getColor(R.styleable.MaskLayerButton_strokeColor, -1);
        strokeType = ta.getInt(R.styleable.MaskLayerButton_strokeType, STROKE_TYPE_SOLID);
        strokeDashSize = ta.getDimensionPixelSize(R.styleable.MaskLayerButton_strokeDashSize, dip2px(DEFAULT_STROKE_DASH_SIZE));
        constraintRadiusWithWidth_percent = ta.getFloat(R.styleable.MaskLayerButton_constraintRadiusWithWidth_percent, 0);
        constraintRadiusWithHeight_percent = ta.getFloat(R.styleable.MaskLayerButton_constraintRadiusWithHeight_percent, 0);
        ta.recycle();
    }

    @Override
    public void onDrawForeground(Canvas canvas) {
        super.onDrawForeground(canvas);
    }


    /**
     * 根据比例设置圆角半径
     *
     * @param isByWidth
     */
    private void setRadiusByPercent(boolean isByWidth) {
        int base = 0;
        float percent = 0;
        if (isByWidth) {
            base = getWidth();
            percent = constraintRadiusWithWidth_percent;
        } else {
            base = getHeight();
            percent = constraintRadiusWithHeight_percent;
        }
        if (percent > 0) {
            if (percent > 1) {
                percent = 1;
            }
            radius = (int) (base * percent);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Paint paint = new Paint();
        Drawable background = getBackground();
        if (radius == 0) {
            if (constraintRadiusWithWidth_percent > 0) {
                setRadiusByPercent(true);
            } else if (constraintRadiusWithHeight_percent > 0) {
                setRadiusByPercent(false);
            }
        }
        if (radius > 0 && background != null) {//如果圆角半径>0且背景不为空，需要绘制圆角背景，需在父类调用onDraw()前，避免覆盖文字
            paint.reset();
            Rect rect = canvas.getClipBounds();
            Bitmap bitmap;
            if (background instanceof ColorDrawable) {
                ColorDrawable colorDrawable = (ColorDrawable) background;
                int color = colorDrawable.getColor();

                //生成纯色bitmap
                bitmap = Bitmap.createBitmap(rect.width(), rect.height(), Bitmap.Config.ARGB_8888);
                bitmap.eraseColor(color);//填充颜色
            } else {
                //bitmap = ((BitmapDrawable) background).getBitmap();
                bitmap = drawableToBitmap(background);
            }
            Bitmap roundBitmap = getRoundBitmap(bitmap);
            //Bitmap roundBitmap =bitmap;
            setBackgroundDrawable(new BitmapDrawable(roundBitmap));//用圆角化后的背景替换原有背景
        }
        super.onDraw(canvas);
        if (strokeColor != -1) {//描边
            drawMaskLayerOrContour(paint, canvas, false);
        }
        if (isNeedMaskLayer) {
            if (isOnPress) {
                drawMaskLayerOrContour(paint, canvas, true);
            }
        }
    }

    /**
     * 绘制遮罩层或描边
     *
     * @param paint
     * @param canvas
     * @param isMaskLayer
     */
    private void drawMaskLayerOrContour(Paint paint, Canvas canvas, boolean isMaskLayer) {
        paint.reset();
        if (isMaskLayer) {//遮罩层
            paint.setColor(maskLayerColor);
            paint.setStyle(Paint.Style.FILL);
        } else {//描边
            paint.setColor(strokeColor);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(strokeWidth);
            paint.setStrokeJoin(Paint.Join.ROUND);
            if (strokeType == STROKE_TYPE_DASH) {
                paint.setPathEffect(new DashPathEffect(new float[]{strokeDashSize, strokeDashSize}, 0));
            }
        }
        RectF rf = new RectF(canvas.getClipBounds());
        if (!isMaskLayer) {
            float padding = strokeWidth / 2;
            rf.bottom -= padding;
            rf.right -= padding;
            rf.top += padding;
            rf.left += padding;
        }
        if (radius > 0) {//圆角矩形
            paint.setAntiAlias(true);
            canvas.drawRoundRect(rf, radius, radius, paint);
        } else {//矩形
            canvas.drawRect(canvas.getClipBounds(), paint);
        }
    }

    /**
     * 获取圆角矩形图片方法
     *
     * @param bitmap
     * @return Bitmap
     * @author caizhiming
     */
    private Bitmap getRoundBitmap(Bitmap bitmap) {
        Bitmap outputBitmap = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(outputBitmap);
        Paint paint = new Paint();
        final int color = 0xff424242;

        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);

        canvas.drawRoundRect(rectF, radius, radius, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return outputBitmap;
    }

    private Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bd = (BitmapDrawable) drawable;
            return bd.getBitmap();
        }
        // 当设置不为图片，为颜色时，获取的drawable宽高会有问题，所有当为颜色时候获取控件的宽高
        int w = drawable.getIntrinsicWidth() <= 0 ? getWidth() : drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight() <= 0 ? getHeight() : drawable.getIntrinsicHeight();
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        drawable.draw(canvas);
        return bitmap;
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
     * 将dip或dp值转换为px值，保证尺寸大小不变
     *
     * @param dipValue
     * @return
     */
    public int dip2px(float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
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


    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public int getMaskLayerColor() {
        return maskLayerColor;
    }

    public void setMaskLayerColor(int maskLayerColor) {
        this.maskLayerColor = maskLayerColor;
    }

    public float getStrokeWidth() {
        return strokeWidth;
    }

    public void setStrokeWidth(float strokeWidth) {
        this.strokeWidth = strokeWidth;
    }

    public int getStrokeColor() {
        return strokeColor;
    }

    public void setStrokeColor(int strokeColor) {
        this.strokeColor = strokeColor;
    }

    public boolean isNeedMaskLayer() {
        return isNeedMaskLayer;
    }

    public void setNeedMaskLayer(boolean needMaskLayer) {
        isNeedMaskLayer = needMaskLayer;
    }

    public int getStrokeType() {
        return strokeType;
    }

    public void setStrokeType(int strokeType) {
        this.strokeType = strokeType;
    }

    public int getStrokeDashSize() {
        return strokeDashSize;
    }

    public void setStrokeDashSize(int strokeDashSize) {
        this.strokeDashSize = strokeDashSize;
    }
}
