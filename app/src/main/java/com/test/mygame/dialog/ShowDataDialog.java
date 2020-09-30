package com.test.mygame.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;

import com.test.mygame.R;

import androidx.annotation.NonNull;

public class ShowDataDialog extends Dialog {

    private Context context;
    private String text;

    public ShowDataDialog(@NonNull Context context, String text) {
        super(context);
        this.text = text;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_data_dialog_layout);

        if (getWindow() != null) {
            getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        ((TextView) findViewById(R.id.textView)).setText(text);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
