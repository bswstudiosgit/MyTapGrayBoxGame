package com.test.mygame;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.test.mygame.alarm.NotificationReceiver;
import com.test.mygame.fragment.GameOverScreen;
import com.test.mygame.fragment.GameScreen;
import com.test.mygame.fragment.HomeScreen;
import com.test.mygame.fragment.SplashScreen;
import com.test.mygame.model.SavedGame;
import com.test.mygame.util.AdmobManager;
import com.test.mygame.util.Factory;
import com.test.mygame.util.MyFragmentManager;
import com.test.mygame.util.MySoundManager;
import com.test.mygame.util.SharedPrefsManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    public FrameLayout fragmentContainer; // container used for all fragments (screens)
    public GameScreen gameScreen = null; // used for using single instance for gameScreen fragment
    private boolean haveRestoredInstanceState; // will true if app launched and received savedInstanceState found not null
    public boolean haveToGoDirectToGameScreen = false; // if true then we directly go to gameScreen immediately from homeScreen
    public FirebaseRemoteConfig mFirebaseRemoteConfig; // instance holder for firebase remote config

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Factory.getInstance().loadLocale(MainActivity.this);
        setContentView(R.layout.activity_main);

        //////////////////////////////////////////////////

        initializeAds(savedInstanceState);

        //////////////////////////////////////////////////

        if (getSupportActionBar() != null)
            getSupportActionBar().hide();

        // check for, is app launched by fcm notification
        if (getIntent().getExtras() != null && getIntent().getExtras().containsKey("message"))
            handleForDataPayloadByFCM(getIntent().getExtras().getString("message"));

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

        //subscribe to topic
        subscribeToTopic();

        myAlarm();
    }

    private void initializeAds(Bundle savedInstanceState) {
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {

            }
        });

        FrameLayout adContainerView = (FrameLayout) findViewById(R.id.ad_view_container);

        AdmobManager.getInstance().initializeAllAds(MainActivity.this, adContainerView);

        if (savedInstanceState != null)
            AdmobManager.getInstance().showBannerAds();
    }

    public void myAlarm() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 16);

        if (calendar.getTime().compareTo(new Date()) < 0)
            calendar.add(Calendar.DAY_OF_MONTH, 1);

        Intent intent = new Intent(getApplicationContext(), NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent,
                0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        if (alarmManager != null) {
            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
        }
    }

    private void subscribeToTopic() {
        FirebaseMessaging.getInstance().subscribeToTopic(Factory.getInstance().getCountryCode(this))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (!task.isSuccessful()) {
                            //
                        }
                    }
                });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent != null && intent.getExtras() != null) {
            Bundle extras = intent.getExtras();
            String data = extras.getString("data");
            handleForDataPayloadByFCM(data);
            handleAfterReceiveFCMNotification();
        }
    }

    private void handleForDataPayloadByFCM(String message) {
        try {
            JSONObject jsonObject = new JSONObject(message);
            JSONObject data = jsonObject.getJSONObject("data");
            if (data.getInt("msgType") == 1) {
                haveToGoDirectToGameScreen = true;
            } else if (data.getInt("msgType") == 2) {
                String s = data.toString();
                SharedPrefsManager.getInstance().write_string_prefs(this, SharedPrefsManager.getInstance().FCM_DATA_PAYLOAD_KEY,
                        s);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }

    private void handleAfterReceiveFCMNotification() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (fragment != null) {
            if (fragment instanceof HomeScreen) {
                ((HomeScreen) fragment).handleAfterReceivingPayload();
            } else if (fragment instanceof GameOverScreen) {
                getSupportFragmentManager().popBackStack();
                Fragment fragmentByTag = getSupportFragmentManager().findFragmentByTag(HomeScreen.TAG);
                HomeScreen homeScreen = null;
                if (fragmentByTag != null)
                    homeScreen = (HomeScreen) fragmentByTag;
                if (homeScreen != null)
                    homeScreen.handleAfterReceivingPayload();
            }
        }
    }

    /**
     * initializes firebase remote config
     */
    private void initFirebaseRemoteConfig() {
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(3600 * 2)
                .build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);
        mFirebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_defaults);

        mFirebaseRemoteConfig.fetchAndActivate()
                .addOnCompleteListener(this, new OnCompleteListener<Boolean>() {
                    @Override
                    public void onComplete(@NonNull Task<Boolean> task) {
                        if (task.isSuccessful()) {
                            Log.d("TAG", "Config params updated");

                            SharedPrefsManager prefsManager = SharedPrefsManager.getInstance();
                            // checking values in remote config
                            if (!TextUtils.isEmpty(mFirebaseRemoteConfig.getString("time_gap")))
                                prefsManager.write_integer_prefs(MainActivity.this, prefsManager.TIME_GAP_KEY,
                                        Integer.parseInt(mFirebaseRemoteConfig.getString("time_gap")));

                            if (!TextUtils.isEmpty(mFirebaseRemoteConfig.getString("colours"))) {
                                try {
                                    JSONObject object = new JSONObject(mFirebaseRemoteConfig.getString("colours"));
                                    prefsManager.write_string_prefs(MainActivity.this, prefsManager.COLOUR_CODE1_KEY,
                                            object.getString("colour1"));
                                    prefsManager.write_string_prefs(MainActivity.this, prefsManager.COLOUR_CODE2_KEY,
                                            object.getString("colour2"));
                                    prefsManager.write_string_prefs(MainActivity.this, prefsManager.COLOUR_CODE3_KEY,
                                            object.getString("colour3"));
                                    prefsManager.write_string_prefs(MainActivity.this, prefsManager.COLOUR_CODE4_KEY,
                                            object.getString("colour4"));
                                    prefsManager.write_string_prefs(MainActivity.this, prefsManager.COLOUR_CODE5_KEY,
                                            object.getString("colour5"));

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    FirebaseCrashlytics.getInstance().recordException(e);
                                }
                            }

                            if (!TextUtils.isEmpty(mFirebaseRemoteConfig.getString("interstitial_ad_show_gap")))
                                prefsManager.write_integer_prefs(MainActivity.this, prefsManager.INTERSTITIAL_AD_SHOW_GAP_KEY,
                                        Integer.parseInt(mFirebaseRemoteConfig.getString("interstitial_ad_show_gap")));
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
                ((GameScreen) fragment).onBackPressed();
            } else if (fragment instanceof GameOverScreen) {
                ((GameOverScreen) fragment).onBackPressed();
            } else if (fragment instanceof HomeScreen) {
                ((HomeScreen) fragment).onBackPressed();
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
    protected void onPause() {
        AdmobManager.getInstance().onPause();
        super.onPause();
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
        AdmobManager.getInstance().onDestroy();
        MySoundManager.getInstance().releaseSoundPool();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();

        AdmobManager.getInstance().onResume();

        if (haveRestoredInstanceState) {
            haveRestoredInstanceState = false;
        } else {
            if (gameScreen != null) {
                if (gameScreen.haveShowedInterstitialAdFromGameStart) {
                    //
                } else {
                    gameScreen.isGamePaused = true;
                    Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                    if (fragment instanceof GameScreen && !gameScreen.isGameStarted && !gameScreen.isGameOver) {
                        int random = new Random().nextInt(4);
                        gameScreen.resumeLastSavedGame(new SavedGame(random + 1, 0));
                    }
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
            if (grantResults != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //
            }
        }
    }
}
