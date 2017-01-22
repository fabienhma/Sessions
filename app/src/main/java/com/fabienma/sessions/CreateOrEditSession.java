package com.fabienma.sessions;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class CreateOrEditSession extends AppCompatActivity {

    String sessionName;
    int sessionID;
    SeekBar sessionBar;
    SeekBar breakBar;
    EditText sessionNameText;
    EditText sessionLengthText;
    EditText breakLengthText;
    TextView sLengthParsed;
    TextView bLengthParsed;
    Spinner notifTypeSpinner;
    ArrayAdapter<String> notifTypeAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_or_edit_session);

        try {

            Intent i = getIntent();
            int createOrNah = i.getIntExtra("placeNumber", -1);
            // if -1, that means we're not creating
            sessionID = i.getIntExtra("sessionId", -1);

            sessionBar = (SeekBar) findViewById(R.id.sessionSeekBar);
            breakBar = (SeekBar) findViewById(R.id.breakSeekBar);
            sessionNameText = (EditText) findViewById(R.id.sessionName);
            sessionLengthText = (EditText) findViewById(R.id.sessionLength);
            breakLengthText = (EditText) findViewById(R.id.breakLength);
            notifTypeSpinner = (Spinner) findViewById(R.id.notifStyle);
            sLengthParsed = (TextView) findViewById(R.id.sLengthParsed);
            bLengthParsed = (TextView) findViewById(R.id.bLengthParsed);


            String[] items = new String[]{"Toast", "Turn off screen", "Notification"};
            notifTypeAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, items);
            notifTypeSpinner.setAdapter(notifTypeAdapter);

            registerForContextMenu(findViewById(R.id.notificationStylesText));


            if (createOrNah == -1) {
                sessionName = i.getStringExtra("sessionName");

                ((TextView) findViewById(R.id.createOrEditTitle)).setText("Edit Session Settings");

                AllSessions.sessionDB = openOrCreateDatabase("Sessions", MODE_PRIVATE, null);

                Cursor c = AllSessions.sessionDB.rawQuery("SELECT * FROM sessions WHERE id = " + sessionID, null);

                int sessionLengthIndex = c.getColumnIndex("sessionLength");
                int breakLengthIndex = c.getColumnIndex("breakLength");
                int notifTypeIndex = c.getColumnIndex("notifType");

                if (c.moveToFirst()) {
                    setSettings(Integer.parseInt(c.getString(sessionLengthIndex)),
                            Integer.parseInt(c.getString(breakLengthIndex)), c.getString(notifTypeIndex));
                }

                AllSessions.sessionDB.close();

            } else {
                ((TextView) findViewById(R.id.createOrEditTitle)).setText("Create New Settings");
                sessionName = "";

                setSettings(200, 300, "Notification");
            }


        } catch (Exception e) {
            e.printStackTrace();
        }


        sessionBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress > 1440) {
                    progress = 1800;
                    sessionBar.setProgress(progress);
                } else if (progress < 15) {
                    progress = 15;
                    sessionBar.setProgress(progress);
                }

                sessionLengthText.setText("" + progress);
                sLengthParsed.setText(String.format("%02d", progress / 60) + "h" + String.format("%02d", progress % 60) + "m");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        breakBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress > 1800) {
                    progress = 1800;
                    breakBar.setProgress(progress);
                } else if (progress < 60) {
                    progress = 60;
                    breakBar.setProgress(progress);
                }

                breakLengthText.setText("" + progress);
                bLengthParsed.setText(String.format("%02d", progress / 60) + "m" + String.format("%02d", progress % 60) + "s");
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        breakLengthText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                try {
                    int val = Integer.parseInt(breakLengthText.getText()
                            .toString());
                    if (val > 1800) {
                        breakLengthText.setText("1800");
                        val = 1800;
                    } else if (val < 60) {
                        breakLengthText.setText("60");
                        val = 60;
                    }

                    breakBar.setProgress(val);
                    bLengthParsed.setText(String.format("%02d", val / 60) + "m" + String.format("%02d", val % 60) + "s");
                } catch (NumberFormatException ex) {
                    ex.printStackTrace();
                }
            }
        });

        sessionLengthText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                try {
                    int val = Integer.parseInt(sessionLengthText.getText()
                            .toString());
                    if (val > 1440) {
                        sessionLengthText.setText("1440");
                        val = 1440;
                    } else if (val < 15) {
                        sessionLengthText.setText("15");
                        val = 15;
                    }

                    sessionBar.setProgress(val);
                    sLengthParsed.setText(String.format("%02d", val / 60) + "h" + String.format("%02d", val % 60) + "m");
                } catch (NumberFormatException ex) {
                    ex.printStackTrace();
                }
            }
        });

    }


    private void setSettings(int sLength, int bLength, String nType) {

        int actualSLength = Math.max(sLength, 0);
        int actualBLength = Math.max(bLength, 0);
        // this will let us call this in other methods that don't have access to blength/slength

        sessionBar.setProgress(actualSLength);
        sessionLengthText.setText(Integer.toString(actualSLength));
        breakBar.setProgress(actualBLength);
        breakLengthText.setText(Integer.toString(actualBLength));

        sLengthParsed.setText(String.format("%02d", actualSLength / 60) + "h" + String.format("%02d", actualSLength % 60) + "m");
        bLengthParsed.setText(String.format("%02d", actualBLength / 60) + "m" + String.format("%02d", actualBLength % 60) + "s");

        sessionNameText.setText(sessionName);

        notifTypeSpinner.setSelection(notifTypeAdapter.getPosition(nType));


    }

    public void saveSession(View view) {

//        sessionDB.execSQL("CREATE TABLE IF NOT EXISTS sessions (" +
//                "sessionName VARCHAR KEY, sessionLength INT, breakLength INT, notifType VARCHAR);");

        if (sessionNameText.getText().toString().replaceAll("\\s", "").length() > 0) {

            AllSessions.sessionDB = openOrCreateDatabase("Sessions", MODE_PRIVATE, null);

            AllSessions.sessionDB.delete("sessions", "id = " + sessionID, null);

            String sql = "INSERT INTO sessions (sessionName, sessionLength, breakLength, notifType) VALUES (?, ?, ?, ?);";

            SQLiteStatement statement = AllSessions.sessionDB.compileStatement(sql);

            statement.bindString(1, sessionNameText.getText().toString());
            statement.bindString(2, sessionLengthText.getText().toString());
            statement.bindString(3, breakLengthText.getText().toString());
            statement.bindString(4, notifTypeSpinner.getSelectedItem().toString());

            statement.execute();

            Toast.makeText(CreateOrEditSession.this, "Saved!", Toast.LENGTH_SHORT).show();

            AllSessions.sessionDB.close();

            finish();

        } else {
            Toast.makeText(CreateOrEditSession.this, "Please enter a name for your session!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle("Notification types");
        menu.add(0, v.getId(), 0, "Toast");
        menu.add(0, v.getId(), 0, "Turn off screen");
        menu.add(0, v.getId(), 0, "Notification");

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getTitle() == "Toast") {
            // ask if sure. If so, delete!
            Toast.makeText(CreateOrEditSession.this, "This is a Toast!", Toast.LENGTH_LONG).show();
        } else if (item.getTitle() == "Turn off screen") {
            Toast.makeText(CreateOrEditSession.this, "This option will, as you'd expect, turn off your screen.", Toast.LENGTH_LONG).show();
        } else if (item.getTitle() == "Notification") {
            Toast.makeText(CreateOrEditSession.this, "This option will send an annoying little notification to you when time's up!", Toast.LENGTH_LONG).show();
        } else {
            return false;
        }
        return true;
    }
}

