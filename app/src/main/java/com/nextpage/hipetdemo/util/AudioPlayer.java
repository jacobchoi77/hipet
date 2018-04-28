package com.nextpage.hipetdemo.util;

import android.content.Context;
import android.media.MediaPlayer;

/**
 * Created by jacobsFactory on 2017-11-16.
 */

public class AudioPlayer {
    private static MediaPlayer mMediaPlayer;

    public void stop() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    public void play(Context c, int rid) {
        stop();
        mMediaPlayer = MediaPlayer.create(c, rid);
        mMediaPlayer.setOnCompletionListener(mediaPlayer -> stop());
        mMediaPlayer.start();
    }
}
