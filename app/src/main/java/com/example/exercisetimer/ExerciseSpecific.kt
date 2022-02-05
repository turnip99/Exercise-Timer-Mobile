package com.example.exercisetimer

class ExerciseSpecific : Exercise
{
    var time:Int = 0;
    var reps:Int = 0;
    var timeTotal:Int = 0;
    constructor(name: String, focus: String, minDuration: Int, maxDuration: Int,
                minReps: Int, maxReps: Int, downtime: Int, perSide: Int, equipment: String, url:String, time:Int, reps:Int, timeTotal:Int)
            : super(name, focus, minDuration, maxDuration, minReps, maxReps, downtime, perSide, equipment, url)
    {
        this.time = time;
        this.reps = reps;
        this.timeTotal = timeTotal;

    }
}