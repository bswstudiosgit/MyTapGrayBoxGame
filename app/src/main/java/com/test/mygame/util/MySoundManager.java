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
    private int MAX_AUDIO_STREAMS = 2;
    private SoundPool soundPool;
    private int tapSound, music;
    private int soundLoaded;

    // private constructor restricted to this class itself
    private MySoundManager() {
        soundLoaded = 0;
        createSoundPool();
    }

    private void createSoundPool() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            soundPool = new SoundPool.Builder()
                    .setMaxStreams(MAX_AUDIO_STREAMS)
                    .setAudioAttributes(audioAttributes)
                    .build();
        } else {
            soundPool = new SoundPool(MAX_AUDIO_STREAMS, AudioManager.STREAM_MUSIC, 0);
        }
    }

    public void loadSound(Context context) {
        tapSound = soundPool.load(context, R.raw.click, 1);
        music = soundPool.load(context, R.raw.music, 1);

        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                soundLoaded++;
                if (soundLoaded == MAX_AUDIO_STREAMS)
                    playMusic();
            }
        });
    }

    // static method to create instance of MySoundManager class
    public static MySoundManager getInstance() {
        if (single_instance == null)
            single_instance = new MySoundManager();

        return single_instance;
    }

    public void releaseSoundPool() {
        stopMusic();
        soundPool.release();
        soundPool = null;
        single_instance = null;
    }

    public void playTapSound() {
        soundPool.play(tapSound, 0.5f, 0.5f, 0, 0, 1);
    }

    public void playMusic() {
        if (soundPool != null)
            soundPool.play(music, 1, 1, 0, -1, 1);
    }

    public void stopMusic() {
        if (soundPool != null)
            soundPool.stop(music);
    }

    public void pause() {
        if (soundPool != null)
            soundPool.autoPause();
    }

    public void resume() {
        if (soundPool != null && soundLoaded == MAX_AUDIO_STREAMS)
            soundPool.autoResume();
    }
}
