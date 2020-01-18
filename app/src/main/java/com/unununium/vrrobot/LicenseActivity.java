package com.unununium.vrrobot;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

/** L1: The activity to display the licenses used for the project. **/
public class LicenseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_license);
        findViewById(R.id.l1_cardboard).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://github.com/pc-chin/HnR2020-VR-Robot/"));
            startActivity(intent);
        });
        findViewById(R.id.l1_arduino).setOnClickListener(v -> {
            Toast.makeText(this, "Coming soon!", Toast.LENGTH_SHORT).show();
        });
        findViewById(R.id.l1_back).setOnClickListener(v -> onBackPressed());
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
