package com.test.mygame;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.installations.FirebaseInstallations;
import com.google.firebase.installations.InstallationTokenResult;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.test.mygame.dialog.MyResponseDialog;
import com.test.mygame.fragment.GameOverScreen;
import com.test.mygame.fragment.GameScreen;
import com.test.mygame.fragment.HomeScreen;
import com.test.mygame.fragment.SplashScreen;
import com.test.mygame.model.SavedGame;
import com.test.mygame.util.Factory;
import com.test.mygame.util.MyFragmentManager;
import com.test.mygame.util.MySoundManager;
import com.test.mygame.util.SharedPrefsManager;

import java.util.Random;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

public class MainActivity extends AppCompatActivity {

    public FrameLayout fragmentContainer; // container used for all fragments
    public GameScreen gameScreen = null;
    private boolean haveRestoredInstanceState;

    public FirebaseRemoteConfig mFirebaseRemoteConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getSupportActionBar() != null)
            getSupportActionBar().hide();

        fragmentContainer = findViewById(R.id.fragment_container);

        if (savedInstanceState == null)
            MyFragmentManager.getInstance().addFragment(MainActivity.this, fragmentContainer, new SplashScreen(), null);
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

        // initializing sound manager and loading required sounds
        MySoundManager.getInstance().loadSound(this);

        // initiating remote config
        initFirebaseRemoteConfig();
    }

    private void initFirebaseRemoteConfig() {
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(3600 * 2)
                .build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);

        mFirebaseRemoteConfig.fetchAndActivate()
                .addOnCompleteListener(this, new OnCompleteListener<Boolean>() {
                    @Override
                    public void onComplete(@NonNull Task<Boolean> task) {
                        if (task.isSuccessful()) {
                            Log.d("TAG", "Config params updated");

                        } else {
                            Log.d("TAG", "Config params updation failed");
                        }
                    }
                });
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

        String message;
        if (BuildConfig.IS_FULL_VERSION)
            message = getString(R.string.do_you_want_to_save_this_game);
        else
            message = getString(R.string.do_you_want_to_exit);

        new MyResponseDialog(MainActivity.this, message,
                getString(R.string.yes), getString(R.string.no), new ResponseListener() {
            @Override
            public void onPositiveResponse() {
                playTapSound();
                if (BuildConfig.IS_FULL_VERSION) {
                    SharedPrefsManager.getInstance().saveGame(MainActivity.this, new SavedGame(gameScreen.grayBox,
                            gameScreen.score));
                }
                getSupportFragmentManager().popBackStack();
            }

            @Override
            public void onNeutralResponse() {
            }

            @Override
            public void onNegativeResponse() {
                playTapSound();
                if (BuildConfig.IS_FULL_VERSION) {
                    SharedPrefsManager.getInstance().deleteSavedGame(MainActivity.this);
                    getSupportFragmentManager().popBackStack();
                }
            }
        }).show();
    }

    private void handleBackPressedOnHomeScreen() {
        new MyResponseDialog(MainActivity.this, getString(R.string.do_you_want_to_exit),
                getString(R.string.yes), getString(R.string.no), new ResponseListener() {
            @Override
            public void onPositiveResponse() {
                playTapSound();
                exitFromGame();
            }

            @Override
            public void onNeutralResponse() {

            }

            @Override
            public void onNegativeResponse() {
                playTapSound();
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

        MySoundManager.getInstance().pause();
    }

    @Override
    protected void onStop() {
        if (BuildConfig.IS_FULL_VERSION && gameScreen != null) {
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (fragment instanceof GameScreen && gameScreen.isGameStarted && !gameScreen.isGameOver) {
                SharedPrefsManager.getInstance().saveGame(MainActivity.this, new SavedGame(gameScreen.grayBox, gameScreen.score));
            }
        }

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        MySoundManager.getInstance().releaseSoundPool();
        super.onDestroy();
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

        MySoundManager.getInstance().resume();

        ///// handle show/hide status and navigation bar //////
        Factory.getInstance().hideSystemUI(MainActivity.this);
        Factory.getInstance().setListenerToHideStatusAndNavigationBar(MainActivity.this);
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

    public void playTapSound() {
        MySoundManager.getInstance().playTapSound();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Factory.getInstance().WRITE_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //
            }
        }
    }
}
