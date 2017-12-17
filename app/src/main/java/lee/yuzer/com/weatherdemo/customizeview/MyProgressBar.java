package lee.yuzer.com.weatherdemo.customizeview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import lee.yuzer.com.weatherdemo.R;

/**
 * Created by Yuzer on 2017/12/15.
 */

public class MyProgressBar extends View {
    private int circleRectWidth;//正方形控件边长
    private float arcStrokeWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getContext().getResources().getDisplayMetrics());//圆弧边框宽度
    private float charNameTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10, getContext().getResources().getDisplayMetrics());//图标名称字符大小
    private float unitTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 20, getContext().getResources().getDisplayMetrics());//圆形中心当前进度数字字符大小
    private Paint backArcPaint;//底层圆弧画笔--最大进度
    private Paint fontArcPaint;//上层圆弧画笔--当前进度
    private Paint chartNamePaint;//图标名称画笔
    private Paint currentProgressNumberPaint;//中心数字画笔
    private int circleRadius;//圆弧半径
    private int centerX;//中心点X坐标
    private int centerY;//中心点Y坐标
    private final float RADIUS_RATIO = 0.3f;//半径占控件宽度的百分比
    private final int START_ANGLE = 135;//圆弧开始绘制的角度
    private final int SWEEP_ANGLE = 270;//绘制圆弧所跨越的角度
    private final int BOTTOM_CIRCLE_BORDER_COLOR = Color.parseColor("#aaf0f1f2");//底层圆弧的颜色
    private final int TOP_CIRCLE_BORDER_COLOR = Color.parseColor("#97FFFF");
//    private final int TITLEANDUNIT_COLOR = Color.parseColor("#fe751a");
    private final int BG_COLOR = Color.TRANSPARENT;
    private int currentProgress = 0;//默认当前进度
    private String charTitle = "无标题";//默认图标名称
    private int maxProgress = 500;//默认最大值
    private String progressUnit = "米";//默认单位
    private float unitTextWidth;//默认单位所占宽度
    private float yPosBottomAlign;//默认标题的y轴坐标
    private RectF mRectF;//画圆弧所依据的矩形

    public MyProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MyProgressBar);
        Integer currentProgress = typedArray.getInteger(R.styleable.MyProgressBar_current_progress, 0);
        String charTitle = typedArray.getString(R.styleable.MyProgressBar_chart_title);
        Integer maxProgress = typedArray.getInteger(R.styleable.MyProgressBar_max_progress, 0);
        String progressUnit = typedArray.getString(R.styleable.MyProgressBar_progress_unit);

        if (currentProgress > 0) {
            this.currentProgress = currentProgress;
        }

        if (maxProgress > 0) {
            this.maxProgress = maxProgress;
        }

        if (!TextUtils.isEmpty(charTitle)) {
            this.charTitle = charTitle;
        }

        if (!TextUtils.isEmpty(progressUnit)) {
            this.progressUnit = progressUnit;
        }

        typedArray.recycle();

        init();
    }

    private void init() {
        backArcPaint = new Paint();
        backArcPaint.setAntiAlias(true);
        backArcPaint.setColor(BOTTOM_CIRCLE_BORDER_COLOR);
        backArcPaint.setStrokeWidth(arcStrokeWidth);
        backArcPaint.setStyle(Paint.Style.STROKE);
        backArcPaint.setStrokeCap(Paint.Cap.ROUND);

        fontArcPaint = new Paint();
        fontArcPaint.setAntiAlias(true);
        fontArcPaint.setColor(TOP_CIRCLE_BORDER_COLOR);
        fontArcPaint.setStrokeWidth(arcStrokeWidth);
        fontArcPaint.setStyle(Paint.Style.STROKE);
        fontArcPaint.setStrokeCap(Paint.Cap.ROUND);

        chartNamePaint = new Paint();
        chartNamePaint.setStyle(Paint.Style.FILL);
        chartNamePaint.setAntiAlias(true);
        chartNamePaint.setTextSize(charNameTextSize);
        chartNamePaint.setColor(Color.WHITE);

        unitTextWidth = chartNamePaint.measureText(progressUnit);

        currentProgressNumberPaint = new Paint();
        currentProgressNumberPaint.setStyle(Paint.Style.FILL);
        currentProgressNumberPaint.setAntiAlias(true);
        currentProgressNumberPaint.setTextSize(unitTextSize);
        currentProgressNumberPaint.setColor(Color.WHITE);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);

        if (widthSpecMode == MeasureSpec.AT_MOST || heightSpecMode == MeasureSpec.AT_MOST) {
            float defaultSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    200, getContext().getResources().getDisplayMetrics());
            widthSpecSize = (int) defaultSize;
            heightSpecSize = (int) defaultSize;
        }
        setMeasuredDimension(Math.min(widthSpecSize, heightSpecSize), Math.min(widthSpecSize, heightSpecSize));

        circleRectWidth = Math.min(widthSpecSize, heightSpecSize);
        circleRadius = (int) (circleRectWidth * RADIUS_RATIO);
        centerX = circleRectWidth / 2;
        centerY = circleRectWidth / 2;
        float rad = (float) (45 * Math.PI / 180);
        yPosBottomAlign = (float) (circleRadius * Math.sin(rad) + centerY);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawChart(canvas, currentProgress);
    }

    private void drawChart(Canvas canvas, float loopIndex) {
        canvas.drawColor(BG_COLOR);
        //1.绘制背景圆弧
        if (mRectF == null) {
            mRectF = new RectF(centerX - circleRadius,//left
                    centerY - circleRadius,//top
                    centerX + circleRadius,//right
                    centerY + circleRadius);//bottom
        }

        canvas.drawArc(mRectF, START_ANGLE, SWEEP_ANGLE, false, backArcPaint);

        //2.绘制进度圆弧
        if (maxProgress > 0) {
            canvas.drawArc(mRectF, START_ANGLE, loopIndex / maxProgress * 270, false, fontArcPaint);
        }

        //3.绘制底部文案
        float chartNameWidth = chartNamePaint.measureText(charTitle);
        Paint.FontMetrics fontMetrics = chartNamePaint.getFontMetrics();
        float chartNameHeight = fontMetrics.descent - fontMetrics.ascent;
        canvas.drawText(charTitle, centerX - chartNameWidth / 2, (float) (yPosBottomAlign + chartNameHeight * 1.5), chartNamePaint);

        //4.绘制中间的当前进度
        float hourNumberWidth = currentProgressNumberPaint.measureText(String.valueOf(loopIndex));
        float hourNumberHeight = currentProgressNumberPaint.getFontMetrics().bottom - currentProgressNumberPaint.getFontMetrics().top;
        //4.1绘制当前进度数字
        canvas.drawText(String.valueOf(loopIndex), centerX - hourNumberWidth / 2, centerY + chartNameHeight / 4, currentProgressNumberPaint);
        //4.1绘制进度单位
        canvas.drawText(progressUnit, centerX - unitTextWidth / 2, centerY + chartNameHeight / 4 + hourNumberHeight / 2, chartNamePaint);
        //绘制最大最小数值
        float minnumberWidth = chartNamePaint.measureText("0");
        float maxnumberWidth = chartNamePaint.measureText(String.valueOf(maxProgress));
        canvas.drawText(String.valueOf(0), centerX - yPosBottomAlign + centerY - minnumberWidth / 2, (float) (yPosBottomAlign + chartNameHeight * 1.5), chartNamePaint);
        canvas.drawText(String.valueOf(maxProgress), centerX - centerY + yPosBottomAlign - maxnumberWidth / 2, (float) (yPosBottomAlign + chartNameHeight * 1.5), chartNamePaint);
    }

    public MyProgressBar setCurrentProgress(int hour) {
        //非动画版
//        if (hour < 0) {
//            currentProgress = 0;
//        } else if (hour > maxProgress) {
//            currentProgress = maxProgress;
//        } else {
//            currentProgress = hour;
//        }

        //动画版
        if (hour < 0) {
            currentProgress = 0;
        } else if (hour > maxProgress) {
            currentProgress = maxProgress;
        } else {
            currentProgress = hour;
        }
        invalidate();
        return this;
    }

    public int getCurrentProgress(){
        return currentProgress;
    }

    /**
     * 设置图标名称(底部)
     *
     * @param chartName
     */
    public MyProgressBar setProgressUnit(String chartName) {
        if (TextUtils.isEmpty(chartName))
            return this;

        this.charTitle = chartName;
        return this;
    }

    /**
     * 设置最大进度
     */
    public MyProgressBar setMaxProgress(int maxHour) {
        if (maxHour <= 0) {
            return this;
        } else if (maxHour < currentProgress) {
            this.maxProgress = currentProgress;
        } else {
            this.maxProgress = maxHour;
        }
        return this;
    }

   //非动画版
    public void refresh() {
        invalidate();

    }
}
