package ptit.nttrung.secretcamera.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import es.dmoral.toasty.Toasty;
import ptit.nttrung.secretcamera.R;
import ptit.nttrung.secretcamera.define.Conts;
import ptit.nttrung.secretcamera.helper.FileHelper;
import ptit.nttrung.secretcamera.helper.SharedPreHelper;
import ptit.nttrung.secretcamera.helper.TimeHelper;
import ptit.nttrung.secretcamera.service.RecorderService;

/**
 * Created by TrungNguyen on 4/21/2017.
 */

public class RecorderFragment extends Fragment {


    private static final String TAG = "RecorderFragment";

    @BindView(R.id.iv_cam_recorder_frag)
    ImageView ivCamera;
    @BindView(R.id.tv_timer_recorder_frag)
    TextView tvTimer;
    @BindView(R.id.tv_click_recorder_frag)
    TextView tvClickRecorderFrag;

    public Handler mHandler;
    public Timer timer;
    public boolean isTimerRunning = false;

    private long elapsedTimer = 0;
    private SharedPreHelper preHelper;
    private LocalBroadcastManager broadcastManagerStop;
    private LocalBroadcastManager broadcastManagerStart;
    private BroadcastReceiver receiverStop = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Conts.ACTION_STOP_EXTRA)) {
                Log.e(TAG,"STOP");
                stopTimer();
            }
        }
    };

    private BroadcastReceiver receiverStart = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Conts.ACTION_START_SEVICE)) {
                Log.e(TAG,"START");
                startTimer();
            }
        }
    };

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        broadcastManagerStop = LocalBroadcastManager.getInstance(getContext());
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Conts.ACTION_STOP_EXTRA);
        broadcastManagerStop.registerReceiver(receiverStop, intentFilter);

        broadcastManagerStart = LocalBroadcastManager.getInstance(getContext());
        IntentFilter intentFilterStart = new IntentFilter();
        intentFilterStart.addAction(Conts.ACTION_START_SEVICE);
        broadcastManagerStart.registerReceiver(receiverStart, intentFilterStart);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recorder, container, false);
        ButterKnife.bind(this, view);

        preHelper = new SharedPreHelper(getContext());
        if (preHelper.haveTimeStart()) {
            isTimerRunning = true;
            ivCamera.setImageResource(R.drawable.icon_stop);
            tvClickRecorderFrag.setText(getString(R.string.click_to_stop));
            mHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    if (elapsedTimer < 0) tvTimer.setText(TimeHelper.convertSecondsToHMmSs(0));
                    else tvTimer.setText(TimeHelper.convertSecondsToHMmSs(elapsedTimer));
                }
            };
            timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    elapsedTimer = preHelper.getSecondsInTwoTime(preHelper.getTimeRecord());
                    mHandler.obtainMessage(1).sendToTarget();
                }
            }, 0, 1000);
        }

        return view;
    }

    @OnClick(R.id.iv_cam_recorder_frag)
    public void onViewClicked() {
        PermissionListener permissionListener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                if (FileHelper.getAvailableExternalMemory() < 50)
                    Toasty.error(getContext(), getString(R.string.low_memory_cant_save), Toast.LENGTH_SHORT).show();
                else
                    doRecord();
            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                Toasty.warning(getContext(), "Permission Denied\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
            }
        };

        new TedPermission(getContext())
                .setPermissionListener(permissionListener)
                .setRationaleMessage(getString(R.string.need_permission))
                .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                .setGotoSettingButtonText(getString(R.string.setting))
                .setPermissions(Conts.permissions)
                .check();
    }

    private void doRecord() {
        Intent intent = new Intent(getActivity(), RecorderService.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction("Recorder");
        if (!isTimerRunning) {
            getActivity().startService(intent);
        } else {
            getActivity().stopService(intent);
            stopTimer();
        }
    }

    public void startTimer() {
        if (!isTimerRunning) {
            isTimerRunning = true;
            if (ivCamera != null)
                ivCamera.setImageResource(R.drawable.icon_stop);
            if (tvClickRecorderFrag != null)
                tvClickRecorderFrag.setText(R.string.click_to_stop);
        }
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (elapsedTimer < 0) tvTimer.setText(TimeHelper.convertSecondsToHMmSs(0));
                else tvTimer.setText(TimeHelper.convertSecondsToHMmSs(elapsedTimer));
            }
        };
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                elapsedTimer = preHelper.getSecondsInTwoTime(preHelper.getTimeRecord());
                mHandler.obtainMessage(1).sendToTarget();
            }
        }, 0, 1000);
    }

    public void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (isTimerRunning) {
            isTimerRunning = false;
            if (ivCamera != null)
                ivCamera.setImageResource(R.drawable.ic_camera);
            if (tvClickRecorderFrag != null)
                tvClickRecorderFrag.setText(getString(R.string.click_to_record));
            if (tvTimer != null)
                tvTimer.setText("");
        }
        Toasty.success(getContext(), getString(R.string.record_success), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        broadcastManagerStop.unregisterReceiver(receiverStop);
        broadcastManagerStart.unregisterReceiver(receiverStart);
    }
}
