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
