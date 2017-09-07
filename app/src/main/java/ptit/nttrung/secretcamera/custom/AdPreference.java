package ptit.nttrung.secretcamera.custom;

import android.app.Activity;
import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import ptit.nttrung.secretcamera.R;

/**
 * Created by TrungNguyen on 6/28/2017.
 */

public class AdPreference extends Preference {

    public AdPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public AdPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AdPreference(Context context) {
        super(context);
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        final View view = super.onCreateView(parent);

        Activity activity = (Activity) getContext();

        final AdView adView = new AdView(activity);
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId(activity.getString(R.string.ad_unit_banner));
        ((LinearLayout) view).addView(adView);

        AdRequest request = new AdRequest.Builder().build();
        adView.loadAd(request);
//        adView.setVisibility(View.GONE);
//
//        final com.facebook.ads.AdView adViewFb = new com.facebook.ads.AdView(
//                activity, getContext().getString(R.string.ad_fb_banner), com.facebook.ads.AdSize.BANNER_320_50);
//        adViewFb.loadAd();
//        adViewFb.setAdListener(new AdListener() {
//            @Override
//            public void onError(Ad ad, AdError adError) {
//                adView.setVisibility(View.VISIBLE);
//            }
//
//            @Override
//            public void onAdLoaded(Ad ad) {
//                ((LinearLayout) view).addView(adViewFb);
//                adView.setVisibility(View.GONE);
//            }
//
//            @Override
//            public void onAdClicked(Ad ad) {
//            }
//        });

        return view;
    }
}
