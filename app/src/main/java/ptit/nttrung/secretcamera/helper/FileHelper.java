package ptit.nttrung.secretcamera.helper;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;
import android.provider.MediaStore;
import android.text.format.Formatter;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import ptit.nttrung.secretcamera.R;


/**
 * Created by TrungNguyen on 4/21/2017.
 */

public class FileHelper {
    public static String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/SercetVideoGapptech";
    public static final int MEDIA_TYPE_VIDEO = 2;
    public static final String ERROR = "ERROR";

    public static File getOutputMediaFile(int type) {
        File mediaStorageDir = new File(dirPath);
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs())
                return null;
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        File mediaFile;

        if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "SERCET_" + timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    public static void removeAllForPaths(String[] paths, Context context) {
        final String[] FIELDS = {MediaStore.MediaColumns._ID, MediaStore.MediaColumns.DATA, MediaStore.MediaColumns.TITLE};
        if (paths == null || paths.length == 0) return;
        String select = "";
        for (String path : paths) {
            if (!select.equals("")) select += " OR ";
            select += MediaStore.MediaColumns.DATA + "=?";
        }

        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        Cursor ca = context.getContentResolver().query(uri, FIELDS, select, paths, null);
        for (ca.moveToFirst(); !ca.isAfterLast(); ca.moveToNext()) {
            int id = ca.getInt(ca.getColumnIndex(MediaStore.MediaColumns._ID));
            uri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id);
            context.getContentResolver().delete(uri, null, null);
        }
        ca.close();
    }

    public static void renameFile(String path, String fileName, Context context) {
        File mediaStorageDir = new File(dirPath);
        File fileFrom = new File(path);
        File fileTo = new File(mediaStorageDir.getPath() + File.separator + fileName + ".mp4");
        fileFrom.renameTo(fileTo);
        Intent mediaScannerIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri fileContentUri = Uri.fromFile(fileTo);
        mediaScannerIntent.setData(fileContentUri);
        context.sendBroadcast(mediaScannerIntent);
        removeAllForPaths(new String[]{path}, context);
    }

    public static boolean externalMemoryAvailable() {
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
    }

    public static String getAvailableExternalMemorySize(Context context) {
        if (externalMemoryAvailable()) {
            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSize();
            long availableBlocks = stat.getAvailableBlocks();
            return Formatter.formatFileSize(context, blockSize * availableBlocks);
        } else {
            return ERROR;
        }
    }

    public static long getAvailableExternalMemory() {
        if (externalMemoryAvailable()) {
            StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
            long bytesAvailable = (long) stat.getBlockSize() * (long) stat.getAvailableBlocks();
            long megAvailable = bytesAvailable / (1024 * 1024);
            return megAvailable;
        } else {
            return 0;
        }
    }

    public static void shareVideo(final Activity activity, String path) {
        MediaScannerConnection.scanFile(activity, new String[]{path},
                null, new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        Intent shareIntent = new Intent(
                                Intent.ACTION_SEND);
                        shareIntent.setType("video/*");
                        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                        shareIntent
                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                        activity.startActivity(Intent.createChooser(shareIntent,
                                activity.getString(R.string.str_share_this_video)));

                    }
                });
    }
}
