# rectloadingview
android条形图加载控件

## 效果图
![image](https://github.com/CCY0122/rectloadingview/blob/master/device-2017-12-02-162710%20(2).gif)
## 用法
```
<ccy.rectloadingview.RectLoadingView
        android:id="@+id/r1"
        android:layout_width="80dp"
        android:layout_height="60dp"
        android:layout_margin="20dp"
        />
```
```
 RectLoadingView r1 = (RectLoadingView) findViewById(R.id.r1);
  r1.setRectCount(10);
  r1.setDuration(500);
  r1.setAnimController(new RandomAnimController());
  // 省略其他的一些 r1.setXXX
  
  r2.startAnim();
```
