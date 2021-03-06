package com.test.mygame.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.test.mygame.model.SavedGame;

public class SharedPrefsManager {

    // static variable single_instance of type SharedPrefsManager
    private static SharedPrefsManager single_instance = null;

    private String PREFERENCES_KEY = "game_preferences";
    public String BEST_SCORE_KEY = "best_score";
    public String SAVED_GAME_KEY = "saved_game";
    public String FCM_DATA_PAYLOAD_KEY = "fcm_data_payload";
    public String TOTAL_GAMES_PLAYED_KEY = "total_games_played";
    public String SELECTED_LANGUAGE_LOCALE_KEY = "selected_language_locale";

    public String TIME_GAP_KEY = "time_gap";
    public String COLOUR_CODE1_KEY = "colour1";
    public String COLOUR_CODE2_KEY = "colour2";
    public String COLOUR_CODE3_KEY = "colour3";
    public String COLOUR_CODE4_KEY = "colour4";
    public String COLOUR_CODE5_KEY = "colour5";
    public String INTERSTITIAL_AD_SHOW_GAP_KEY = "interstitial_ad_show_gap";


    // private constructor restricted to this class itself
    private SharedPrefsManager() {
    }

    // static method to create instance of MyPreferences class
    public static SharedPrefsManager getInstance() {
        if (single_instance == null)
            single_instance = new SharedPrefsManager();

        return single_instance;
    }

    public void write_integer_prefs(Context context, String key, int value) {
        if (context == null)
            return;
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCES_KEY, Context.MODE_PRIVATE);
        if (sharedPreferences != null) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(key, value);
            editor.apply();
        }
    }

    public int read_integer_prefs(Context context, String key) {
        if (context == null)
            return 0;
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCES_KEY, Context.MODE_PRIVATE);
        if (sharedPreferences != null)
            return sharedPreferences.getInt(key, 0);
        return 0;
    }

    public void write_string_prefs(Context context, String key, String value) {
        if (context == null)
            return;
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCES_KEY, Context.MODE_PRIVATE);
        if (sharedPreferences != null) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(key, value);
            editor.apply();
        }
    }

    public String read_string_prefs(Context context, String key) {
        if (context == null)
            return "";
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCES_KEY, Context.MODE_PRIVATE);
        if (sharedPreferences != null)
            return sharedPreferences.getString(key, "");
        return "";
    }

    public void saveGame(Context context, SavedGame savedGame) {
        if (context == null)
            return;
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCES_KEY, Context.MODE_PRIVATE);
        if (sharedPreferences != null) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            Gson gson = new Gson();
            String data = gson.toJson(savedGame);
            editor.putString(SAVED_GAME_KEY, data);
            editor.apply();
        }
    }

    public SavedGame getLastSavedGame(Context context) {
        if (context == null)
            return null;
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCES_KEY, Context.MODE_PRIVATE);
        if (sharedPreferences != null && sharedPreferences.contains(SAVED_GAME_KEY)) {
            String data = sharedPreferences.getString(SAVED_GAME_KEY, "");
            if (data.equals(""))
                return null;
            Gson gson = new Gson();
            SavedGame savedGame = gson.fromJson(data, SavedGame.class);
            return savedGame;
        }
        return null;
    }

    public void deleteSavedGame(Context context) {
        if (context == null)
            return;
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCES_KEY, Context.MODE_PRIVATE);
        if (sharedPreferences != null) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(SAVED_GAME_KEY, "");
            editor.apply();
        }
    }

}
