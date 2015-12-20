package com.mdeiml.vertretungsplan;

import android.content.Context;
import android.content.BroadcastReceiver;
import android.content.Intent;

public class NotificationServiceStartReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationEventReceiver.setupAlarm(context);
    }
}
