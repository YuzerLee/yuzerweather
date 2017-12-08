package lee.yuzer.com.weatherdemo.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Yuzer on 2017/12/7.
 */

public class Basic {
    @SerializedName("location")
    public String cityName;

    @SerializedName("id")
    public String weatherId;
}
