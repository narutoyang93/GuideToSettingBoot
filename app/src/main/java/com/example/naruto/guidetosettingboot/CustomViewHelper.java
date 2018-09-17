package com.example.naruto.guidetosettingboot;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

import java.lang.reflect.Field;

/**
 * @Purpose
 * @Author Naruto Yang
 * @CreateDate 2018/9/14 0014
 * @Note
 */
public class CustomViewHelper {
    private final static int DEFAULT_STROKE_COLOR = 0x00000000;
    private final static int DEFAULT_STROKE_WIDTH = 1;//单位：dp
    private final static int DEFAULT_STROKE_DASH_SIZE = 3;//默认虚线间隔，单位：dp
    private final static int STROKE_TYPE_SOLID = 0;//实线
    private final static int STROKE_TYPE_DASH = 1;//虚线
    private float strokeWidth;//描边画笔宽度//单位：px
    private int strokeColor;//描边颜色
    private int radius;//圆角半径
    private int strokeType;//描边类型
    private int strokeDashLength;//虚线线段长度
    private int strokeDashInterval;//虚线线段间隔
    private float constraintRadiusWithWidth_percent;//圆角半径相对于控件宽度的比例
    private float constraintRadiusWithHeight_percent;//圆角半径相对于控件高度的比例
    private Context context;
    private String customViewName;
    private View view;


    public CustomViewHelper(Context context, String customViewName, View view) {
        this.context = context;
        this.customViewName = customViewName;
        this.view = view;
    }

    public void getAttrs(TypedArray ta) {
        radius = ta.getDimensionPixelSize(getAttrsId("radius"), 0);
        strokeWidth = ta.getDimensionPixelSize(getAttrsId("strokeWidth"), dip2px(DEFAULT_STROKE_WIDTH));
        strokeColor = ta.getColor(getAttrsId("strokeColor"), DEFAULT_STROKE_COLOR);
        strokeType = ta.getInt(getAttrsId("strokeType"), STROKE_TYPE_SOLID);
        strokeDashLength = ta.getDimensionPixelSize(getAttrsId("strokeDashLength"), dip2px(DEFAULT_STROKE_DASH_SIZE));
        strokeDashInterval = ta.getDimensionPixelSize(getAttrsId("strokeDashInterval"), dip2px(DEFAULT_STROKE_DASH_SIZE));
        constraintRadiusWithWidth_percent = ta.getFloat(getAttrsId("constraintRadiusWithWidth_percent"), 0);
        constraintRadiusWithHeight_percent = ta.getFloat(getAttrsId("constraintRadiusWithHeight_percent"), 0);
    }

    private int getAttrsId(String attrs) {
        int id = 999999999;
        try {
            Field field = R.styleable.class.getDeclaredField(customViewName + "_" + attrs);
            id = (int) field.get(R.styleable.class);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return id;
    }

    /**
     * 将dip或dp值转换为px值，保证尺寸大小不变
     *
     * @param dipValue
     * @return
     */
    private int dip2px(float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    public void setRadiusByPercent() {
        if (radius == 0) {
            if (constraintRadiusWithWidth_percent > 0) {
                setRadiusByPercent(true);
            } else if (constraintRadiusWithHeight_percent > 0) {
                setRadiusByPercent(false);
            }
        }
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
            base = view.getWidth();
            percent = constraintRadiusWithWidth_percent;
        } else {
            base = view.getHeight();
            percent = constraintRadiusWithHeight_percent;
        }
        if (percent > 0) {
            if (percent > 1) {
                percent = 1;
            }
            radius = (int) (base * percent);
        }
    }

    /**
     * 绘制描边
     *
     * @param paint
     * @param canvas
     */
    public void drawStroke(Paint paint, Canvas canvas) {
        if (strokeColor == DEFAULT_STROKE_COLOR) {
            return;
        }
        paint.reset();
        paint.setColor(strokeColor);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(strokeWidth);
        paint.setStrokeJoin(Paint.Join.ROUND);

        RectF rf = new RectF(canvas.getClipBounds());
        float padding = strokeWidth / 2;
        rf.left += padding;
        rf.top += padding;
        rf.right -= padding;
        rf.bottom -= padding;

        if (strokeType == STROKE_TYPE_DASH) {
            paint.setPathEffect(new DashPathEffect(new float[]{strokeDashLength, strokeDashInterval}, 0));
        }
        drawRect(paint, canvas, rf);
    }

    /**
     * 设置view为圆角
     *
     * @param canvas
     */
    public void makeToRoundRect(Canvas canvas) {
        int paddingTop = view.getPaddingTop();
        int paddingBottom = view.getPaddingBottom();
        int paddingLeft = view.getPaddingLeft();
        int paddingRight = view.getPaddingRight();
        int r = radius - Math.min(Math.min(paddingLeft, paddingRight), Math.min(paddingTop, paddingBottom));
        if (radius > 0 && r > 0 && view.getWidth() >= 2 * radius && view.getHeight() >= 2 * radius) {
            Path path = new Path();
            RectF rf = new RectF(canvas.getClipBounds());
            rf.left += paddingLeft;
            rf.top += paddingTop;
            rf.right -= paddingRight;
            rf.bottom -= paddingBottom;
            path.addRoundRect(rf, r, r, Path.Direction.CW);
            canvas.clipPath(path);
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
        int w = drawable.getIntrinsicWidth() <= 0 ? view.getWidth() : drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight() <= 0 ? view.getHeight() : drawable.getIntrinsicHeight();
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        drawable.draw(canvas);
        return bitmap;
    }

    /***
     * 缩放Bitmap
     * @param bitmap
     * @param newWidth
     * @param newHeight
     * @return
     */
    public static Bitmap resizeImage(Bitmap bitmap, int newWidth, int newHeight) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        if (width == newWidth && height == newHeight) {
            return bitmap;
        }

        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;

        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        return newBitmap;
    }

    /**
     * 绘制遮罩层
     *
     * @param paint
     * @param canvas
     * @param maskLayerColor
     */
    public void drawMaskLayer(Paint paint, Canvas canvas, int maskLayerColor) {
        paint.reset();
        paint.setColor(maskLayerColor);
        paint.setStyle(Paint.Style.FILL);
        RectF rf = new RectF(canvas.getClipBounds());
        drawRect(paint, canvas, rf);
    }

    /**
     * 绘制矩形，填充或者描边
     *
     * @param paint
     * @param canvas
     * @param rf
     */
    public void drawRect(Paint paint, Canvas canvas, RectF rf) {
        if (radius > 0) {//圆角矩形
            paint.setAntiAlias(true);
            canvas.drawRoundRect(rf, radius, radius, paint);
        } else {//矩形
            canvas.drawRect(canvas.getClipBounds(), paint);
        }
    }

    /**
     * 使背景圆角
     *
     * @param paint
     * @param canvas
     */
    public void makeBackgroundRoundRect(Paint paint, Canvas canvas) {
        Drawable background = view.getBackground();
        if (radius > 0 && background != null) {
            Bitmap bitmap;
            if (background instanceof ColorDrawable) {
                paint.reset();
                Rect rect = canvas.getClipBounds();
                ColorDrawable colorDrawable = (ColorDrawable) background;
                int color = colorDrawable.getColor();

                //生成纯色bitmap
                bitmap = Bitmap.createBitmap(rect.width(), rect.height(), Bitmap.Config.ARGB_8888);
                bitmap.eraseColor(color);//填充颜色
            } else {
                bitmap = drawableToBitmap(background);
            }
            Bitmap roundBitmap = getRoundBitmap(bitmap);
            view.setBackgroundDrawable(new BitmapDrawable(roundBitmap));//用圆角化后的背景替换原有背景
        }
    }
}
