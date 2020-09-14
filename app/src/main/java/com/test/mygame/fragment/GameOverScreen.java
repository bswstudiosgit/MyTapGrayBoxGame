package com.test.mygame.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.test.mygame.MainActivity;
import com.test.mygame.util.MyFragmentManager;
import com.test.mygame.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class GameOverScreen extends Fragment {

    public static String TAG = "game_over_screen_tag";
    private TextView currentScoreView, bestScoreView;
    private Button homeButton, replayButton;
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

        homeButton = view.findViewById(R.id.homeButton);
        replayButton = view.findViewById(R.id.replayButton);

        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getContext() != null) {
                    ((MainActivity) getContext()).playTapSound();
                    FirebaseCrashlytics.getInstance().setCustomKey("CLICKED_HOME_BUTTON_FROM_GAMEOVER_SCREEN", true);
                    MainActivity context = (MainActivity) getContext();
                    context.getSupportFragmentManager().popBackStack();
                }
            }
        });

        replayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getContext() != null) {
                    FirebaseCrashlytics.getInstance().setCustomKey("CLICKED_REPLAY_BUTTON_FROM_GAMEOVER_SCREEN", true);
                    MainActivity context = (MainActivity) getContext();
                    context.playTapSound();
                    context.getSupportFragmentManager().popBackStack();
                    if (context.gameScreen == null)
                        context.gameScreen = new GameScreen();
                    MyFragmentManager.getInstance().addFragment(context, context.fragmentContainer, context.gameScreen,
                            context.gameScreen.TAG);
                }
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
