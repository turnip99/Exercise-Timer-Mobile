package com.example.exercisetimer

import android.content.Context
import kotlin.random.Random


class ExerciseSelector //Class holds valid exercises and returns exercise routine to activities when requested.
{
    var exerciseList = arrayListOf<Exercise>();
    var specificList = arrayListOf<ExerciseSpecific>();

    constructor(context: Context, prefHolder: PreferenceHolder)
    {
        var dbHandler = CustomDBHandler(context);
        val tempExerciseList = dbHandler.GetAll();
        for (exercise in tempExerciseList)
        {
            var valid = true; //Holds if exercise is valid for current settings.
            if (prefHolder.focus != "All") //If current setting is that user wants exercises for all focus areas, all exercises are valid for this check.
            {
                if (exercise.focus != prefHolder.focus && exercise.focus != "Whole Body") //If not the same focuses, and not an exercise for the whole body, not valid.
                {
                    valid = false;
                }
            }
            val equipment = exercise.equipment;
            if (equipment != "None") //If exercise needs no equipment, it must be valid for this check.
            {
                if (equipment == "Ball" && !prefHolder.ball || equipment == "Band" && !prefHolder.band ||
                    equipment == "Box" && !prefHolder.box || equipment == "Weights" && !prefHolder.weights) //If necessary equipment is not available, not valid.
                {
                    valid = false;
                }
            }
            if (valid) //If valid after checks, append to new list of valid exercises.
            {
                exerciseList.add(exercise);
            }
        }
    }

    var time = 0; //These temporary variables are used in the selection algorithm to hold variables for comparisons and checks.
    var reps = 0;
    var totalTime = 0; //Total time for individual exercise.

    fun GetExercises(timerTime: Int): String
    {
        if (exerciseList.size == 0)
        {
            return "No exercises could be found suitable for the current settings."
        }
        else
        {
            var fail: Boolean;
            var totalTimeAll: Int;
            var exercise: Exercise? = null;
            do
            {
                fail = false;
                specificList.clear(); //Clears specific list from previous attempt.
                for (exercise in exerciseList)
                {
                    exercise.used = false; //Ensures every exercise can be used again.
                }
                totalTimeAll = 0; //Total time of all exercises.
                var failCount = 0; //Holds number of times algorithm has failed.
                do //Loops to get initial exercise (this is special as the algorithm will go through extra effort to find at least one exercise, which may be necessary with small timer durations).
                {
                    failCount += 1;
                    if (failCount <= 30) //If failed 30 times, more careful algorithm is used to find at least a single exercise that fits within the time frame.
                    {
                        exercise = GetRandomExercise();
                    } else if (failCount == 31)
                    {
                        var nothingFound = true;
                        exerciseList.shuffle(); //Shuffles list so that the same exercise will not always come out first.
                        for (ex in exerciseList)
                        {
                            if (nothingFound)
                            {
                                var minTotalTime =
                                    (ex.minDuration + ex.downtime) * ex.minReps * (ex.perSide + 1); //Minimum possible time for an instance of this exercise.
                                if (!ex.used && (minTotalTime <= timerTime)) //If exercise has not yet been used, and the minimum total time taken is less than the available time remaining.
                                {
                                    time = ex.minDuration; //Start with minimum reps/time, before working up.
                                    reps = ex.minReps;
                                    totalTime = minTotalTime;
                                    while (totalTime <= timerTime) //Increase reps as many times as possible.
                                    {
                                        reps += 1;
                                        totalTime = (time + ex.downtime) * reps * (ex.perSide + 1);
                                    }
                                    reps -= 1; //Decrement reps by 1 to get highest legal number of reps.
                                    totalTime = (time + ex.downtime) * reps * (ex.perSide + 1);
                                    nothingFound = false;
                                    exercise = ex;
                                }
                            }
                        }
                        if (nothingFound) //If nothing found, return error message.
                        {
                            return "No exercises could be found suitable for this time.";
                        }
                    }
                } while (totalTime > timerTime && failCount <= 30);
                totalTimeAll += totalTime; //Adds time of new exercise to total time and appends 5 seconds rest.
                specificList.add(ExerciseSpecific(exercise!!.name, exercise!!.focus, exercise!!.minDuration, exercise!!.maxDuration,
                    exercise!!.minReps, exercise!!.maxReps, exercise!!.downtime, exercise!!.perSide, exercise!!.equipment, exercise!!.url, time, reps, totalTime));
                MarkAsComplete(exercise!!.name);
                failCount = 0;
                if (exerciseList.size == 1)
                {
                    exerciseList[0].used = false; //If list consists of only 1 exercise, mark as unused so it can be used again when looking for more exercises.
                }
                while (!GetIfTimeIsClose(totalTimeAll, timerTime) && failCount < 30) //While the total time is not high enough and it has not given up yet.
                {
                    exercise = GetRandomExercise();
                    while (totalTimeAll + totalTime > timerTime && failCount < 30) //Attempt up to 30 times to find an exercise that fits within the remaining time slot.
                    {
                        exercise = GetRandomExercise();
                        failCount++;
                    }
                    if (failCount < 30) //If successfully found an exercise that fits within time slot.
                    {
                        failCount = 0;
                        specificList.add(ExerciseSpecific(exercise!!.name, exercise!!.focus, exercise!!.minDuration, exercise!!.maxDuration,
                            exercise!!.minReps, exercise!!.maxReps, exercise.downtime, exercise!!.perSide, exercise!!.equipment, exercise!!.url, time, reps, totalTime));
                        MarkAsComplete(exercise!!.name);
                        totalTimeAll += (totalTime + 5); //Adds new exercise's time (plus 5 seconds rest) to total time.
                        if (AllUsed())
                        {
                            for (ex in exerciseList)
                            {
                                ex.used = false; //If all exercises in list have been marked as used, refresh so that they can all be used again.
                            }
                        }
                    }
                }
                //Everything else below (apart from the string builder) is for optimising the exercises to fill the time slot better.
                if (!GetIfTimeIsClose(totalTimeAll, timerTime)) //If not yet within acceptable time, find smallest exercise by time and try to increase reps slowly.
                {
                    var smallExercise = specificList[0];
                    var smallSize = specificList[0].time;
                    var smallIndex = 0;
                    var index = 0;
                    for (ex in specificList)
                    {
                        if (ex.time < smallSize && ex.reps < ex.maxReps)
                        {
                            smallExercise = ex; //Gets smallest exercise not at max reps.
                            smallIndex = index;
                        }
                        index += 1;
                    }
                    var extraReps = 0; //Counts how many extra reps can be added without going over the total.
                    while (totalTimeAll + ((smallExercise.time + smallExercise.downtime) * extraReps * (smallExercise.perSide + 1)) < timerTime + (2 + time / 60)
                        && smallExercise.reps + extraReps < smallExercise.maxReps)
                    {
                        extraReps++; //Increment extra reps until they get too high or they hit the max number of reps.
                    }
                    if (extraReps > 0)
                    {
                        specificList[smallIndex].reps += extraReps; //Append new reps.
                        specificList[smallIndex].timeTotal += (smallExercise.time + smallExercise.downtime) * extraReps * (smallExercise.perSide + 1);
                        totalTimeAll =
                            RecalculateTotalTimeAll(); //Calculate new total time now that exercise has been modified.
                    }
                }
                if (!GetIfTimeIsClose(totalTimeAll, timerTime)
                ) //If not yet within acceptable time, find smallest exercise by reps and try to increase time slowly.
                {
                    var smallExercise = specificList[0];
                    var smallSize = specificList[0].reps;
                    var smallIndex = 0;
                    var index = 0;
                    for (ex in specificList)
                    {
                        if (ex.reps < smallSize && ex.time < ex.maxDuration)
                        {
                            smallExercise = ex; //Gets smallest exercise not at max time.
                            smallIndex = index;
                        }
                        index += 1;
                    }
                    var extraTime = 0; //Counts how much extra duration can be added without going over the total.
                    while (totalTimeAll + (smallExercise.reps * extraTime * (smallExercise.perSide + 1)) < timerTime + (2 + time / 60)
                        && smallExercise.time + extraTime < smallExercise.maxDuration
                    )
                    {
                        extraTime++; //Increment extra reps until they get too high or they hit the max number of reps.
                    }
                    if (extraTime > 0)
                    {
                        specificList[smallIndex].time += extraTime; //Append new time.
                        specificList[smallIndex].timeTotal += smallExercise.reps * extraTime * (smallExercise.perSide + 1);
                        totalTimeAll =
                            RecalculateTotalTimeAll(); //Calculate new total time now that exercise has been modified.
                    }
                }
                var changed = true; //Holds if any item's time or reps have changed.
                while (totalTimeAll > timerTime + (2 + (time / 60)) && changed) //Loops attempting to reduce anything that may put the time total above the timer length.
                {
                    changed = false;
                    for (ex in specificList) //Checks each exercise in list of specific exercises to find values to reduce.
                    {
                        if (ex.time > ex.minDuration) //If above min duration, reduce by 1.
                        {
                            changed = true;
                            ex.time -= 1;
                            ex.timeTotal -= ex.reps * (ex.perSide + 1);
                            totalTimeAll -= ex.reps * (ex.perSide + 1);
                        }
                        if (totalTimeAll > timerTime + (2 + (time / 60))) //If reached goal of being below threshold, break out of loop to save computation.
                        {
                            break;
                        }
                        if (ex.reps > ex.minReps) //If above min reps, reduce by 1.
                        {
                            changed = true;
                            ex.reps -= 1;
                            ex.timeTotal -= (ex.time + ex.downtime) * (ex.perSide + 1);
                            totalTimeAll -= (ex.time + ex.downtime) * (ex.perSide + 1);
                        }
                        if (totalTimeAll > timerTime + (2 + (time / 60))) //If reached goal of being below threshold, break out of loop to save computation.
                        {
                            break;
                        }
                    }
                }
                totalTimeAll = RecalculateTotalTimeAll(); //Recalculate total time, to be safe.
                if (!GetIfTimeIsClose(totalTimeAll, timerTime) || totalTimeAll > timerTime + (2 + timerTime / 60))
                {
                    fail = true; //If, after all, the resulting exercises are still not within the correct time threshold, mark as complete fail and try again.
                }
            } while (fail) //Function iterates until not complete fail.
            var builder = StringBuilder(); //Builder to put together text to be displayed to user concerning exercises.
            var i = 0; //Keeps count of index in array so that it know when on the last item.
            for (exercise in specificList)
            {
                i++;
                builder.append(exercise.name + " (" + exercise.time + "s) x" + exercise.reps); //Appends the details of the specific exercise to the textview.
                if (exercise.perSide == 1)
                {
                    builder.append(" (each side)"); //If exercise is two-sided, append text.
                }
                if (i < specificList.count())
                {
                    builder.append("\n");
                }
            }
            //builder.append("\nTotal time: " + totalTimeAll + "s.");
            return builder.toString();
        }
    }

    fun GetRandomExercise() : Exercise //Function returns a random exercise from the list that has not been used.
    {
        var exercise:Exercise? = null;
        do
        {
            val index = Random.nextInt(0, exerciseList.count());
            exercise = exerciseList[index];
        } while (exercise!!.used) //Gets random exercise until it finds one that isn't used already.
        time = Random.nextInt(exercise.minDuration, exercise.maxDuration+1); //Gets random duration of exercise.
        reps = Random.nextInt(exercise.minReps, exercise.maxReps+1); //Gets random reps of exercise.
        totalTime = (time + exercise.downtime)*reps; //Calculates total duration of exercise.
        if (exercise.perSide == 1) //If exercise is done on both sides, double total duration.
        {
            totalTime*=2;
        }
        return exercise;
    }

    fun MarkAsComplete(name:String) //Function goes through list of exercises and marks given exercise as complete.
    {
        for (ex in exerciseList)
        {
            if (ex.name == name)
            {
                ex.used = true;
            }
        }
    }

    fun GetIfTimeIsClose(totalTimeAll:Int, timerTime: Int) : Boolean //Checks if the time for the exercises is close enough to the timer time to be acceptable.
    {
        val leeway = 5 + (timerTime/60); //Leeway allowed is 5 + 1 for every minute of the timer.
        if (totalTimeAll > timerTime - leeway) //If within leeway margin of timer time, total time is acceptable.
        {
            return true;
        }
        return false;
    }

    fun AllUsed():Boolean //Function checks if all exercises have been marked as used, to determine if they should be refreshed for use again.
    {
        for (ex in exerciseList)
        {
            if (!ex.used) //if any exercise is unused, return false.
            {
                return false;
            }
        }
        return true;
    }

    fun RecalculateTotalTimeAll():Int //Recalculates the total time of all exercises in specificList, after some modification to exercise(s).
    {
        var total = -5;
        for (ex in specificList)
        {
            total+=(ex.timeTotal + 5)
        }
        return total;
    }
}