package ccy.rectloadingview.animcontroller;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.view.animation.LinearInterpolator;

import ccy.rectloadingview.R;
import ccy.rectloadingview.RectLoadingView;

/**
 * Created by ccy on 2017-11-22.
 * 波浪型
 */

public class WaveAnimController implements IAnimController {

    private RectLoadingView view;
    private ValueAnimator[] animators;
    private int halfWaveCount = -1; //可以理解为半个波浪所占用的矩形数
    private long delay;
    private boolean singleWave; //是否一次动画只有一个波浪
    private float minFraction = 0.1f;
    private float maxFraction = 1.0f;

    //停止动画标志。因为调用cancel()会触发onAnimationEnd监听，而该监听内可能又重新开启了动画，故设该标志位
    private boolean needCancel;

    public WaveAnimController() {
    }

    /**
     * @see WaveAnimController#WaveAnimController(int, boolean, float, float)
     */
    public WaveAnimController(int halfWaveCount) {
        this(halfWaveCount, false);
    }

    /**
     * @see WaveAnimController#WaveAnimController(int, boolean, float, float)
     */
    public WaveAnimController(boolean singleWave) {
        this(-1, singleWave);
    }

    /**
     * @see WaveAnimController#WaveAnimController(int, boolean, float, float)
     */
    public WaveAnimController(int halfWaveCount, boolean singleWave) {
        this.halfWaveCount = halfWaveCount;
        this.singleWave = singleWave;
    }

    /**
     * @param halfWaveCount 可以理解为半个波浪所占用的矩形数,值小于0则默认一次波浪占全部矩形
     * @param singleWave    是否一次动画内只有一个波浪
     * @param minFraction   波浪最低高度比例值
     * @param maxFraction   波浪最高高度比例值
     */
    public WaveAnimController(int halfWaveCount, boolean singleWave, float minFraction, float maxFraction) {
        this.halfWaveCount = halfWaveCount;
        this.singleWave = singleWave;
        this.minFraction = minFraction;
        this.maxFraction = maxFraction;
    }


    @Override
    public void createAnim(final RectLoadingView view) {
        this.view = view;
        if (halfWaveCount < 0) {
            delay = view.getDuration() / (view.getFractions().length - 1);   //默认一次波浪占全部矩形
        } else {
            delay = view.getDuration() / halfWaveCount;
        }
        animators = new ValueAnimator[view.getFractions().length];
        for (int i = 0; i < animators.length; i++) {
            animators[i] = ValueAnimator.ofFloat(minFraction, maxFraction);
            animators[i].setDuration(view.getDuration());
            animators[i].setRepeatCount(singleWave ? 1 : ValueAnimator.INFINITE);
            animators[i].setRepeatMode(ValueAnimator.REVERSE);
            animators[i].setInterpolator(new LinearInterpolator());
//            animators[i].setStartDelay(delay * i);
            final int finalI = i;
            animators[i].addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationRepeat(Animator animation) {
                    if (animation.getStartDelay() != 0) {
                        animators[finalI].setStartDelay(0);
                    }
                }
            });
            animators[i].addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    view.setFraction(finalI, (Float) animation.getAnimatedValue());
                }
            });

            if (singleWave && i == animators.length - 1) {  //  再最后一个矩形动画结束后再重新开始整个动画
                animators[i].addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (!needCancel) {
                            startAnim();
                        }
                    }
                });
            }
        }

    }

    @Override
    public void startAnim() {
        needCancel = false;
        if (view != null) {
            float[] fractions = view.getFractions();
            for (int i = 0; i < fractions.length; i++) {
                fractions[i] = minFraction;
            }
            view.setFractions(fractions);
        }
        if (animators != null) {
            for (int i = 0; i < animators.length; i++) {
                if (animators[i] != null)
                    animators[i].setStartDelay(delay * i); //要在这里设置，而不是createAnim里设置
                animators[i].start();
            }
        }
    }

    @Override
    public void stopAnim() {
        needCancel = true;
        if (animators != null) {
            for (int i = 0; i < animators.length; i++) {
                if (animators[i] != null)
                    animators[i].cancel();
            }
        }
    }
}
