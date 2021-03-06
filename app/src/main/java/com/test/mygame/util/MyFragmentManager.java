package com.test.mygame.util;

import android.widget.FrameLayout;

import com.test.mygame.MainActivity;
import com.test.mygame.R;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class MyFragmentManager {

    // static variable single_instance of type MyFragmentManager
    private static MyFragmentManager single_instance = null;

    // private constructor restricted to this class itself
    private MyFragmentManager() {
    }

    // static method to create instance of MyFragmentManager class
    public static MyFragmentManager getInstance() {
        if (single_instance == null)
            single_instance = new MyFragmentManager();

        return single_instance;
    }

    /**
     * add given fragment to fragment back stack with given tag
     *
     * @param context           represents main activity
     * @param fragmentContainer represents frame layout as container for fragment
     * @param fragment          represents a screen
     * @param tag
     */
    public void addFragment(MainActivity context, FrameLayout fragmentContainer, Fragment fragment, String tag) {
        if (context == null)
            return;
        FragmentManager fragmentManager = context.getSupportFragmentManager();
        if (fragmentManager != null && !fragment.isStateSaved()) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.setCustomAnimations(R.anim.slide_in,
                    R.anim.fade_out,
                    R.anim.fade_in,
                    R.anim.slide_out);
            transaction.add(fragmentContainer.getId(), fragment, tag);
            transaction.addToBackStack(tag);
            transaction.commit();
        }
    }

    /**
     * replace given fragment to fragment back stack with given tag
     *
     * @param context           represents main activity
     * @param fragmentContainer represents frame layout as container for fragment
     * @param fragment          represents a screen
     * @param tag
     */
    public void replaceFragment(MainActivity context, FrameLayout fragmentContainer, Fragment fragment, String tag) {
        if (context == null)
            return;
        FragmentManager fragmentManager = context.getSupportFragmentManager();
        if (fragmentManager != null && !fragmentManager.isStateSaved()) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.setCustomAnimations(R.anim.slide_in,
                    R.anim.fade_out,
                    R.anim.fade_in,
                    R.anim.slide_out);
            transaction.replace(fragmentContainer.getId(), fragment, tag);
            transaction.addToBackStack(tag);
            transaction.commit();
        }
    }
}
