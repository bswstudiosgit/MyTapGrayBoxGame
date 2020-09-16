package com.test.mygame.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

import com.test.mygame.BuildConfig;
import com.test.mygame.MainActivity;
import com.test.mygame.R;
import com.test.mygame.ResponseListener;
import com.test.mygame.dialog.MyResponseDialog;
import com.test.mygame.model.SavedGame;
import com.test.mygame.util.MyFragmentManager;
import com.test.mygame.util.SharedPrefsManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class HomeScreen extends Fragment {

    public static String TAG = "home_screen_tag";
    private Button playButton;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.homescreen_fragment_layout, container, false);

        playButton = view.findViewById(R.id.play);
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
}
