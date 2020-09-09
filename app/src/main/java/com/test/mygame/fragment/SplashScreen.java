package com.test.mygame.fragment;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.test.mygame.MainActivity;
import com.test.mygame.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class SplashScreen extends Fragment {

    public static String TAG = "splash_screen_tag";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.splash_fragment_layout, container, false);
        if (getContext() != null)
            ((MainActivity) getContext()).setTitle(getString(R.string.app_name));
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        startTimer();
    }

    private void startTimer() {
        new CountDownTimer(2000, 2000) {

            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                if (getContext() != null) {
                    MainActivity context = (MainActivity) getContext();
                    HomeScreen homeScreen = new HomeScreen();
                    context.addFragment(homeScreen, homeScreen.TAG);
                }
            }
        }.start();
    }
}
