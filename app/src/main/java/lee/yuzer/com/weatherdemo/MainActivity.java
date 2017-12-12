package lee.yuzer.com.weatherdemo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

import lee.yuzer.com.weatherdemo.db.StoredCity;

public class MainActivity extends AppCompatActivity {
    private List<StoredCity> mStoredCities;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //ViewPager模式
        mStoredCities = new ArrayList<>();
        mStoredCities = DataSupport.findAll(StoredCity.class);
        if(mStoredCities.size() != 0){
            Intent intent = new Intent(this, WeatherViewPagerActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
