package com.example.exercisetimer

import android.content.Context
import android.support.v7.app.AppCompatActivity;

class RecordHolder //Class holds records from the timer activity to be displayed in the records activity.
{
    var exerciseDayToday = 0;
    var exerciseDayYesterday = 0;
    var exerciseDayBest = 0;
    var exerciseWeekThis = 0;
    var exerciseWeekLast = 0;
    var exerciseWeekBest = 0;
    var delayToday = 0;
    var delayYesterday = 0;
    var delayBest = 0;
    var currentDate = "";
    var resetDate = "";
    var context: Context;

    constructor(context: Context)
    {
        this.context = context;
        val records = context.getSharedPreferences("records", AppCompatActivity.MODE_PRIVATE);
        exerciseDayToday = records.getInt("exerciseDayToday",0);
        exerciseDayYesterday = records.getInt("exerciseDayYesterday",0);
        exerciseDayBest = records.getInt("exerciseDayBest",0);
        exerciseWeekThis = records.getInt("exerciseWeekThis",0);
        exerciseWeekLast = records.getInt("exerciseWeekLast",0);
        exerciseWeekBest = records.getInt("exerciseWeekBest",0);
        delayToday = records.getInt("delayToday",0);
        delayYesterday = records.getInt("delayYesterday",0);
        delayBest = records.getInt("delayBest",0);
        currentDate = records.getString("currentDate", "01-01-2001");
        resetDate = records.getString("resetDate", "01-01-2001");
    }
}