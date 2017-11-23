package ccy.rectloadingview.animcontroller;

import ccy.rectloadingview.RectLoadingView;

/**
 * Created by ccy on 2017-11-22.
 * {@link RectLoadingView}的动画控制器接口
 */

public interface IAnimController {


    /**
     * 创建动画，通过控制{@link RectLoadingView#fractions}来控制每个矩形的高度变化。
     * {@link RectLoadingView#fractions} 存储着每根矩形高度的比例值，
     * 在绘制时会将矩形原始高度乘上该比例值作为最终高度
     * 考虑到该方法可能多次调用，建议调用之前先调用stopAnim()
     *
     * @param duration  一次完整的动画时长的建议值
     * @param view      RectLoadingView,主要为了根据动画给{@link RectLoadingView#fractions}赋值然后{@link RectLoadingView#invalidate()}来实现动画效果
     */
    void createAnim(long duration, RectLoadingView view);

    /**
     * 开启动画，注意判空
     */
    void startAnim();

    /**
     * 停止动画，注意判空。
     * 考虑到这里创建的动画基本都是无限循环的，在view从界面移除时、更换IAnimController时等都应该调用，防内存泄漏
     */
    void stopAnim();

}
