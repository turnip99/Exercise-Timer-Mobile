package com.example.exercisetimer

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Spinner
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_settings.*


class SettingsActivity : AppCompatActivity()
{
    var audio = Audio();

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings);
        val prefHolder = PreferenceHolder(this);
        constraintLayout.setBackgroundColor(prefHolder.GetColour());
        LoadPreferences(prefHolder);
    }

    fun LoadPreferences(prefHolder:PreferenceHolder) //Function that takes saved preferences and updates the values of the spinners and checkboxes accordingly.
    {
        spnColour.setSelection(GetSpinnerIndex(spnColour, prefHolder.colour));
        spnReminder.setSelection(GetSpinnerIndex(spnReminder, prefHolder.reminder));
        spnFocus.setSelection(GetSpinnerIndex(spnFocus, prefHolder.focus));
        spnReset.setSelection(GetSpinnerIndex(spnReset, prefHolder.reset));
        chkBall.setChecked(prefHolder.ball);
        chkBand.setChecked(prefHolder.band);
        chkBox.setChecked(prefHolder.box);
        chkWeights.setChecked(prefHolder.weights);
    }

    fun SoundClick(view: View) //Function runs when sound button is pressed, to play sound effect selected.
    {
        var soundName = spnReminder.selectedItem.toString().toLowerCase();
        var sound = this.resources.getIdentifier(soundName, "raw", this.packageName);
        audio.Play(sound, this);

    }

    fun SaveClick(view: View) //Function to save app preferences.
    {
        val dateHelper =  DateHelper();
        val preferences =  getSharedPreferences("settings", MODE_PRIVATE);
        var editor = preferences.edit();
        editor.putString("colour", spnColour.selectedItem.toString());
        editor.putString("reminder", spnReminder.selectedItem.toString());
        editor.putString("focus", spnFocus.selectedItem.toString());
        editor.putString("reset", spnReset.selectedItem.toString());
        editor.putBoolean("ball", chkBall.isChecked);
        editor.putBoolean("band", chkBand.isChecked);
        editor.putBoolean("box", chkBox.isChecked);
        editor.putBoolean("weights", chkWeights.isChecked);
        editor.commit();

        val records =  getSharedPreferences("records", MODE_PRIVATE);
        editor = records.edit();
        editor.putString("resetDate", dateHelper.GetResetDate(spnReset.selectedItem.toString()))
        editor.commit()

        Toast.makeText(this,"Changes Saved", Toast.LENGTH_SHORT).show();
        finish();
}

    private fun GetSpinnerIndex(spinner: Spinner, value: String): Int //Function gets the index of a value in a spinner.
    {
        for (i in 0 until spinner.count)
        {
            if (spinner.getItemAtPosition(i).toString() == value)
            {
                return i
            }
        }
        return -1
    }
}
