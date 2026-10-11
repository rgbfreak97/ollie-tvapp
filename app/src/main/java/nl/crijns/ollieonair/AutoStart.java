package nl.crijns.ollieonair;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;

/** Alles rond automatisch openen als de tv aangaat. */
final class AutoStart {
    private AutoStart() {}

    /** Android laat een app alleen vanuit de achtergrond op de voorgrond komen met de toestemming "Weergeven over andere apps". */
    static boolean allowed(Context c) {
        return Settings.canDrawOverlays(c);
    }

    /** Start of stopt de kleine achtergronddienst die op "scherm aan" let. */
    static void ensureService(Context c) {
        ensureService(c, false);
    }

    /** @param launchSoon na het opstarten: open de jukebox over een paar seconden nog een keer, als het startscherm klaar is */
    static void ensureService(Context c, boolean launchSoon) {
        Intent i = new Intent(c, WakeService.class);
        if (launchSoon) i.putExtra("launch", true);
        if (!Prefs.autostart(c) || Prefs.url(c) == null) {
            c.stopService(i);
            return;
        }
        try {
            c.startForegroundService(i);
        } catch (Exception ignored) {
            // bijv. als Android het starten op dit moment niet toestaat; bij het openen van de app proberen we het opnieuw
        }
    }

    /** Opent het tv-scherm (of haalt het naar voren). */
    static void launch(Context c) {
        if (Prefs.url(c) == null || !Prefs.autostart(c)) return;
        Intent i = new Intent(c, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        try {
            c.startActivity(i);
        } catch (Exception ignored) {
            // zonder toestemming blokkeert Android dit stil
        }
    }
}
