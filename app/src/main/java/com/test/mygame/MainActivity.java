package com.test.mygame;

import android.os.Bundle;
import android.widget.FrameLayout;

import com.test.mygame.fragment.AskForSaveGameScreen;
import com.test.mygame.fragment.ExitScreen;
import com.test.mygame.fragment.GameOverScreen;
import com.test.mygame.fragment.GameScreen;
import com.test.mygame.fragment.HomeScreen;
import com.test.mygame.fragment.ResumeSaveGameScreen;
import com.test.mygame.fragment.SplashScreen;
import com.test.mygame.model.SavedGame;
import com.test.mygame.util.SharedPrefsManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity {

    public FrameLayout fragmentContainer; // container used for all fragments
    public GameScreen gameScreen = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fragmentContainer = findViewById(R.id.fragment_container);

        addFragment(new SplashScreen(), null);

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
     * adds given fragment to fragment back stack with given tag
     *
     * @param fragment represents a screen
     * @param tag
     */
    public void addFragment(Fragment fragment, String tag) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(fragmentContainer.getId(), fragment, tag);
        transaction.addToBackStack(tag);
        transaction.commit();
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
                if (!gameScreen.isGameStarted)
                    return;
                gameScreen.isGamePaused = true;
                AskForSaveGameScreen screen = new AskForSaveGameScreen();
                addFragment(screen, screen.TAG);
            } else if (fragment instanceof AskForSaveGameScreen) {
                getSupportFragmentManager().popBackStack();
                gameScreen.resumeGame();
            } else if (fragment instanceof GameOverScreen) {
                getSupportFragmentManager().popBackStack();
            } else if (fragment instanceof HomeScreen) {
                ExitScreen exitScreen = new ExitScreen();
                addFragment(exitScreen, exitScreen.TAG);
            } else if (fragment instanceof ExitScreen) {
                getSupportFragmentManager().popBackStack();
            } else if (fragment instanceof ResumeSaveGameScreen) {
                getSupportFragmentManager().popBackStack();
            }

        }
    }

    /**
     * exits from game
     */
    public void exitFromGame() {
        finish();
    }

    @Override
    protected void onStop() {
        if (gameScreen != null) {
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (fragment instanceof GameScreen && gameScreen.isGameStarted && !gameScreen.isGameOver) {
                SharedPrefsManager.getInstance().saveGame(this, new SavedGame(gameScreen.grayBox, gameScreen.score));
            }
        }
        super.onStop();
    }
}
