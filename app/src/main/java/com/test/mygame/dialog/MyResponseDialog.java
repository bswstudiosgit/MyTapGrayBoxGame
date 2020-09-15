package com.test.mygame.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.test.mygame.R;
import com.test.mygame.ResponseListener;

import androidx.annotation.NonNull;

public class MyResponseDialog extends Dialog {

    private Context context;
    private String title, positiveButtonText, negativeButtonText;
    private ResponseListener listener;
    private Button positiveButton, negativeButton;

    public MyResponseDialog(@NonNull Context context, String title, String positiveButtonText, String negativeButtonText,
                            ResponseListener listener) {
        super(context);
        this.context = context;
        this.title = title;
        this.positiveButtonText = positiveButtonText;
        this.negativeButtonText = negativeButtonText;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_response_dialog_layout);

        positiveButton = findViewById(R.id.yes);
        negativeButton = findViewById(R.id.no);

        ((TextView) findViewById(R.id.textView)).setText(title);
        positiveButton.setText(positiveButtonText);
        negativeButton.setText(negativeButtonText);

        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null)
                    listener.onPositiveResponse();
                dismiss();
            }
        });

        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null)
                    listener.onNegativeResponse();
                dismiss();
            }
        });

        setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (listener != null)
                    listener.onNeutralResponse();
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
