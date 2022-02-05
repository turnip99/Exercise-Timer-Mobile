package com.example.exercisetimer

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import android.util.Log
import java.text.SimpleDateFormat



class MainActivity : AppCompatActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val prefHolder = PreferenceHolder(this);
        constraintLayout.setBackgroundColor(prefHolder.GetColour());
    }

    fun ManageRecords() //Function for updating record variables upon opening the app (e.g. refreshing daily records if it is a new day).
    {
        val dateHelper = DateHelper();
        val cal = Calendar.getInstance();
        val date = cal.time; //Get date from calender.
        val dateFormat = SimpleDateFormat("dd-MM-yyyy");
        val currentDateFormatted = dateFormat.format(date);
        val currentDate = currentDateFormatted.toString(); //Gets current date as a string so it can be compared with records.

        val recordHolder = RecordHolder(this);

        val records = getSharedPreferences("records", AppCompatActivity.MODE_PRIVATE); //Get reference to shared preferences.
        var editor = records.edit();

        if (recordHolder.currentDate != currentDate) //If it is a new day.
        {
            var diff = dateHelper.GetDateDifference(recordHolder.currentDate, currentDate); //Gets number of days until reset.
            Toast.makeText(this,"Daily Records Reset.", Toast.LENGTH_SHORT).show();

            if (diff == 1) //If 1 day later, set yesterday's records to what were today's records.
            {
                editor.putInt("exerciseDayYesterday", recordHolder.exerciseDayToday);
                editor.putInt("delayYesterday", recordHolder.delayToday);
            }
            else //Else, if more than 1 day later, set yesterday's records to 0.
            {
                editor.putInt("exerciseDayYesterday", 0);
                editor.putInt("delayYesterday", 0);
            }

            editor.putInt("exerciseDayToday", 0);
            editor.putInt("delayToday", 0);
            editor.putString("currentDate", currentDate); //Sets the current date so that this reset will not happen again today.
        }

        var diff = dateHelper.GetDateDifference(currentDate, recordHolder.resetDate); //Gets number of days until reset.

        Log.e("weekly reset date", recordHolder.resetDate);
        if (diff <= 0) //If it's time to reset.
        {
            Toast.makeText(this,"Weekly Records Reset.", Toast.LENGTH_SHORT).show();
            if (diff < 7) //If less than a week ago, set last week's record to what was this week's record.
            {
                editor.putInt("exerciseWeekLast", recordHolder.exerciseWeekThis);
            }
            else //Else, if it's been more than 7 days, last week's record = 0.
            {
                editor.putInt("exerciseWeekLast", 0);
            }
            editor.putInt("exerciseWeekThis", 0);
            val prefHolder = PreferenceHolder(this);
            editor.putString("resetDate", dateHelper.GetResetDate(prefHolder!!.reset)) //Sets new reset date.
        }
        editor.commit(); //Saves changes to records.
    }


    override fun onResume()
    {
        super.onResume();
        var prefHolder = PreferenceHolder(this);
        constraintLayout.setBackgroundColor(prefHolder.GetColour());
        ManageRecords();
    }

    fun ManageClick(view: View)
    {
        val intent = Intent(this, ManageActivity::class.java);
        startActivity(intent);
    }

    fun GenerateClick(view: View)
    {
        val intent = Intent(this, GenerateActivity::class.java);
        startActivity(intent);
    }

    fun TimerClick(view: View)
    {
        val intent = Intent(this, StartTimerActivity::class.java);
        startActivity(intent);
    }

    fun RecordsClick(view: View)
    {
        val intent = Intent(this, RecordsActivity::class.java);
        startActivity(intent);
    }

    fun SettingsClick(view: View)
    {
        val intent = Intent(this, SettingsActivity::class.java);
        startActivity(intent);
    }

    fun GuideClick(view: View)
    {
        val intent = Intent(this, UserGuideActivity::class.java);
        startActivity(intent);
    }
}