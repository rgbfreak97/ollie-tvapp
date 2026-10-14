package nl.crijns.ollieonair;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

/** Bewaart het adres van het tv-scherm. */
final class Prefs {
    private static final String FILE = "ollie";
    private static final String KEY_URL = "url";
    private static final String KEY_AUTO = "autostart";

    private Prefs() {}

    static String url(Context c) {
        return sp(c).getString(KEY_URL, null);
    }

    static void setUrl(Context c, String url) {
        sp(c).edit().putString(KEY_URL, url).apply();
    }

    static boolean autostart(Context c) {
        return sp(c).getBoolean(KEY_AUTO, true);
    }

    static void setAutostart(Context c, boolean on) {
        sp(c).edit().putBoolean(KEY_AUTO, on).apply();
    }

    /**
     * Maakt van wat er is ingetikt een volledig adres:
     * "192.168.1.20:8888" wordt "http://192.168.1.20:8888/tv",
     * "jouwdomein.nl" wordt "https://jouwdomein.nl/tv".
     */
    static String normalize(String in) {
        if (in == null) return null;
        String s = in.trim().replace(" ", "");
        if (s.isEmpty()) return null;
        if (!s.contains("://")) {
            boolean local = s.matches("^(\\d{1,3}\\.){3}\\d{1,3}(:\\d+)?(/.*)?$") || s.startsWith("localhost") || s.matches("^[^/]*:\\d+.*");
            s = (local ? "http://" : "https://") + s;
        }
        Uri u = Uri.parse(s);
        if (u.getHost() == null || u.getHost().isEmpty()) return null;
        String path = u.getPath();
        if (path == null || path.isEmpty() || path.equals("/")) {
            s = u.buildUpon().path("/tv").build().toString(); // ook goed met ?key=... erachter
        }
        return s;
    }

    private static SharedPreferences sp(Context c) {
        return c.getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }
}
