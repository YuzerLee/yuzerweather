package lee.yuzer.com.weatherdemo;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import org.litepal.crud.DataSupport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lee.yuzer.com.weatherdemo.db.StoredCity;
import lee.yuzer.com.weatherdemo.gson.Forecast;
import lee.yuzer.com.weatherdemo.gson.Lifestyle;
import lee.yuzer.com.weatherdemo.gson.Weather;
import lee.yuzer.com.weatherdemo.util.HttpUtil;
import lee.yuzer.com.weatherdemo.util.Utility;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherViewPagerActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener {
    private ScrollView weatherLayout;
    private TextView titleCity;
    private TextView titleUpdateTime;
    private TextView degreeText;
    private TextView weatherInfoText;
    private LinearLayout forecastLayout;
    private TextView aqiText;
    private TextView pm25Text;
    private TextView comfortText;
    private TextView carWashText;
    private TextView sportText;
    private ImageView bingPicImg;
    public SwipeRefreshLayout mSwipeRefreshLayout;
    private String countyName;
    public DrawerLayout mDrawerLayout;
    private Button chooseCityButton;
    public ViewPager mViewPager;
    private PagerAdapter mPagerAdapter;
    private List<View> mViewPagerList;
    private List<StoredCity> mStoredCities;
    private List<ImageView> dotView;
    private Button removeCityButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        if(Build.VERSION.SDK_INT >= 21){
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        initView();

        //ViewPager版本
        initViewPager();
    }

    private void initView(){
        mStoredCities = new ArrayList<>();
        mViewPagerList = new ArrayList<>();

        titleCity = (TextView)findViewById(R.id.title_city);
        titleUpdateTime = (TextView)findViewById(R.id.title_update_time);
        bingPicImg = (ImageView)findViewById(R.id.bing_pic_img);
        dotView = new ArrayList<>();
        removeCityButton = (Button)findViewById(R.id.deletecity_button);
        mStoredCities = DataSupport.findAll(StoredCity.class);
        if(mStoredCities.size() <= 1){
            removeCityButton.setVisibility(View.GONE);
        }
        removeCityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mStoredCities = DataSupport.findAll(StoredCity.class);
                if(mStoredCities.size() > 1){
                    View ViewPagerLayout = MyViewPagerAdatper.mCurrentView;
                    mStoredCities = DataSupport.where("name = ?", titleCity.getText().toString()).find(StoredCity.class);
                    DataSupport.deleteAll(StoredCity.class, "name = ?", mStoredCities.get(0).getName());
                    mViewPagerList.remove(ViewPagerLayout);
                    mViewPagerList.clear();
                    LinearLayout containerlayout = (LinearLayout)findViewById(R.id.container_layout);
                    containerlayout.removeView(mViewPager);
                    mPagerAdapter = new MyViewPagerAdatper(mViewPagerList);
                    mViewPager = new ViewPager(WeatherViewPagerActivity.this);
                    mViewPager.setAdapter(mPagerAdapter);
                    mViewPager.setOnPageChangeListener(WeatherViewPagerActivity.this);
                    containerlayout.addView(mViewPager);
                    loadHistoryViewPagerData();
                }
            }
        });
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        chooseCityButton = (Button)findViewById(R.id.nav_button);
        chooseCityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.openDrawer(GravityCompat.START);
            }
        });
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String bingPic = prefs.getString("bing_pic", null);
        if(bingPic != null){
            Glide.with(this).load(bingPic).into(bingPicImg);
        }else{
            loadBingPic();
        }
    }

    private void initViewPager(){
        mViewPager = (ViewPager)findViewById(R.id.myViewPager);
        mStoredCities = DataSupport.findAll(StoredCity.class);
        mPagerAdapter = new MyViewPagerAdatper(mViewPagerList);
        mViewPager.setAdapter(mPagerAdapter);

        if(mStoredCities.size() == 0){
            countyName = getIntent().getStringExtra("county_name");
            requestWeather(countyName);
        }else{
            loadHistoryData();
        }

        mViewPager.setOnPageChangeListener(this);
    }

    private void loadHistoryViewPagerData(){
        mStoredCities = DataSupport.findAll(StoredCity.class);
        for(int i = 0; i < mStoredCities.size(); i++){
            Weather weather = Utility.handleWeatherResponse(mStoredCities.get(i).getContent());
            showWeatherInfoForLoad(weather);
        }
        mViewPager.setCurrentItem(0);

        titleCity.setText(mStoredCities.get(0).getName());
        titleUpdateTime.setText(mStoredCities.get(0).getTime().split(" ")[1]);

        if(mStoredCities.size() == 1){
            removeCityButton.setVisibility(View.GONE);
        }else{
            removeCityButton.setVisibility(View.VISIBLE);
        }

        if(mStoredCities.size() > 1){
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(15, 15);
            params.setMargins(10, 0, 10, 0);
            LinearLayout dotlayout = (LinearLayout)findViewById(R.id.dot_layout);
            dotlayout.removeAllViews();
            dotView.clear();
            for(int i = 0; i < mStoredCities.size(); i++){
                ImageView iv = new ImageView(this);
                iv.setLayoutParams(params);
                iv.setImageResource(R.drawable.dot_selector);
                if(i == 0){
                    iv.setSelected(true);
                }else{
                    iv.setSelected(false);
                }
                dotView.add(iv);
                dotlayout.addView(iv);
            }
        }else{
            LinearLayout dotlayout = (LinearLayout)findViewById(R.id.dot_layout);
            dotlayout.removeAllViews();
        }
    }

    private void loadHistoryData(){
        mStoredCities = DataSupport.findAll(StoredCity.class);
        for(int i = 0; i < mStoredCities.size(); i++){
            Weather weather = Utility.handleWeatherResponse(mStoredCities.get(i).getContent());
            showWeatherInfoForLoad(weather);
        }
        mViewPager.setCurrentItem(0);

        titleCity.setText(mStoredCities.get(0).getName());
        titleUpdateTime.setText(mStoredCities.get(0).getTime().split(" ")[1]);

        if(mStoredCities.size() > 1){
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(15, 15);
            params.setMargins(10, 0, 10, 0);
            LinearLayout dotlayout = (LinearLayout)findViewById(R.id.dot_layout);
            dotlayout.removeAllViews();
            dotView.clear();
            for(int i = 0; i < mStoredCities.size(); i++){
                ImageView iv = new ImageView(this);
                iv.setLayoutParams(params);
                iv.setImageResource(R.drawable.dot_selector);
                if(i == 0){
                    iv.setSelected(true);
                }else{
                    iv.setSelected(false);
                }
                dotView.add(iv);
                dotlayout.addView(iv);
            }
        }
    }

    private void loadBingPic(){
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherViewPagerActivity.this).edit();
                editor.putString("bing_pic", bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherViewPagerActivity.this).load(bingPic).into(bingPicImg);
                    }
                });
            }
        });
    }

    /**
     * 根据天气id请求城市天气信息
     */
    public void requestWeather(final String countyName){
        String weatherUrl = "https://free-api.heweather.com/s6/weather?location=" + countyName + "&key=4a379f0c0bde4c53b70c47628a72a8c3";

        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherViewPagerActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                Log.d("网络请求返回", responseText);
                final Weather weather = Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(weather != null && "ok".equals(weather.status) ){
                            //ViewPager版本
                            List<StoredCity> mTempCities = new ArrayList<>();
                            mTempCities = DataSupport.where("name = ?", weather.basic.cityName).find(StoredCity.class);
                            if(mTempCities.size() == 0){
                                // 新城市，进行添加操作
                                StoredCity city = new StoredCity();
                                city.setName(weather.basic.cityName);
                                city.setContent(responseText);
                                city.setTime(weather.update.loc);
                                city.save();
                                showWeatherInfo(weather);
                            }else{
                                //进行该城市的信息刷新
                                StoredCity city = new StoredCity();
                                city.setName(weather.basic.cityName);
                                city.setContent(responseText);
                                city.setTime(weather.update.loc);
                                city.updateAll("name = ?", weather.basic.cityName);
                                showWeatherInfoRefresh(weather);
                            }

                        }else{
                            Toast.makeText(WeatherViewPagerActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        }
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        });
    }

    //刷新时进行viewPager的初始化显示
    public void showWeatherInfoRefresh(Weather weather){
        View ViewPagerLayout = MyViewPagerAdatper.mCurrentView;
        mSwipeRefreshLayout = (SwipeRefreshLayout)ViewPagerLayout.findViewById(R.id.swipe_refresh);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                String RefreshCityName = titleCity.getText().toString();
                requestWeather(RefreshCityName);
            }
        });

        String cityName = weather.basic.cityName;
        String updateTime = weather.update.loc.split(" ")[1];
        String degree = weather.now.temperature + "°C";
        String weatherInfo = weather.now.cond_txt;
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText = (TextView)ViewPagerLayout.findViewById(R.id.degree_text);
        weatherInfoText = (TextView)ViewPagerLayout.findViewById(R.id.weather_info_text);
        forecastLayout = (LinearLayout)ViewPagerLayout.findViewById(R.id.forecast_layout);
        aqiText = (TextView)ViewPagerLayout.findViewById(R.id.aqi_text);
        pm25Text = (TextView)ViewPagerLayout.findViewById(R.id.pm25_text);
        comfortText = (TextView)ViewPagerLayout.findViewById(R.id.comfort_text);
        carWashText = (TextView)ViewPagerLayout.findViewById(R.id.car_wash_text);
        sportText = (TextView)ViewPagerLayout.findViewById(R.id.sport_text);
        weatherLayout = (ScrollView)ViewPagerLayout.findViewById(R.id.weather_layout);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        forecastLayout.removeAllViews();
        for(Forecast forecast : weather.forecastList){
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout, false);
            TextView dateText = (TextView)view.findViewById(R.id.date_text);
            TextView infoText = (TextView)view.findViewById(R.id.info_text);
            TextView maxText = (TextView)view.findViewById(R.id.max_text);
            TextView minText = (TextView)view.findViewById(R.id.min_text);
            dateText.setText(forecast.date);
            infoText.setText(forecast.cond_txt_d);
            maxText.setText(forecast.tmp_max);
            minText.setText(forecast.tmp_min);
            forecastLayout.addView(view);
        }
//        if(weather.aqi != null){
//            aqiText.setText(weather.aqi.city.aqi);
//            pm25Text.setText(weather.aqi.city.pm25);
//        }
        String comfort;
        String carWash;
        String sport;
        for(Lifestyle lifestyle : weather.lifestyleList){
            if(lifestyle.type.equals("comf")){
                comfort = "舒适度：" + lifestyle.txt;
                comfortText.setText(comfort);
            }else if(lifestyle.type.equals("cw")){
                carWash = "洗车指数：" + lifestyle.txt;
                carWashText.setText(carWash);
            }else if(lifestyle.type.equals("sport")){
                sport = "运动建议：" + lifestyle.txt;
                sportText.setText(sport);
            }
        }
        weatherLayout.setVisibility(View.VISIBLE);
        mPagerAdapter.notifyDataSetChanged();
    }

    //新城市添加时进行viewPager的初始化显示
    public void showWeatherInfo(Weather weather){
        View ViewPagerLayout = getLayoutInflater().from(this).inflate(R.layout.viewpager_item, null);
        mSwipeRefreshLayout = (SwipeRefreshLayout)ViewPagerLayout.findViewById(R.id.swipe_refresh);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                String RefreshCityName = titleCity.getText().toString();
                requestWeather(RefreshCityName);
            }
        });

        String cityName = weather.basic.cityName;
        String updateTime = weather.update.loc.split(" ")[1];
        String degree = weather.now.temperature + "°C";
        String weatherInfo = weather.now.cond_txt;
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText = (TextView)ViewPagerLayout.findViewById(R.id.degree_text);
        weatherInfoText = (TextView)ViewPagerLayout.findViewById(R.id.weather_info_text);
        forecastLayout = (LinearLayout)ViewPagerLayout.findViewById(R.id.forecast_layout);
        aqiText = (TextView)ViewPagerLayout.findViewById(R.id.aqi_text);
        pm25Text = (TextView)ViewPagerLayout.findViewById(R.id.pm25_text);
        comfortText = (TextView)ViewPagerLayout.findViewById(R.id.comfort_text);
        carWashText = (TextView)ViewPagerLayout.findViewById(R.id.car_wash_text);
        sportText = (TextView)ViewPagerLayout.findViewById(R.id.sport_text);
        weatherLayout = (ScrollView)ViewPagerLayout.findViewById(R.id.weather_layout);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        forecastLayout.removeAllViews();
        for(Forecast forecast : weather.forecastList){
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout, false);
            TextView dateText = (TextView)view.findViewById(R.id.date_text);
            TextView infoText = (TextView)view.findViewById(R.id.info_text);
            TextView maxText = (TextView)view.findViewById(R.id.max_text);
            TextView minText = (TextView)view.findViewById(R.id.min_text);
            dateText.setText(forecast.date);
            infoText.setText(forecast.cond_txt_d);
            maxText.setText(forecast.tmp_max);
            minText.setText(forecast.tmp_min);
            forecastLayout.addView(view);
        }
//        if(weather.aqi != null){
//            aqiText.setText(weather.aqi.city.aqi);
//            pm25Text.setText(weather.aqi.city.pm25);
//        }
        String comfort;
        String carWash;
        String sport;
        for(Lifestyle lifestyle : weather.lifestyleList){
            if(lifestyle.type.equals("comf")){
                comfort = "舒适度：" + lifestyle.txt;
                comfortText.setText(comfort);
            }else if(lifestyle.type.equals("cw")){
                carWash = "洗车指数：" + lifestyle.txt;
                carWashText.setText(carWash);
            }else if(lifestyle.type.equals("sport")){
                sport = "运动建议：" + lifestyle.txt;
                sportText.setText(sport);
            }
        }
        weatherLayout.setVisibility(View.VISIBLE);

        mViewPagerList.add(ViewPagerLayout);
        mPagerAdapter.notifyDataSetChanged();
        mViewPager.setCurrentItem(DataSupport.findAll(StoredCity.class).size());
    }

    //启动程序加载已存储城市数据时进行viewPager的初始化显示
    public void showWeatherInfoForLoad(Weather weather){
        View ViewPagerLayout = getLayoutInflater().from(this).inflate(R.layout.viewpager_item, null);
        mSwipeRefreshLayout = (SwipeRefreshLayout)ViewPagerLayout.findViewById(R.id.swipe_refresh);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                String RefreshCityName = titleCity.getText().toString();
                requestWeather(RefreshCityName);
            }
        });

        String cityName = weather.basic.cityName;
        String updateTime = weather.update.loc.split(" ")[1];
        String degree = weather.now.temperature + "°C";
        String weatherInfo = weather.now.cond_txt;
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText = (TextView)ViewPagerLayout.findViewById(R.id.degree_text);
        weatherInfoText = (TextView)ViewPagerLayout.findViewById(R.id.weather_info_text);
        forecastLayout = (LinearLayout)ViewPagerLayout.findViewById(R.id.forecast_layout);
        aqiText = (TextView)ViewPagerLayout.findViewById(R.id.aqi_text);
        pm25Text = (TextView)ViewPagerLayout.findViewById(R.id.pm25_text);
        comfortText = (TextView)ViewPagerLayout.findViewById(R.id.comfort_text);
        carWashText = (TextView)ViewPagerLayout.findViewById(R.id.car_wash_text);
        sportText = (TextView)ViewPagerLayout.findViewById(R.id.sport_text);
        weatherLayout = (ScrollView)ViewPagerLayout.findViewById(R.id.weather_layout);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        forecastLayout.removeAllViews();
        for(Forecast forecast : weather.forecastList){
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout, false);
            TextView dateText = (TextView)view.findViewById(R.id.date_text);
            TextView infoText = (TextView)view.findViewById(R.id.info_text);
            TextView maxText = (TextView)view.findViewById(R.id.max_text);
            TextView minText = (TextView)view.findViewById(R.id.min_text);
            dateText.setText(forecast.date);
            infoText.setText(forecast.cond_txt_d);
            maxText.setText(forecast.tmp_max);
            minText.setText(forecast.tmp_min);
            forecastLayout.addView(view);
        }
//        if(weather.aqi != null){
//            aqiText.setText(weather.aqi.city.aqi);
//            pm25Text.setText(weather.aqi.city.pm25);
//        }
        String comfort;
        String carWash;
        String sport;
        for(Lifestyle lifestyle : weather.lifestyleList){
            if(lifestyle.type.equals("comf")){
                comfort = "舒适度：" + lifestyle.txt;
                comfortText.setText(comfort);
            }else if(lifestyle.type.equals("cw")){
                carWash = "洗车指数：" + lifestyle.txt;
                carWashText.setText(carWash);
            }else if(lifestyle.type.equals("sport")){
                sport = "运动建议：" + lifestyle.txt;
                sportText.setText(sport);
            }
        }
        weatherLayout.setVisibility(View.VISIBLE);

        mViewPagerList.add(ViewPagerLayout);
        mPagerAdapter.notifyDataSetChanged();
        mViewPager.setCurrentItem(0);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        mStoredCities = DataSupport.findAll(StoredCity.class);
        if(mStoredCities.size() == 1){
            removeCityButton.setVisibility(View.GONE);
        }else{
            removeCityButton.setVisibility(View.VISIBLE);
        }
        for(int i = 0; i < mStoredCities.size(); i++){
            if(i == position){
                titleCity.setText(mStoredCities.get(i).getName());
                titleUpdateTime.setText(mStoredCities.get(i).getTime().split(" ")[1]);
            }
        }
        if(mStoredCities.size() > 1){
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(15, 15);
            params.setMargins(10, 0, 10, 0);
            LinearLayout dotlayout = (LinearLayout)findViewById(R.id.dot_layout);
            dotlayout.removeAllViews();
            dotView.clear();
            for(int i = 0; i < mStoredCities.size(); i++){
                ImageView iv = new ImageView(WeatherViewPagerActivity.this);
                iv.setLayoutParams(params);
                iv.setImageResource(R.drawable.dot_selector);
                if(i == position){
                    iv.setSelected(true);
                }else{
                    iv.setSelected(false);
                }
                dotView.add(iv);
                dotlayout.addView(iv);
            }
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
