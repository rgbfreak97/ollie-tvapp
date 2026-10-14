package nl.crijns.ollieonair;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Zoekt op GitHub naar een nieuwere versie van de app en installeert die.
 * De eerste keer vraagt Android om bevestiging; daarna mag de app zichzelf meestal zonder vraag bijwerken.
 */
final class Updater {
    interface Result { void done(String message); }

    private static final long INTERVAL = 12L * 60 * 60 * 1000;
    private static volatile boolean busy = false;

    private Updater() {}

    static boolean available() {
        return BuildConfig.UPDATE_BASE != null && !BuildConfig.UPDATE_BASE.isEmpty();
    }

    static boolean allowed(Context c) {
        return c.getPackageManager().canRequestPackageInstalls();
    }

    /** Alleen als de vorige controle lang genoeg geleden is. */
    static void checkIfDue(Context c) {
        long last = c.getSharedPreferences("ollie", Context.MODE_PRIVATE).getLong("lastUpdateCheck", 0);
        if (System.currentTimeMillis() - last < INTERVAL) return;
        check(c, null);
    }

    static void check(Context ctx, Result result) {
        final Context c = ctx.getApplicationContext();
        final Handler main = new Handler(Looper.getMainLooper());
        if (!available()) { report(main, result, "Automatisch bijwerken werkt alleen met de versie die GitHub heeft gebouwd."); return; }
        if (busy) { report(main, result, "Er wordt al gezocht naar een update."); return; }
        busy = true;
        new Thread(() -> {
            try {
                c.getSharedPreferences("ollie", Context.MODE_PRIVATE).edit().putLong("lastUpdateCheck", System.currentTimeMillis()).apply();
                String json = new String(get(BuildConfig.UPDATE_BASE + "version.json"), "UTF-8");
                Matcher m = Pattern.compile("\"versionCode\"\\s*:\\s*(\\d+)").matcher(json);
                if (!m.find()) { report(main, result, "Kon de versie op GitHub niet lezen."); return; }
                int latest = Integer.parseInt(m.group(1));
                if (latest <= BuildConfig.VERSION_CODE) { report(main, result, "Je hebt de nieuwste versie (" + BuildConfig.VERSION_NAME + ")."); return; }
                if (!allowed(c)) { report(main, result, "Er is een nieuwe versie, maar de app mag nog geen updates installeren. Geef eerst toestemming."); return; }
                report(main, result, "Nieuwe versie gevonden, bezig met downloaden…");
                File apk = new File(c.getCacheDir(), "update.apk");
                try (OutputStream out = new FileOutputStream(apk)) { out.write(get(BuildConfig.UPDATE_BASE + "ollie-on-air.apk")); }
                install(c, apk);
            } catch (Exception e) {
                report(main, result, "Zoeken naar een update lukte niet: " + e.getMessage());
            } finally {
                busy = false;
            }
        }).start();
    }

    private static void install(Context c, File apk) throws Exception {
        PackageInstaller pi = c.getPackageManager().getPackageInstaller();
        PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL);
        params.setAppPackageName(c.getPackageName());
        if (Build.VERSION.SDK_INT >= 31) params.setRequireUserAction(PackageInstaller.SessionParams.USER_ACTION_NOT_REQUIRED);
        int id = pi.createSession(params);
        try (PackageInstaller.Session s = pi.openSession(id)) {
            try (OutputStream out = s.openWrite("ollie-on-air", 0, apk.length()); InputStream in = new FileInputStream(apk)) {
                byte[] buf = new byte[65536];
                int n;
                while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
                s.fsync(out);
            }
            Intent done = new Intent(c, UpdateReceiver.class);
            int flags = PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= 31 ? PendingIntent.FLAG_MUTABLE : 0);
            PendingIntent p = PendingIntent.getBroadcast(c, id, done, flags);
            s.commit(p.getIntentSender());
        }
    }

    private static byte[] get(String url) throws Exception {
        HttpURLConnection con = (HttpURLConnection) new URL(url + (url.contains("?") ? "&" : "?") + "t=" + System.currentTimeMillis()).openConnection();
        con.setConnectTimeout(15000);
        con.setReadTimeout(60000);
        con.setInstanceFollowRedirects(true);
        con.setRequestProperty("Cache-Control", "no-cache");
        int code = con.getResponseCode();
        if (code != 200) throw new Exception("HTTP " + code);
        try (InputStream in = con.getInputStream(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buf = new byte[65536];
            int n;
            while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
            return out.toByteArray();
        } finally {
            con.disconnect();
        }
    }

    private static void report(Handler main, Result r, String msg) {
        if (r != null) main.post(() -> r.done(msg));
    }
}
