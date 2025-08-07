package com.example.mainhabit

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.util.Calendar


fun scheduleReminders(context: Context, habitReminders: List<HabitReminder>) {
    Log.d("HabitReminder",habitReminders.toString())
    for (habit in habitReminders) {
        for (day in habit.daysInWeek) {
            scheduleWeeklyReminder(
                context = context,
                habitId = habit.taskId,
                title = habit.taskName,
                dayOfWeek = day,
                hour = habit.time.hour,
                minute = habit.time.minute
            )
        }
    }
}

fun scheduleChecklistReminders(context: Context, checklistReminders: List<ChecklistReminder>) {
    Log.d("ChecklistReminder",checklistReminders.toString())
    for (habit in checklistReminders) {
        for (day in habit.daysInWeek) {
            scheduleWeeklyReminder(
                context = context,
                habitId = habit.id,
                title = habit.task,
                dayOfWeek = day,
                hour = habit.time.hour,
                minute = habit.time.minute
            )
        }
    }
}


@RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
private fun scheduleWeeklyReminder(
    context: Context,
    dayOfWeek: Int,
    habitId:String,
    title:String,
    hour: Int,
    minute: Int) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    val intent = Intent(context, AlarmReceiver::class.java).apply {
        putExtra("habitId", habitId)
        putExtra("title", title)
        putExtra("dayOfWeek", dayOfWeek)
        putExtra("hour", hour)
        putExtra("minute", minute)
    }

    val requestCode = habitId.hashCode()*10 + dayOfWeek
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        requestCode,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val calendar = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_WEEK, dayOfWeek)
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)

        if (before(Calendar.getInstance())) {
            add(Calendar.WEEK_OF_YEAR, 1)
        }
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
        context.startActivity(intent)
        return
    }
    alarmManager.setExactAndAllowWhileIdle(
        AlarmManager.RTC_WAKEUP,
        calendar.timeInMillis,
        pendingIntent
    )
}

fun cancelHabitAlarms(context: Context, habitId: String, daysOfWeek: List<Int>) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    Log.d("Cancel Alarm",habitId)
    for (day in daysOfWeek) {
        val intent = Intent(context, AlarmReceiver::class.java)

        val requestCode = habitId.hashCode() * 10 + day

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
    }
}

class AlarmReceiver : BroadcastReceiver() {
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "Reminder"
        val habitId = intent.getStringExtra("habitId") ?: return
        val dayOfWeek = intent.getIntExtra("dayOfWeek", -1)
        val hour = intent.getIntExtra("hour", 9)
        val minute = intent.getIntExtra("minute", 0)


        NotificationUtils.showNotification(context, title,"You have a schedule for $title")

        scheduleWeeklyReminder(context, dayOfWeek, habitId, title, hour, minute)
    }
}


object NotificationUtils {
    private const val CHANNEL_ID = "habit_channel_id"

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showNotification(context: Context, title: String, message: String) {
        Log.d("Notification","Showing Notification")
        createNotificationChannel(context)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.habit_tracker_logo)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        NotificationManagerCompat.from(context).notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun createNotificationChannel(context: Context) {
        val name = "Habit Reminders"
        val descriptionText = "Notifications for habit reminders"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}