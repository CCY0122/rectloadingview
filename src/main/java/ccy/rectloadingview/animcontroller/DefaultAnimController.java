package ccy.rectloadingview.animcontroller;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.util.Log;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import ccy.rectloadingview.RectLoadingView;

/**
 * Created by ccy on 2017-11-22.
 * 从左至右高度递增，然后再慢慢递减
 * 高度升高的时长都一样，高度下降时，越后面的矩形下降的越快
 * <p></p>
 * 详细规则：
 * 1、每根矩形上升时间相同,相邻矩形动画开始的间隔为上升时间的一半
 * 2、第一根矩形的上升时间 + 下降时间 = 动画总时长
 * 3、最后一根矩形的下降时间 = 上升时间，且最后一根矩形下降结束后，正好到达动画总时长。换种说法就是所有矩形是同时结束的
 */

public class DefaultAnimController implements IAnimController {

    private RectLoadingView view;
    private ValueAnimator[] upAnims;
    private ValueAnimator[] downAnims;
    private long upDuration;  //上升动画时长
    private long upDelay; //相邻矩形上升动画间隔
    private long[] downDurations; //各矩形下降动画时长
    private float fractions[];
    //停止动画标志。因为调用cancel()会触发onAnimationEnd监听，而该监听内可能又重新开启了动画，故设该标志位
    private boolean needCancel;

    @Override
    public void createAnim(long duration, final RectLoadingView view) {
        this.view = view;
        fractions = view.getFractions();
        initDurations(duration, fractions.length);
        upAnims = new ValueAnimator[fractions.length];
        downAnims = new ValueAnimator[fractions.length];
        for (int i = 0; i < fractions.length; i++) {
            createUpAnim(i, view);
            createDownAnim(i, view);
        }
    }

    /**
     * 创建上升动画
     *
     * @param index 对应哪一根矩形
     */
    private void createUpAnim(final int index, final RectLoadingView view) {
        upAnims[index] = ValueAnimator.ofFloat(0.1f, 1.0f);
        upAnims[index].setDuration(1000);
        upAnims[index].setStartDelay(upDelay * index);
        upAnims[index].setInterpolator(new AccelerateInterpolator());
        upAnims[index].addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                view.setFraction(index, (float) animation.getAnimatedValue());
            }
        });
        upAnims[index].addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (downAnims[index] != null && !needCancel) {
                    downAnims[index].start();
                }
            }
        });
    }

    /**
     * 创建下降动画
     */
    private void createDownAnim(final int index, final RectLoadingView view) {
        downAnims[index] = ValueAnimator.ofFloat(1.0f, 0.1f);
        downAnims[index].setDuration(downDurations[index]);
        downAnims[index].setInterpolator(new DecelerateInterpolator());
        downAnims[index].addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                view.setFraction(index, (Float) animation.getAnimatedValue());
            }
        });

        if (index == 0) {
            downAnims[index].addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if(!needCancel){
                        startAnim();
                    }
                }
            });
        }
    }

    /**
     * 计算好各阶段时长数据
     */
    private void initDurations(long duration, int rectCount) {
        //根据动画规则化简后的公式： （原公式：(count - 1 ) * upDelay + 2 * upDuration = duration)
        upDuration = (2 * duration) / (rectCount + 3);
        upDelay = upDuration / 2;
        downDurations = new long[rectCount];
        for (int i = 0; i < downDurations.length; i++) {
            downDurations[i] = duration - upDuration - upDelay * i;  //总时间减去上升时间减去延迟，即下降时间
//            Log.d("ccy","i = " + i + "down = " + downDurations[i]);
        }
//        Log.d("ccy", "up = " + upDuration + ";delay = " + upDelay);

    }

    @Override
    public void startAnim() {
        needCancel = false;
        if (view != null) {
            float[] fractions = view.getFractions();
            for (int i = 0; i < fractions.length; i++) {
                fractions[i] = 0.1f;
            }
            view.setFractions(fractions);
        }

        if (upAnims != null) {
            for (int i = 0; i < upAnims.length; i++) {
                if (upAnims[i] != null)
                    upAnims[i].start();
            }
        }
    }

    @Override
    public void stopAnim() {
        needCancel = true;
        if (upAnims != null) {
            for (int i = 0; i < upAnims.length; i++) {
                if (upAnims[i] != null)
                    upAnims[i].cancel();
            }
        }
        if (downAnims != null) {
            for (int i = 0; i < downAnims.length; i++) {
                if (downAnims[i] != null)
                    downAnims[i].cancel();
            }
        }

    }
}
