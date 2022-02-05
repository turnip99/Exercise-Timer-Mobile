package com.example.exercisetimer

import java.text.SimpleDateFormat
import java.util.*

class DateHelper //Class used to hold date-based functions that are used in multiple activities.
{
    val dateFormat = SimpleDateFormat("dd-MM-yyyy");
    fun GetResetDate(day:String) : String //Given a reset day, calculate the date of said reset.
    {
        val now = Calendar.getInstance();
        val dayOfWeek = now.get(Calendar.DAY_OF_WEEK); //Get current day.
        var dayCal = GetCalenderDay(day); //Sets day in a format that the calender object can work with.
        var days = 7; //Default, if same day, wait 1 week before reset.
        if (dayOfWeek != dayCal)
        {
            days = (7-(dayOfWeek - dayCal))%7 //Gets number of days until the next instance of dayCal.
        }
        now.add(Calendar.DAY_OF_YEAR, days); //Adds days to get reset date.
        val date = now.getTime();
        return dateFormat.format(date).toString(); //Returns formatted date.
    }

    fun GetCalenderDay(day:String) : Int //Gets the day in a format recognised by the calender object.
    {
        when (day)
        {
            "Monday" -> return Calendar.MONDAY;
            "Tuesday" -> return Calendar.TUESDAY;
            "Wednesday" -> return Calendar.WEDNESDAY;
            "Thursday" -> return Calendar.THURSDAY;
            "Friday" -> return Calendar.FRIDAY;
            "Saturday" -> return Calendar.SATURDAY;
            else -> return Calendar.SUNDAY;
        }
    }

    fun GetDateDifference (current:String, reset:String) : Int //Gets the difference in days between the current date and the reset date.
    {
        try
        {
            var currentDate = dateFormat.parse(current);
            var resetDate = dateFormat.parse(reset);
            return ((resetDate.time - currentDate.time)/86400000).toInt();
        }
        catch (e : RuntimeException)
        {
            return 0;
        }
    }
}