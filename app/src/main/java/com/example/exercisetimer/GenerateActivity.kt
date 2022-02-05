package com.example.exercisetimer

import android.os.Bundle
import android.os.CountDownTimer
import android.support.v7.app.AppCompatActivity
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_generate.*
import kotlin.math.ceil


class GenerateActivity : AppCompatActivity()
{
    var timer : CountDownTimer? = null;
    var audio = Audio();
    var timeController = TimeController(); //Object holds functions that control conversions to/from time in seconds.
    var selector:ExerciseSelector? = null; //Object that generates exercises and returns them as a string when called.
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_generate)
        val prefHolder = PreferenceHolder(this);
        constraintLayout.setBackgroundColor(prefHolder.GetColour());
        lblExercises.setMovementMethod(ScrollingMovementMethod()); //Makes the box displaying exercises scrollable.
        selector = ExerciseSelector(this, prefHolder);
    }

    fun GenerateClick(view: View) //Function to generate exercises for given time.
    {
        val time = timeController.ConvertToSeconds(txtDuration.text.toString()); //Converts time input in text box to actual time in seconds.
        if (time == 0) //If time returned by controller in seconds is 0, this implies an invalid time.
        {
            Toast.makeText(this,"Invalid time...", Toast.LENGTH_SHORT).show();
        }
        else
        {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0) //Remove soft keyboard.
            val exerciseText = selector!!.GetExercises(time); //Gets exercises from the exercise generator object for the given time and the settings in the preference holder object.
            lblExercises.setText(exerciseText);
            if(timer!=null)
            {
                timer!!.cancel();
            }
            var secondsRemaining = 0; //Keeps track of seconds remaining for timer.
            timer = object: CountDownTimer((time*1000).toLong(), 100)
            {
                override fun onTick(millisUntilFinished: Long) //Function runs whenever the timer ticks.
                {
                    var timeRemaining = ceil(millisUntilFinished.toFloat()/1000).toInt(); //Gets the time remaining in seconds.
                    if (timeRemaining != secondsRemaining) //If onto a new whole second, modify UI/play sound.
                    {
                        secondsRemaining = timeRemaining;
                        lblTime.setText(timeController.ConvertToDisplayTime(timeRemaining)); //Show remaining time in label at bottom of screen.
                        if (timeRemaining == 1 || timeRemaining == 2 || timeRemaining == 3) //Play small beep when on the last few seconds of the timer.
                        {
                            var soundName = "minor_beep";
                            PlaySound(soundName);
                        }
                    }
                }

                override fun onFinish() //Play beep when timer ends.
                {
                    var soundName = "major_beep";
                    PlaySound(soundName);
                    lblTime.setText("0:00");
                }
            }
            timer!!.start();
        }
    }

    fun PlaySound(soundName: String) //Function to play sound for timer, since timer doesn't have context.
    {
        var sound = this.resources.getIdentifier(soundName, "raw", this.packageName);
        audio.Play(sound, this);
    }

    override fun finish() //Cancel timer when leaving activity.
    {
        if(timer!=null)
        {
            timer!!.cancel();
        }
        super.finish()
    }
}
