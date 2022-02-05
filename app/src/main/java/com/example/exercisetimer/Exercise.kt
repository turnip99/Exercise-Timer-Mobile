package com.example.exercisetimer

open class Exercise
{
    public var name: String = ""
    public var focus: String = ""
    public var minDuration: Int = 0
    public var maxDuration: Int = 0
    public var minReps: Int = 0
    public var maxReps: Int = 0
    public var downtime: Int = 0
    public var perSide: Int = 0;
    public var equipment: String = "";
    public var used = false; //Holds if exercise has been used yet by selector object.
    public var url = "";

    constructor() {}

    constructor(name: String, focus: String, minDuration: Int, maxDuration: Int,
                minReps: Int, maxReps: Int, downtime: Int, perSide: Int, equipment: String, url: String)
    {
        this.name = name
        this.focus = focus
        this.minDuration = minDuration
        this.maxDuration = maxDuration
        this.minReps = minReps
        this.maxReps = maxReps
        this.downtime = downtime
        this.perSide = perSide
        this.equipment = equipment
        this.url = url;
    }
}