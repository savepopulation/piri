package com.raqun.piri.sample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.raqun.PiriActivity;
import com.raqun.PiriParam;


/**
 * Created by tyln on 09/05/2017.
 */

@PiriActivity
public class SecondActivity extends AppCompatActivity {
    private static final String BUNDLE_ID = "extra_key";
    private static final String BUNDLE_NAME = "extra_name";

    @PiriParam(key = BUNDLE_ID)
    private Long id;

    @PiriParam(key = BUNDLE_NAME)
    private String name;

    // This is not a PiriParam so it's not passing by bundle in new intent.
    private String description;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        final Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            id = bundle.getLong(BUNDLE_ID);
            name = bundle.getString(BUNDLE_NAME);
        }

        // INIT UI
        final TextView textViewId = (TextView) findViewById(R.id.textview_id);
        textViewId.setText("The id passed with Piri " + id);

        final TextView textViewName = (TextView) findViewById(R.id.textview_name);
        textViewName.setText("The name passed with Piri " + name);
    }
}
