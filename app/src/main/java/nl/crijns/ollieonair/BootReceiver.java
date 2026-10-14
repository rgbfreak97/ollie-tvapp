package nl.crijns.ollieonair;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** Na het opstarten van de Chromecast (of na een update van de app): dienst starten en de jukebox openen. */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        boolean updated = Intent.ACTION_MY_PACKAGE_REPLACED.equals(intent.getAction());
        if (updated) {
            // net bijgewerkt: dienst weer aan en de jukebox meteen weer openen
            AutoStart.ensureService(context, false);
            AutoStart.bringToFront(context);
            return;
        }
        if (!Prefs.autostart(context) || Prefs.url(context) == null) return;
        AutoStart.ensureService(context, true);
        AutoStart.launch(context);
    }
}
