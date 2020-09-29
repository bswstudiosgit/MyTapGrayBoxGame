package com.test.mygame.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.formats.UnifiedNativeAdView;
import com.test.mygame.R;
import com.test.mygame.ResponseListener;
import com.test.mygame.util.AdmobManager;
import com.test.mygame.util.Factory;

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

        if (getWindow() != null) {
            getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

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

        if (AdmobManager.getInstance().nativeAd != null) {
            FrameLayout container = (FrameLayout) findViewById(R.id.native_ad_container);
            UnifiedNativeAdView adView = (UnifiedNativeAdView) getLayoutInflater().inflate(R.layout.native_ad_layout, null);
            AdmobManager.getInstance().showNativeAd(context, container, adView);
        } else {
            AdmobManager.getInstance().refreshNativeAdvancedAd(context);
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void dismiss() {
        super.dismiss();
        AdmobManager.getInstance().refreshNativeAdvancedAd(context);
        Factory.getInstance().hideSystemUI((Activity) context);
        Factory.getInstance().setListenerToHideStatusAndNavigationBar((Activity) context);
    }
}
