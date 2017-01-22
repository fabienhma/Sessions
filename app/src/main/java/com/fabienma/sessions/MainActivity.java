package com.fabienma.sessions;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ArrayList<String> mainMenuList = new ArrayList<String>();
    ArrayAdapter<String> mainMenuAdapter;
    ListView mainMenuListView;

    @Override
    protected void onResume() {
        super.onResume();
        if (EndSession.inSession) {
            Intent intent = new Intent(MainActivity.this, EndSession.class);
            startActivity(intent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if (EndSession.inSession) {
            Intent intent = new Intent(MainActivity.this, EndSession.class);
            startActivity(intent);
        }

        mainMenuList.add("Start session");
        mainMenuList.add("Manage sessions");

        mainMenuListView = (ListView) findViewById(R.id.mainMenuListView);

        mainMenuAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mainMenuList);

        mainMenuListView.setAdapter(mainMenuAdapter);

        mainMenuListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                goToMenuItem(position);
            }
        });

        SpannableString ss = new SpannableString("Questions? Contact me");
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("message/rfc822");
                i.putExtra(Intent.EXTRA_EMAIL, new String[]{"fabienhma@gmail.com"});
                i.putExtra(Intent.EXTRA_SUBJECT, "Sessions - Contact");
                i.putExtra(Intent.EXTRA_TEXT, "Issue: \n Question:\n ");
                try {
                    startActivity(Intent.createChooser(i, "Send mail..."));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(MainActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
            }
        };

        ss.setSpan(clickableSpan, 11, 21, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        TextView textView = (TextView) findViewById(R.id.contact);
        textView.setText(ss);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setHighlightColor(Color.TRANSPARENT);

    }

    private void goToMenuItem(int itemNum) {
        switch (itemNum) {
            case 0:
            case 1:
                // Go to AllSessions page.  0 should be start sessions list, 1 to manage.
                Intent i = new Intent(MainActivity.this, AllSessions.class);
                i.putExtra("placeNumber", itemNum);

                startActivity(i);
                return;
        }


    }


}
