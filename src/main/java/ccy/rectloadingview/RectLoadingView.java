package ccy.rectloadingview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import ccy.rectloadingview.animcontroller.DefaultAnimController;
import ccy.rectloadingview.animcontroller.IAnimController;

/**
 * Created by ccy on 2017-11-21.
 */

public class RectLoadingView extends View {

    //默认参数
    private final float MAX_RECT_HEIGHT = dp2pxF(50);
    private final float MIN_RECT_HEIGHT = dp2pxF(30);
    private static final int DURATION = 1000;
    private static final int RECT_COUNT = 6;
    private static final int RECT_COLOR = 0xff13b5b1;


    //数据相关
    private float maxRectHeight;    //矩形最大高度
    private float minRectHeight;    //矩形最小高度
    private long duration;          //一次完整动画时长
    private int rectCount;          //矩形数量
    private int rectColor;          //矩形颜色
    private boolean roundMode;      //圆角矩形

    private float deltaX; //相邻矩形间隔
    private float deltaY; //相邻矩形高度差
    private float rectWidth; //矩形宽度

    //绘图及动画
    private Paint mPaint;
    private RectF[] originrRects; //每个矩形的原始rect
    private float fractions[]; //当前矩形高度相对于自己最高高度的比例值
    private IAnimController animController;  //动画控制器
    private RectF tempRectf = new RectF();


    public RectLoadingView(Context context) {
        this(context, null);
    }

    public RectLoadingView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }


    public RectLoadingView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.RectLoadingView);
        maxRectHeight = ta.getDimension(R.styleable.RectLoadingView_maxRectHeight, MAX_RECT_HEIGHT);
        minRectHeight = ta.getDimension(R.styleable.RectLoadingView_minRectHeight, MIN_RECT_HEIGHT);
        duration = ta.getInt(R.styleable.RectLoadingView_duartion, DURATION);
        rectCount = ta.getInt(R.styleable.RectLoadingView_count, RECT_COUNT);
        rectColor = ta.getColor(R.styleable.RectLoadingView_color, RECT_COLOR);
        roundMode = ta.getBoolean(R.styleable.RectLoadingView_round_mode, true);
        ta.recycle();

        checkAttr();

        initPaint();

        animController = new DefaultAnimController();
    }

    /**
     * 校正初始参数
     */
    private void checkAttr() {
        //不强制要求maxRectHeight > minRectHeight,反过来也可以，效果即变为高度先递减再递增
        maxRectHeight = maxRectHeight < 0 ? MAX_RECT_HEIGHT : maxRectHeight;
        minRectHeight = minRectHeight < 0 ? MIN_RECT_HEIGHT : minRectHeight;
        duration = duration < 0 ? DURATION : duration;
        rectCount = rectCount < 0 ? RECT_COUNT : rectCount;
    }

    /**
     * 初始化画笔相关数据
     */
    private void initPaint() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(rectColor);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int wSize = MeasureSpec.getSize(widthMeasureSpec);
        int wMode = MeasureSpec.getMode(widthMeasureSpec);
        int hSize = MeasureSpec.getSize(heightMeasureSpec);
        int hMode = MeasureSpec.getMode(heightMeasureSpec);

        if (wMode != MeasureSpec.EXACTLY) {
            wSize = dp2px(80);
        }
        if (hMode != MeasureSpec.EXACTLY) {
            hSize = (int) (maxRectHeight + dp2px(30));
        }
        setMeasuredDimension(wSize, hSize);

    }

    /**
     * 初始化绘图所需的参数,为了避免在onDraw中有过多计算、实例等操作，尽量在这提前准备好各种数据
     * 至少在onMeasure之后才能调用该方法
     */
    private void initData() {
        deltaX = getMeasuredWidth() * 1.0f / (rectCount + 1);
        rectWidth = deltaX * 0.25f;

        if (rectCount < 3) { //若矩形数量小于3条，无高度差
            deltaY = 0;
        } else {
            int tempCountY;
            if (rectCount % 2 != 0) {
                //若矩形数为奇数，则只有中间那一条能达到最高高度
                tempCountY = (rectCount - 1) / 2;
            } else {
                //若矩形数为偶数，则中间两条都能达到最高高度
                tempCountY = (rectCount - 2) / 2;
            }

            deltaY = (maxRectHeight - minRectHeight) / tempCountY;
        }

        //计算好onDraw时所需的各种数据

        initFractions();

        originrRects = new RectF[rectCount];
        float centerY = getMeasuredHeight() / 2.0f;
        float left, right, top, bottom;  //矩形顶点
        float rectHeigt; //矩形高度
        for (int i = 0; i < originrRects.length; i++) {
            if (i < Math.ceil(rectCount / 2.0f)) {  //高度递增阶段，若rectCount为偶数，则能整除，若rectCount为奇数，需向上取整
                rectHeigt = minRectHeight + deltaY * i;
            } else {
                rectHeigt = minRectHeight + deltaY * (rectCount - 1 - i);
            }
            left = deltaX * (i + 1) - rectWidth / 2.0f;
            right = deltaX * (i + 1) + rectWidth / 2.0f;
            top = centerY - rectHeigt / 2;
            bottom = centerY + rectHeigt / 2;
            originrRects[i] = new RectF(left, top, right, bottom);
        }
        animController.stopAnim();
        animController.createAnim(duration, this);
    }

    /**
     * 初始化各矩形高度比例值为1.0，即高度无变化
     */
    private void initFractions(){
        fractions = new float[rectCount];
        for (int i = 0; i < fractions.length; i++) {
            fractions[i] = 1.0f;
        }
    }



    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        initData();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();

        drawRects(canvas);

        canvas.restore();
    }

    /**
     * 绘制矩形
     */
    private void drawRects(Canvas canvas) {
        float deltaHeight;
        for (int i = 0; i < rectCount; i++) {
            deltaHeight = originrRects[i].height() - originrRects[i].height() * fractions[i];   //根据比例值转换后的高度变化差
            tempRectf.set(
                    originrRects[i].left,
                    originrRects[i].top + deltaHeight / 2.0f,
                    originrRects[i].right,
                    originrRects[i].bottom - deltaHeight / 2.0f);

            if (roundMode) {
                canvas.drawRoundRect(
                        tempRectf,
                        tempRectf.width() / 2.0f,
                        tempRectf.width() / 2.0f,
                        mPaint);
            } else {
                canvas.drawRect(tempRectf, mPaint);
            }
        }
    }



    @Override
    protected void onDetachedFromWindow() {
        animController.stopAnim();
        super.onDetachedFromWindow();
    }


    public void startAnim() {
//        if(getVisibility() == View.GONE){  //为GONE的时候不会走流程，initData未必已执行
//            return;
//        }
        post(new Runnable() {
            @Override
            public void run() {
                animController.stopAnim(); //先停止
                animController.startAnim();
            }
        });
    }

    /**
     * 停止动画
     * @param reset 是否将矩形高度恢复到初始状态
     */
    public void stopAnim(final boolean reset){
        post(new Runnable() {
            @Override
            public void run() {
                animController.stopAnim();
                if(reset){
                    initFractions();
                    invalidate();
                }
            }
        });
    }



    //setter、getter

    public IAnimController getAnimController() {
        return animController;
    }

    public void setAnimController(IAnimController animController) {
        this.animController.stopAnim(); //释放上一个动画控制器
        this.animController = animController;
        initData();
    }

    public float[] getFractions() {
        return fractions;
    }

    public void setFractions(float[] fractions) {
        this.fractions = fractions;
        invalidate();
    }

    public void setFraction(int index, float fraction) {
        this.fractions[index] = fraction;
        invalidate();
    }


    public float getMaxRectHeight() {
        return maxRectHeight;
    }

    public void setMaxRectHeight(float maxRectHeight) {
        this.maxRectHeight = maxRectHeight;
        requestLayout();
    }

    public float getMinRectHeight() {
        return minRectHeight;
    }

    public void setMinRectHeight(float minRectHeight) {
        this.minRectHeight = minRectHeight;
        requestLayout();
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
        initData();
        invalidate();
    }

    public int getRectCount() {
        return rectCount;
    }

    public void setRectCount(int rectCount) {
        this.rectCount = rectCount;
//        requestLayout();
        initData();
        invalidate();
    }

    public int getRectColor() {
        return rectColor;
    }

    public void setRectColor(int rectColor) {
        this.rectColor = rectColor;
        mPaint.setColor(rectColor);
        invalidate();
    }

    public boolean isRoundMode() {
        return roundMode;
    }

    public void setRoundMode(boolean roundMode) {
        this.roundMode = roundMode;
        invalidate();
    }




    private float dp2pxF(float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    private int dp2px(float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }
}
