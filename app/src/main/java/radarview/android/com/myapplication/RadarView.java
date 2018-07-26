package radarview.android.com.myapplication;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.SweepGradient;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

/**
 * Created by luotao
 * 2018/5/17
 * emil:luotaosc@foxmail.com
 * qq:751423471
 *
 * @author 罗涛
 */
public class RadarView extends View {
    /**
     * 默认的主题颜色
     */
    private int DEFAULT_COLOR = Color.parseColor("#91D7F4");
    //圈的颜色
    private int mCircleColor = DEFAULT_COLOR;
    /*
    圆圈的数量  默认3个
     */
    private int mCircleNum = 3;
    /*
    弧形的颜色   做渐变透明处理
     */
    private int mSweepColor = DEFAULT_COLOR;
    /**
     * 水滴的颜色
     */
    private int mRaindropColor = DEFAULT_COLOR;
    /**
     * 水滴的数量 这里表示的是水滴最多能同时出现的数量。因为水滴是随机产生的，数量是不确定的
     */
    private int mRaindropNum;
    /**
     * 是否显示交叉线
     */
    private boolean mShowCross;
    /**
     * 是否显示水滴
     */
    private boolean mShowRaindrop;
    /**
     * 表示水滴的速度  几秒转一圈
     */
    private float mSpeed = 3.0f;
    /**
     * 水滴消失的速度
     */
    private float mFlicker = 3.0f;

    // 圆的画笔
    private Paint mCirclePaint;
    //扫描效果的画笔
    private Paint mSweepPaint;
    // 水滴的画笔
    private Paint mRaindropPaint;
    //扫描时的扫描旋转角度。
    private float mDegrees;
    //是否扫描
    private boolean isScanning = false;

    public RadarView(Context context) {
        super(context);
        init();
    }


    public RadarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
        getAttrs(context, attrs);
    }


    public RadarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        getAttrs(context, attrs);
        init();
    }

    private void getAttrs(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RadarView);
            mCircleColor = typedArray.getColor(R.styleable.RadarView_circleColor, DEFAULT_COLOR);
            mCircleNum = typedArray.getInt(R.styleable.RadarView_circleNum, mCircleNum);
            if (mCircleNum < 1) {
                mCircleNum = 3;
            }
            mSweepColor = typedArray.getColor(R.styleable.RadarView_sweepColor, DEFAULT_COLOR);
            mRaindropColor = typedArray.getColor(R.styleable.RadarView_raindropColor, DEFAULT_COLOR);
            mRaindropNum = typedArray.getInt(R.styleable.RadarView_raindropNum, mRaindropNum);
            mShowCross = typedArray.getBoolean(R.styleable.RadarView_showCross, true);
            mShowRaindrop = typedArray.getBoolean(R.styleable.RadarView_showRaindrop, true);
            mSpeed = typedArray.getFloat(R.styleable.RadarView_speed, mSpeed);
            if (mSpeed <= 0) {
                mSpeed = 3;
            }
            mFlicker = typedArray.getFloat(R.styleable.RadarView_flicker, mFlicker);
            if (mFlicker <= 0) {
                mFlicker = 3;
            }
            //回收
            typedArray.recycle();
        }
    }

    /**
     * 初始化画笔
     */
    private void init() {
        mCirclePaint = new Paint();
        mCirclePaint.setAntiAlias(true);
        mCirclePaint.setStrokeWidth(1f);
        mCirclePaint.setColor(mCircleColor);
        mCirclePaint.setStyle(Paint.Style.STROKE);

        mRaindropPaint = new Paint();
        mRaindropPaint.setStyle(Paint.Style.FILL);
        mRaindropPaint.setAntiAlias(true);

        mSweepPaint = new Paint();
        mSweepPaint.setAntiAlias(true);
    }

    /**
     * 设置尺寸大小
     *
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //设置宽高,默认200dp
        int defaultSize = dp2px(getContext(), 200);
        setMeasuredDimension(measureWidth(widthMeasureSpec, defaultSize),
                measureHeigth(heightMeasureSpec, defaultSize));
    }

    /**
     * 测量宽度
     *
     * @param heightMeasureSpec
     * @param defaultSize
     * @return
     */
    private int measureHeigth(int heightMeasureSpec, int defaultSize) {
        int result;
        int mode = MeasureSpec.getMode(heightMeasureSpec);
        int specSize = MeasureSpec.getSize(heightMeasureSpec);

        if (mode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = defaultSize + getPaddingLeft() + getPaddingRight();
            if (mode == MeasureSpec.AT_MOST) {
                result = Math.min(result, defaultSize);
            }
        }
        result = Math.max(result, getSuggestedMinimumHeight());
        return result;
    }

    private int measureWidth(int widthMeasureSpec, int defaultSize) {
        int result;
        int mode = MeasureSpec.getMode(widthMeasureSpec);
        int size = MeasureSpec.getSize(widthMeasureSpec);

        if (mode == MeasureSpec.EXACTLY) {
            result = size;
        } else {
            result = defaultSize + getPaddingRight() + getPaddingLeft();
            if (mode == MeasureSpec.AT_MOST) {
                result = Math.min(defaultSize, size);
            }
        }
        result = Math.max(result, getSuggestedMinimumWidth());
        return result;
    }

    /**
     * dp to px
     *
     * @param context
     * @param dpVal
     * @return
     */
    private int dp2px(Context context, int dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpVal
                , context.getResources().getDisplayMetrics());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //计算圆的半径
        int width = getWidth() - getPaddingLeft() - getPaddingRight();
        int height = getHeight() - getPaddingBottom() - getPaddingTop();
        int radius = Math.min(width, height) / 2;

        //计算圆心
        int cx = getPaddingLeft() + (getWidth() - getPaddingLeft() - getPaddingRight()) / 2;
        int cy = getPaddingTop() + (getHeight() - getPaddingTop() - getPaddingBottom()) / 2;
        //画圆
        drawCircle(canvas, cx, cy, radius);
        //画交叉线
        if (mShowCross) {
            drawCross(canvas, cx, cy, radius);
        }
        //画扫描线
        if (isScanning) {
            if (mShowRaindrop) {
                drawRainDrop(canvas, cx, cy, radius);
            }
            drawSweep(canvas, cx, cy, radius);
            //计算弧度
            mDegrees = (mDegrees + (360 / mSpeed / 60)) % 360;
            Log.e("test", "mDegrees=====" + mDegrees);
            //触发View重新绘制，通过不断的绘制实现View的扫描动画效果
            invalidate();
        }
    }

    /**
     * 画圆弧
     *
     * @param canvas
     * @param cx
     * @param cy
     * @param radius
     */
    private void drawSweep(Canvas canvas, int cx, int cy, int radius) {
        SweepGradient sweepGradient = new SweepGradient(cx, cy, new int[]{Color.TRANSPARENT, changeAlpha(mSweepColor, 0)
                , changeAlpha(mSweepColor, 168), changeAlpha(mSweepColor, 255)},
                new float[]{0.0f, 0.6f, 0.99f, 1f});
        mSweepPaint.setShader(sweepGradient);
        canvas.rotate(-90 + mDegrees, cx, cy);
        canvas.drawCircle(cx, cy, radius, mSweepPaint);
    }

    /**
     * 改变透明度
     *
     * @param sweepColor
     * @param alpha
     * @return
     */
    private int changeAlpha(int sweepColor, int alpha) {
        int red = Color.red(sweepColor);
        int green = Color.green(sweepColor);
        int blue = Color.blue(sweepColor);
        return Color.argb(alpha, red, green, blue);
    }

    /**
     * 画水滴
     *
     * @param canvas
     * @param cx
     * @param cy
     * @param radius
     */
    private void drawRainDrop(Canvas canvas, int cx, int cy, int radius) {

    }

    /**
     * 画交叉线
     *
     * @param canvas
     * @param cx
     * @param cy
     * @param radius
     */
    private void drawCross(Canvas canvas, int cx, int cy, int radius) {
        //画水平线
        canvas.drawLine(cx - radius, cy, cx + radius, cy, mCirclePaint);
        //画垂直线
        canvas.drawLine(cx, cy - radius, cx, cy + radius, mCirclePaint);
    }

    /**
     * 画圆
     *
     * @param canvas
     * @param cx
     * @param cy
     * @param radius
     */
    private void drawCircle(Canvas canvas, int cx, int cy, int radius) {
        //画mCircleNum 个圆
        for (int i = 0; i < mCircleNum; i++) {
            canvas.drawCircle(cx, cy, radius - (radius / mCircleNum * i), mCirclePaint);
        }
    }

    public void start() {
        if (!isScanning) {
            isScanning = true;
            invalidate();
        }
    }

    public void stop() {
        if (isScanning) {
            isScanning = false;
            //            mRaindrops.clear();
            mDegrees = 0.0f;
        }
    }
}
