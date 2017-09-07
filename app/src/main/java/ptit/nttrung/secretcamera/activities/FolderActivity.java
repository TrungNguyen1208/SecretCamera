package ptit.nttrung.secretcamera.activities;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.AdSize;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;
import ptit.nttrung.secretcamera.R;
import ptit.nttrung.secretcamera.adapter.VideoRecordAdapter;
import ptit.nttrung.secretcamera.define.Conts;
import ptit.nttrung.secretcamera.helper.FileHelper;
import ptit.nttrung.secretcamera.helper.TimeHelper;
import ptit.nttrung.secretcamera.model.VideoRecord;

public class FolderActivity extends AppCompatActivity {

    private static final String TAG = FolderActivity.class.getName();

    @BindView(R.id.toolbar_folder_activity)
    Toolbar toolbar;
    @BindView(R.id.rv_list_videos)
    RecyclerView rvVideos;
    @BindView(R.id.ad_folder)
    AdView adFolder;
    @BindView(R.id.layout_ad_folder)
    LinearLayout layoutAdFolder;

    com.facebook.ads.AdView adViewFb;

    private List<VideoRecord> listVideo;
    private VideoRecordAdapter adapter;
    private AdRequest adRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder);

        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        setupRecylerView();

        adRequest = new AdRequest.Builder().build();
        adFolder.loadAd(adRequest);

        adViewFb = new com.facebook.ads.AdView(FolderActivity.this, getString(R.string.ad_fb_banner), AdSize.BANNER_320_50);
        adViewFb.loadAd();
        adViewFb.setAdListener(new AdListener() {
            @Override
            public void onError(Ad ad, AdError adError) {
                adFolder.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAdLoaded(Ad ad) {
                layoutAdFolder.addView(adViewFb);
                adFolder.setVisibility(View.GONE);
            }

            @Override
            public void onAdClicked(Ad ad) {
            }

            @Override
            public void onLoggingImpression(Ad ad) {

            }
        });
    }


    private void setupRecylerView() {
        listVideo = new ArrayList<>();
        adapter = new VideoRecordAdapter(FolderActivity.this, listVideo);
        rvVideos.setAdapter(adapter);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rvVideos.setLayoutManager(linearLayoutManager);
        rvVideos.setHasFixedSize(true);

        adapter.setItemClickCallBack(new VideoRecordAdapter.ItemClickCallBack() {
            @Override
            public void onImageClick(View view, int position) {
                SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(FolderActivity.this);
                boolean checkWatch = sharedPrefs.getBoolean("prefWatch", true);
                if (checkWatch) {
                    Uri uri = Uri.parse(listVideo.get(position).getFilePath());
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    intent.setDataAndType(uri, "video/mp4");
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(FolderActivity.this, VideoViewActivity.class);
                    intent.putExtra(Conts.INTENT_KEY_FILE_PATH, listVideo.get(position).getFilePath());
                    startActivity(intent);
                }
            }

            @Override
            public void onChkBoxClick(View view, int position, boolean isChecked) {
                listVideo.get(position).setSelect(isChecked);
            }
        });

        new LoadVideo().execute(FileHelper.dirPath);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    class LoadVideo extends AsyncTask<String, Void, Void> {

        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(FolderActivity.this);
            dialog.setMessage(getString(R.string.loading));
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        protected Void doInBackground(String... strings) {
            String folderPath = strings[0];
            Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            String condition = MediaStore.Video.Media.DATA + " like ?";
            String[] selectionArguments = new String[]{"%" + folderPath + "%"};

            String sortOrder = MediaStore.Video.Media.DATE_TAKEN + " DESC";
            String[] projection = {MediaStore.Video.Media._ID, MediaStore.Video.Media.BUCKET_ID,
                    MediaStore.Video.Media.DATA, MediaStore.Video.Media.DURATION,
                    MediaStore.Video.Media.DISPLAY_NAME, MediaStore.Video.Media.DATE_TAKEN};

            Cursor cursor = getContentResolver().query(uri, projection, condition, selectionArguments, sortOrder);

            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID);
            int pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            int duraColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION);
            int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME);
            int dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_TAKEN);

            if (cursor != null) {
                ContentResolver resolver = getApplicationContext().getContentResolver();
                while (cursor.moveToNext()) {
                    int id = cursor.getInt(idColumn);
                    String filePath = cursor.getString(pathColumn);
                    String fileName = cursor.getString(nameColumn).replace(".mp4", "");
                    String duration = cursor.getString(duraColumn);
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(Long.parseLong(cursor.getString(dateColumn)));
                    String date = TimeHelper.parseCalen2StrNoSS(calendar);

                    Bitmap thumbNail = MediaStore.Video.Thumbnails.getThumbnail(resolver, id,
                            MediaStore.Video.Thumbnails.MICRO_KIND, null);

                    VideoRecord videoRecord = new VideoRecord(filePath, thumbNail, duration, fileName, date);
                    listVideo.add(videoRecord);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (dialog != null && dialog.isShowing())
                dialog.dismiss();
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_folder, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete:
                if (checkSelectVideo(listVideo) == -1)
                    Toasty.warning(this, getString(R.string.select_item_to_delete), Toast.LENGTH_SHORT).show();
                else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this)
                            .setCancelable(true)
                            .setTitle(getString(R.string.confirm))
                            .setMessage(getString(R.string.do_you_want_delete));

                    builder.setPositiveButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });

                    builder.setNegativeButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Iterator iterator = listVideo.iterator();
                            while (iterator.hasNext()) {
                                VideoRecord videoRecord = (VideoRecord) iterator.next();
                                if (videoRecord.isSelect()) {
                                    FileHelper.removeAllForPaths(new String[]{videoRecord.getFilePath()}, FolderActivity.this);
                                    iterator.remove();
                                }
                            }
                            adapter.notifyDataSetChanged();
                        }
                    });
                    builder.create().show();
                }
                break;

            case R.id.action_select_all:
                for (VideoRecord videoRecord : listVideo)
                    videoRecord.setSelect(true);
                adapter.notifyDataSetChanged();
                break;

            case R.id.action_rename:
                final int pos = checkSelectVideo(listVideo);

                if (pos < 0) {
                    AlertDialog.Builder warringBuilder = new AlertDialog.Builder(this)
                            .setCancelable(true)
                            .setTitle(getString(R.string.warning))
                            .setMessage(getString(R.string.msg_rename_video));
                    warringBuilder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    warringBuilder.create().show();
                } else {
                    LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View layout = inflater.inflate(R.layout.dialog_rename, null);
                    AlertDialog.Builder builderRename = new AlertDialog.Builder(this)
                            .setView(layout)
                            .setCancelable(true);

                    final EditText editVideoName = (EditText) layout.findViewById(R.id.edit_name_video);
                    editVideoName.setText(listVideo.get(pos).getFileName());
                    builderRename.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String videoName = editVideoName.getText().toString().trim();
                            if (videoName.equals("")) {
                                Toasty.error(FolderActivity.this, getString(R.string.input_wrong), Toast.LENGTH_SHORT).show();
                            } else {
                                listVideo.get(pos).setFileName(videoName);
                                FileHelper.renameFile(listVideo.get(pos).getFilePath(), videoName, FolderActivity.this);
                                adapter.notifyDataSetChanged();
                            }
                        }
                    });

                    builderRename.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });

                    builderRename.create().show();
                }
                break;
            case R.id.action_share:
                final int posShare = checkSelectVideo(listVideo);

                if (posShare < 0) {
                    AlertDialog.Builder warringBuilder = new AlertDialog.Builder(this)
                            .setCancelable(true)
                            .setTitle(getString(R.string.warning))
                            .setMessage(R.string.warring_share);
                    warringBuilder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    warringBuilder.create().show();
                } else {
                    FileHelper.shareVideo(this, listVideo.get(posShare).getFilePath());
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private int checkSelectVideo(List<VideoRecord> listVideo) {
        int kt = 0;
        int pos = -1;
        for (int j = 0; j < listVideo.size(); j++) {
            if (listVideo.get(j).isSelect()) {
                kt++;
                if (kt > 1) return -2;
                pos = j;
            }
        }
        return pos;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        adViewFb.destroy();
    }
}
