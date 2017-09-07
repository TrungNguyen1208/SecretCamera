package ptit.nttrung.secretcamera.helper;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import ptit.nttrung.secretcamera.service.RecorderService;


/**
 * Created by TrungNguyen on 5/15/2017.
 */

public class ServiceHelper {
    public static boolean isServiceRunning(Class<?> serviceClass, Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static void startRecordService(Context context){
        Intent videoIntent = new Intent(context, RecorderService.class);
        Log.e("aaa", "Service running : " + ServiceHelper.isServiceRunning(RecorderService.class, context));
        if (!ServiceHelper.isServiceRunning(RecorderService.class, context))
            context.startService(videoIntent);
        else
            context.stopService(videoIntent);
    }
}
