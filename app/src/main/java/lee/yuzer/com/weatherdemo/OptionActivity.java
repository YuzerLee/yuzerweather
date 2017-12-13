package lee.yuzer.com.weatherdemo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import java.io.IOException;
import lee.yuzer.com.weatherdemo.service.AutoUpdateService;
import lee.yuzer.com.weatherdemo.util.HttpUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class OptionActivity extends AppCompatActivity implements View.OnClickListener {
    private ImageView bg;
    private Button backToWeatherButton;
    private Switch mSwitch;
    private RelativeLayout IntervalLayout;
    private TextView IntervalTimeText;
    private TextView IntervalTitleText;
    private TextView intervalText;
    public static String SendInfo;
    private PopupWindow popupWindow;
    public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            intervalText.setText(msg.obj.toString());
            SendInfo = msg.obj.toString();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_option);

        initView();
    }

    private void initView() {
        bg = (ImageView) findViewById(R.id.optionbackground_imageview);
        backToWeatherButton = (Button) findViewById(R.id.backtoweatheractivity_button);
        mSwitch = (Switch) findViewById(R.id.switch1);
        IntervalLayout = (RelativeLayout) findViewById(R.id.updateinterval_layout);
        IntervalTimeText = (TextView) findViewById(R.id.interval_textView);
        IntervalTitleText = (TextView) findViewById(R.id.intervaltitle_textview);
        intervalText = (TextView) findViewById(R.id.interval_textView);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SendInfo = prefs.getString("selected_text", "未知");
        intervalText.setText(SendInfo);

        //加载背景图片
        String bingPic = prefs.getString("bing_pic", null);
        Boolean state = prefs.getBoolean("switch_state", true);
        mSwitch.setChecked(state);
        if (state == false) {
            IntervalTitleText.setTextColor(0xFF909090);
            IntervalTimeText.setTextColor(0xFF909090);
            IntervalLayout.setOnClickListener(null);
            Intent intent = new Intent(this, AutoUpdateService.class);
            stopService(intent);
        } else {
            IntervalTitleText.setTextColor(Color.WHITE);
            IntervalTimeText.setTextColor(Color.WHITE);
            IntervalLayout.setOnClickListener(this);
            Intent intent = new Intent(this, AutoUpdateService.class);
            startService(intent);
        }
        if (bingPic != null) {
            Glide.with(this).load(bingPic).into(bg);
        } else {
            loadBingPic();
        }

        backToWeatherButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    IntervalTitleText.setTextColor(Color.WHITE);
                    IntervalTimeText.setTextColor(Color.WHITE);
                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(OptionActivity.this).edit();
                    editor.putBoolean("switch_state", true);
                    editor.apply();
                    IntervalLayout.setOnClickListener(OptionActivity.this);
                } else {
                    IntervalTitleText.setTextColor(0xFF909090);
                    IntervalTimeText.setTextColor(0xFF909090);
                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(OptionActivity.this).edit();
                    editor.putBoolean("switch_state", false);
                    editor.apply();
                    IntervalLayout.setOnClickListener(null);
                }
            }
        });
    }

    private void loadBingPic() {
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(OptionActivity.this).edit();
                editor.putString("bing_pic", bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(OptionActivity.this).load(bingPic).into(bg);
                    }
                });
            }
        });
    }

    @Override
    public void onClick(View v) {
        //Toast.makeText(OptionActivity.this, "kkk", Toast.LENGTH_SHORT).show();
        popupWindow = new BasePopupWindow(this, mHandler);
        popupWindow.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setContentView(LayoutInflater.from(this).inflate(R.layout.popupwindow_item, null));
        popupWindow.setBackgroundDrawable(new ColorDrawable(0x00000000));
        popupWindow.setOutsideTouchable(false);
        popupWindow.setFocusable(true);
        popupWindow.setAnimationStyle(R.style.popupwindow_anim);
        FrameLayout parent = (FrameLayout) findViewById(R.id.parent_layout);
        popupWindow.showAtLocation(parent, Gravity.BOTTOM, 0, 50);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SendInfo = prefs.getString("selected_text", "未知");

    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(OptionActivity.this).edit();
        editor.putString("selected_text", SendInfo);
        editor.apply();
    }

}
