package com.easyway.app;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        RelativeLayout layout = new RelativeLayout(this);
        layout.setBackgroundColor(Color.parseColor(BuildConfig.SPLASH_COLOR));
        
        ImageView logo = new ImageView(this);
        logo.setId(android.view.View.generateViewId());
        RelativeLayout.LayoutParams logoParams = new RelativeLayout.LayoutParams(300, 300);
        logoParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        logo.setLayoutParams(logoParams);
        
        if (BuildConfig.SPLASH_LOGO_URL != null && !BuildConfig.SPLASH_LOGO_URL.isEmpty()) {
            Glide.with(this).load(BuildConfig.SPLASH_LOGO_URL).into(logo);
        } else {
            logo.setImageResource(R.mipmap.ic_launcher);
        }
        
        layout.addView(logo);
        
        TextView appName = new TextView(this);
        RelativeLayout.LayoutParams textParams = new RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        textParams.addRule(RelativeLayout.BELOW, logo.getId());
        textParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        textParams.topMargin = 40;
        appName.setLayoutParams(textParams);
        appName.setText(BuildConfig.SPLASH_TEXT);
        appName.setTextColor(Color.WHITE);
        appName.setTextSize(24f);
        
        layout.addView(appName);
        setContentView(layout);
        
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finish();
        }, 2000);
    }
}
