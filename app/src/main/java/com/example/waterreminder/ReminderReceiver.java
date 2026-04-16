package com.example.waterreminder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import android.app.AlarmManager;
import android.app.PendingIntent;

public class ReminderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        // 🔔 Show Notification
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, "WATER_CHANNEL")
                        .setSmallIcon(android.R.drawable.ic_dialog_info)
                        .setContentTitle("Drink Water 💧")
                        .setContentText("Time to drink water!")
                        .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        manager.notify(1, builder.build());

        // ⏱ Get interval (default 15 min)
        long interval = intent.getLongExtra("interval", 15 * 60 * 1000);

        // 🔁 Schedule next reminder
        AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent nextIntent = new Intent(context, ReminderReceiver.class);
        nextIntent.putExtra("interval", interval);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                nextIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        long nextTime = System.currentTimeMillis() + interval;

        if (alarmManager != null) {
            alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    nextTime,
                    pendingIntent
            );
        }
    }
}