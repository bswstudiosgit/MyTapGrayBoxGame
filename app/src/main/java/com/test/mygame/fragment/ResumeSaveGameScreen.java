package com.test.mygame.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.test.mygame.MainActivity;
import com.test.mygame.R;
import com.test.mygame.util.SharedPrefsManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ResumeSaveGameScreen extends Fragment {

    public static String TAG = "resume_save_game_screen_tag";
    private Button continueButton, newGameButton;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.ask_save_game_screen_layout, container, false);
        initViews(view);
        return view;
    }

    private void initViews(View view) {
        ((TextView) view.findViewById(R.id.textView)).setText(getResources().getString(R.string.do_you_want_to_resume_saved_game));

        continueButton = view.findViewById(R.id.yes);
        newGameButton = view.findViewById(R.id.no);

        continueButton.setText(R.string.continue_text);
        newGameButton.setText(R.string.new_game);

        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getContext() != null) {
                    MainActivity context = (MainActivity) getContext();
                    context.getSupportFragmentManager().popBackStack();
                    if (context.gameScreen == null)
                        context.gameScreen = new GameScreen();
                    context.addFragment(context.gameScreen, context.gameScreen.TAG);
                }
            }
        });

        newGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getContext() != null) {
                    SharedPrefsManager.getInstance().deleteSavedGame(getContext());
                    MainActivity context = (MainActivity) getContext();
                    context.getSupportFragmentManager().popBackStack();
                    if (context.gameScreen == null)
                        context.gameScreen = new GameScreen();
                    context.addFragment(context.gameScreen, context.gameScreen.TAG);
                }
            }
        });
    }
}
