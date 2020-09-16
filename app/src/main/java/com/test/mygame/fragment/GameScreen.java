package com.test.mygame.fragment;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.test.mygame.BuildConfig;
import com.test.mygame.MainActivity;
import com.test.mygame.R;
import com.test.mygame.model.SavedGame;
import com.test.mygame.util.Factory;
import com.test.mygame.util.MyFragmentManager;
import com.test.mygame.util.SharedPrefsManager;

import java.io.File;
import java.util.Random;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

public class GameScreen extends Fragment {

    public static String TAG = "game_screen_tag";
    private LinearLayout box1, box2, box3, box4;
    private TextView textView, scoreView;
    private Button mailUsButtonView;
    public int grayBox = 0, score;
    private boolean tapped, canTouch;
    public boolean isGamePaused, isGameStarted, isGameOver;
    private int sec;
    private CountDownTimer timer;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getContext() != null && savedInstanceState != null && savedInstanceState.containsKey("onScreen")
                && ((MainActivity) getContext()).gameScreen == null)
            ((MainActivity) getContext()).gameScreen = this;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.gamescreen_fragment_layout, container, false);
        init(view);
        return view;
    }

    /**
     * initializes all views and data for game play
     *
     * @param view
     */
    private void init(View view) {
        sec = 3;
        score = 0;
        grayBox = 0;
        isGameOver = false;
        isGameStarted = false;
        tapped = true;
        isGamePaused = false;
        canTouch = true;
        setDefaultBoxesColor();

        box1 = view.findViewById(R.id.box1);
        box2 = view.findViewById(R.id.box2);
        box3 = view.findViewById(R.id.box3);
        box4 = view.findViewById(R.id.box4);

        textView = view.findViewById(R.id.textView);
        scoreView = view.findViewById(R.id.scoreView);
        mailUsButtonView = view.findViewById(R.id.mailUs);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        scoreView.setText(getString(R.string.gameplay_title_score) + " " + score);

        addListeners();

        SavedGame lastSavedGame = SharedPrefsManager.getInstance().getLastSavedGame(getContext());
        if (lastSavedGame == null || !BuildConfig.IS_FULL_VERSION) {
            if (savedInstanceState != null && savedInstanceState.containsKey("onScreen") && savedInstanceState.containsKey("score")) {
                resumeLastSavedGame(new SavedGame(savedInstanceState.getInt("grayBox"), savedInstanceState.getInt("score")));
                return;
            }
            startTimer();
        } else {
            resumeLastSavedGame(lastSavedGame);
        }
    }

    /**
     * resumes given game state
     *
     * @param lastSavedGame respresents last saved game state
     */
    public void resumeLastSavedGame(SavedGame lastSavedGame) {
        if (textView.getVisibility() == View.VISIBLE)
            textView.setVisibility(View.GONE);
        setDefaultBoxesColor();
        grayBox = lastSavedGame.grayBox;
        score = lastSavedGame.score;
        isGameOver = false;
        tapped = false;
        isGamePaused = true;
        isGameStarted = true;
        sec = 0;

        setGrayColorForGivenBox(grayBox);

        scoreView.setText(getString(R.string.gameplay_title_score) + " " + score);
    }

    private void setGrayColorForGivenBox(int grayBox) {
        if (grayBox == 1)
            box1.setBackgroundColor(getResources().getColor(R.color.grey));
        else if (grayBox == 2)
            box2.setBackgroundColor(getResources().getColor(R.color.grey));
        else if (grayBox == 3)
            box3.setBackgroundColor(getResources().getColor(R.color.grey));
        else
            box4.setBackgroundColor(getResources().getColor(R.color.grey));
    }

    private void addListeners() {
        box1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleTapOnBox(1);
            }
        });

        box2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleTapOnBox(2);
            }
        });

        box3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleTapOnBox(3);
            }
        });

        box4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleTapOnBox(4);
            }
        });

        mailUsButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (canTouch && isGameStarted) {
                    canTouch = false;
                    onClickMailUs();
                    enableTouch();
                }
            }
        });
    }

    private void enableTouch() {
        timer = new CountDownTimer(2000, 2000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                canTouch = true;
                timer = null;
            }
        }.start();
    }

    private void handleTapOnBox(int boxTouched) {
        if (isGamePaused)
            resumeGame();
        if (isGameStarted && !tapped) {
            if (getContext() != null)
                ((MainActivity) getContext()).playTapSound();
            if ((boxTouched == 1 && grayBox == 1) || (boxTouched == 2 && grayBox == 2) || (boxTouched == 3 && grayBox == 3) ||
                    (boxTouched == 4 && grayBox == 4))
                incrementScore();
            else
                gameOver();
        }
    }

    /**
     * handle after game over
     */
    private void gameOver() {
        isGameOver = true;

        SharedPrefsManager prefsManager = SharedPrefsManager.getInstance();
        prefsManager.deleteSavedGame(getContext());

        int bestScore = prefsManager.read_integer_prefs(getContext(), prefsManager.BEST_SCORE_KEY);
        if (bestScore < score) {
            bestScore = score;
            prefsManager.write_integer_prefs(getContext(), prefsManager.BEST_SCORE_KEY, score);
        }

        GameOverScreen fragment = new GameOverScreen();
        Bundle bundle = new Bundle();
        bundle.putInt("currentScore", score);
        bundle.putInt("bestScore", bestScore);
        fragment.setArguments(bundle);
        if (getContext() != null) {
            MainActivity context = (MainActivity) getContext();
            context.getSupportFragmentManager().popBackStack();
            MyFragmentManager.getInstance().addFragment(context, context.fragmentContainer, fragment, fragment.TAG);
        }
    }

    /**
     * handle changing gray box position after every second,
     * also handles after tap on boxes to check whether game is over or not
     */
    private void setGrayColor() {
        try {
            if (isGameOver || isGamePaused)
                return;

            if (!tapped) {
                gameOver();
                return;
            }

            setDefaultBoxesColor();

            // used to generate a random number between 0 to 3
            int random = new Random().nextInt(4);
            random += 1;

            if (random == grayBox) {
                setGrayColor();
                return;
            }
            grayBox = random;

            setGrayColorForGivenBox(grayBox);

            tapped = false;
            timer = new CountDownTimer(1000, 1000) {

                @Override
                public void onTick(long millisUntilFinished) {

                }

                @Override
                public void onFinish() {
                    timer = null;
                    if (!isGamePaused)
                        setGrayColor();
                }
            }.start();
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }

    /**
     * resume game from last tap after pausing game
     */
    public void resumeGame() {
        isGamePaused = false;
        timer = new CountDownTimer(1000, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                timer = null;
                if (!isGamePaused)
                    setGrayColor();
            }
        }.start();
    }

    /**
     * sets default color for all four boxes
     */
    private void setDefaultBoxesColor() {
        if (grayBox != 0) {
            if (grayBox == 1)
                box1.setBackgroundColor(getResources().getColor(R.color.orange));
            else if (grayBox == 2)
                box2.setBackgroundColor(getResources().getColor(R.color.blue));
            else if (grayBox == 3)
                box3.setBackgroundColor(getResources().getColor(R.color.yellow));
            else
                box4.setBackgroundColor(getResources().getColor(R.color.green));
        }
    }

    /**
     * handle timer for 3 seconds in start of game
     */
    private void startTimer() {
        timer = new CountDownTimer(3000, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                if (!isGamePaused) {
                    textView.setText("" + sec);
                    sec -= 1;
                }
            }

            @Override
            public void onFinish() {
                timer = null;
                if (!isGamePaused) {
                    if (textView.getVisibility() == View.VISIBLE)
                        textView.setVisibility(View.GONE);
                    isGameStarted = true;
                    setGrayColor();
                }
            }
        }.start();
    }

    /**
     * handle increment of score after avery tap
     */
    private void incrementScore() {
        score++;
        tapped = true;
        scoreView.setText(getString(R.string.gameplay_title_score) + " " + score);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("onScreen", "gameScreen");
        if (isGameStarted && !isGameOver) {
            outState.putInt("grayBox", grayBox);
            outState.putInt("score", score);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        canTouch = true;
        if (timer != null)
            timer.cancel();
    }

    public void onClickMailUs() {
        if (getActivity() != null) {
            isGamePaused = true;
            ((MainActivity) getActivity()).playTapSound();
            File file = Factory.getInstance().takeScreenshot(getActivity());
            if (file != null) {
                String subject = getString(R.string.feedback_mail_subject);
                String body = getString(R.string.feedback) + ":\n" +
                        getString(R.string.network_type) + ": " + Factory.getInstance().getNetworkType(getContext()) + "\n" +
                        getString(R.string.device_info) + ": " + Build.BRAND.toUpperCase() + " " + Build.MODEL + "\n" +
                        getString(R.string.score) + ": " + score;
                Uri uri = FileProvider.getUriForFile(getActivity(), BuildConfig.APPLICATION_ID + ".provider", file);
                Factory.getInstance().sendMail(getActivity(), subject, body, uri);
            }
        }
    }
}
