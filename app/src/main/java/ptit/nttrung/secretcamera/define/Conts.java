package ptit.nttrung.secretcamera.define;

import android.Manifest;

/**
 * Created by TrungNguyen on 5/5/2017.
 */

public class Conts {
    public static final String[] permissions = {
            Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO,
            Manifest.permission.SYSTEM_ALERT_WINDOW, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.INTERNET};

    public static final String PRE_NAME = "mydata";
    public static final String TIME_RECORD = "time_record";
    public static final String CAMERA_USE = "camera_use";
    public static final String CAMERA_DURATION = "camera_duration";
    public static final String INTENT_KEY_FILE_PATH = "filepath";
    public static final String FIRST_RUN = "first_run";
    public static final String SMS_START_RECORD = "sms_start_record";
    public static final String ACTION_STOP_EXTRA = "STOP_EXTRA";
    public static final String ACTION_START_SEVICE = "START_EXTRA";
}
