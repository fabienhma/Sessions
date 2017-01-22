package com.fabienma.sessions;

import android.app.AlarmManager;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

/**
 * Created by fabienma on 1/19/17.
 */

public class PhoneUnlockedReceiver extends BroadcastReceiver {

    int sessionId;

    public PhoneUnlockedReceiver(int sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        KeyguardManager keyguardManager = (KeyguardManager)context.getSystemService(Context.KEYGUARD_SERVICE);
        if( keyguardManager.inKeyguardRestrictedInputMode()) {
            //it is locked
            Log.i("Locked?", "Yes");
            Intent i = new Intent(context, EndSession.class);
            i.putExtra("methodToRun", "cancelNotif");
            i.putExtra("sessionId", sessionId);

            context.startActivity(i);

        } else {
            //phone was unlocked, do stuff here
            Log.i("Unlocked?", "Yes");

            Intent i = new Intent(context, EndSession.class);
            i.putExtra("methodToRun", "sendNotif");
            i.putExtra("sessionId", sessionId);

            context.startActivity(i);
        }

    }



}
