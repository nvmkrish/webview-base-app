package com.example.webviewapp;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

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

        // Add Floating Pill Bottom Navigation if enabled
        if (BuildConfig.ENABLE_BOTTOM_NAV) {
            try {
                JSONArray navItems = new JSONArray(BuildConfig.NAV_ITEMS_JSON);
                int count = Math.min(navItems.length(), 3); // cap at 3 tabs
                if (count > 0) {

                    // --- Outer container for the pill (provides bottom margin) ---
                    FrameLayout navContainer = new FrameLayout(this);
                    navContainer.setId(View.generateViewId());
                    int bottomMarginPx = dp(16);
                    int sidePadPx    = dp(20);
                    int pillHeightPx = dp(64);

                    RelativeLayout.LayoutParams navContainerParams = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        pillHeightPx + bottomMarginPx
                    );
                    navContainerParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                    rootLayout.addView(navContainer, navContainerParams);

                    // Webview stops above the nav container
                    webParams.addRule(RelativeLayout.ABOVE, navContainer.getId());
                    webView.setLayoutParams(webParams);

                    // --- Pill background ---
                    LinearLayout pill = new LinearLayout(this);
                    pill.setOrientation(LinearLayout.HORIZONTAL);
                    pill.setGravity(Gravity.CENTER_VERTICAL);
                    pill.setId(View.generateViewId());

                    GradientDrawable pillBg = new GradientDrawable();
                    pillBg.setColor(Color.WHITE);
                    pillBg.setCornerRadius(dp(40));
                    pillBg.setStroke(dp(1), Color.parseColor("#E5E7EB"));
                    pill.setBackground(pillBg);
                    pill.setElevation(dp(8));
                    pill.setPadding(dp(6), dp(6), dp(6), dp(6));

                    FrameLayout.LayoutParams pillParams = new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT, pillHeightPx
                    );
                    pillParams.leftMargin  = sidePadPx;
                    pillParams.rightMargin = sidePadPx;
                    pillParams.gravity     = Gravity.BOTTOM;
                    pillParams.bottomMargin = bottomMarginPx;
                    navContainer.addView(pill, pillParams);

                    // --- Sliding indicator (added first so it's behind tab buttons) ---
                    View indicator = new View(this);
                    GradientDrawable indicatorBg = new GradientDrawable();
                    indicatorBg.setColor(Color.parseColor("#1A3B82FF")); // blue tint
                    indicatorBg.setCornerRadius(dp(40));
                    indicator.setBackground(indicatorBg);
                    LinearLayout.LayoutParams indParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
                    indParams.weight = 1;
                    // We manage indicator position manually via x/width, so use absolute
                    // Actually we overlay it using a FrameLayout trick below

                    // Re-structure: use a FrameLayout so indicator can overlay
                    FrameLayout pillFrame = new FrameLayout(this);
                    pillFrame.setLayoutParams(new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

                    // Replace pill children with pillFrame
                    pill.addView(pillFrame, new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

                    // Indicator inside pillFrame
                    FrameLayout.LayoutParams indFp = new FrameLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
                    pillFrame.addView(indicator, indFp);

                    // --- Tab buttons row inside pillFrame ---
                    LinearLayout tabRow = new LinearLayout(this);
                    tabRow.setOrientation(LinearLayout.HORIZONTAL);
                    tabRow.setLayoutParams(new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
                    pillFrame.addView(tabRow);

                    int[] ICONS = {
                        android.R.drawable.ic_menu_compass,
                        android.R.drawable.ic_menu_search,
                        android.R.drawable.ic_menu_my_calendar
                    };

                    final String[] tabUrls = new String[count];
                    final View[] tabViews = new View[count];

                    for (int i = 0; i < count; i++) {
                        JSONObject item = navItems.getJSONObject(i);
                        String label = item.getString("label");
                        tabUrls[i] = item.getString("path"); // ✅ store full URL directly

                        LinearLayout tab = new LinearLayout(this);
                        tab.setOrientation(LinearLayout.VERTICAL);
                        tab.setGravity(Gravity.CENTER);
                        tab.setClickable(true);
                        tab.setFocusable(true);
                        tab.setId(View.generateViewId());

                        // Ripple / touch feedback
                        TypedValue outValue = new TypedValue();
                        getTheme().resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, outValue, true);
                        tab.setBackgroundResource(outValue.resourceId);

                        LinearLayout.LayoutParams tabLp = new LinearLayout.LayoutParams(
                            0, ViewGroup.LayoutParams.MATCH_PARENT, 1f);
                        tab.setLayoutParams(tabLp);

                        ImageView icon = new ImageView(this);
                        icon.setImageResource(ICONS[i]);
                        icon.setColorFilter(Color.parseColor("#6B7280")); // default grey
                        icon.setLayoutParams(new LinearLayout.LayoutParams(dp(22), dp(22)));
                        tab.addView(icon);

                        TextView lbl = new TextView(this);
                        lbl.setText(label);
                        lbl.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
                        lbl.setTextColor(Color.parseColor("#6B7280"));
                        lbl.setGravity(Gravity.CENTER);
                        LinearLayout.LayoutParams lblLp = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        lblLp.topMargin = dp(3);
                        tab.addView(lbl, lblLp);

                        tabRow.addView(tab);
                        tabViews[i] = tab;
                    }

                    // --- Helper to animate indicator ---
                    final int[] activeTab = {0};

                    Runnable updateIndicator = new Runnable() {
                        @Override public void run() {
                            View t = tabViews[activeTab[0]];
                            float targetX = t.getLeft();
                            float targetW = t.getWidth();

                            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) indicator.getLayoutParams();
                            float fromX = indicator.getTranslationX();
                            float fromW = lp.width == 0 ? targetW : lp.width;

                            ValueAnimator anim = ValueAnimator.ofFloat(0f, 1f);
                            anim.setDuration(250);
                            anim.addUpdateListener(a -> {
                                float f = (float) a.getAnimatedValue();
                                indicator.setTranslationX(fromX + (targetX - fromX) * f);
                                lp.width = (int)(fromW + (targetW - fromW) * f);
                                indicator.setLayoutParams(lp);
                            });
                            anim.start();
                        }
                    };

                    // Colour helpers
                    int activeColor  = Color.parseColor("#3B82F6");
                    int inactiveColor = Color.parseColor("#6B7280");

                    Runnable refreshColors = () -> {
                        for (int j = 0; j < count; j++) {
                            LinearLayout tab = (LinearLayout) tabViews[j];
                            ImageView ic  = (ImageView) tab.getChildAt(0);
                            TextView  tx  = (TextView)  tab.getChildAt(1);
                            boolean sel = (j == activeTab[0]);
                            ic.setColorFilter(sel ? activeColor : inactiveColor);
                            tx.setTextColor(sel ? activeColor : inactiveColor);
                        }
                    };

                    // Click listeners for each tab
                    for (int i = 0; i < count; i++) {
                        final int idx = i;
                        tabViews[i].setOnClickListener(v -> {
                            activeTab[0] = idx;
                            refreshColors.run();
                            updateIndicator.run();
                            // ✅ Use the path URL directly — no concatenation!
                            webView.loadUrl(tabUrls[idx]);
                        });
                    }

                    // Init indicator position after layout
                    tabRow.post(() -> {
                        refreshColors.run();
                        View first = tabViews[0];
                        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) indicator.getLayoutParams();
                        lp.width = first.getWidth();
                        indicator.setTranslationX(first.getLeft());
                        indicator.setLayoutParams(lp);
                    });
                }
            } catch (Exception e) {
                Log.e("NAV", "Bottom nav error", e);
            }
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

    /** Convert dp to pixels */
    private int dp(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
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