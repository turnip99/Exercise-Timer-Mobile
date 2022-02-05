package com.example.exercisetimer

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_start_timer.*

class StartTimerActivity : AppCompatActivity()
{
    var timeController = TimeController(); //Object holds functions that control conversions to/from time in seconds.
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_timer)
        val prefHolder = PreferenceHolder(this);
        constraintLayout.setBackgroundColor(prefHolder.GetColour());
        txtWork.setText(timeController.ConvertToDisplayTime(prefHolder.workTime));
        txtBreak.setText(timeController.ConvertToDisplayTime(prefHolder.breakTime))
    }

    fun BeginClick(view: View) //Function to generate exercises for given time.
    {
        var workTime = timeController.ConvertToSeconds(txtWork.text.toString());
        var breakTime = timeController.ConvertToSeconds(txtBreak.text.toString());
        if (workTime == 0 || breakTime == 0) //If time returned by controller in seconds is 0, this implies an invalid time.
        {
            Toast.makeText(this,"Invalid time...", Toast.LENGTH_SHORT).show();
        }
        else
        {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0) //Remove soft keyboard.
            val preferences =  getSharedPreferences("settings", MODE_PRIVATE);
            var editor = preferences.edit();
            editor.putInt("workTime", workTime);
            editor.putInt("breakTime", breakTime);
            editor.commit(); //Save work and break time so that the settings will be the same next time the activity is loaded.

            val intent = Intent(this, RunTimerActivity::class.java);
            intent.putExtra("workTime", workTime); //Append work and break time to the intent to be received by timer activity.
            intent.putExtra("breakTime", breakTime);

            startActivity(intent);
        }
    }
}
