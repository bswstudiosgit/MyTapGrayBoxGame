package com.test.mygame.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.test.mygame.MainActivity;
import com.test.mygame.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ExitScreen extends Fragment {

    public static String TAG = "exit_screen_tag";
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
        ((TextView) view.findViewById(R.id.textView)).setText(getResources().getString(R.string.do_you_want_to_exit));

        yesButton = view.findViewById(R.id.yes);
        noButton = view.findViewById(R.id.no);

        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getContext() != null) {
                    MainActivity context = (MainActivity) getContext();
                    context.getSupportFragmentManager().popBackStack();
                    context.exitFromGame();
                }
            }
        });

        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getContext() != null) {
                    MainActivity context = (MainActivity) getContext();
                    context.getSupportFragmentManager().popBackStack();
                }
            }
        });
    }
}
