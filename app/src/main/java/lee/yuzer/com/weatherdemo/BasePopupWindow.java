package lee.yuzer.com.weatherdemo;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;


/**
 * Created by Yuzer on 2017/12/13.
 */

public class BasePopupWindow extends PopupWindow {
    private Context mContext;
    private float mShowAlpha = 0.88f;
    private Drawable mBackgroundDrawable;
    private View contentView;
    private RadioGroup mRadioGroup;
    private RadioButton halfRButton;
    private RadioButton oneRButton;
    private RadioButton sixRButton;
    private RadioButton twelveRButton;
    private RadioButton onedayRButton;
    private Button cancelButton;
    private String SelectedItemText;
    private Handler mHandler;


    public BasePopupWindow(Context context, Handler handler) {
        this.mContext = context;
        this.mHandler = handler;
        initBasePopupWindow();
    }

    @Override
    public void setOutsideTouchable(boolean touchable) {
        super.setOutsideTouchable(touchable);
        if (touchable) {
            if (mBackgroundDrawable == null) {
                mBackgroundDrawable = new ColorDrawable(0x00000000);
            }
            super.setBackgroundDrawable(mBackgroundDrawable);
        } else {
            super.setBackgroundDrawable(null);
        }
    }

    @Override
    public void setBackgroundDrawable(Drawable background) {
        mBackgroundDrawable = background;
        setOutsideTouchable(isOutsideTouchable());
    }

    /**
     * 初始化BasePopupWindow的一些信息
     */
    private void initBasePopupWindow() {
        setAnimationStyle(android.R.style.Animation_Dialog);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        setOutsideTouchable(true);  //默认设置outside点击无响应
        setFocusable(true);
    }

    @Override
    public void setContentView(View contentView) {
        if (contentView != null) {
            contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            super.setContentView(contentView);
            addKeyListener(contentView);
            mRadioGroup = (RadioGroup) contentView.findViewById(R.id.radiogroup);
            halfRButton = (RadioButton) contentView.findViewById(R.id.half_rt);
            oneRButton = (RadioButton) contentView.findViewById(R.id.one_rt);
            sixRButton = (RadioButton) contentView.findViewById(R.id.six_rt);
            twelveRButton = (RadioButton) contentView.findViewById(R.id.twelve_rt);
            onedayRButton = (RadioButton) contentView.findViewById(R.id.oneday_rt);
            cancelButton = (Button) contentView.findViewById(R.id.cancel_button);
            if (OptionActivity.SendInfo.equals("未知")) {
                halfRButton.setChecked(true);
            } else if (OptionActivity.SendInfo.equals("半小时")) {
                halfRButton.setChecked(true);
            } else if (OptionActivity.SendInfo.equals("1小时")) {
                oneRButton.setChecked(true);
            } else if (OptionActivity.SendInfo.equals("6小时")) {
                sixRButton.setChecked(true);
            } else if (OptionActivity.SendInfo.equals("12小时")) {
                twelveRButton.setChecked(true);
            } else if (OptionActivity.SendInfo.equals("24小时")) {
                onedayRButton.setChecked(true);
            }

            SelectedItemText = OptionActivity.SendInfo;//如果没有点击RadioButton，要先赋值，防止空值出现

            mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    switch (checkedId) {
                        case R.id.half_rt:
                            SelectedItemText = halfRButton.getText().toString();
                            dismiss();
                            //OptionActivity.SendInfo = SelectedItemText;
                            break;
                        case R.id.one_rt:
                            SelectedItemText = oneRButton.getText().toString();
                            dismiss();
                            //OptionActivity.SendInfo = SelectedItemText;
                            break;
                        case R.id.six_rt:
                            SelectedItemText = sixRButton.getText().toString();
                            dismiss();
                            //OptionActivity.SendInfo = SelectedItemText;
                            break;
                        case R.id.twelve_rt:
                            SelectedItemText = twelveRButton.getText().toString();
                            dismiss();
                            //OptionActivity.SendInfo = SelectedItemText;
                            break;
                        case R.id.oneday_rt:
                            SelectedItemText = onedayRButton.getText().toString();
                            dismiss();
                            //OptionActivity.SendInfo = SelectedItemText;
                            break;
                        default:
                            break;
                    }
                }
            });

            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
        }
    }

    public Context getContext() {
        return mContext;
    }

    @Override
    public void showAtLocation(View parent, int gravity, int x, int y) {
        super.showAtLocation(parent, gravity, x, y);
        //showAnimator().start();
    }

    @Override
    public void showAsDropDown(View anchor) {
        super.showAsDropDown(anchor);
        //showAnimator().start();
    }

    @Override
    public void showAsDropDown(View anchor, int xoff, int yoff) {
        super.showAsDropDown(anchor, xoff, yoff);
        //showAnimator().start();
    }

    @Override
    public void showAsDropDown(View anchor, int xoff, int yoff, int gravity) {
        super.showAsDropDown(anchor, xoff, yoff, gravity);
        //showAnimator().start();
    }

    @Override
    public void dismiss() {
        super.dismiss();
        Message msg = new Message();
        msg.obj = SelectedItemText;
        mHandler.sendMessage(msg);
        //dismissAnimator().start();
    }

    /**
     * 为窗体添加outside点击事件
     */
    private void addKeyListener(View contentView) {
        if (contentView != null) {
            contentView.setFocusable(true);
            contentView.setFocusableInTouchMode(true);
            contentView.setOnKeyListener(new View.OnKeyListener() {

                @Override
                public boolean onKey(View view, int keyCode, KeyEvent event) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_BACK:
                            dismiss();
                            return true;
                        default:
                            break;
                    }
                    return false;
                }
            });
        }
    }
}
