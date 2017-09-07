package ptit.nttrung.secretcamera.fragment;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;
import ptit.nttrung.secretcamera.R;
import ptit.nttrung.secretcamera.define.Conts;
import ptit.nttrung.secretcamera.helper.TimeHelper;
import ptit.nttrung.secretcamera.receiver.AlarmReceiver;

import static android.content.Context.ALARM_SERVICE;


/**
 * Created by TrungNguyen on 4/21/2017.
 */

public class SettingTimeFragment extends Fragment implements View.OnClickListener {

    public static final String TAG = SettingTimeFragment.class.getName();

    @BindView(R.id.btn_save)
    Button btnSave;
    @BindView(R.id.tv_edit_date_frag)
    TextView tvEditDate;
    @BindView(R.id.tv_edit_time_frag)
    TextView tvEditTime;
    @BindView(R.id.tv_edit_duration_frag)
    TextView tvEditDuration;
    @BindView(R.id.tv_edit_use_cam_frag)
    TextView tvEditUseCam;
    @BindView(R.id.tv_show_date_frag)
    TextView tvDate;
    @BindView(R.id.tv_show_time_frag)
    TextView tvTime;
    @BindView(R.id.tv_show_duration_frag)
    TextView tvDuration;
    @BindView(R.id.tv_show_use_cam_frag)
    TextView tvUseCam;
    @BindView(R.id.fl_setting_frag)
    FrameLayout flSettingFrag;

    private DatePickerDialog datePickerDialog;
    private TimePickerDialog tpd;
    private Calendar now;
    private Context mContext;
    private int duration = 5;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting_time, container, false);
        ButterKnife.bind(this, view);

        now = Calendar.getInstance();
        setupDisplay();

        tvEditDate.setOnClickListener(this);
        tvEditTime.setOnClickListener(this);
        tvEditDuration.setOnClickListener(this);
        tvEditUseCam.setOnClickListener(this);
        btnSave.setOnClickListener(this);

        tvDuration.setText(duration + " " + getString(R.string.min));

        return view;
    }

    private void setupDisplay() {
        tvDate.setText(TimeHelper.parseDate2Str(Calendar.getInstance().getTime()));
        tvTime.setText(TimeHelper.parseTime2Str(Calendar.getInstance()));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_save:
                Intent intent = new Intent(mContext, AlarmReceiver.class);
                int useCam;
                if (tvUseCam.equals(getString(R.string.front))) useCam = 1;
                else useCam = 0;
                intent.putExtra(Conts.CAMERA_USE, String.valueOf(useCam));
                intent.putExtra(Conts.CAMERA_DURATION, String.valueOf(duration * 60));

                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        getActivity().getApplication(), 0, intent, 0);
                AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(ALARM_SERVICE);
                alarmManager.set(AlarmManager.RTC_WAKEUP, now.getTimeInMillis(), pendingIntent);

                Toasty.success(mContext, "Start recorder at : " + TimeHelper.parseCalen2Str(now), Toast.LENGTH_SHORT).show();
                break;

            case R.id.tv_edit_date_frag:
                datePickerDialog = DatePickerDialog.newInstance(new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
                        now.set(year, monthOfYear, dayOfMonth);
                        Date date = now.getTime();
                        tvDate.setText(TimeHelper.parseDate2Str(date));
                    }
                }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.setVersion(DatePickerDialog.Version.VERSION_1);
                datePickerDialog.show(getActivity().getFragmentManager(), "Datepickerdialog");
                break;

            case R.id.tv_edit_time_frag:
                tpd = TimePickerDialog.newInstance(new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {
                        now.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        now.set(Calendar.MINUTE, minute);
                        tvTime.setText(TimeHelper.parseTime2Str(now));
                    }
                }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), false);

                tpd.setVersion(TimePickerDialog.Version.VERSION_1);
                tpd.show(getActivity().getFragmentManager(), "Timepickerdialog");
                break;

            case R.id.tv_edit_duration_frag:
                showAlertDialog();
                break;

            case R.id.tv_edit_use_cam_frag:
                if (tvUseCam.getText().equals(getString(R.string.back))) {
                    tvUseCam.setText(getString(R.string.front));
                } else {
                    tvUseCam.setText(getString(R.string.back));
                }
                break;
        }
    }

    private void showAlertDialog() {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.dialog_seekbar, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext)
                .setView(layout)
                .setCancelable(true);

        final SeekBar sb = (SeekBar) layout.findViewById(R.id.seekBar1);
        final TextView tvDuraDialog = (TextView) layout.findViewById(R.id.tv_duration_dialog);
        tvDuraDialog.setText(sb.getProgress() + 2 + " " + getString(R.string.min));

        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
                progress = progressValue;
                tvDuraDialog.setText(progress + 2 + " " + getString(R.string.min));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                duration = sb.getProgress() + 2;
                tvDuration.setText(duration + " " + getString(R.string.min));
            }
        });

        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        builder.create().show();
    }
}
