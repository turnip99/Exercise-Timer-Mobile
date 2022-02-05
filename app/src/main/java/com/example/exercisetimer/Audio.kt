package com.example.exercisetimer

import android.content.Context
import android.media.MediaPlayer

class Audio
{
    var mp:MediaPlayer = MediaPlayer();

    fun Play(res: Int, context:Context)
    {
        if(mp != null)
        {
            mp.stop();
            mp.release();
        }
        mp = MediaPlayer.create(context, res);
        if(mp != null)
        {
            mp.start();
        }
    }
}