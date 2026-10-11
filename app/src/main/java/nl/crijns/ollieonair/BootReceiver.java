package nl.crijns.ollieonair;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** Na het opstarten van de Chromecast (of na een update van de app): dienst starten en de jukebox openen. */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Prefs.autostart(context) || Prefs.url(context) == null) return;
        boolean boot = !Intent.ACTION_MY_PACKAGE_REPLACED.equals(intent.getAction());
        AutoStart.ensureService(context, boot);
        if (boot) AutoStart.launch(context);
    }
}
