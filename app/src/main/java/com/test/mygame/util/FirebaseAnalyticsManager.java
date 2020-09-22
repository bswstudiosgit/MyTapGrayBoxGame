package com.test.mygame.util;

import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;

public class FirebaseAnalyticsManager {

    private static FirebaseAnalyticsManager single_instance = null;

    // private constructor restricted to this class itself
    private FirebaseAnalyticsManager() {
    }

    // static method to create instance of FirebaseAnalyticsManager class
    public static FirebaseAnalyticsManager getInstance() {
        if (single_instance == null)
            single_instance = new FirebaseAnalyticsManager();

        return single_instance;
    }

    public void logEvent(Context context, String event, Bundle bundle) {
        FirebaseAnalytics.getInstance(context).logEvent(event, bundle);
    }

    public void setUserProperty(Context context, String name, String value) {
        FirebaseAnalytics.getInstance(context).setUserProperty(name, value);
    }
}
