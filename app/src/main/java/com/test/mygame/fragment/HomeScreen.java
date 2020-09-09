package com.test.mygame.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

import com.test.mygame.MainActivity;
import com.test.mygame.R;
import com.test.mygame.model.SavedGame;
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
                    if (lastSavedGame == null) {
                        MainActivity context = (MainActivity) getContext();
                        if (context.gameScreen == null)
                            context.gameScreen = new GameScreen();
                        context.addFragment(context.gameScreen, context.gameScreen.TAG);
                    } else {
                        MainActivity context = (MainActivity) getContext();
                        ResumeSaveGameScreen resumeSaveGameScreen = new ResumeSaveGameScreen();
                        context.addFragment(resumeSaveGameScreen, resumeSaveGameScreen.TAG);
                    }
                }
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        SavedGame lastSavedGame = SharedPrefsManager.getInstance().getLastSavedGame(getContext());
        if (lastSavedGame != null && getContext() != null) {
            MainActivity context = (MainActivity) getContext();
            if (context.gameScreen == null)
                context.gameScreen = new GameScreen();
            context.addFragment(context.gameScreen, context.gameScreen.TAG);
        }
    }
}
