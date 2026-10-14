package nl.crijns.ollieonair;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

/**
 * Een Chromecast gaat bij het uitzetten van de tv meestal in slaapstand in plaats van uit.
 * Deze kleine dienst merkt dat het scherm weer aangaat en opent dan de jukebox.
 */
public class WakeService extends Service {
    private static final String CHANNEL = "ollie_autostart";
    private final Handler handler = new Handler(Looper.getMainLooper());
    private BroadcastReceiver screenOn;
    private final Runnable updateCheck = new Runnable() {
        @Override
        public void run() {
            Updater.checkIfDue(WakeService.this);
            handler.postDelayed(this, 60L * 60 * 1000); // elk uur kijken of de 12 uur om zijn
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) {
            NotificationChannel ch = new NotificationChannel(CHANNEL, "Automatisch starten", NotificationManager.IMPORTANCE_MIN);
            ch.setShowBadge(false);
            nm.createNotificationChannel(ch);
        }
        Notification n = new Notification.Builder(this, CHANNEL)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Ollie on Air")
                .setContentText("Opent vanzelf als de tv aangaat")
                .setOngoing(true)
                .build();
        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(1, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
        } else {
            startForeground(1, n);
        }

        screenOn = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                handler.postDelayed(() -> AutoStart.launch(WakeService.this), 1500);
            }
        };
        registerReceiver(screenOn, new IntentFilter(Intent.ACTION_SCREEN_ON));
        handler.postDelayed(updateCheck, 5L * 60 * 1000);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!Prefs.autostart(this)) {
            stopSelf();
            return START_NOT_STICKY;
        }
        if (intent != null && intent.getBooleanExtra("launch", false)) {
            handler.postDelayed(() -> AutoStart.launch(WakeService.this), 8000);
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        if (screenOn != null) {
            try {
                unregisterReceiver(screenOn);
            } catch (Exception ignored) {
            }
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
