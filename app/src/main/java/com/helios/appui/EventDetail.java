package com.helios.appui;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

public class EventDetail extends AppCompatActivity {

    ImageView imageView;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_event_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Hey! This app is under construction.", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        int position = getIntent().getIntExtra("position", 0);
        String name = getIntent().getStringExtra("name");

        setupView(position, name);
    }

    public void setupView(int position, String name)
    {

        imageView = (ImageView) findViewById(R.id.eventBanner);
        textView = (TextView) findViewById(R.id.eventName);
        textView.setText(name);

        switch(position)
        {
            case 0:
                imageView.setImageResource(R.drawable.event_1);
                break;
            case 1:
                imageView.setImageResource(R.drawable.event_2);
                break;
            case 2:
                imageView.setImageResource(R.drawable.event_3);
                break;
            case 3:
                imageView.setImageResource(R.drawable.event_4);
                break;
            default:
                imageView.setImageResource(R.drawable.event_1);

        }
    }

}
