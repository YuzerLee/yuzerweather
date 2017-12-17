package lee.yuzer.com.weatherdemo.customizeview;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

import lee.yuzer.com.weatherdemo.scrollviewinterface.MyOnScrollChangedListener;

/**
 * Created by Yuzer on 2017/12/16.
 */

public class MyScrollView extends ScrollView {
    private MyOnScrollChangedListener mMyOnScrollChangedListener;

    public MyScrollView(Context context, AttributeSet attrs,
                                int defStyle) {
        super(context, attrs, defStyle);
    }

    public MyScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyScrollView(Context context) {
        super(context);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if(this.mMyOnScrollChangedListener != null) {
            mMyOnScrollChangedListener.onScrollChanged(t, oldt);
        }
    }

    public void setMyOnScrollChangedListener(MyOnScrollChangedListener myOnScrollChangedListener) {
        this.mMyOnScrollChangedListener = myOnScrollChangedListener;
    }

}
