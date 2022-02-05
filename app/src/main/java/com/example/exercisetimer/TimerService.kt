package com.example.exercisetimer

import android.R
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.PowerManager.WakeLock
import android.support.v7.app.AppCompatActivity
import java.util.*


class TimerService : Service() //Bound service runs timer in background.
{
    val binder = LocalBinder();
    var state = "work"; //State of the timer (work, break, wait).
    var paused = false; //Holds if the timer is paused.
    var workTime = 0; //Time to work per loop of timer.
    var breakTime = 0; //Time for break per loop of timer.
    var totalTime = 0; //Total time timer has ticked for.
    var timeRemaining = 0;
    var totalExercise = 0;
    var totalDelay = 0;
    var delay = 0;
    var audio = Audio();
    var shutdown = false;
    var reminderSoundName = "";
    var timeController = TimeController(); //Object holds functions that control conversions to/from time in seconds.
    var timer = Timer();
    var wakeLock : WakeLock ?= null;

    var timerTask = object : TimerTask()
    {
        override fun run() //Runs every second, ticking the timer down.
        {
            if (shutdown) //If activity has told service to shut down, stop self.
            {
                stopSelf();
                timer.cancel();
            }
            totalTime+=1;
            if (!paused)
            {
                if (state != "wait")
                {
                    timeRemaining-=1;
                    if (state == "break")
                    {
                        totalExercise += 1; //Increment total time exercising (for records).
                    }
                    if (timeRemaining == 0) //If time remaining = 0, prepare for state change.
                    {
                        var soundName = "major_beep";
                        PlaySound(soundName);
                        when (state)
                        {
                            "work" -> BeginState("wait");
                            "break" -> BeginState("work");
                        }
                    }
                }
                else
                {
                    delay+=1;
                    totalDelay+=1; //Increment total time delaying (for records).
                    if (delay == 60) //Every minute that break time is delayed, play reminder sound.
                    {
                        delay = 0;
                        PlaySound(reminderSoundName);
                    }
                }
                if (timeRemaining == 1 || timeRemaining == 2 || timeRemaining == 3)
                {
                    var soundName = "minor_beep";
                    PlaySound(soundName);
                }
                CreateNotification();
            }
            BroadcastTime(timeRemaining, totalTime); //Sends time to the activity to be displayed.
        }
    }


    override fun onCreate()
    {
        CreateNotification();
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "timer:wakelock");
        wakeLock!!.acquire()
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int
    {
        workTime = intent.getIntExtra("workTime", 1200); //Get input work and break time from intent.
        breakTime = intent.getIntExtra("breakTime", 120);
        reminderSoundName = intent.getStringExtra("reminderSoundName");
        timeRemaining = workTime;

        BeginTimer();

        return Service.START_STICKY;
    }

    override fun onBind(intent: Intent): IBinder //Returns binder object to client activity.
    {
        return binder;
    }

    fun BeginTimer()
    {
        timer.schedule(timerTask,1000, 1000);
    }

    fun BeginState(newState : String) //Functions that occur when a new state is begun, including informing the activity so that UI elements can be updated.
    {
        state = newState;
        if (state == "wait")
        {
            BroadcastStateChange("wait");
            delay = 0;
        }
        else if (state == "work")
        {
            BroadcastStateChange("work");
            timeRemaining = workTime;
        }
        else if (state == "break")
        {
            BroadcastStateChange("break");
            timeRemaining = breakTime;
        }
    }

    fun PlaySound(soundName: String) //Function to play sound for timer, since timer doesn't have context.
    {
        var sound = this.resources.getIdentifier(soundName, "raw", this.packageName);
        audio.Play(sound, this);
    }

    inner class LocalBinder: Binder() //The binder class is used to communicate between client activity and service.
    {
        fun GetService(): TimerService //Function gets the service object for the client activity so that it can directly interface with its activities.
        {
            return this@TimerService;
        }
    }

    fun BroadcastTime(time: Int, total:Int) //Broadcast event to the activity giving updated time so that UI can be updated.
    {
        var intent = Intent();
        intent.putExtra("time", time);
        intent.putExtra("total", total);
        intent.putExtra("type", "timeUpdate")
        intent.action = "timer"; //Action matches the intent filter for the broadcast listening in the activity.
        intent.flags = Intent.FLAG_INCLUDE_STOPPED_PACKAGES;
        sendBroadcast(intent);
    }

    fun BroadcastStateChange(state:String) //Broadcast event to the activity notifying it of a state change, so that UI can be updated.
    {
        var intent = Intent();
        intent.putExtra("state", state);
        intent.putExtra("type", "stateChange")
        intent.action = "timer"; //Action matches the intent filter for the broadcast listening in the activity.
        intent.flags = Intent.FLAG_INCLUDE_STOPPED_PACKAGES;
        sendBroadcast(intent);
    }

    override fun onDestroy()
    {
        wakeLock!!.release(); //Wake lock is released when service is destroyed, to save battery.
        SaveRecords();
        super.onDestroy()
    }

    override fun onTaskRemoved(rootIntent: Intent) //Stop service when app is killed by the user.
    {
        stopSelf()
    }

    fun SaveRecords() //Function for updating records.
    {
        val recordHolder = RecordHolder(this); //Holds records in the form of shared preferences and allows them to be updated.
        val records = getSharedPreferences("records", AppCompatActivity.MODE_PRIVATE); //Get reference to shared preferences.
        var editor = records.edit();
        var exerciseToday = recordHolder.exerciseDayToday + totalExercise;
        var delayToday = recordHolder.delayToday + totalDelay;
        editor.putInt("exerciseDayToday", exerciseToday);
        editor.putInt("delayToday", delayToday);
        if (exerciseToday > recordHolder.exerciseDayBest)
        {
            editor.putInt("exerciseDayBest", exerciseToday); //If new record for most exercise, save this in the 'best' record.
        }
        if (delayToday > recordHolder.delayBest)
        {
            editor.putInt("delayBest", delayToday); //If new record for most delay, save this in the 'best' record.
        }

        var exerciseWeek = recordHolder.exerciseWeekThis + totalExercise;
        editor.putInt("exerciseWeekThis", exerciseWeek);
        if (exerciseWeek > recordHolder.exerciseWeekBest)
        {
            editor.putInt("exerciseWeekBest", exerciseWeek); //If new record for weekly exercise save this in the 'best' record.
        }

        editor.commit();
    }

    fun CreateNotification() //Function for building the notification that displays when the timer service is running. This updates every time the timer is incremented.
    {
        val notificationIntent = Intent(this, RunTimerActivity::class.java) //Intent needed for a foreground service notification.

        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)

        var notification : Notification ?= null;

        var title = "";
        var text = "";

        when (state) //When statement to define the title of the notification.
        {
            "work" -> title = "Work time...";
            else -> title = "Break time..."
        }
        when (state) //When statement to define the text of the notification.
        {
            "wait" -> title = "Return to app to begin exercises.";
            else -> text = timeController.ConvertToDisplayTime(timeRemaining);
        }

        if (Build.VERSION.SDK_INT >= 26) //If SDK is 26 or newer, notification channel is needed.
        {
            val channelID = "com.example.exercisetimer.timer";
            notification = Notification.Builder(this, channelID) //Build simple notification using notification channel.
                .setSmallIcon(R.drawable.ic_lock_idle_alarm)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(pendingIntent).build()
        }
        else
        {
            notification = Notification.Builder(this) //Build simple notification.
                .setSmallIcon(R.drawable.ic_lock_idle_alarm)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(pendingIntent).build();
        }
        startForeground(1337, notification); //Start service in foreground with notification (to prevent android killing it as a background service)
    }
}