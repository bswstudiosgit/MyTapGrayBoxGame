package com.test.mygame.util;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;

import com.test.mygame.R;

public class MySoundManager {

    // static variable single_instance of type MySoundManager
    private static MySoundManager single_instance = null;
    private SoundPool soundPool;
    private int tapSound
//            , music
            ;
    private int soundLoaded;

    // private constructor restricted to this class itself
    private MySoundManager(Context context) {
        soundLoaded = 0;
        createSoundPool();
    }

    private void createSoundPool() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            soundPool = new SoundPool.Builder()
                    .setMaxStreams(1)
                    .setAudioAttributes(audioAttributes)
                    .build();
        } else {
            soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        }
    }

    public void loadSound(Context context) {
        tapSound = soundPool.load(context, R.raw.click, 1);
//        music = soundPool.load(context, R.raw.music, 1);

        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
//                soundLoaded++;
//                if (soundLoaded == 2)
//                    playMusic();
            }
        });
    }

    // static method to create instance of MySoundManager class
    public static MySoundManager getInstance(Context context) {
        if (single_instance == null)
            single_instance = new MySoundManager(context);

        return single_instance;
    }

    public void releaseSoundPool() {
//        stopMusic();
        soundPool.release();
        single_instance = null;
    }

    public void playTapSound() {
        soundPool.play(tapSound, 1, 1, 0, 0, 1);
    }

//    public void playMusic() {
//        soundPool.play(music, 1, 1, 0, -1, 1);
//    }
//
//    public void stopMusic() {
//        soundPool.stop(music);
//    }
}
