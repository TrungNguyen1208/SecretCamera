package ptit.nttrung.secretcamera.helper;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Calendar;
import java.util.Random;

import ptit.nttrung.secretcamera.define.Conts;


/**
 * Created by TrungNguyen on 6/9/2017.
 */

public class SharedPreHelper {
    public SharedPreferences preferences;
    public SharedPreferences.Editor editor;
    private Context context;
    private Calendar timeRecord;

    public SharedPreHelper(Context context) {
        preferences = context.getSharedPreferences(Conts.PRE_NAME, Context.MODE_PRIVATE);
        editor = preferences.edit();
        this.context = context;
    }

    public void saveTimeToPre() {
        Calendar calendar = Calendar.getInstance();
        editor.putString(Conts.TIME_RECORD, TimeHelper.parseCalen2Str(calendar));
        editor.commit();
    }

    public void remove() {
        editor.remove(Conts.TIME_RECORD);
        editor.commit();
    }

    public boolean haveTimeStart() {
        return preferences.contains(Conts.TIME_RECORD);
    }

    public Calendar getTimeRecord() {
        String strTimeRecord = preferences.getString(Conts.TIME_RECORD, TimeHelper.parseCalen2Str(Calendar.getInstance()));
        Calendar timeRecord = TimeHelper.parseStr2Calendar(strTimeRecord);
        return timeRecord;
    }

    public long getSecondsInTwoTime(Calendar timeRecord) {
        long seconds = 0;
        Calendar now = Calendar.getInstance();
        seconds = (now.getTimeInMillis() - timeRecord.getTimeInMillis()) / 1000;
        return seconds;
    }

    public void saveSendMsg() {
        boolean firstCreateDb = preferences.getBoolean(Conts.FIRST_RUN, false);
        if (firstCreateDb == false) {
            editor.putString(Conts.SMS_START_RECORD,createRandomSendMsg());
            editor.putBoolean(Conts.FIRST_RUN, true);
            editor.commit();
        }
    }

    public String getSendMsg(){
        return preferences.getString(Conts.SMS_START_RECORD,createRandomSendMsg());
    }

    public String createRandomSendMsg() {
        String s = "SECRET ";
        String alphabet = "0123456789";

        for (int i = 0; i < 26; i++) {
            char c = (char) (i + 'A');
            alphabet = alphabet + c;
        }
        final int N = alphabet.length();
        Random r = new Random();
        String random = "";
        while (!hasNums(random)) {
            random = "";
            for (int i = 0; i < 6; i++)
                random = random + alphabet.charAt(r.nextInt(N));
        }
        return s + random;
    }

    public boolean hasNums(String str) {
        char[] nums = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
        for (int i = 0; i < str.length(); i++) {
            for (int j = 0; j < nums.length; j++) {
                if (str.charAt(i) == nums[j]) return true;
            }
        }
        return false;
    }
}
