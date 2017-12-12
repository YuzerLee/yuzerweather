package lee.yuzer.com.weatherdemo.db;

import org.litepal.crud.DataSupport;

/**
 * Created by Yuzer on 2017/12/10.
 */

public class StoredCity extends DataSupport{
    private int Id;

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    private String time;
    private String name;
    private String content;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
