package com.test.mygame.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.Locale;

public class Factory {

    public static final int WRITE_REQUEST_CODE = 1020;
    // static variable single_instance of type Factory
    private static Factory single_instance = null;

    // private constructor restricted to this class itself
    private Factory() {
    }

    // static method to create instance of Factory class
    public static Factory getInstance() {
        if (single_instance == null)
            single_instance = new Factory();

        return single_instance;
    }

    public void hideSystemUI(Activity activity) {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = activity.getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    public void setListenerToHideStatusAndNavigationBar(final Activity activity) {
        View decorView = activity.getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0)
                    hideSystemUI(activity);
            }
        });
    }

    public File takeScreenshot(Activity activity) {
        if (!isPermissionGranted(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                activity.requestPermissions(permissions, WRITE_REQUEST_CODE);
            }
            return null;
        }

        Date now = new Date();
        DateFormat.format("yyyy-MM-dd_hh:mm:ss", now);

        try {
            // image naming and path  to include sd card  appending name you choose for file
            String mPath = Environment.getExternalStorageDirectory().toString() + "/" + now + ".jpg";

            // create bitmap screen capture
            View v1 = activity.getWindow().getDecorView().getRootView();
            v1.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
            v1.setDrawingCacheEnabled(false);

            File imageFile = new File(mPath);
            if (!imageFile.exists())
                imageFile.createNewFile();

            FileOutputStream outputStream = new FileOutputStream(imageFile);
            int quality = 100;
            bitmap.compress(Bitmap.CompressFormat.PNG, quality, outputStream);
            outputStream.flush();
            outputStream.close();

            return imageFile;

        } catch (Throwable e) {
            // Several error may come out with file handling or DOM
            e.printStackTrace();
        }
        return null;
    }

    public boolean isPermissionGranted(Context context, String permission) {
        int res = context.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    public String getNetworkType(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return manager.getActiveNetworkInfo().getTypeName();
    }

    public void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public void sendMail(Activity activity, String subject, String body, Uri uri) {
        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.setType("image/jpg");
        intent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"sagar@bswgames.com"});
        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(android.content.Intent.EXTRA_TEXT, body);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.putExtra(android.content.Intent.EXTRA_STREAM, uri);
        activity.startActivity(Intent.createChooser(intent, "Send mail..."));
    }

    public String getCountryCode(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getNetworkCountryIso();
    }

    public void setLocale(Activity context, String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        Configuration configuration = new Configuration();
        configuration.setLocale(locale);
        context.getBaseContext().getResources().updateConfiguration(configuration,
                context.getBaseContext().getResources().getDisplayMetrics());

        // save selected language to shared preferences
        SharedPrefsManager prefs = SharedPrefsManager.getInstance();
        prefs.write_string_prefs(context, prefs.SELECTED_LANGUAGE_LOCALE_KEY, languageCode);
    }

    public void loadLocale(Activity context) {
        SharedPrefsManager prefs = SharedPrefsManager.getInstance();
        String locale = prefs.read_string_prefs(context, prefs.SELECTED_LANGUAGE_LOCALE_KEY);
        setLocale(context, locale);
    }
}
