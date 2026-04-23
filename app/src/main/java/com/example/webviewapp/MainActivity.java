package com.example.webviewapp;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import android.provider.Settings;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    private WebView webView;

    public WebView getWebView() {
        return webView;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        RelativeLayout rootLayout = new RelativeLayout(this);
        rootLayout.setLayoutParams(new RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT, 
            RelativeLayout.LayoutParams.MATCH_PARENT
        ));

        webView = new WebView(this);
        webView.setId(View.generateViewId());
        
        RelativeLayout.LayoutParams webParams = new RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT, 
            RelativeLayout.LayoutParams.MATCH_PARENT
        );
        rootLayout.addView(webView, webParams);

        // Add Bottom Navigation if enabled
        if (BuildConfig.ENABLE_BOTTOM_NAV) {
            try {
                JSONArray navItems = new JSONArray(BuildConfig.NAV_ITEMS_JSON);
                if (navItems.length() > 0) {
                    BottomNavigationView bottomNav = new BottomNavigationView(this);
                    bottomNav.setId(View.generateViewId());
                    
                    RelativeLayout.LayoutParams navParams = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT, 
                        RelativeLayout.LayoutParams.WRAP_CONTENT
                    );
                    navParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                    rootLayout.addView(bottomNav, navParams);
                    
                    webParams.addRule(RelativeLayout.ABOVE, bottomNav.getId());
                    webView.setLayoutParams(webParams);
                    
                    Menu menu = bottomNav.getMenu();
                    for (int i = 0; i < navItems.length(); i++) {
                        JSONObject item = navItems.getJSONObject(i);
                        String label = item.getString("label");
                        String path = item.getString("path");
                        // Ignoring specific icons for simplicity, using a default icon or parsing if needed
                        MenuItem menuItem = menu.add(0, i, 0, label);
                        menuItem.setIcon(android.R.drawable.ic_menu_compass);
                    }
                    
                    bottomNav.setOnItemSelectedListener(item -> {
                        try {
                            JSONObject navItem = navItems.getJSONObject(item.getItemId());
                            String path = navItem.getString("path");
                            webView.loadUrl(BuildConfig.WEB_URL + path);
                        } catch (Exception e) {}
                        return true;
                    });
                }
            } catch (Exception e) {}
        }

        setContentView(rootLayout);

        // ✅ Enable WebView debugging (safe)
        WebView.setWebContentsDebuggingEnabled(true);

        // ✅ Enable cookies
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setAcceptThirdPartyCookies(webView, true);

        // ✅ WebView settings
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setSupportMultipleWindows(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                if (url.startsWith("mailto:") || url.startsWith("tel:")
                        || url.startsWith("whatsapp:") || url.startsWith("intent:")) {
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(intent);
                    } catch (Exception e) {}
                    return true;
                }
                return false; // all http/https load inside WebView
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onCreateWindow(WebView view, boolean isDialog,
                                           boolean isUserGesture, Message resultMsg) {
                WebView tempWebView = new WebView(MainActivity.this);
                tempWebView.setWebViewClient(new WebViewClient() {
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest req) {
                        webView.loadUrl(req.getUrl().toString());
                        return true;
                    }
                });
                WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
                transport.setWebView(tempWebView);
                resultMsg.sendToTarget();
                return true;
            }
        });

        // Add JS Bridge
        webView.addJavascriptInterface(new AndroidBridge(this), "Android");

        // ✅ SAFE URL HANDLING
        String tempUrl = BuildConfig.WEB_URL;
        if (tempUrl == null || tempUrl.trim().isEmpty()) {
            tempUrl = "https://google.com";
        }
        final String finalUrl = tempUrl;

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            webView.loadUrl(finalUrl);
        }, 200);

        // ✅ Back navigation
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (webView.canGoBack()) {
                    webView.goBack();
                } else {
                    finish();
                }
            }
        });

        // FCM Init
        if (BuildConfig.ENABLE_PUSH) {
            FirebaseMessaging.getInstance().getToken().addOnSuccessListener(token -> {
                sendTokenToBackend(token);
            });
        }
    }

    private void sendTokenToBackend(String token) {
        new Thread(() -> {
            try {
                String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
                URL url = new URL(BuildConfig.BACKEND_URL + "/api/apps/" + BuildConfig.APP_ID + "/fcm-token");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; utf-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);
                
                String jsonInputString = "{\"token\":\"" + token + "\", \"deviceId\":\"" + deviceId + "\"}";
                
                try(OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);			
                }
                conn.getResponseCode();
                conn.disconnect();
            } catch (Exception e) {
                Log.e("FCM", "Failed to send token", e);
            }
        }).start();
    }
}