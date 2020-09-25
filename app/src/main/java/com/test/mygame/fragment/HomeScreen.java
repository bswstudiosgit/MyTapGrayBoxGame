package com.test.mygame.fragment;

import android.content.DialogInterface;
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
import com.test.mygame.util.AdmobManager;
import com.test.mygame.util.Factory;
import com.test.mygame.util.MyFragmentManager;
import com.test.mygame.util.SharedPrefsManager;

import org.json.JSONException;
import org.json.JSONObject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

public class HomeScreen extends Fragment {

    public static String TAG = "home_screen_tag";
    private Button playButton, showRCValuesButton, chooseLangButton;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.homescreen_fragment_layout, container, false);

        playButton = view.findViewById(R.id.play);
        showRCValuesButton = view.findViewById(R.id.showRcValue);
        chooseLangButton = view.findViewById(R.id.chooseLanguage);

        if (getContext() != null) {
            Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.button_scale_animation);
            playButton.startAnimation(animation);

            ((MainActivity) getContext()).setTitle(getString(R.string.app_name));
        }

        addListeners();
        loadRequiredAds();

        return view;
    }

    private void loadRequiredAds() {
        if (getActivity() != null)
            AdmobManager.getInstance().loadInterstitialAd();
    }

    private void addListeners() {
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

        /////////////////

        chooseLangButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChooseLanguageDialog();
            }
        });
    }

    /**
     * displays choose language dialog
     */
    private void showChooseLanguageDialog() {
        if (getContext() == null)
            return;

        String[] languages = {getString(R.string.english), getString(R.string.hindi), getString(R.string.urdu)};
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(getResources().getString(R.string.choose_language));
        builder.setSingleChoiceItems(languages, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                String languageCode = "en";
                if (i == 0)
                    languageCode = "en";
                else if (i == 1)
                    languageCode = "hi";
                else if (i == 2)
                    languageCode = "ur";

                if (getActivity() != null) {
                    Factory.getInstance().setLocale(getActivity(), languageCode);
                    getActivity().recreate();
                }

                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void goToGameScreen(MainActivity context) {
        if (context.gameScreen == null)
            context.gameScreen = new GameScreen();
        MyFragmentManager.getInstance().addFragment(context, context.fragmentContainer, context.gameScreen, GameScreen.TAG);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (getContext() != null) {
            MainActivity context = (MainActivity) getContext();
            if (context.haveToGoDirectToGameScreen) {
                context.haveToGoDirectToGameScreen = false;
                goToGameScreen(context);
                return;
            }

            String payloadData = SharedPrefsManager.getInstance().read_string_prefs(getContext(),
                    SharedPrefsManager.getInstance().FCM_DATA_PAYLOAD_KEY);
            if (!TextUtils.isEmpty(payloadData)) {
                showPayloadData(payloadData);
                return;
            }
        }

        // directly navigates to game screen if there is a saved game found (for full version)
        if (BuildConfig.IS_FULL_VERSION && (savedInstanceState == null || !savedInstanceState.containsKey("onScreen"))) {
            SavedGame lastSavedGame = SharedPrefsManager.getInstance().getLastSavedGame(getContext());
            if (lastSavedGame != null && getContext() != null) {
                MainActivity context = (MainActivity) getContext();
                goToGameScreen(context);
            }
        }
    }

    private void showPayloadData(String payloadData) {
        if (getContext() == null)
            return;

        String data = "";
        try {
            JSONObject jsonObject = new JSONObject(payloadData);
            data += "Name : " + jsonObject.getString("Name") + "\n";
            data += "Msg : " + jsonObject.getString("Msg") + "\n";
            data += "msgType : " + jsonObject.getString("msgType");
            if (!TextUtils.isEmpty(data))
                new ShowDataDialog(getContext(), data).show();

            // clearing payload data from preferences
            SharedPrefsManager.getInstance().write_string_prefs(getContext(), SharedPrefsManager.getInstance().FCM_DATA_PAYLOAD_KEY,
                    "");

        } catch (JSONException e) {
            e.printStackTrace();
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("onScreen", "homeScreen");
    }

    /**
     * displays remote config data on click of show rc values button
     */
    private void showRemoteConfigData() {
        String data = "";
        if (getContext() != null) {
            FirebaseRemoteConfig remoteConfig = ((MainActivity) getContext()).mFirebaseRemoteConfig;
            if (remoteConfig != null) {
                if (!TextUtils.isEmpty(remoteConfig.getString("time_gap")))
                    data += "time_gap " + remoteConfig.getString("time_gap") + "\n";

                if (!TextUtils.isEmpty(remoteConfig.getString("interstitial_ad_show_gap")))
                    data += "interstitial_ad_show_gap " + remoteConfig.getString("interstitial_ad_show_gap") + "\n";

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

    public void onBackPressed() {
        if (getContext() == null)
            return;
        final MainActivity context = (MainActivity) getContext();
        new MyResponseDialog(getContext(), getString(R.string.do_you_want_to_exit),
                getString(R.string.yes), getString(R.string.no), new ResponseListener() {
            @Override
            public void onPositiveResponse() {
                context.playTapSound();
                context.exitFromGame();
            }

            @Override
            public void onNeutralResponse() {

            }

            @Override
            public void onNegativeResponse() {
                context.playTapSound();
            }
        }).show();
    }

    public void handleAfterReceivingPayload() {
        if (getContext() != null) {
            MainActivity context = (MainActivity) getContext();
            if (context.haveToGoDirectToGameScreen) {
                context.haveToGoDirectToGameScreen = false;
                goToGameScreen(context);
                return;
            }

            String payloadData = SharedPrefsManager.getInstance().read_string_prefs(getContext(),
                    SharedPrefsManager.getInstance().FCM_DATA_PAYLOAD_KEY);
            if (!TextUtils.isEmpty(payloadData)) {
                showPayloadData(payloadData);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
