package com.example.exercisetimer

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_manage.*


class ManageActivity : AppCompatActivity()
{
    var newItem = false;
    var exercises = arrayListOf<String>();

    var dbHandler: CustomDBHandler? = null;

    var selectedPos = -1; //Current index of selected item (needs to be saved for when orientayion is changed.)


    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage);
        val prefHolder = PreferenceHolder(this);
        constraintLayout.setBackgroundColor(prefHolder.GetColour());
        DisableFields();

        dbHandler = CustomDBHandler(this);

        exercises = dbHandler!!.GetNames();
        PopulateListView();

        exerciseList.setOnItemClickListener { parent, view, position, id -> //Event for when list item is selected.
            selectedPos = position;
            if (position == 0) //If new exercise
            {
                newItem = true;
                txtName.setText("");
                spnFocus.setSelection(0);
                txtReps1.setText("")
                txtReps2.setText("")
                txtDuration1.setText("")
                txtDuration2.setText("")
                txtDowntime.setText("")
                chkPerSide.setChecked(false);
                spnEquipment.setSelection(0);
                txtURL.setText("");
            }
            else
            {
                newItem = false;
                var name = exerciseList.getItemAtPosition(position).toString();
                txtName.setText(name);
                val details = dbHandler!!.GetDetails(name);
                spnFocus.setSelection(GetSpinnerIndex(spnFocus, details[0]));
                txtDuration1.setText(details[1]);
                txtDuration2.setText(details[2]);
                txtReps1.setText(details[3]);
                txtReps2.setText(details[4]);
                txtDowntime.setText(details[5]);
                if (details[6].toInt() == 1)
                {
                    chkPerSide.setChecked(true);
                }
                else
                {
                    chkPerSide.setChecked(false);
                }
                spnEquipment.setSelection(GetSpinnerIndex(spnEquipment, details[7]));
                txtURL.setText(details[8]);
            }
            EnableFields();
            Log.e("click", selectedPos.toString());
        }
    }

    fun PopulateListView()
    {
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, exercises)
        exerciseList.adapter = adapter;
        selectedPos = -1;
    }

    fun DisableFields() //Function to disable all form elements.
    {
        txtDowntime.isEnabled = false;
        txtDuration1.isEnabled = false;
        txtDuration2.isEnabled = false;
        txtName.isEnabled = false;
        txtReps1.isEnabled = false;
        txtReps2.isEnabled = false;
        chkPerSide.isEnabled = false;
        spnEquipment.isEnabled = false;
        spnFocus.isEnabled = false;
        btnSave.isEnabled = false;
        btnDelete.isEnabled = false;
        txtURL.isEnabled = false;
        txtName.setText("");
        spnFocus.setSelection(0);
        txtDuration1.setText("");
        txtDuration2.setText("");
        txtReps1.setText("");
        txtReps2.setText("");
        txtDowntime.setText("");
        chkPerSide.setChecked(false);
        spnEquipment.setSelection(0);
        txtURL.setText("");
    }

    fun EnableFields() //Function to enable all form elements.
    {
        txtDowntime.isEnabled = true;
        txtDuration1.isEnabled = true;
        txtDuration2.isEnabled = true;
        if (newItem) //If updating, the user should not be able to modify the name (the primary key).
        {
            txtName.isEnabled = true;
            btnDelete.isEnabled = false;

        }
        else
        {
            txtName.isEnabled = false;
            btnDelete.isEnabled = true;
        }
        txtReps1.isEnabled = true;
        txtReps2.isEnabled = true;
        chkPerSide.isEnabled = true;
        spnEquipment.isEnabled = true;
        spnFocus.isEnabled = true;
        txtURL.isEnabled = true;
        btnSave.isEnabled = true;
    }

    fun ValidateInputs() : Boolean //Function to check if all values in input fields are valid.
    {
        val duplicate = GetDuplicateName(txtName.text.toString());
        if (txtName.text.toString() == "" || txtDuration1.text.toString() == "" || txtDuration2.text.toString() == ""
            || txtReps1.text.toString() == "" || txtReps2.text.toString() == "" || txtDowntime.text.toString() == ""
            || txtDuration1.text.toString().toIntOrNull() == null || txtDuration2.text.toString().toIntOrNull() == null
            || txtReps1.text.toString().toIntOrNull() == null || txtReps2.text.toString().toIntOrNull() == null
            || txtDowntime.text.toString().toIntOrNull() == null || (newItem && duplicate))
        {
            return false;
        }
        else
        {
            if (txtDuration1.text.toString().toInt() > txtDuration2.text.toString().toInt() || txtReps1.text.toString().toInt() > txtReps2.text.toString().toInt()
                ||txtDuration1.text.toString().toInt() < 1 || txtDuration2.text.toString().toInt() < 1 || txtReps1.text.toString().toInt() < 1 || txtReps2.text.toString().toInt() < 1)
            {
                return false;
            }
        }
        return true;
    }

    fun SaveClick(view: View) //Function to save a new exercise/save changes to an existing one.
    {
        if (ValidateInputs())
        {
            val name = txtName.text.toString();
            val focus = spnFocus.selectedItem.toString();
            val minDuration  = txtDuration1.text.toString().toInt();
            val maxDuration = txtDuration2.text.toString().toInt();
            val minReps = txtReps1.text.toString().toInt();
            val maxReps = txtReps2.text.toString().toInt();
            val downtime = txtDowntime.text.toString().toInt();
            var perSide = 0;
            if (chkPerSide.isChecked)
            {
                perSide = 1;
            }
            val equipment = spnEquipment.selectedItem.toString();
            val url = txtURL.text.toString();
            val exercise = Exercise(name, focus, minDuration, maxDuration, minReps, maxReps, downtime, perSide, equipment, url);
            if (newItem)
            {
                dbHandler!!.AddExericse(exercise);
                exercises.add(name);
            }
            else
            {
                dbHandler!!.UpdateExericse(exercise);
            }
            PopulateListView();
            DisableFields();
            Toast.makeText(this,"Exercise Saved", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Toast.makeText(this,"Invalid Input...", Toast.LENGTH_SHORT).show();
        }
    }

    fun DeleteClick(view: View) //Function to delete selected exercise from database.
    {
        val name = txtName.text.toString();
        dbHandler!!.DeleteExercise(name);
        val deleteIndex = exercises.indexOf(name);
        exercises.removeAt(deleteIndex);
        PopulateListView();
        Toast.makeText(this,"Exercise Deleted", Toast.LENGTH_SHORT).show();
        DisableFields();
    }

    fun URLClick(view: View) //Function to open browser via intent and go to exercise URL.
    {
        var url = txtURL.text.toString();
        if (!url.startsWith("http://") && !url.startsWith("https://"))
        {
            url = "http://" + url;
        }
        Log.e("url", url);
        val openURL = Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(openURL);
    }

    private fun GetDuplicateName(name: String): Boolean //Function gets if a given name is already in the listView.
    {
        for (item in exercises)
        {
            if (item == name)
            {
                return true;
            }
        }
        return false;
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
    /*Screen rotation was deselecting selected item from list box, but not clearing other fields.
    Since I was unable to prevent the deselection, all fields will be cleared on rotation*/
    override fun onConfigurationChanged(newConfig: Configuration?)
    {
        super.onConfigurationChanged(newConfig)
        DisableFields();
    }
}