package com.example.exercisetimer
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.exercisetimer.provider.MyContentProvider

class CustomDBHandler(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION)
{
    companion object
    {
        private val DB_NAME = "exerciseDB";
        private val DB_VERSION = 1;
        val TABLE_NAME = "tblExercises";
    }

    val contentResolver: ContentResolver; //Provides access to content for activity.

    init
    {
        contentResolver = context.contentResolver; //Assigns content resolver once object is instantiated.
    }

    override fun onCreate(db: SQLiteDatabase) //Overridden function to create database table.
    {
        Log.e("pls run me", "pls run me")
        val CREATE_TABLE =
            "CREATE TABLE $TABLE_NAME (name TEXT PRIMARY KEY, focus TEXT, minDuration INTEGER, maxDuration INTEGER, " +
                    "minReps INTEGER, maxReps INTEGER, downtime INTEGER, perSide INTEGER, equipment TEXT, url TEXT)";
        db.execSQL(CREATE_TABLE);
    }

    fun AddExericse(exercise: Exercise) //Function to add new exercise to exercise table.
    {
        val values = ContentValues() //Creates new instance of class to store key/value pairs to place in database table.
        values.put("name", exercise.name);
        values.put("focus", exercise.focus);
        values.put("minDuration", exercise.minDuration);
        values.put("maxDuration", exercise.maxDuration);
        values.put("minReps", exercise.minReps);
        values.put("maxReps", exercise.maxReps);
        values.put("downtime", exercise.downtime);
        values.put("perSide", exercise.perSide);
        values.put("equipment", exercise.equipment);
        values.put("url", exercise.url);
        contentResolver.insert(MyContentProvider.CONTENT_URI, values);
    }

    fun UpdateExericse(exercise: Exercise) //Function to update existing exercise in exercise table.
    {
        val values = ContentValues() //Creates new instance of class to store key/value pairs to place in database table.
        values.put("focus", exercise.focus);
        values.put("minDuration", exercise.minDuration);
        values.put("maxDuration", exercise.maxDuration);
        values.put("minReps", exercise.minReps);
        values.put("maxReps", exercise.maxReps);
        values.put("downtime", exercise.downtime);
        values.put("perSide", exercise.perSide);
        values.put("equipment", exercise.equipment);
        values.put("url", exercise.url);
        val name = arrayOf(exercise.name);
        contentResolver.update(MyContentProvider.CONTENT_URI, values, "name=?", name); //Cursor interfaces with database and gets deletes row.
    }

    fun DeleteExercise(name: String)
    {
        contentResolver.delete(MyContentProvider.CONTENT_URI,"name == '$name'", null); //Cursor interfaces with database and gets deletes row.
    }

    fun GetNames() : ArrayList<String> //Function gets items for list box containing names of exercises in database table.
    {
        var exercises = arrayListOf ("New Exercise"); //Initial value of the list box is the 'new exercise' option.
        val projection = arrayOf("name");
        val cursor = contentResolver.query(MyContentProvider.CONTENT_URI, projection, null, null,null); //Cursor interfaces with database and gets query result data.
        if (cursor != null) //If any results.
        {
            while (cursor.moveToNext()) //Loops while there is unread return data.
            {
                var name = cursor.getString(cursor.getColumnIndex("name"));
                exercises.add(name);
            }
        }
        return exercises;
    }

    fun GetDetails(name: String) : Array<String> //Function gets all details concerning a given exercise name, to populate the form when updating records.
    {
        var res = arrayOf<String>();
        val cursor = contentResolver.query(MyContentProvider.CONTENT_URI,null, "name == '$name'", null,null); //Cursor interfaces with database and gets query result data.
        if (cursor != null) //If any results.
        {
            while (cursor.moveToNext()) //Loops while there is unread return data.
            {
                res += cursor.getString(cursor.getColumnIndex("focus"));
                res += cursor.getString(cursor.getColumnIndex("minDuration"));
                res += cursor.getString(cursor.getColumnIndex("maxDuration"));
                res += cursor.getString(cursor.getColumnIndex("minReps"));
                res += cursor.getString(cursor.getColumnIndex("maxReps"));
                res += cursor.getString(cursor.getColumnIndex("downtime"));
                res += cursor.getString(cursor.getColumnIndex("perSide"));
                res += cursor.getString(cursor.getColumnIndex("equipment"));
                res += cursor.getString(cursor.getColumnIndex("url"));
            }
        }
        return res;
    }

    fun GetAll() : ArrayList<Exercise> //Function gets all details concerning all exercises, in the form of Exercise objects, for the ExerciseSelector class.
    {
        var exerciseList = arrayListOf<Exercise>();
        val cursor = contentResolver.query(MyContentProvider.CONTENT_URI,null, null, null,null); //Cursor interfaces with database and gets query result data.
        if (cursor != null) //If any results.
        {
            while (cursor.moveToNext()) //Loops while there is unread return data.
            {
                val name = cursor.getString(cursor.getColumnIndex("name"));
                val focus = cursor.getString(cursor.getColumnIndex("focus"));
                val minDuration = cursor.getString(cursor.getColumnIndex("minDuration")).toInt();
                val maxDuration = cursor.getString(cursor.getColumnIndex("maxDuration")).toInt();
                val minReps = cursor.getString(cursor.getColumnIndex("minReps")).toInt();
                val maxReps = cursor.getString(cursor.getColumnIndex("maxReps")).toInt();
                val downtime = cursor.getString(cursor.getColumnIndex("downtime")).toInt();
                val perSide = cursor.getString(cursor.getColumnIndex("perSide")).toInt();
                val equipment = cursor.getString(cursor.getColumnIndex("equipment"));
                val url = cursor.getString(cursor.getColumnIndex("url"));
                val exercise = Exercise(name, focus, minDuration, maxDuration, minReps, maxReps, downtime, perSide, equipment, url);
                exerciseList.add(exercise);
            }
        }
        return exerciseList;
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) //Function when database is updated to new version. Drops table and rebuilds it.
    {
        val DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
        db.execSQL(DROP_TABLE);
        onCreate(db);
    }
}