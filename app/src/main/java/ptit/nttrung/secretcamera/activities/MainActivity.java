package ptit.nttrung.secretcamera.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.AdSize;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;
import ptit.nttrung.secretcamera.R;
import ptit.nttrung.secretcamera.adapter.ViewPagerAdapter;
import ptit.nttrung.secretcamera.define.Conts;
import ptit.nttrung.secretcamera.fragment.RecorderFragment;
import ptit.nttrung.secretcamera.fragment.SettingTimeFragment;
import ptit.nttrung.secretcamera.helper.FileHelper;
import ptit.nttrung.secretcamera.helper.SharedPreHelper;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.tabs)
    TabLayout tabLayout;
    @BindView(R.id.viewpager)
    ViewPager viewPager;
    @BindView(R.id.adView)
    AdView adView;
    @BindView(R.id.layoutAd)
    LinearLayout layoutAd;

    public static PermissionListener permissionListener;

    private SharedPreHelper sharedPreHelper;
    private AdRequest adRequest;
    InterstitialAd intr_admob;
    com.facebook.ads.InterstitialAd intr_fan;
    com.facebook.ads.AdView adViewFb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);
        setupTabIcons();

        loadAd();

        permissionListener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {

            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                Toasty.warning(MainActivity.this, "Permission Denied\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
            }
        };

        new TedPermission(this)
                .setPermissionListener(permissionListener)
                .setRationaleMessage(getString(R.string.need_permission))
                .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                .setGotoSettingButtonText(getString(R.string.setting))
                .setPermissions(Conts.permissions)
                .check();

        if (FileHelper.getAvailableExternalMemory() <= 100) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setCancelable(true)
                    .setTitle(getString(R.string.low_memory))
                    .setMessage(getString(R.string.you_have) + FileHelper.getAvailableExternalMemorySize(this) +
                            getString(R.string.should_clear_data));

            builder.setNegativeButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            builder.create().show();
        }

        sharedPreHelper = new SharedPreHelper(this);
        sharedPreHelper.saveSendMsg();
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFrag(new RecorderFragment());
        adapter.addFrag(new SettingTimeFragment());
        viewPager.setAdapter(adapter);
    }

    private void setupTabIcons() {
        int[] tabIcons = {
                R.drawable.ic_tab_camera,
                R.drawable.ic_tab_watch
        };

        tabLayout.getTabAt(0).setIcon(tabIcons[0]);
        tabLayout.getTabAt(1).setIcon(tabIcons[1]);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_setting:
                Intent intent = new Intent(MainActivity.this, SettingActivity.class);
                startActivity(intent);
                break;
            case R.id.menu_folder:
                Intent intent2 = new Intent(MainActivity.this, FolderActivity.class);
                startActivity(intent2);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    void loadAd() {
        adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        adViewFb = new com.facebook.ads.AdView(
                MainActivity.this, getString(R.string.ad_fb_banner), AdSize.BANNER_320_50);
        adViewFb.loadAd();
        adViewFb.setAdListener(new AdListener() {
            @Override
            public void onError(Ad ad, AdError adError) {
                adView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAdLoaded(Ad ad) {
                layoutAd.addView(adViewFb);
                adView.setVisibility(View.GONE);
            }

            @Override
            public void onAdClicked(Ad ad) {
            }

            @Override
            public void onLoggingImpression(Ad ad) {

            }
        });

        intr_admob = new InterstitialAd(this);
        intr_admob.setAdUnitId(getString(R.string.ad_unit_full));
        intr_admob.loadAd(adRequest);

        intr_fan = new com.facebook.ads.InterstitialAd(this, getString(R.string.ad_fb_full));
        intr_fan.loadAd();
    }

    public void dialogExit() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(getString(R.string.exit_app));
        alertDialogBuilder
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setMessage(getString(R.string.hint))
                .setCancelable(true)
                .setPositiveButton(getString(R.string.rate), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        final String appPackageName = getPackageName();
                        try {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                        } catch (android.content.ActivityNotFoundException anfe) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + appPackageName)));
                        }
                    }
                }).setNegativeButton(getString(R.string.exit), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            showIntrAd();
            dialogExit();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    void showIntrAd() {
        if (intr_fan != null && intr_fan.isAdLoaded()) intr_fan.show();
        else {
            if (intr_admob != null && intr_admob.isLoaded()) intr_admob.show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        adViewFb.destroy();
    }
}