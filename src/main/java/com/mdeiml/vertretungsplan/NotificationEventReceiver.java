package com.mdeiml.vertretungsplan;

import android.support.v4.content.WakefulBroadcastReceiver;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class NotificationEventReceiver extends WakefulBroadcastReceiver {

    private static final String ACTION_START = "ACTION_START";
    private static final String ACTION_STOP = "ACTION_STOP";

    public static void setupAlarm(Context context, int timer) {
        SharedPreferences pref = context.getSharedPreferences("com.mdeiml.vertretungsplan.Einstellungen", context.MODE_PRIVATE);
        if(!pref.getBoolean("notifications", false))
            return;
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent alarmIntent = getStartIntent(context);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + timer * 60000, AlarmManager.INTERVAL_FIFTEEN_MINUTES, alarmIntent);
    }
    
    public static void stopAlarm(Context context) {
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent alarmIntent = getStartIntent(context);
        alarmManager.cancel(alarmIntent);
    }

    public static PendingIntent getStartIntent(Context context) {
        Intent intent = new Intent(context, NotificationEventReceiver.class);
        intent.setAction(ACTION_START);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static PendingIntent getStopIntent(Context context) {
        Intent intent = new Intent(context, NotificationEventReceiver.class);
        intent.setAction(ACTION_STOP);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Intent serviceIntent = null;
        if(action.equals(ACTION_START)) {
            serviceIntent = NotificationService.createStartIntent(context);
        }else if(action.equals(ACTION_STOP)) {
            serviceIntent = NotificationService.createStopIntent(context);
        }

        if(serviceIntent != null) {
            startWakefulService(context, serviceIntent);
        }
    }

}
