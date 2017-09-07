package ptit.nttrung.secretcamera.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import ptit.nttrung.secretcamera.define.Conts;
import ptit.nttrung.secretcamera.helper.ServiceHelper;
import ptit.nttrung.secretcamera.service.RecorderService;

/**
 * Created by TrungNguyen on 5/16/2017.
 */

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent serviceIntent = new Intent(context, RecorderService.class);
        serviceIntent.putExtra(Conts.CAMERA_USE, intent.getStringExtra(Conts.CAMERA_USE));
        serviceIntent.putExtra(Conts.CAMERA_DURATION, intent.getStringExtra(Conts.CAMERA_DURATION));

        if (!ServiceHelper.isServiceRunning(RecorderService.class, context))
            context.startService(serviceIntent);
    }
}
