package com.test.mygame.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.test.mygame.BuildConfig;
import com.test.mygame.MainActivity;
import com.test.mygame.R;
import com.test.mygame.ResponseListener;
import com.test.mygame.dialog.MyResponseDialog;
import com.test.mygame.dialog.ShowDataDialog;
import com.test.mygame.model.SavedGame;
import com.test.mygame.util.MyFragmentManager;
import com.test.mygame.util.SharedPrefsManager;

import org.json.JSONException;
import org.json.JSONObject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class HomeScreen extends Fragment {

    public static String TAG = "home_screen_tag";
    private Button playButton, showRCValuesButton;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.homescreen_fragment_layout, container, false);

        playButton = view.findViewById(R.id.play);
        showRCValuesButton = view.findViewById(R.id.button);
        if (getContext() != null) {
            Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.button_scale_animation);
            playButton.startAnimation(animation);

            ((MainActivity) getContext()).setTitle(getString(R.string.app_name));
        }

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getContext() != null) {
                    SavedGame lastSavedGame = SharedPrefsManager.getInstance().getLastSavedGame(getContext());
                    final MainActivity context = (MainActivity) getContext();
                    context.playTapSound();
                    if (lastSavedGame == null || !BuildConfig.IS_FULL_VERSION) {
                        goToGameScreen(context);
                    } else {
                        new MyResponseDialog(getContext(), getString(R.string.do_you_want_to_resume_saved_game),
                                getString(R.string.continue_text), getString(R.string.new_game), new ResponseListener() {
                            @Override
                            public void onPositiveResponse() {
                                context.playTapSound();
                                goToGameScreen(context);
                            }

                            @Override
                            public void onNeutralResponse() {

                            }

                            @Override
                            public void onNegativeResponse() {
                                context.playTapSound();
                                SharedPrefsManager.getInstance().deleteSavedGame(getContext());
                                goToGameScreen(context);
                            }
                        }).show();
                    }
                }
            }
        });

        ////////////////

        showRCValuesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRemoteConfigData();
            }
        });

        return view;
    }

    private void goToGameScreen(MainActivity context) {
        if (context.gameScreen == null)
            context.gameScreen = new GameScreen();
        MyFragmentManager.getInstance().addFragment(context, context.fragmentContainer, context.gameScreen, context.gameScreen.TAG);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // directly navigates to game screen if there is a saved game found (for full version)
        if (BuildConfig.IS_FULL_VERSION && (savedInstanceState == null || !savedInstanceState.containsKey("onScreen"))) {
            SavedGame lastSavedGame = SharedPrefsManager.getInstance().getLastSavedGame(getContext());
            if (lastSavedGame != null && getContext() != null) {
                MainActivity context = (MainActivity) getContext();
                goToGameScreen(context);
            }
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("onScreen", "homeScreen");
    }

    private void showRemoteConfigData() {
        String data = "";
        if (getContext() != null) {
            FirebaseRemoteConfig remoteConfig = ((MainActivity) getContext()).mFirebaseRemoteConfig;
            if (remoteConfig != null) {
                if (!TextUtils.isEmpty(remoteConfig.getString("time_gap")))
                    data += "time_gap " + remoteConfig.getString("time_gap") + "\n";

                if (!TextUtils.isEmpty(remoteConfig.getString("colours"))) {
                    try {
                        JSONObject object = new JSONObject(remoteConfig.getString("colours"));
                        data += "colour1 " + object.getString("colour1") + "\n";
                        data += "colour2 " + object.getString("colour2") + "\n";
                        data += "colour3 " + object.getString("colour3") + "\n";
                        data += "colour4 " + object.getString("colour4") + "\n";
                        data += "colour5 " + object.getString("colour5");
                    } catch (JSONException e) {
                        e.printStackTrace();
                        FirebaseCrashlytics.getInstance().recordException(e);
                    }
                }
            }
        }

        if (!TextUtils.isEmpty(data))
            new ShowDataDialog(getContext(), data).show();
    }
}
