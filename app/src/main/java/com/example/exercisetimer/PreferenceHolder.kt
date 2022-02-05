package com.example.exercisetimer

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity

class PreferenceHolder //Class holds preferences from settings activity to use in other activities.
{
    var colour = "";
    var reminder = "";
    var focus = "";
    var reset = "";
    var ball = false;
    var band = false;
    var box = false;
    var weights = false;
    var workTime = 0;
    var breakTime = 0;
    var context: Context;

    constructor(context: Context)
    {
        this.context = context;
        val preferences = context.getSharedPreferences("settings", AppCompatActivity.MODE_PRIVATE);
        colour = preferences.getString("colour","Grey");
        reminder = preferences.getString("reminder","Alarm");
        focus = preferences.getString("focus","All");
        reset = preferences.getString("reset","Monday");
        ball = preferences.getBoolean("ball",false);
        band = preferences.getBoolean("band",false);
        box = preferences.getBoolean("box",false);
        weights = preferences.getBoolean("weights",false);
        workTime = preferences.getInt("workTime",1200);
        breakTime = preferences.getInt("breakTime",120);
    }

    fun GetColour() : Int //Gets a colour value from colors.xml based on the colour text string.
    {
        when (colour)
        {
            "White" -> return ContextCompat.getColor(context, R.color.colourWhite);
            "Yellow" -> return ContextCompat.getColor(context, R.color.colourYellow);
            "Blue" -> return ContextCompat.getColor(context, R.color.colourBlue);
            else -> return ContextCompat.getColor(context, R.color.colourGrey)
        }
    }
}