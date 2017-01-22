package com.fabienma.sessions;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Instrumentation;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

public class EndSession extends AppCompatActivity {
    // Scheduled notifications are sent with id 0;


    static boolean inSession = false;
    static boolean locked = false;
    int notifNumber;
    int sLengthMin;
    int bLengthSec;
    String sessionName;
    String notifType;
    int sessionId;
    Handler notifHandler;
//    ComponentName compName;

//    private PowerManager mPowerManager;
//    private PowerManager.WakeLock mWakeLock;


    public void turnOffScreen(int val) {
        // turn off screen
//        mWakeLock = mPowerManager.newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, "tag");
//        mWakeLock.acquire();
//
        if (val == 0) {
            Intent i = new Intent(getApplicationContext(), EndSession.class);
            i.putExtra("methodToRun", "turnOffScreen");
            startActivity(i);
        } else {
            new Thread(new Runnable() {
                @Override
                public void run() {
//                    findViewById(R.id.endSessionButton).requestFocus();
//                    Instrumentation inst = new Instrumentation();
//
//                    inst.sendKeyDownUpSync(KeyEvent.KEYCODE_POWER);

//                    ((DevicePolicyManager)getSystemService(Context.DEVICE_POLICY_SERVICE)).lockNow();

                }
            }).start();

        }
    }

    private void getSessionInfo() {

        AllSessions.sessionDB = openOrCreateDatabase("Sessions", MODE_PRIVATE, null);

        Cursor c = AllSessions.sessionDB.rawQuery("SELECT * FROM sessions WHERE id = " + sessionId, null);

        int sessionNameIndex = c.getColumnIndex("sessionName");
        int sessionLengthIndex = c.getColumnIndex("sessionLength");
        int breakLengthIndex = c.getColumnIndex("breakLength");
        int notifTypeIndex = c.getColumnIndex("notifType");

        if (c.moveToFirst()) {
            sLengthMin = Integer.parseInt(c.getString(sessionLengthIndex));
            bLengthSec = Integer.parseInt(c.getString(breakLengthIndex));
            sessionName = c.getString(sessionNameIndex);
            notifType = c.getString(notifTypeIndex);
        }

        AllSessions.sessionDB.close();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end_session);

        inSession = true;
        notifNumber = 1;
        Intent i = getIntent();
        sessionId = i.getIntExtra("sessionId", -1);

        notifHandler = new Handler();

        getSessionInfo();
        // all important information has been received
        if (i.getStringExtra("methodToRun") == null) {
//            Log.i("notifType", notifType);
//            if(notifType.equals("Turn off screen")) {
//                Log.i("In condition", "uh yes");
//                compName = new ComponentName(this, EndSession.class);
//
//                Intent intent = new Intent(DevicePolicyManager
//                        .ACTION_ADD_DEVICE_ADMIN);
//                intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,
//                        compName);
//                intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
//                        "This is to allow Sessions to lock your screen when time is up!");
//                startActivity(intent);
//                startActivityForResult(intent, RESULT_OK);
//            }

            registerReceiver(new PhoneUnlockedReceiver(sessionId), new IntentFilter("android.intent.action.USER_PRESENT"));
            registerReceiver(new PhoneUnlockedReceiver(sessionId), new IntentFilter("android.intent.action.SCREEN_OFF"));
            startSession();
        } else if (i.getStringExtra("methodToRun").equals("cancelNotif")) {
            notifHandler.removeCallbacks(null);

            Log.i("callBacks", "removed");
        } else {
            if (notifType == null) {
                // probably turn screen off
                turnOffScreen(1);
            } else if (notifType.equals("Notification")) {
                if (notifType.equals("Notification")) {
                    notifHandler.postDelayed(
                            new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(EndSession.this,
                                            "Time's Up!",
                                            Toast.LENGTH_LONG).show();
                                }
                            }, bLengthSec * 1000);
                } else if (notifType.equals("Toast")) {
                    notifHandler.postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            Toast.makeText(EndSession.this,
                                    "Time's Up!",
                                    Toast.LENGTH_LONG).show();
                        }
                    }, bLengthSec * 1000);
                } else if (notifType.equals("Turn off screen")) {
                    notifHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            turnOffScreen(0);
                        }
                    }, bLengthSec * 1000);
                }
            }

        }
    }

    public void endSession(View view) {
        new AlertDialog.Builder(EndSession.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Are you sure?")
                .setMessage("Are you sure you want to end your \"" + sessionName + "\" session early?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        inSession = false;
                        Intent i = new Intent(EndSession.this, MainActivity.class);

                        startActivity(i);
                        Toast.makeText(EndSession.this, "Ending!", Toast.LENGTH_SHORT).show();

                    }
                })
                .setNegativeButton("Not yet", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .show();
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.i("NewIntent", "received");

        if (intent.getStringExtra("methodToRun") == null) {
            return;
        } else if (intent.getStringExtra("methodToRun").equals("endSessionTime")) {
            endSessionTime();
        } else if (intent.getStringExtra("methodToRun").equals("cancelNotif")) {
            notifHandler.removeCallbacks(null);
            Log.i("NotifHandler", "called back");
        } else if (intent.getStringExtra("methodToRun").equals("sendNotif")) {

            if (notifType.equals("Notification")) {
                notifHandler.postDelayed(
                        new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(EndSession.this,
                                        "Time's Up!",
                                        Toast.LENGTH_LONG).show();
                            }
                        }, bLengthSec * 1000);

//                scheduleNotification(getNotification("Time's Up!"), bLengthSec * 1000);
            } else if (notifType.equals("Toast")) {
                notifHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        PendingIntent pIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);

                        Notification notification = new Notification.Builder(getApplicationContext())
                                .setContentTitle("Time's Up!")
                                .setContentIntent(pIntent)
                                .setSmallIcon(android.R.drawable.sym_def_app_icon)
                                .build();

                        NotificationManager notifManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

                        notifManager.notify(1, notification);
                    }
                }, bLengthSec * 1000);
            } else if (notifType.equals("Turn of screen")) {
                notifHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        new Handler().postDelayed(new Runnable() {

                            @Override
                            public void run() {
                                turnOffScreen(0);
                            }
                        }, bLengthSec * 1000);
                    }
                }, bLengthSec * 1000);
            }
        }
        else if (intent.getStringExtra("methodToRun").equals("turnOffScreen")) {
            turnOffScreen(1);
        }
    }

    private void scheduleNotification(Notification notification, int delay) {
        notifNumber++;
        Log.i("Notification", "scheduled");
        Intent notificationIntent = new Intent(this, NotificationPublisher.class);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, notifNumber);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION, notification);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, notifNumber, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        long futureInMillis = SystemClock.elapsedRealtime() + delay;
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent);
    }

    private Notification getNotification(String content) {
        Notification.Builder builder = new Notification.Builder(this);
        builder.setContentTitle("Sessions");
        builder.setContentText(content);
        ;
        builder.setSmallIcon(android.R.drawable.sym_def_app_icon);
        builder.setPriority(Notification.PRIORITY_HIGH);
        builder.setDefaults(Notification.DEFAULT_ALL);
        return builder.build();
    }

    private void startSession() {
        if (inSession) {
            Intent endIntent = new Intent(EndSession.this, EndSession.class);
            endIntent.putExtra("methodToRun", "endSessionTime");

            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, endIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            alarmManager.set(AlarmManager.ELAPSED_REALTIME, sLengthMin * 60000, pendingIntent);

            if (notifType == null) {
                Log.i("notifType", "None");
            } else if (notifType.equals("Toast")) {
                Log.i("notifType", "Toast");
            } else if (notifType.equals("Notification")) {
                // TODO: Figure out how to "un-schedule" notif it when it's re-locked
                Log.i("notifType", "Notification");
//                scheduleNotification(getNotification("Time's up!"), bLengthSec * 1000);
            } else if (notifType.equals("Turn off screen")) {
                Log.i("notifType", "Turn off screen");
            }
        }
    }

    private void endSessionTime() {
        inSession = false;
        Intent i = new Intent(EndSession.this, MainActivity.class);

        startActivity(i);
        Toast.makeText(EndSession.this, "Congrats!", Toast.LENGTH_SHORT).show();
    }


}
