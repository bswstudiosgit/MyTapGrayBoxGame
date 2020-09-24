package com.test.mygame.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAdCallback;
import com.test.mygame.MainActivity;
import com.test.mygame.R;
import com.test.mygame.util.AdmobManager;
import com.test.mygame.util.MyFragmentManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class GameOverScreen extends Fragment {

    public static String TAG = "game_over_screen_tag";
    private TextView currentScoreView, bestScoreView;
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
                if (getActivity() != null) {
                    MainActivity activity = (MainActivity) getActivity();
                    activity.playTapSound();
                    handleClickOnContinueButton(activity);
                }
            }
        });
    }

    private void handleClickOnContinueButton(final MainActivity activity) {
        if (AdmobManager.getInstance().isRewardedVideoAdLoaded()) {
            AdmobManager.getInstance().showRewardedVideoAd(getActivity(), new RewardedAdCallback() {
                @Override
                public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                    if (activity.gameScreen != null)
                        activity.gameScreen.haveToResumeLastGame = true;
                }

                @Override
                public void onRewardedAdClosed() {
                    super.onRewardedAdClosed();
                    if (activity.gameScreen != null && activity.gameScreen.haveToResumeLastGame) {
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
        } else {

        }
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
}
