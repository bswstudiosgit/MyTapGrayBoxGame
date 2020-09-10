package com.test.mygame;

import android.os.Bundle;
import android.widget.FrameLayout;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.test.mygame.dialog.MyResponseDialog;
import com.test.mygame.fragment.GameOverScreen;
import com.test.mygame.fragment.GameScreen;
import com.test.mygame.fragment.HomeScreen;
import com.test.mygame.fragment.SplashScreen;
import com.test.mygame.model.SavedGame;
import com.test.mygame.util.SharedPrefsManager;

import java.util.Random;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity {

    public FrameLayout fragmentContainer; // container used for all fragments
    public GameScreen gameScreen = null;
    private boolean haveRestoredInstanceState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fragmentContainer = findViewById(R.id.fragment_container);

        if (savedInstanceState == null)
            addFragment(new SplashScreen(), null);
        else
            haveRestoredInstanceState = true;

        final FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                Fragment fragment = fragmentManager.findFragmentById(R.id.fragment_container);
                if (!(fragment instanceof GameScreen)) {
                    setTitle(getString(R.string.app_name));
                }
            }
        });
    }

    /**
     * add given fragment to fragment back stack with given tag
     *
     * @param fragment represents a screen
     * @param tag
     */
    public void addFragment(Fragment fragment, String tag) {
        try {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(fragmentContainer.getId(), fragment, tag);
            transaction.addToBackStack(tag);
            transaction.commit();
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }

    public void replaceFragment(Fragment fragment, String tag) {
        try {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(fragmentContainer.getId(), fragment, tag);
            transaction.addToBackStack(tag);
            transaction.commit();
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }

    /**
     * set title for showing given score on title bar
     *
     * @param score
     */
    public void setScore(int score) {
        setTitle(getString(R.string.gameplay_title_score) + " " + score);
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (fragment != null) {
            if (fragment instanceof GameScreen) {
                handleBackPressedOnGameScreen();
            } else if (fragment instanceof GameOverScreen) {
                getSupportFragmentManager().popBackStack();
            } else if (fragment instanceof HomeScreen) {
                handleBackPressedOnHomeScreen();
            }
        }
    }

    private void handleBackPressedOnGameScreen() {
        if (!gameScreen.isGameStarted)
            return;
        gameScreen.isGamePaused = true;
        new MyResponseDialog(MainActivity.this, getString(R.string.do_you_want_to_save_this_game),
                getString(R.string.yes), getString(R.string.no), new ResponseListener() {
            @Override
            public void onPositiveResponse() {
                FirebaseCrashlytics.getInstance().setCustomKey("SAVED_CURRENT_GAME_FROM_SAVED_GAME_DIALOG", true);
                SharedPrefsManager.getInstance().saveGame(MainActivity.this, new SavedGame(gameScreen.grayBox,
                        gameScreen.score));
                getSupportFragmentManager().popBackStack();
            }

            @Override
            public void onNeutralResponse() {
            }

            @Override
            public void onNegativeResponse() {
                FirebaseCrashlytics.getInstance().setCustomKey("SAVED_CURRENT_GAME_FROM_SAVED_GAME_DIALOG", false);
                SharedPrefsManager.getInstance().deleteSavedGame(MainActivity.this);
                getSupportFragmentManager().popBackStack();
            }
        }).show();
    }

    private void handleBackPressedOnHomeScreen() {
        new MyResponseDialog(MainActivity.this, getString(R.string.do_you_want_to_exit),
                getString(R.string.yes), getString(R.string.no), new ResponseListener() {
            @Override
            public void onPositiveResponse() {
                FirebaseCrashlytics.getInstance().setCustomKey("CLICKED_YES_BUTTON_FROM_EXIT_DIALOG", true);
                exitFromGame();
            }

            @Override
            public void onNeutralResponse() {

            }

            @Override
            public void onNegativeResponse() {
                FirebaseCrashlytics.getInstance().setCustomKey("CLICKED_NO_BUTTON_FROM_EXIT_DIALOG", true);
            }
        }).show();
    }

    /**
     * exits from game
     */
    public void exitFromGame() {
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (gameScreen != null)
            gameScreen.isGamePaused = true;
    }

    @Override
    protected void onStop() {
        if (gameScreen != null) {
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (fragment instanceof GameScreen && gameScreen.isGameStarted && !gameScreen.isGameOver) {
                SharedPrefsManager.getInstance().saveGame(MainActivity.this, new SavedGame(gameScreen.grayBox, gameScreen.score));
            }
        }
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (haveRestoredInstanceState) {
            haveRestoredInstanceState = false;
        } else {
            if (gameScreen != null) {
                gameScreen.isGamePaused = true;
                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                if (fragment instanceof GameScreen && !gameScreen.isGameStarted && !gameScreen.isGameOver) {
                    int random = new Random().nextInt(4);
                    gameScreen.resumeLastSavedGame(new SavedGame(random + 1, 0));
                }
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("title", "title");
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }
}
