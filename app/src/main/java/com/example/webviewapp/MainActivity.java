package com.example.webviewapp;

import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Open full website in Chrome Custom Tab
        CustomTabsIntent customTabsIntent =
                new CustomTabsIntent.Builder().build();

        customTabsIntent.launchUrl(
                this,
                Uri.parse(BuildConfig.WEB_URL)
        );

        // Close app activity after launching browser
        finish();
    }
}
