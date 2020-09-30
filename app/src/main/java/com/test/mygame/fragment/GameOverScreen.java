package com.test.mygame.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAdCallback;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.test.mygame.MainActivity;
import com.test.mygame.R;
import com.test.mygame.util.AdmobManager;
import com.test.mygame.util.Factory;
import com.test.mygame.util.MyFragmentManager;

public class GameOverScreen extends Fragment {

    public static String TAG = "game_over_screen_tag";
    private TextView currentScoreView, bestScoreView;
    private ProgressBar progressBar;
    public int currentScore, bestScore;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.gameover_fragment_layout, container, false);
        initViews(view);
        return view;
    }

    private void initViews(View view) {
        currentScoreView = view.findViewById(R.id.currentScoreView);
        bestScoreView = view.findViewById(R.id.bestScoreView);

        Button homeButton = view.findViewById(R.id.homeButton);
        Button replayButton = view.findViewById(R.id.replayButton);
        Button continueButton = view.findViewById(R.id.continueButton);
        progressBar = view.findViewById(R.id.progressBar);

        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getContext() != null) {
                    ((MainActivity) getContext()).playTapSound();
                    MainActivity context = (MainActivity) getContext();
                    context.getSupportFragmentManager().popBackStack();
                }
            }
        });

        replayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideProgressBar();
                if (getContext() != null) {
                    MainActivity context = (MainActivity) getContext();
                    context.playTapSound();
                    context.getSupportFragmentManager().popBackStack();
                    if (context.gameScreen == null)
                        context.gameScreen = new GameScreen();
                    MyFragmentManager.getInstance().addFragment(context, context.fragmentContainer, context.gameScreen, GameScreen.TAG);
                }
            }
        });

        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideProgressBar();
                if (getActivity() != null && progressBar.getVisibility() == View.GONE) {
                    MainActivity activity = (MainActivity) getActivity();
                    activity.playTapSound();
                    handleClickOnContinueButton(activity);
                }
            }
        });
    }

    private void handleClickOnContinueButton(final MainActivity activity) {
        if (AdmobManager.getInstance().isRewardedVideoAdLoaded()) {
            showRewardedAdAndHandle(activity);
        } else {
            showProgressBar();
            AdmobManager.getInstance().loadRewardedVideoAd(new RewardedAdLoadCallback() {
                @Override
                public void onRewardedAdLoaded() {
                    super.onRewardedAdLoaded();
                    Log.d(AdmobManager.getInstance().TAG_REWARDED_VIDEO_AD, "onRewardedAdLoaded()");
                    Factory.getInstance().showToast(activity, AdmobManager.getInstance().TAG_REWARDED_VIDEO_AD + "onRewardedAdLoaded()");

                    if (progressBar.getVisibility() == View.VISIBLE) {
                        Factory.getInstance().showToast(getContext(), getString(R.string.ad_loaded));
                        showRewardedAdAndHandle(activity);
                        hideProgressBar();
                    }
                }

                @Override
                public void onRewardedAdFailedToLoad(LoadAdError loadAdError) {
                    super.onRewardedAdFailedToLoad(loadAdError);
                    Log.d(AdmobManager.getInstance().TAG_REWARDED_VIDEO_AD, "onRewardedAdFailedToLoad()");
                    Factory.getInstance().showToast(activity, AdmobManager.getInstance().TAG_REWARDED_VIDEO_AD + "onRewardedAdFailedToLoad()");

                    if (progressBar.getVisibility() == View.VISIBLE) {
                        Factory.getInstance().showToast(getContext(), getString(R.string.ad_not_available));
                        hideProgressBar();
                    }
                }
            });
        }
    }

    private void showRewardedAdAndHandle(final MainActivity activity) {
        AdmobManager.getInstance().showRewardedVideoAd(new RewardedAdCallback() {
            @Override
            public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                if (activity.gameScreen != null)
                    activity.gameScreen.haveToResumeLastGame = true;
            }

            @Override
            public void onRewardedAdClosed() {
                super.onRewardedAdClosed();
                if (activity.gameScreen != null && activity.gameScreen.haveToResumeLastGame) {
                    activity.getSupportFragmentManager().popBackStack();
                    MyFragmentManager.getInstance().addFragment(activity, activity.fragmentContainer, activity.gameScreen, GameScreen.TAG);
                }
            }

            @Override
            public void onRewardedAdFailedToShow(AdError adError) {
                super.onRewardedAdFailedToShow(adError);
            }

            @Override
            public void onRewardedAdOpened() {
                super.onRewardedAdOpened();
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState == null || !savedInstanceState.containsKey("onScreen")) {
            updateScoresFromReceivedBundle();
        } else {
            currentScore = savedInstanceState.getInt("currentScore");
            bestScore = savedInstanceState.getInt("bestScore");
            setScoreToRespectiveViews();
        }

        showInterstitialAd();
    }

    private void showInterstitialAd() {
        AdmobManager.getInstance().showInterstitialAd(new AdListener() {
            @Override
            public void onAdClicked() {
                super.onAdClicked();
                Log.d(AdmobManager.getInstance().TAG_INTERSTITIAL, "onAdClicked()");
                Factory.getInstance().showToast(getContext(), AdmobManager.getInstance().TAG_INTERSTITIAL + "onAdClicked()");
            }

            @Override
            public void onAdClosed() {
                super.onAdClosed();
                Log.d(AdmobManager.getInstance().TAG_INTERSTITIAL, "onAdClosed()");
                Factory.getInstance().showToast(getContext(), AdmobManager.getInstance().TAG_INTERSTITIAL + "onAdClosed()");
                AdmobManager.getInstance().loadInterstitialAd();
            }

            @Override
            public void onAdLeftApplication() {
                super.onAdLeftApplication();
                Log.d(AdmobManager.getInstance().TAG_INTERSTITIAL, "onAdLeftApplication()");
                Factory.getInstance().showToast(getContext(), AdmobManager.getInstance().TAG_INTERSTITIAL + "onAdLeftApplication()");
            }

            @Override
            public void onAdOpened() {
                super.onAdOpened();
                Log.d(AdmobManager.getInstance().TAG_INTERSTITIAL, "onAdOpened()");
                Factory.getInstance().showToast(getContext(), AdmobManager.getInstance().TAG_INTERSTITIAL + "onAdOpened()");
            }

            @Override
            public void onAdFailedToLoad(LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                Log.d(AdmobManager.getInstance().TAG_INTERSTITIAL, "onAdFailedToLoad()");
                Factory.getInstance().showToast(getContext(), AdmobManager.getInstance().TAG_INTERSTITIAL + "onAdFailedToLoad()");
            }
        });
    }

    private void setScoreToRespectiveViews() {
        currentScoreView.setText(getString(R.string.current_score) + " : " + currentScore);
        bestScoreView.setText(getString(R.string.best_score) + " : " + bestScore);
    }

    /**
     * receive bundle and sets current and best score on respective textviews
     */
    private void updateScoresFromReceivedBundle() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            currentScore = bundle.getInt("currentScore", 0);
            bestScore = bundle.getInt("bestScore", 0);

            setScoreToRespectiveViews();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("onScreen", "gameOverScreen");
        outState.putInt("currentScore", currentScore);
        outState.putInt("bestScore", bestScore);
    }

    private void showProgressBar() {
        Factory.getInstance().showToast(getContext(), getString(R.string.loading_ad));
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        if (progressBar != null && progressBar.getVisibility() == View.VISIBLE)
            progressBar.setVisibility(View.GONE);
    }

    public void onBackPressed() {
        if (getContext() == null)
            return;
        final MainActivity context = (MainActivity) getContext();
        context.getSupportFragmentManager().popBackStack();
        hideProgressBar();
    }
}
