package com.example.exercisetimer


import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.*
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_run_timer.*


class RunTimerActivity : AppCompatActivity()
{

    var timerService:TimerService? = null; //Service that runs timer in background.
    var isBound = false; //Holds if the timerService is currently bound to the activity.
    var timeController = TimeController(); //Object holds functions that control conversions to/from time in seconds.
    var selector:ExerciseSelector? = null; //Object that generates exercises and returns them as a string when called when break time begins.
    var notificationManager: NotificationManager? = null; //Contains useful properties and methods concerning notifications.
    var baseBackroundColour : Int ?= null; //Holds default background colour for easy reference when it is changed later in the activity's runtime.
    override fun onCreate(savedInstanceState: Bundle?)
    {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_run_timer)
        val prefHolder = PreferenceHolder(this); //Contains settings.
        constraintLayout.setBackgroundColor(prefHolder!!.GetColour());
        lblExercises.setMovementMethod(ScrollingMovementMethod()); //Makes the box displaying exercises scrollable.
        selector = ExerciseSelector(this, prefHolder);
        baseBackroundColour = prefHolder.GetColour();

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager; //Creates notificiation channel as soon as activity begins.

        CreateNotificationChannel("com.example.exercisetimer.timer", "Timer", "Timer Info"); //Call function to create a basic notification channel for SDK 26 and higher.


        var workTime = intent.getIntExtra("workTime", 1200); //Get input work and break time from intent.
        var breakTime = intent.getIntExtra("breakTime", 120);

        lblTimeNum.setText(timeController.ConvertToDisplayTime(workTime));
        lblTotalNum.setText(timeController.ConvertToDisplayTime(0));

        var intent = Intent(this, TimerService::class.java) //Create intent to pass initial data to timer service.
        intent.putExtra("workTime", workTime); //Append work and break time to the intent to be received by timer activity.
        intent.putExtra("breakTime", breakTime);
        intent.putExtra("reminderSoundName", prefHolder.reminder.toString().toLowerCase());
        startService(intent);

        val filter = IntentFilter("timer");
        registerReceiver(receiver,filter); //Register broadcast receiver to only listen to broadcast messages with the 'timer' action (aka from the timer service).
    }

    private fun CreateNotificationChannel(id: String, name: String, description: String) //Create a notification channel so that notification work on SDK 26 or higher.
    {
        if (Build.VERSION.SDK_INT >= 26) //Notification channels are necessary on SDK 26 and higher.
        {
            val importance = NotificationManager.IMPORTANCE_LOW;
            val channel = NotificationChannel(id, name, importance)

            channel.description = description
            notificationManager?.createNotificationChannel(channel) //Create channel via notification manager object.
        }

    }

    val receiver = object : BroadcastReceiver() //Receiver waits for the timer service to send time or state update.
    {
        override fun onReceive(contxt: Context, intent: Intent) //When an update is receieved.
        {
            val broadcastType = (intent.getStringExtra("type"));
            if (broadcastType == "timeUpdate") //If receiving time update from the timer, update the labels that display time.
            {
                lblTimeNum.setText(timeController.ConvertToDisplayTime(intent.getIntExtra("time", 100)));
                lblTotalNum.setText(timeController.ConvertToDisplayTime(intent.getIntExtra("total", 100)));
                Log.e("receieved", intent.getIntExtra("time", 100).toString());
            }
            else if (broadcastType == "stateChange") //If being informed that the state of the timer has changed.
            {
                val state = intent.getStringExtra("state");
                if (state == "work")
                {
                    constraintLayout.setBackgroundColor(baseBackroundColour!!);
                    lblTime.setText("Work: ")
                    lblExercises.setText("");
                    lblExercises.visibility = View.INVISIBLE;
                }
                else if (state == "break")
                {
                    btnInteract.setText("Pause");
                    btnInteract.setBackgroundResource(R.drawable.button_orange_border);
                    lblTime.setText("Break: ")
                    val exerciseText = selector!!.GetExercises(timerService!!.breakTime); //Gets exercises from the exercise generator object for the given break time and the settings in the preference holder object.
                    lblExercises.setText(exerciseText);
                    lblExercises.visibility = View.VISIBLE;
                }
                else if (state == "wait")
                {
                    constraintLayout.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.colourLightOrange)); //Background colour is orange during breaks.
                    btnInteract.setText("Continue");
                    btnInteract.setBackgroundResource(R.drawable.button_green_border);
                }
            }
        }
    }

    fun ButtonClick(view: View) //Button for pausing/beginning exercises, depending on timer service state.
    {
        val buttonText = btnInteract.text;
        if (buttonText == "Continue")
        {
            timerService!!.BeginState("break");
            lblTimeNum.setText(timeController.ConvertToDisplayTime(timerService!!.breakTime));
        }
        else if (buttonText == "Pause")
        {
            timerService!!.paused = true;
            btnInteract.setText("Resume");
        }
        else if (buttonText == "Resume")
        {
            timerService!!.paused = false;
            btnInteract.setText("Pause");
        }
    }

    val conn = object : ServiceConnection //Connection object that holds methods that control the binding and unbinding of the activity to the timer service.
    {
        override fun onServiceConnected(className: ComponentName, service: IBinder)
        {
            val binder = service as TimerService.LocalBinder; //Creates binder to get service.
            timerService = binder.GetService(); //Get service from binder, so that properties/methods can be referenced directly.
            isBound = true; //When timer is connected, marked as not bound.
            Log.e("Bind", "Bound");
        }

        override fun onServiceDisconnected(name: ComponentName)
        {
            isBound = false //When timer is disconnected, marked as not bound.
            Log.e("Bind", "Unbound");
        }
    }

    override fun finish() //Cancel timer when leaving activity.
    {
        timerService!!.shutdown = true; //Tell timer service to shut down.
        Log.e("finish", "finish")
        super.finish()
    }

    override fun onDestroy()
    {
        unregisterReceiver(receiver); //Stop listening for broadcasts by unregistering from manifest.
        Log.e("Destroy", "Destroy Activity");
        super.onDestroy()
    }

    override fun onPause() //Function runs when app is paused.
    {
        Log.e("pause", "pause")
        if (isBound)
        {
            unbindService(conn); //Unbind service from activity to prevent errors.
        }
        super.onPause()
    }

    override fun onResume() //Function when app is brought back into the foreground.
    {
        Log.e("resume", "resume");
        var intent = Intent(this, TimerService::class.java) //Create intent to pass initial data to timer service.
        bindService(intent, conn, BIND_AUTO_CREATE); //Rebind service on resuming.
        super.onResume();
    }
}