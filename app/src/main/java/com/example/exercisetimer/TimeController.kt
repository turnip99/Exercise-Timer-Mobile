package com.example.exercisetimer

class TimeController //Class controls conversions between time and strings for the generate exercises and timer activities.
{
    fun ConvertToSeconds(text:String):Int //Converts a given time as a string (in a variety of formats) and converts it into a time in seconds.
    {
        if (text.toIntOrNull() != null)
        {
            if (text.toInt() > 59940)
            {
                return 0;
            }
            return text.toInt(); //If valid int, assume value in seconds.
        }
        else
        {
            var beforeColon = "";
            var afterColon = "";
            var stage = 1;
            for (char in text) //Iterate through the string, character by character.
            {
                if (stage == 1) //If before colon.
                {
                    if (char.toString().toIntOrNull() != null)
                    {
                        beforeColon+=char; //If valid number, append.
                    }
                    else if (char == ':')
                    {
                        stage = 2; //Else, if colon, set stage to 2 (after colon).
                    }
                    else
                    {
                        return 0; //If not number of colon, invalid, thus return 0.
                    }
                }
                else if (stage == 2) //If before colon.
                {
                    if (char.toString().toIntOrNull() != null)
                    {
                        afterColon+=char; //If valid number, append.
                    }
                    else
                    {
                        return 0; //If not number of colon, invalid, thus return 0.
                    }
                }
            }
            if (afterColon.length != 2 || afterColon.toInt() > 60)
            {
                return 0; //If the value after the colon (the seconds) does not equal 2 characters, or is not less than 60, failure.
            }
            var time = 60*beforeColon.toInt()+afterColon.toInt(); //Calculates full time in seconds.
            return time;
        }
    }

    fun ConvertToDisplayTime(seconds:Int):String //Converts a given time in seconds into a string to be displayed in a text field.
    {
        var time = "";
        time += (seconds/60).toString(); //Adds minutes before colon.
        time+=":";
        var secondsStr = (seconds%60).toString(); //Gets number of remaining seconds as a string.
        if (secondsStr.length != 2) //If seconds is a single digit number, append a 0 to the left.
        {
            secondsStr = "0" + secondsStr;
        }
        time+=secondsStr; //Appends seconds to display time.
        return time;
    }
}