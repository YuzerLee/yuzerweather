package lee.yuzer.com.weatherdemo.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Yuzer on 2017/12/7.
 */

public class Weather {
    public String status;

    public Basic basic;

    public Now now;

    public Update update;

    @SerializedName("daily_forecast")
    public List<Forecast> forecastList;

    @SerializedName("lifestyle")
    public List<Lifestyle> lifestyleList;

    public class Update{
        public String loc;
    }
}
