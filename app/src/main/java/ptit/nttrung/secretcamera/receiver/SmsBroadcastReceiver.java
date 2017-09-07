package ptit.nttrung.secretcamera.receiver;

/**
 * Created by im on 6/29/2017.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsMessage;

import ptit.nttrung.secretcamera.helper.ServiceHelper;
import ptit.nttrung.secretcamera.helper.SharedPreHelper;


public class SmsBroadcastReceiver extends BroadcastReceiver {

    public static final String SMS_BUNDLE = "pdus";
    private SharedPreHelper sharedPreHelper;

    public void onReceive(Context context, Intent intent) {
        Bundle intentExtras = intent.getExtras();
        if (intentExtras != null) {
            Object[] sms = (Object[]) intentExtras.get(SMS_BUNDLE);
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
            boolean recordBySms = sharedPrefs.getBoolean("prefRecordBySms", false);
            String value = sharedPrefs.getString("prefTypeSend", "0");
            String phoneNumber = sharedPrefs.getString("prefPhoneNumber", "");

            if (recordBySms) {
                for (int i = 0; i < sms.length; ++i) {
                    SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) sms[i]);
                    String smsBody = smsMessage.getMessageBody().toString();
                    sharedPreHelper = new SharedPreHelper(context);
                    switch (value) {
                        case "0"://bat ky so nao
                            if (smsBody.trim().equalsIgnoreCase(sharedPreHelper.getSendMsg()))
                                ServiceHelper.startRecordService(context);
                            break;
                        case "1"://so cu the
                            String address = smsMessage.getOriginatingAddress();
                            boolean valid = (!phoneNumber.equals("") && PhoneNumberUtils.compare(context, address, phoneNumber)
                                    && smsBody.trim().equalsIgnoreCase(sharedPreHelper.getSendMsg()));
                            if (valid) ServiceHelper.startRecordService(context);
                            break;
                    }
                }
            }
        }
    }
}