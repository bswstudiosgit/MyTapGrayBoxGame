package com.test.mygame.util;

import android.app.Activity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.FrameLayout;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdCallback;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.test.mygame.MainActivity;
import com.test.mygame.R;
import com.test.mygame.ResponseListener;

public class AdmobManager {

    private static AdmobManager single_instance = null;
    private String TAG_BANNER = "Banner : ";
    private String TAG_INTERSTITIAL = "Interstitial : ";
    private String TAG_REWARDED_VIDEO_AD = "Rewarded Video : ";

    private AdView adView;
    private InterstitialAd mInterstitialAd;
    private RewardedAd rewardedAd;

    // private constructor restricted to this class itself
    private AdmobManager() {
    }

    // static method to create instance of AdmobManager class
    public static AdmobManager getInstance() {
        if (single_instance == null)
            single_instance = new AdmobManager();

        return single_instance;
    }

    public void onPause() {
        if (adView != null)
            adView.pause();
    }

    public void onResume() {
        if (adView != null)
            adView.resume();
    }

    public void onDestroy() {
        if (adView != null)
            adView.destroy();
    }

    ///////////////////////// banner ads /////////////////////////////////

    public void loadBanner(final Activity context, FrameLayout adContainerView) {
        adView = new AdView(context);
        adView.setAdUnitId(context.getString(R.string.BANNER_AD_ID));

        adContainerView.removeAllViews();
        adContainerView.addView(adView);

        AdSize adSize = getAdSize(context, adContainerView);
        adView.setAdSize(adSize);

        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        hideBannerAds();

        adView.setAdListener(new AdListener() {
            @Override
            public void onAdClicked() {
                super.onAdClicked();
                Log.d(TAG_BANNER, "onAdClicked()");
                Factory.getInstance().showToast(context, TAG_BANNER + "onAdClicked()");
            }

            @Override
            public void onAdClosed() {
                super.onAdClosed();
                Log.d(TAG_BANNER, "onAdClosed()");
                Factory.getInstance().showToast(context, TAG_BANNER + "onAdClosed()");
            }

            @Override
            public void onAdFailedToLoad(LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                Log.d(TAG_BANNER, "onAdFailedToLoad()");
                Factory.getInstance().showToast(context, TAG_BANNER + "onAdFailedToLoad()");
            }

            @Override
            public void onAdImpression() {
                super.onAdImpression();
                Log.d(TAG_BANNER, "onAdImpression()");
                Factory.getInstance().showToast(context, TAG_BANNER + "onAdImpression()");
            }

            @Override
            public void onAdLeftApplication() {
                super.onAdLeftApplication();
                Log.d(TAG_BANNER, "onAdLeftApplication()");
                Factory.getInstance().showToast(context, TAG_BANNER + "onAdLeftApplication()");
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                Log.d(TAG_BANNER, "onAdLoaded()");
                Factory.getInstance().showToast(context, TAG_BANNER + "onAdLoaded()");
            }

            @Override
            public void onAdOpened() {
                super.onAdOpened();
                Log.d(TAG_BANNER, "onAdOpened()");
                Factory.getInstance().showToast(context, TAG_BANNER + "onAdOpened()");
            }
        });
    }

    private AdSize getAdSize(Activity context, FrameLayout adContainerView) {
        // Determine the screen width (less decorations) to use for the ad width.
        Display display = context.getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float density = outMetrics.density;

        float adWidthPixels = adContainerView.getWidth();

        // If the ad hasn't been laid out, default to the full screen width.
        if (adWidthPixels == 0) {
            adWidthPixels = outMetrics.widthPixels;
        }

        int adWidth = (int) (adWidthPixels / density);

        return AdSize.getCurrentOrientationBannerAdSizeWithWidth(context, adWidth);
    }

    public void showBannerAds() {
        if (adView != null)
            adView.setVisibility(View.VISIBLE);
    }

    public void hideBannerAds() {
        if (adView != null)
            adView.setVisibility(View.GONE);
    }

    ///////////////////////// banner ads /////////////////////////////////

    ///////////////////////// interstitial ad /////////////////////////////////

    public void initializeInterstitialAd(final Activity context) {
        mInterstitialAd = new InterstitialAd(context);
        mInterstitialAd.setAdUnitId(context.getString(R.string.INTERSTITIAL_AD_ID));
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdOpened() {
                super.onAdOpened();
                Log.d(TAG_INTERSTITIAL, "onAdOpened()");
                Factory.getInstance().showToast(context, TAG_INTERSTITIAL + "onAdOpened()");
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                Log.d(TAG_INTERSTITIAL, "onAdLoaded()");
                Factory.getInstance().showToast(context, TAG_INTERSTITIAL + "onAdLoaded()");
            }

            @Override
            public void onAdLeftApplication() {
                super.onAdLeftApplication();
                Log.d(TAG_INTERSTITIAL, "onAdLeftApplication()");
                Factory.getInstance().showToast(context, TAG_INTERSTITIAL + "onAdLeftApplication()");
            }

            @Override
            public void onAdImpression() {
                super.onAdImpression();
                Log.d(TAG_INTERSTITIAL, "onAdImpression()");
                Factory.getInstance().showToast(context, TAG_INTERSTITIAL + "onAdImpression()");
            }

            @Override
            public void onAdFailedToLoad(LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                Log.d(TAG_INTERSTITIAL, "onAdFailedToLoad()");
                Factory.getInstance().showToast(context, TAG_INTERSTITIAL + "onAdFailedToLoad()");
            }

            @Override
            public void onAdClosed() {
                super.onAdClosed();
                Log.d(TAG_INTERSTITIAL, "onAdClosed()");
                Factory.getInstance().showToast(context, TAG_INTERSTITIAL + "onAdClosed()");
                loadInterstitialAd();

                MainActivity mainActivity = (MainActivity) context;
                if (mainActivity.gameScreen != null && mainActivity.gameScreen.haveShowedInterstitialAdFromGameStart) {
                    mainActivity.gameScreen.handleAfterInterstitialAdClosed();
                }
            }

            @Override
            public void onAdClicked() {
                super.onAdClicked();
                Log.d(TAG_INTERSTITIAL, "onAdClicked()");
                Factory.getInstance().showToast(context, TAG_INTERSTITIAL + "onAdClicked()");
            }
        });
    }

    public void loadInterstitialAd() {
        if (mInterstitialAd != null && !mInterstitialAd.isLoaded() && !mInterstitialAd.isLoading())
            mInterstitialAd.loadAd(new AdRequest.Builder().build());
    }

    public void showInterstitialAd() {
        if (mInterstitialAd != null && mInterstitialAd.isLoaded())
            mInterstitialAd.show();
    }

    public boolean isInterstitialAdLoaded() {
        if (mInterstitialAd != null)
            return mInterstitialAd.isLoaded();
        return false;
    }

    ///////////////////////// interstitial ad /////////////////////////////////

    ///////////////////////// rewarded video ad /////////////////////////////////

    public void loadRewardedVideoAd(final Activity context, final ResponseListener listener) {
        rewardedAd = new RewardedAd(context, context.getString(R.string.REWARDED_VIDEO_AD_ID));
        rewardedAd.loadAd(new AdRequest.Builder().build(), new RewardedAdLoadCallback() {
            @Override
            public void onRewardedAdLoaded() {
                super.onRewardedAdLoaded();
                Log.d(TAG_REWARDED_VIDEO_AD, "onRewardedAdLoaded()");
                Factory.getInstance().showToast(context, TAG_REWARDED_VIDEO_AD + "onRewardedAdLoaded()");

                if (listener != null)
                    listener.onPositiveResponse();
            }

            @Override
            public void onRewardedAdFailedToLoad(LoadAdError loadAdError) {
                super.onRewardedAdFailedToLoad(loadAdError);
                Log.d(TAG_REWARDED_VIDEO_AD, "onRewardedAdFailedToLoad()");
                Factory.getInstance().showToast(context, TAG_REWARDED_VIDEO_AD + "onRewardedAdFailedToLoad()");

                if (listener != null)
                    listener.onNegativeResponse();
            }
        });
    }

    public boolean isRewardedVideoAdLoaded() {
        if (rewardedAd != null)
            return rewardedAd.isLoaded();
        return false;
    }

    public void showRewardedVideoAd(Activity activity, RewardedAdCallback adCallback) {
        if (rewardedAd != null && rewardedAd.isLoaded())
            rewardedAd.show(activity, adCallback);
    }

    ///////////////////////// rewarded video ad /////////////////////////////////
}