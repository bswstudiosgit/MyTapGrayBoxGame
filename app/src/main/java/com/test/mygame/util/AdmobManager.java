package com.test.mygame.util;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.VideoController;
import com.google.android.gms.ads.VideoOptions;
import com.google.android.gms.ads.formats.MediaView;
import com.google.android.gms.ads.formats.NativeAdOptions;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.ads.formats.UnifiedNativeAdView;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdCallback;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.test.mygame.MainActivity;
import com.test.mygame.R;

public class AdmobManager {

    private static AdmobManager single_instance = null;
    private String TAG_BANNER = "Banner : ";
    public String TAG_INTERSTITIAL = "Interstitial : ";
    public String TAG_REWARDED_VIDEO_AD = "Rewarded Video : ";
    private String TAG_NATIVE_ADVANCED_AD = "Native Advanced : ";

    private AdView adView;
    private InterstitialAd mInterstitialAd;
    private RewardedAd rewardedAd;
    public UnifiedNativeAd nativeAd;

    // private constructor restricted to this class itself
    private AdmobManager() {
    }

    // static method to create instance of AdmobManager class
    public static AdmobManager getInstance() {
        if (single_instance == null)
            single_instance = new AdmobManager();

        return single_instance;
    }

    public void initializeAllAds(MainActivity activity, FrameLayout adContainerView) {
        // initiating adaptive banner ad
        loadBanner(activity, adContainerView);

        // initiating interstitial ads
        initializeInterstitialAd(activity);
        loadInterstitialAd();

        // loading and caching native advanced ad
        refreshNativeAdvancedAd(activity);
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

        if (nativeAd != null)
            nativeAd.destroy();
    }

    ///////////////////////// banner ads /////////////////////////////////

    private void loadBanner(final Activity context, FrameLayout adContainerView) {
        adView = new AdView(context);
        adView.setAdUnitId(context.getString(R.string.BANNER_AD_ID));

        adContainerView.removeAllViews();
        adContainerView.addView(adView);

        AdSize adSize = getAdSize(context, adContainerView);
        resetAdHeight(adSize.getHeightInPixels(context), adContainerView);
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

    private void resetAdHeight(int heightInPixels, FrameLayout adContainerView) {
        try {
            ViewGroup.LayoutParams params = adContainerView.getLayoutParams();
            params.height = heightInPixels;
            adContainerView.setLayoutParams(params);
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
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

        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth);
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
    }

    public void loadInterstitialAd() {
        if (mInterstitialAd != null && !mInterstitialAd.isLoaded() && !mInterstitialAd.isLoading())
            mInterstitialAd.loadAd(new AdRequest.Builder().build());
    }

    public void showInterstitialAd(AdListener listener) {
        if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
            if (listener != null)
                mInterstitialAd.setAdListener(listener);
        }
    }

    public boolean isInterstitialAdLoaded() {
        if (mInterstitialAd != null)
            return mInterstitialAd.isLoaded();
        return false;
    }

    ///////////////////////// interstitial ad /////////////////////////////////

    ///////////////////////// rewarded video ad /////////////////////////////////

    public void loadRewardedVideoAd(final Activity context, RewardedAdLoadCallback rewardedAdLoadCallback) {
        rewardedAd = new RewardedAd(context, context.getString(R.string.REWARDED_VIDEO_AD_ID));
        rewardedAd.loadAd(new AdRequest.Builder().build(), rewardedAdLoadCallback);
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

    ///////////////////////// native advanced ad /////////////////////////////////

    private void refreshNativeAdvancedAd(final Context context) {
        AdLoader.Builder builder = new AdLoader.Builder(context, context.getString(R.string.NATIVE_ADVANCED_AD_ID));

        builder.forUnifiedNativeAd(new UnifiedNativeAd.OnUnifiedNativeAdLoadedListener() {
            // OnUnifiedNativeAdLoadedListener implementation.
            @Override
            public void onUnifiedNativeAdLoaded(UnifiedNativeAd unifiedNativeAd) {
                // You must call destroy on old ads when you are done with them,
                // otherwise you will have a memory leak.
                if (nativeAd != null) {
                    nativeAd.destroy();
                }
                nativeAd = unifiedNativeAd;
            }

        });

        VideoOptions videoOptions = new VideoOptions.Builder()
                .setStartMuted(false)
                .build();

        NativeAdOptions adOptions = new NativeAdOptions.Builder()
                .setVideoOptions(videoOptions)
                .build();
        builder.withNativeAdOptions(adOptions);

        AdLoader adLoader = builder.withAdListener(new AdListener() {
            @Override
            public void onAdClicked() {
                super.onAdClicked();
                Log.d(TAG_NATIVE_ADVANCED_AD, "onAdClicked()");
                Factory.getInstance().showToast(context, TAG_NATIVE_ADVANCED_AD + "onAdClicked()");
            }

            @Override
            public void onAdClosed() {
                super.onAdClosed();
                Log.d(TAG_NATIVE_ADVANCED_AD, "onAdClosed()");
                Factory.getInstance().showToast(context, TAG_NATIVE_ADVANCED_AD + "onAdClosed()");
                refreshNativeAdvancedAd(context);
            }

            @Override
            public void onAdFailedToLoad(LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                Log.d(TAG_NATIVE_ADVANCED_AD, "onAdFailedToLoad()");
                Factory.getInstance().showToast(context, TAG_NATIVE_ADVANCED_AD + "onAdFailedToLoad()");
            }

            @Override
            public void onAdImpression() {
                super.onAdImpression();
                Log.d(TAG_NATIVE_ADVANCED_AD, "onAdImpression()");
                Factory.getInstance().showToast(context, TAG_NATIVE_ADVANCED_AD + "onAdImpression()");
            }

            @Override
            public void onAdLeftApplication() {
                super.onAdLeftApplication();
                Log.d(TAG_NATIVE_ADVANCED_AD, "onAdLeftApplication()");
                Factory.getInstance().showToast(context, TAG_NATIVE_ADVANCED_AD + "onAdLeftApplication()");
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                Log.d(TAG_NATIVE_ADVANCED_AD, "onAdLoaded()");
                Factory.getInstance().showToast(context, TAG_NATIVE_ADVANCED_AD + "onAdLoaded()");
            }

            @Override
            public void onAdOpened() {
                super.onAdOpened();
                Log.d(TAG_NATIVE_ADVANCED_AD, "onAdOpened()");
                Factory.getInstance().showToast(context, TAG_NATIVE_ADVANCED_AD + "onAdOpened()");
            }
        }).build();
        adLoader.loadAd(new AdRequest.Builder().build());
    }

    public void showNativeAd(FrameLayout container, UnifiedNativeAdView adView) {
        if (nativeAd != null) {
            populateUnifiedNativeAdView(nativeAd, adView);
            container.removeAllViews();
            container.addView(adView);
        }
    }

    private void populateUnifiedNativeAdView(UnifiedNativeAd nativeAd, UnifiedNativeAdView adView) {
        // Set the media view.
        adView.setMediaView((MediaView) adView.findViewById(R.id.ad_media));

        // Set other ad assets.
        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
        adView.setBodyView(adView.findViewById(R.id.ad_text_body));
        adView.setCallToActionView(adView.findViewById(R.id.ad_click_button));
        adView.setIconView(adView.findViewById(R.id.ad_icon));
        adView.setStarRatingView(adView.findViewById(R.id.ad_rating_bar));

        // The headline and mediaContent are guaranteed to be in every UnifiedNativeAd.
        ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());
        adView.getMediaView().setMediaContent(nativeAd.getMediaContent());

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        if (nativeAd.getBody() == null) {
            adView.getBodyView().setVisibility(View.INVISIBLE);
        } else {
            adView.getBodyView().setVisibility(View.VISIBLE);
            ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
        }

        if (nativeAd.getCallToAction() == null) {
            adView.getCallToActionView().setVisibility(View.INVISIBLE);
        } else {
            adView.getCallToActionView().setVisibility(View.VISIBLE);
            ((Button) adView.getCallToActionView()).setText(nativeAd.getCallToAction());
        }

        if (nativeAd.getIcon() == null) {
            adView.getIconView().setVisibility(View.GONE);
        } else {
            ((ImageView) adView.getIconView()).setImageDrawable(
                    nativeAd.getIcon().getDrawable());
            adView.getIconView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getStarRating() == null) {
            adView.getStarRatingView().setVisibility(View.INVISIBLE);
        } else {
            ((RatingBar) adView.getStarRatingView())
                    .setRating(nativeAd.getStarRating().floatValue());
            adView.getStarRatingView().setVisibility(View.VISIBLE);
        }

        // This method tells the Google Mobile Ads SDK that you have finished populating your
        // native ad view with this native ad.
        adView.setNativeAd(nativeAd);

        // Get the video controller for the ad. One will always be provided, even if the ad doesn't
        // have a video asset.
        VideoController vc = nativeAd.getVideoController();

        // Updates the UI to say whether or not this ad has a video asset.
        if (vc.hasVideoContent()) {
            // Create a new VideoLifecycleCallbacks object and pass it to the VideoController. The
            // VideoController will call methods on this object when events occur in the video
            // lifecycle.
            vc.setVideoLifecycleCallbacks(new VideoController.VideoLifecycleCallbacks() {
                @Override
                public void onVideoEnd() {
                    // Publishers should allow native ads to complete video playback before
                    // refreshing or replacing them with another ad in the same UI location.
                    super.onVideoEnd();
                }
            });
        }
    }

    ///////////////////////// native advanced ad /////////////////////////////////
}
