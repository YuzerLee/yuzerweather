package lee.yuzer.com.weatherdemo;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import java.util.List;

/**
 * Created by Yuzer on 2017/12/10.
 */

public class MyViewPagerAdatper extends PagerAdapter {
    private List<View> mList;
    public static View mCurrentView;

    public MyViewPagerAdatper(List<View> mList) {
        this.mList = mList;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View)object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {

        container.addView(mList.get(position));

        return mList.get(position);
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        mCurrentView = (View)object;
    }


}
