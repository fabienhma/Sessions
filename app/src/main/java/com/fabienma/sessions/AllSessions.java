package com.fabienma.sessions;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;

public class AllSessions extends AppCompatActivity {
    static SQLiteDatabase sessionDB;
    int placeNumber = -1;
    TextView allSessionsTitle;
    ListView allSessionListView;
    ArrayList<String> sessionNames = new ArrayList<String>();
    ArrayList<Integer> sessionIDs = new ArrayList<Integer>();
    ArrayAdapter<String> allSessionsAdapter;

    @Override
    protected void onResume() {
        super.onResume();

        if (EndSession.inSession) {
            Intent intent = new Intent(AllSessions.this, EndSession.class);
            startActivity(intent);
        }
        sessionDB = openOrCreateDatabase("Sessions", MODE_PRIVATE, null);
        updateSessionListView();
        sessionDB.close();
    }

    public void updateSessionListView() {
        Cursor c = sessionDB.rawQuery("SELECT * FROM sessions", null);

        int sessionNameIndex = c.getColumnIndex("sessionName");
        int sessionIDIndex = c.getColumnIndex("id");

        Log.i("sessionIDIndex", "" + sessionIDIndex);
//            int sessionLengthIndex = c.getColumnIndex("sessionLength");
//            int breakLengthIndex = c.getColumnIndex("breakLength");
//            int notifTypeIndex = c.getColumnIndex("notifType");

        sessionNames.clear();
        sessionIDs.clear();

        if (c.moveToFirst()) {
            do {
                sessionNames.add(c.getString(sessionNameIndex));
                sessionIDs.add(Integer.parseInt(c.getString(sessionIDIndex)));
            } while (c.moveToNext());

        } else {
            sessionNames.add("Pomodoro Technique");
            sessionIDs.add(1);
            sessionDB.execSQL("INSERT INTO sessions (id, sessionName, sessionLength, breakLength, notifType) " +
                    "VALUES (1, 'Pomodoro Technique', 120, 300, 'Toast');");
        }


        allSessionsAdapter.notifyDataSetChanged();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_sessions);

        if (EndSession.inSession) {
            Intent intent = new Intent(AllSessions.this, EndSession.class);
            startActivity(intent);
        }


        allSessionsTitle = (TextView) findViewById(R.id.allSessionTitle);

        Intent i = getIntent();

        placeNumber = i.getIntExtra("placeNumber", -1);

        if (placeNumber == 0) {
            allSessionsTitle.setText("Start Session!");
        } else if (placeNumber == 1) {
            allSessionsTitle.setText("Manage Sessions");
        } else {
            allSessionsTitle.setText("Poop.");
        }
        try {
            sessionDB = openOrCreateDatabase("Sessions", MODE_PRIVATE, null);

            sessionDB.execSQL("CREATE TABLE IF NOT EXISTS sessions (id INTEGER PRIMARY KEY, " +
                    "sessionName VARCHAR, sessionLength INTEGER, breakLength INTEGER, notifType VARCHAR);");

            allSessionListView = (ListView) findViewById(R.id.allSessionListView);

            allSessionsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, sessionNames);

            allSessionListView.setAdapter(allSessionsAdapter);

            updateSessionListView();

            sessionDB.close();

            // sessionLength in minutes; breakLength in seconds
        } catch (Exception e) {
            e.printStackTrace();
        }

        registerForContextMenu(allSessionListView);

        allSessionListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                if (placeNumber == 0) {
                    new AlertDialog.Builder(AllSessions.this)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle("Are you ready?")
                            .setMessage("Do you want to start \"" + sessionNames.get(position) + "\"?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    Intent goToEndScreen = new Intent(AllSessions.this, EndSession.class);
                                    Log.i("sessionID sent", ""+sessionIDs.get(position));
                                    goToEndScreen.putExtra("sessionId", sessionIDs.get(position));
                                    startActivity(goToEndScreen);

                                    //start, for now
                                    //Alarm manager
                                    // get relevant settings
                                    // go to endsession page
                                    // create notification with
                                    // make a new page with just one button - end session

                                }
                            })
                            .setNegativeButton("Not yet", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // do nothing
                                }
                            })
                            .show();
                    // start session confirmation
                } else if (placeNumber == 1) {
                    Intent i = new Intent(AllSessions.this, CreateOrEditSession.class);
                    i.putExtra("sessionName", sessionNames.get(position));
                    i.putExtra("sessionId", sessionIDs.get(position));

                    startActivity(i);
                    // go to CreateOrEditSession, no placeNumber, will need to put sessionName.
                }
            }
        });
    }

    public void createNewSession(View view) {
        Intent intent = new Intent(AllSessions.this, CreateOrEditSession.class);
        intent.putExtra("placeNumber", 0);
        startActivity(intent);
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, v.getId(), 0, "Delete");
        menu.add(0, v.getId(), 0, "Show info");
//        menu.add(0, v.getId(), 0, "Reorder");
//        not yet a priority - check this link out later though. https://github.com/bauerca/drag-sort-listview

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        if (item.getTitle() == "Delete") {
            new AlertDialog.Builder(AllSessions.this)
                    .setIcon(android.R.drawable.ic_delete)
                    .setTitle("Are you sure?")
                    .setMessage("Are you sure you want to delete the session \"" + sessionNames.get(info.position) + "\"?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deleteSession(info.position);
                            Toast.makeText(AllSessions.this, "Deleted!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // do nothing
                        }
                    })
                    .show();
            // ask if sure. If so, delete!
        } else if (item.getTitle() == "Show info") {

            sessionDB = openOrCreateDatabase("Sessions", MODE_PRIVATE, null);
            Log.i("id", ""+sessionIDs.get(info.position));

            Cursor c = sessionDB.rawQuery("SELECT * FROM sessions WHERE id = " + sessionIDs.get(info.position), null);

            int sessionLengthIndex = c.getColumnIndex("sessionLength");
            int breakLengthIndex = c.getColumnIndex("breakLength");
            int notifTypeIndex = c.getColumnIndex("notifType");

            String sLengthMin = "";
            String bLengthSec = "";
            String notifType = "";

            if (c.moveToFirst()) {
                sLengthMin = c.getString(sessionLengthIndex);
                bLengthSec = c.getString(breakLengthIndex);
                notifType = c.getString(notifTypeIndex);
            }

            AllSessions.sessionDB.close();


            new AlertDialog.Builder(AllSessions.this)
                    .setTitle(sessionNames.get(info.position))
                    .setMessage("Session Length: " + sLengthMin + " minutes"+
                            "\nBreak Length: " + bLengthSec + " seconds" +
                            "\nNotification Type: " + notifType)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .setNegativeButton("Edit", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent i = new Intent(AllSessions.this, CreateOrEditSession.class);
                            i.putExtra("sessionName", sessionNames.get(info.position));
                            i.putExtra("sessionId", sessionIDs.get(info.position));

                            startActivity(i);
                        }
                    })
                    .show();


//        } else if (item.getTitle() == "Reorder") {
//
        } else {
            return false;
        }
        return true;
    }

    private void deleteSession(int position) {
        sessionNames.remove(position);
        int idToRemove = sessionIDs.remove(position);

        sessionDB.delete("sessions", "id = " + idToRemove, null);
        allSessionsAdapter.notifyDataSetChanged();
    }

}

