package ccy.rectloadingview.animcontroller;

import android.animation.ValueAnimator;
import android.view.animation.LinearInterpolator;

import ccy.rectloadingview.RectLoadingView;

/**
 * Created by ccy on 2017-11-22.
 * 随机时长（duration±0.3）
 * 随机高度范围 ( 最低高度 0.1~0.3，最高高度0.8~1.0）
 */

public class RandomAnimController implements IAnimController {

    private ValueAnimator[] animators;

    public RandomAnimController() {
    }

    @Override
    public void createAnim(final RectLoadingView view) {
        animators = new ValueAnimator[view.getFractions().length];
        for (int i = 0; i < animators.length; i++) {
            float min = (float) (0.1 + 0.2 * Math.random());
            float max = (float) (0.8 + 0.2 * Math.random());
            animators[i] = ValueAnimator.ofFloat(min, max);
            animators[i].setRepeatCount(ValueAnimator.INFINITE);
            animators[i].setRepeatMode(ValueAnimator.REVERSE);
            animators[i].setDuration((long) (view.getDuration() * (0.7 + 0.6 * Math.random())));  //时长±0.3随机
            animators[i].setInterpolator(new LinearInterpolator());
            final int finalI = i;
            animators[i].addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    view.setFraction(finalI, (Float) animation.getAnimatedValue());
                }
            });
        }
    }

    @Override
    public void startAnim() {
        if (animators != null) {
            for (int i = 0; i < animators.length; i++) {
                if (animators[i] != null)
                    animators[i].start();
            }
        }
    }

    @Override
    public void stopAnim() {
        if (animators != null) {
            for (int i = 0; i < animators.length; i++) {
                if (animators[i] != null)
                    animators[i].cancel();
            }
        }
    }
}
