package lee.yuzer.com.weatherdemo.BroadcastReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import lee.yuzer.com.weatherdemo.service.AutoUpdateService;

public class BootCompleteReceiver extends BroadcastReceiver {
    private static String SERVICE_START= "lee.yuzer.com.houtai";

    public BootCompleteReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        //Toast.makeText(context, "开机了", Toast.LENGTH_SHORT).show();
        Intent intent1 = new Intent(context, AutoUpdateService.class);
        context.startService(intent1);
        intent.setAction(SERVICE_START);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Log.d("service", "start");
    }
}
