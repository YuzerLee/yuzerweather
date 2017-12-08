package lee.yuzer.com.weatherdemo.gson;

/**
 * Created by Yuzer on 2017/12/7.
 */

public class AQI {
    public AQICity city;
    public class AQICity {
        public String aqi;
        public String pm25;
    }
}
