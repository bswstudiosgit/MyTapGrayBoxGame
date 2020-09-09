package com.test.mygame.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.test.mygame.MainActivity;
import com.test.mygame.R;
import com.test.mygame.model.SavedGame;
import com.test.mygame.util.SharedPrefsManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class AskForSaveGameScreen extends Fragment {

    public static String TAG = "save_game_screen_tag";
    private Button yesButton, noButton;

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
        ((TextView) view.findViewById(R.id.textView)).setText(getResources().getString(R.string.do_you_want_to_save_this_game));

        yesButton = view.findViewById(R.id.yes);
        noButton = view.findViewById(R.id.no);

        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getContext() != null) {
                    MainActivity context = (MainActivity) getContext();
                    SharedPrefsManager.getInstance().saveGame(getContext(), new SavedGame(context.gameScreen.grayBox,
                            context.gameScreen.score));
                    context.getSupportFragmentManager().popBackStack();
                    context.getSupportFragmentManager().popBackStack();
                }
            }
        });

        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getContext() != null) {
                    SharedPrefsManager.getInstance().deleteSavedGame(getContext());
                    MainActivity context = (MainActivity) getContext();
                    context.getSupportFragmentManager().popBackStack();
                    context.getSupportFragmentManager().popBackStack();
                }
            }
        });
    }
}
