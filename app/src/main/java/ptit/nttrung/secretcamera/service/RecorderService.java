package ptit.nttrung.secretcamera.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import java.io.File;
import java.io.IOException;
import java.util.List;

import ptit.nttrung.secretcamera.R;
import ptit.nttrung.secretcamera.activities.MainActivity;
import ptit.nttrung.secretcamera.define.Conts;
import ptit.nttrung.secretcamera.helper.CameraHelper;
import ptit.nttrung.secretcamera.helper.FileHelper;
import ptit.nttrung.secretcamera.helper.SharedPreHelper;

import static ptit.nttrung.secretcamera.R.string.stop;

/**
 * Created by TrungNguyen on 4/25/2017.
 */

public class RecorderService extends Service implements SurfaceHolder.Callback {

    private static final String TAG = RecorderService.class.getName();

    private WindowManager windowManager;
    private SurfaceView surfaceView;
    private Camera camera = null;
    private MediaRecorder mediaRecorder = null;
    private static File outputmp4;
    private SharedPreHelper sharedPreHelper;
    ;

    public static final int MEDIA_TYPE_VIDEO = 2;
    private int NOTIFCATION_ID = 1;

    private static final String ACTION_STOP_SERVICE = "stop_service";
    private boolean checkNotify, chkSlientRecord, chkFreeStore;
    private String videoQuality, useCam, audioSource, recordDuration, orientation;

    @Override
    public void onCreate() {
        readData(this);
        sharedPreHelper = new SharedPreHelper(this);

        windowManager = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        surfaceView = new SurfaceView(this);
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(
                1, 1,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT
        );
        layoutParams.gravity = Gravity.LEFT | Gravity.TOP;
        windowManager.addView(surfaceView, layoutParams);
        surfaceView.getHolder().addCallback(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        if (intent.getStringExtra(Conts.CAMERA_USE) != null) {
            useCam = intent.getStringExtra(Conts.CAMERA_USE);
            recordDuration = intent.getStringExtra(Conts.CAMERA_DURATION);
        }

        if (ACTION_STOP_SERVICE.equals(intent.getAction())) {
            stopForeground(true);
            stopSelf();
        }

        if (checkNotify) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
            builder.setContentTitle(getString(R.string.camera_running));
            builder.setContentText(getString(R.string.tap_to_open));
            builder.setPriority(NotificationCompat.PRIORITY_HIGH);
            builder.setSmallIcon(R.drawable.notify_icon);
            builder.setWhen(0);
            builder.setPriority(Notification.PRIORITY_MAX);

            Intent resultIntent = new Intent(this.getApplicationContext(), MainActivity.class);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this.getApplicationContext());
            stackBuilder.addNextIntent(resultIntent);

            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(resultPendingIntent);

            Intent stopSelf = new Intent(this, RecorderService.class);
            stopSelf.setAction(ACTION_STOP_SERVICE);
            PendingIntent pStopSelf = PendingIntent.getService(this, 1, stopSelf, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.addAction(R.drawable.notify_stop, getString(stop), pStopSelf);

            startForeground(NOTIFCATION_ID, builder.build());
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        sharedPreHelper.remove();
        stopForeground(true);

        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
        Intent intent = new Intent();
        intent.setAction(Conts.ACTION_STOP_EXTRA);
        broadcastManager.sendBroadcast(intent);

        releaseMediaRecorder();
        releaseCamera();

        Intent mediaScannerIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri fileContentUri = Uri.fromFile(outputmp4);
        mediaScannerIntent.setData(fileContentUri);
        this.sendBroadcast(mediaScannerIntent);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        try {
            if (prepareVideoRecorder(surfaceHolder)) {
                mediaRecorder.start();
                sharedPreHelper.saveTimeToPre();

                LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
                Intent intent = new Intent();
                intent.setAction(Conts.ACTION_START_SEVICE);
                broadcastManager.sendBroadcast(intent);
            } else stopSelf();
        } catch (Exception e) {
            stopSelf();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        camera.stopPreview();
        camera.release();
        camera = null;
    }

    private boolean prepareVideoRecorder(SurfaceHolder surfaceHolder) {
        camera = MediaRecorderFactory.setVideoCamera(useCam);
        CamcorderProfile profile = MediaRecorderFactory.setCamcorderProfile(videoQuality);

        Camera.Parameters parameters = camera.getParameters();
        List<Camera.Size> mSupportedPreviewSizes = parameters.getSupportedPreviewSizes();
        List<Camera.Size> mSupportedVideoSizes = parameters.getSupportedVideoSizes();
        Camera.Size optimalSize = CameraHelper.getOptimalVideoSize(mSupportedVideoSizes,
                mSupportedPreviewSizes, profile.videoFrameWidth, profile.videoFrameHeight);

        profile.videoFrameWidth = optimalSize.width;
        profile.videoFrameHeight = optimalSize.height;
        parameters.setPreviewSize(profile.videoFrameWidth, profile.videoFrameHeight);

        camera.setParameters(parameters);

        mediaRecorder = new MediaRecorder();

        camera.unlock();
        mediaRecorder.setPreviewDisplay(surfaceHolder.getSurface());
        mediaRecorder.setCamera(camera);

        mediaRecorder = MediaRecorderFactory.setOrientation(mediaRecorder, orientation, useCam);
        mediaRecorder = MediaRecorderFactory.setSource(mediaRecorder, chkSlientRecord, audioSource, profile);

        outputmp4 = FileHelper.getOutputMediaFile(MEDIA_TYPE_VIDEO);
        if (outputmp4 == null) return false;
        mediaRecorder.setOutputFile(outputmp4.getPath());

        mediaRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
            @Override
            public void onInfo(MediaRecorder mediaRecorder, int what, int extra) {
                if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED
                        || what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED)
                    stopSelf();
            }
        });

        int duraMax = Integer.parseInt(recordDuration);
        if (duraMax < 10000) mediaRecorder.setMaxDuration(duraMax * 1000);

        if (chkFreeStore) {
            long availableExternalMemory = FileHelper.getAvailableExternalMemory();
            long maxSize = availableExternalMemory - 100;
            mediaRecorder.setMaxFileSize(maxSize * 1000000); //10MB
        }

        try {
            mediaRecorder.prepare();
        } catch (IllegalStateException e) {
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            releaseMediaRecorder();
            return false;
        }
        return true;
    }

    private void releaseMediaRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder = null;
            camera.lock();
        }
    }

    private void releaseCamera() {
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }

    private void readData(Context context) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        checkNotify = sharedPrefs.getBoolean("prefShowNotifi", true);
        useCam = sharedPrefs.getString("prefVideoCamera", "0");
        videoQuality = sharedPrefs.getString("prefVideoQuality", "0");
        audioSource = sharedPrefs.getString("prefAudioSource", "1");
        chkFreeStore = sharedPrefs.getBoolean("prefChkFreeSto", true);
        chkSlientRecord = sharedPrefs.getBoolean("prefChkSlientRecord", false);
        orientation = sharedPrefs.getString("prefOrientation", "1");
        recordDuration = sharedPrefs.getString("prefDuration", "300");
    }
}