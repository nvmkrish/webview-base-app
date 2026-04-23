package com.easyway.app;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Vibrator;
import android.provider.Settings;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;

public class AndroidBridge {
    private MainActivity context;
    private WebView webView;

    public AndroidBridge(MainActivity context) {
        this.context = context;
        this.webView = context.getWebView();
    }

    @JavascriptInterface
    public void shareText(String text) {
        if (!BuildConfig.ENABLE_SHARE) return;
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, text);
        context.startActivity(Intent.createChooser(intent, "Share via"));
    }

    @JavascriptInterface
    public void vibrate(int ms) {
        Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (v != null) v.vibrate(ms);
    }

    @JavascriptInterface
    public String getDeviceId() {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    @JavascriptInterface
    public void showToast(String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    @JavascriptInterface
    public void openCamera() {
        if (BuildConfig.ENABLE_CAMERA) {
            Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
            if (intent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(intent);
            }
        }
    }

    @JavascriptInterface
    public void getLocation(final String callbackFn) {
        if (BuildConfig.ENABLE_LOCATION) {
            webView.post(() -> {
                double lat = 0.0;
                double lng = 0.0;
                try {
                    LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                    if (lm != null) {
                        Location loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (loc == null) {
                            loc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        }
                        if (loc != null) {
                            lat = loc.getLatitude();
                            lng = loc.getLongitude();
                        }
                    }
                } catch (SecurityException e) {}
                webView.evaluateJavascript("javascript:" + callbackFn + "(" + lat + "," + lng + ")", null);
            });
        }
    }
}
