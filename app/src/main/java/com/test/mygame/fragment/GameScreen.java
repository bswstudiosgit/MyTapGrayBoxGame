package com.test.mygame.fragment;

import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.test.mygame.BuildConfig;
import com.test.mygame.MainActivity;
import com.test.mygame.R;
import com.test.mygame.model.SavedGame;
import com.test.mygame.util.Factory;
import com.test.mygame.util.MyFragmentManager;
import com.test.mygame.util.SharedPrefsManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Random;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

public class GameScreen extends Fragment {

    // remote config keys
    private static final String CONFIG_KEY_TIME_GAP = "time_gap";
    private static final String CONFIG_KEY_COLOURS = "colours";

    public static String TAG = "game_screen_tag";
    private LinearLayout box1, box2, box3, box4;
    private TextView textView, scoreView;
    private Button mailUsButtonView;
    public int grayBox = 0, score;
    private boolean tapped, canTouch;
    public boolean isGamePaused, isGameStarted, isGameOver;
    private int sec, gameTimeInSec;
    private String box1Color, box2Color, box3Color, box4Color, grayBoxColor;
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
        initializeValuesFromRemoteConfig();
        setDefaultBoxesColor();

        box1 = view.findViewById(R.id.box1);
        box2 = view.findViewById(R.id.box2);
        box3 = view.findViewById(R.id.box3);
        box4 = view.findViewById(R.id.box4);

        box1.setBackgroundColor(Color.parseColor(box1Color));
        box2.setBackgroundColor(Color.parseColor(box2Color));
        box3.setBackgroundColor(Color.parseColor(box3Color));
        box4.setBackgroundColor(Color.parseColor(box4Color));

        textView = view.findViewById(R.id.textView);
        scoreView = view.findViewById(R.id.scoreView);
        mailUsButtonView = view.findViewById(R.id.mailUs);
    }

    private void initializeValuesFromRemoteConfig() {
        // default values
        gameTimeInSec = 1;
        box1Color = getString(R.string.color_code1);
        box2Color = getString(R.string.color_code2);
        box3Color = getString(R.string.color_code3);
        box4Color = getString(R.string.color_code4);
        grayBoxColor = getString(R.string.color_code5);

        // from remote config
        if (getContext() != null) {
            FirebaseRemoteConfig remoteConfig = ((MainActivity) getContext()).mFirebaseRemoteConfig;
            if (remoteConfig != null) {
                if (!TextUtils.isEmpty(remoteConfig.getString(CONFIG_KEY_TIME_GAP)))
                    gameTimeInSec = Integer.parseInt(remoteConfig.getString(CONFIG_KEY_TIME_GAP));

                if (!TextUtils.isEmpty(remoteConfig.getString(CONFIG_KEY_COLOURS))) {
                    try {
                        JSONObject object = new JSONObject(remoteConfig.getString(CONFIG_KEY_COLOURS));
                        box1Color = object.getString("colour1");
                        box2Color = object.getString("colour2");
                        box3Color = object.getString("colour3");
                        box4Color = object.getString("colour4");
                        grayBoxColor = object.getString("colour5");

                    } catch (JSONException e) {
                        e.printStackTrace();
                        FirebaseCrashlytics.getInstance().recordException(e);
                    }
                }
            }
        }
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
        if (getContext() != null) {
            prefsManager.deleteSavedGame(getContext());

            // incrementing total game played
            prefsManager.write_integer_prefs(getContext(), prefsManager.TOTAL_GAMES_PLAYED_KEY,
                    prefsManager.read_integer_prefs(getContext(), prefsManager.TOTAL_GAMES_PLAYED_KEY) + 1);

            // logging event
            if (getContext() != null) {
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.SCORE, "" + score);
                ((MainActivity) getContext()).mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.POST_SCORE, bundle);
                Factory.getInstance().showToast(getContext(), FirebaseAnalytics.Event.POST_SCORE + " : " +
                        FirebaseAnalytics.Param.SCORE + " = " + score);
            }

            int gamesPlayed = prefsManager.read_integer_prefs(getContext(), prefsManager.TOTAL_GAMES_PLAYED_KEY);
            String userExp;
            if (gamesPlayed < 4)
                userExp = "Beginner";
            else if (gamesPlayed < 15)
                userExp = "Intermediate";
            else
                userExp = "Expert";
            ((MainActivity) getContext()).mFirebaseAnalytics.setUserProperty("experience", userExp);
            Factory.getInstance().showToast(getContext(), "experience : " + userExp);
        }

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
            timer = new CountDownTimer(gameTimeInSec * 1000, gameTimeInSec * 1000) {

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
        timer = new CountDownTimer(gameTimeInSec * 1000, gameTimeInSec * 1000) {

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
                box1.setBackgroundColor(Color.parseColor(box1Color));
            else if (grayBox == 2)
                box2.setBackgroundColor(Color.parseColor(box2Color));
            else if (grayBox == 3)
                box3.setBackgroundColor(Color.parseColor(box3Color));
            else
                box4.setBackgroundColor(Color.parseColor(box4Color));
        }
    }

    private void setGrayColorForGivenBox(int grayBox) {
        if (grayBox == 1)
            box1.setBackgroundColor(Color.parseColor(grayBoxColor));
        else if (grayBox == 2)
            box2.setBackgroundColor(Color.parseColor(grayBoxColor));
        else if (grayBox == 3)
            box3.setBackgroundColor(Color.parseColor(grayBoxColor));
        else
            box4.setBackgroundColor(Color.parseColor(grayBoxColor));
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

                    if (getContext() != null) {
                        Bundle bundle = new Bundle();
                        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "game_start");
                        ((MainActivity) getContext()).mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                        Factory.getInstance().showToast(getContext(), FirebaseAnalytics.Param.ITEM_NAME + " : game_start");
                    }
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
