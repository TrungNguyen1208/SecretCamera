package ptit.nttrung.secretcamera.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import ptit.nttrung.secretcamera.helper.ServiceHelper;
import ptit.nttrung.secretcamera.service.RecorderService;


public class QuickStartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!ServiceHelper.isServiceRunning(RecorderService.class, this)) {
            Intent intent = new Intent(this, RecorderService.class);
            startService(intent);
        }
        finish();
    }
}
