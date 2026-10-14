package nl.crijns.ollieonair;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

/**
 * Ollie on Air op de tv: een schermvullende browser die het tv-scherm van de jukebox opent.
 * - video's mogen meteen met geluid starten (geen "druk op OK voor geluid")
 * - het scherm blijft aan (geen screensaver)
 * - nummers die alleen op YouTube zelf mogen, gaan naar de YouTube-app
 * - OK twee seconden ingedrukt houden: instellingen
 */
public class MainActivity extends Activity {

    private static final String YT_TV_APP = "com.google.android.youtube.tv";
    private static final String ERROR_PAGE = "file:///android_asset/offline.html";

    private final Handler handler = new Handler(Looper.getMainLooper());
    private WebView web;
    private String url;
    private boolean showingError = false;
    private boolean wentToYouTube = false;
    private boolean autoReturned = false;

    /** Na afloop van de karaokevideo de jukebox vanzelf terughalen (kan dankzij "Weergeven over andere apps"). */
    private final Runnable returnFromYouTube = () -> {
        autoReturned = true;
        AutoStart.bringToFront(getApplicationContext());
    };
    private long lastBackAt = 0;

    private final Runnable openSettingsRunnable = this::openSettings;
    private final Runnable retryRunnable = () -> {
        if (showingError) load();
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        url = Prefs.url(this);
        if (url == null) {
            startActivity(new Intent(this, SettingsActivity.class));
            finish();
            return;
        }

        AutoStart.ensureService(this);
        Updater.checkIfDue(this);
        WebView.setWebContentsDebuggingEnabled(true);
        web = new WebView(this);
        web.setBackgroundColor(Color.BLACK);
        web.setFocusable(true);
        web.setFocusableInTouchMode(true);
        setContentView(web);

        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
        s.setUseWideViewPort(true);
        s.setLoadWithOverviewMode(true);
        s.setJavaScriptCanOpenWindowsAutomatically(true);
        s.setSupportMultipleWindows(false);
        s.setUserAgentString(s.getUserAgentString() + " OllieOnAirTV/1.0");

        web.addJavascriptInterface(new Bridge(), "OllieTV");
        web.setWebChromeClient(new WebChromeClient());
        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return handleUrl(request.getUrl());
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (request.isForMainFrame() && !request.getUrl().toString().startsWith(ERROR_PAGE)) showError();
            }
        });

        load();
        web.requestFocus();
        hideSystemUi();
    }

    private void load() {
        showingError = false;
        handler.removeCallbacks(retryRunnable);
        web.loadUrl(url);
    }

    private void showError() {
        showingError = true;
        web.loadUrl(ERROR_PAGE + "#" + Uri.encode(url));
        handler.removeCallbacks(retryRunnable);
        handler.postDelayed(retryRunnable, 15000);
    }

    /** YouTube-links gaan naar de YouTube-app; al het andere blijft in de app. */
    private boolean handleUrl(Uri u) {
        String host = u.getHost() == null ? "" : u.getHost();
        if (host.endsWith("youtube.com") || host.equals("youtu.be")) {
            String id = u.getQueryParameter("v");
            if (id == null && host.equals("youtu.be") && u.getPath() != null) id = u.getPath().replace("/", "");
            if (id == null && u.getPath() != null && u.getPath().startsWith("/embed/")) id = u.getPath().substring(7);
            if (id != null && id.matches("[A-Za-z0-9_-]{11}")) {
                openYouTube(id);
                return true;
            }
        }
        return false;
    }

    private void openYouTube(String id) {
        openYouTube(id, 0);
    }

    /** @param seconds speelduur van de video; 0 = onbekend, dan niet vanzelf terugkomen */
    private void openYouTube(String id, int seconds) {
        wentToYouTube = true;
        autoReturned = false;
        handler.removeCallbacks(returnFromYouTube);
        if (seconds > 0) handler.postDelayed(returnFromYouTube, (seconds + 6) * 1000L);
        String watch = "https://www.youtube.com/watch?v=" + id;
        // De YouTube-app voor tv start een video het betrouwbaarst met een gewone watch-link.
        Intent[] tries = new Intent[] {
                yt(watch, YT_TV_APP),
                yt("vnd.youtube:" + id, YT_TV_APP),
                yt(watch, null)
        };
        for (Intent i : tries) {
            try {
                startActivity(i);
                return;
            } catch (ActivityNotFoundException ignored) {
                // volgende manier proberen
            }
        }
        wentToYouTube = false;
        handler.removeCallbacks(returnFromYouTube);
        Toast.makeText(this, "Geen YouTube-app gevonden op deze tv", Toast.LENGTH_LONG).show();
    }

    private static Intent yt(String uri, String pkg) {
        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.putExtra("force_fullscreen", true);
        i.putExtra("finish_on_ended", true);
        if (pkg != null) i.setPackage(pkg);
        return i;
    }

    private void openSettings() {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUi();
        if (web == null) return;
        web.onResume();
        String saved = Prefs.url(this);
        if (saved != null && !saved.equals(url)) {
            url = saved;
            load();
            return;
        }
        handler.removeCallbacks(returnFromYouTube);
        if (wentToYouTube) {
            wentToYouTube = false;
            boolean auto = autoReturned;
            autoReturned = false;
            web.evaluateJavascript("window.ollieTvResumed && window.ollieTvResumed(" + auto + ")", null);
        }
    }

    @Override
    protected void onPause() {
        if (web != null) web.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        if (web != null) web.destroy();
        super.onDestroy();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) hideSystemUi();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int code = event.getKeyCode();
        // OK lang ingedrukt houden: instellingen
        if (code == KeyEvent.KEYCODE_DPAD_CENTER || code == KeyEvent.KEYCODE_ENTER) {
            if (event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0) {
                handler.postDelayed(openSettingsRunnable, 2000);
            } else if (event.getAction() == KeyEvent.ACTION_UP) {
                handler.removeCallbacks(openSettingsRunnable);
            }
        }
        if ((code == KeyEvent.KEYCODE_MENU || code == KeyEvent.KEYCODE_SETTINGS) && event.getAction() == KeyEvent.ACTION_UP) {
            openSettings();
            return true;
        }
        if (code == KeyEvent.KEYCODE_BACK) {
            if (event.getAction() == KeyEvent.ACTION_UP) onBackKey();
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    /** Terug: eerst terug naar het tv-scherm (bijv. vanaf /admin), anders twee keer drukken om af te sluiten. */
    private void onBackKey() {
        if (web != null && !showingError) {
            String current = web.getUrl();
            if (current != null && !current.equals(url) && web.canGoBack()) {
                web.goBack();
                return;
            }
        }
        long now = System.currentTimeMillis();
        if (now - lastBackAt < 2500) {
            finish();
        } else {
            lastBackAt = now;
            Toast.makeText(this, "Druk nog een keer op Terug om Ollie on Air te sluiten", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressWarnings("deprecation")
    private void hideSystemUi() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
    }

    /** Wat de webpagina van de jukebox aan de app kan vragen (window.OllieTV). */
    private class Bridge {
        @JavascriptInterface
        public void openYouTube(String id) {
            if (id == null || !id.matches("[A-Za-z0-9_-]{11}")) return;
            handler.post(() -> MainActivity.this.openYouTube(id));
        }

        @JavascriptInterface
        public void openYouTubeFor(String id, int seconds) {
            if (id == null || !id.matches("[A-Za-z0-9_-]{11}")) return;
            int s = Math.max(0, Math.min(seconds, 60 * 30));
            handler.post(() -> MainActivity.this.openYouTube(id, s));
        }

        @JavascriptInterface
        public void openSettings() {
            handler.post(MainActivity.this::openSettings);
        }

        @JavascriptInterface
        public void reload() {
            handler.post(MainActivity.this::load);
        }

        @JavascriptInterface
        public String version() {
            return "1.0";
        }
    }
}
