package com.example.practicalfive;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

public class Municipality extends Activity {
    public static final String EXTRA_MUNICIPALITY = "com.example.practicalfive.MUNICIPALITY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String name = new TextProcessor().processMunicipality(
                getIntent().getStringExtra(EXTRA_MUNICIPALITY));
        if (name == null) {
            Toast.makeText(this, R.string.invalid_details_error, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        setContentView(R.layout.activity_municipality);
        UiInsets.apply(this, R.id.municipality_root);
        TextView municipalityName = findViewById(R.id.municipality_name);
        municipalityName.setText(name);
    }
}
