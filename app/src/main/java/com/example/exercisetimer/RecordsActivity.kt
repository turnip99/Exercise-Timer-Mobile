package com.example.exercisetimer

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_records.*
import android.view.View


class RecordsActivity : AppCompatActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_records)
        val prefHolder = PreferenceHolder(this);
        constraintLayout.setBackgroundColor(prefHolder.GetColour());
        val recordHolder = RecordHolder(this); //Holds records as shared preferences.
        val timeController = TimeController(); //Object holds functions that control conversions to/from time in seconds.
        lblExerciseDayToday.setText("Today's exercise: " + timeController.ConvertToDisplayTime(recordHolder.exerciseDayToday));
        lblExerciseDayYesterday.setText("Yesterday's exercise: " + timeController.ConvertToDisplayTime(recordHolder.exerciseDayYesterday));
        lblExerciseDayBest.setText("Best daily exercise: " + timeController.ConvertToDisplayTime(recordHolder.exerciseDayBest));
        lblExerciseWeekThis.setText("This week's exercise: " + timeController.ConvertToDisplayTime(recordHolder.exerciseWeekThis));
        lblExerciseWeekLast.setText("Last week's exercise: " + timeController.ConvertToDisplayTime(recordHolder.exerciseWeekLast));
        lblExerciseWeekBest.setText("Best weekly exercise: " + timeController.ConvertToDisplayTime(recordHolder.exerciseWeekBest));
        lblDelayToday.setText("Today's delay: " + timeController.ConvertToDisplayTime(recordHolder.delayToday));
        lblDelayYesterday.setText("Yesterday's delay: " + timeController.ConvertToDisplayTime(recordHolder.delayYesterday));
        lblDelayBest.setText("Worst daily delay: " + timeController.ConvertToDisplayTime(recordHolder.delayBest));
    }

    fun ExportClick(view: View) //Function to export records as a PDF.
    {
        OpenPDF();
    }

    fun OpenPDF() //Opens PDF file using intent to external app.
    {

    }
}
