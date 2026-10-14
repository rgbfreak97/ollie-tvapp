# Ollie on Air – tv-app voor Chromecast met Google TV

Een eigen app die het tv-scherm van de jukebox schermvullend opent. In plaats van TV Bro:

- **Geen "Druk op OK voor geluid"**: video's starten meteen met geluid.
- **Geen screensaver** zolang de app openstaat.
- **Eigen icoon** tussen je apps, geen adresbalk of cursor.
- **Nummers "via YouTube"** openen in de echte YouTube-app op je tv (ingelogd met Premium = geen reclame). Na afloop van de video komt de jukebox vanzelf terug en gaat door naar de volgende zanger (daarvoor is de toestemming "Weergeven over andere apps" nodig, zie hieronder). Eerder klaar? Druk op **Terug** en daarna op **OK**.

## 1. De app laten bouwen (gratis, via GitHub)

Je hoeft niets te installeren; GitHub bouwt het app-bestand (APK) voor je.

1. Maak een gratis account op github.com (als je er nog geen hebt).
2. Klik rechtsboven op **+ → New repository**. Naam: `ollie-tvapp`, kies **Public** (dan kan de tv het bestand zonder inloggen downloaden), klik **Create repository**.
3. Klik op **uploading an existing file**. Sleep **alle bestanden en mappen** uit deze map erin (ook de map `.github`). Klik onderaan **Commit changes**.
   - Zie je daarna geen map `.github` in de lijst? Windows/Mac slaat die verborgen map soms over. Klik dan **Add file → Create new file**, typ als naam `.github/workflows/build.yml`, plak de inhoud van dat bestand uit deze map erin en klik **Commit changes**.
4. Open het tabblad **Actions**. Na 3 à 5 minuten staat er een groen vinkje.
5. Rechts op de hoofdpagina staat nu onder **Releases** "Ollie on Air TV-app" met het bestand `ollie-on-air.apk`.

Je vaste downloadlink is:
`https://github.com/JOUW-GEBRUIKERSNAAM/ollie-tvapp/releases/latest/download/ollie-on-air.apk`

Liever zonder GitHub? Installeer Android Studio, kies **Open** en selecteer deze map, en kies daarna **Build → Build App Bundle(s) / APK(s) → Build APK(s)**.

## 2. Installeren op de Chromecast

1. **Ontwikkelaarsopties aan:** Instellingen → Systeem → Over → klik **7 keer** op "Android TV OS-build". (Namen kunnen per versie iets verschillen.)
2. Installeer uit de Play Store de gratis app **Downloader** (van AFTVnews).
3. **Onbekende apps toestaan:** Instellingen → Apps → Beveiliging en beperkingen → Onbekende bronnen → zet **Downloader** aan.
4. Open Downloader en typ het adres van de APK:
   - de GitHub-link hierboven, of
   - zet `ollie-on-air.apk` naast `server.js` van de jukebox en typ `http://<ip-van-je-pc>:8888/app.apk`
5. Kies **Installeren**. Klaar: **Ollie on Air** staat bij je apps (zet hem met lang OK drukken vooraan in je favorieten).

## 3. Eerste keer openen

De app vraagt het adres van de jukebox. Typ wat er in het servervenster staat, bijvoorbeeld `192.168.1.20:8888` (de app zet er zelf `http://` en `/tv` bij). Heb je later een eigen domein: typ `jouwdomein.nl`.

## Automatisch openen als de tv aangaat

Staat standaard aan (instellingen: OK 2 seconden ingedrukt houden). De app opent dan vanzelf na het opstarten van de Chromecast én als die uit slaapstand komt (tv aan).

Android vraagt daarvoor één keer de toestemming **"Weergeven over andere apps"**. In de instellingen van de app zie je of die aanstaat (groen vinkje). Staat hij nog niet aan:

1. Probeer eerst de knop **Toestemming geven** in de instellingen van de app en zet Ollie on Air daar aan.
2. Heeft je Chromecast dat scherm niet (veel Google TV-versies verbergen het), dan zet je het eenmalig aan vanaf je pc:
   1. Op de tv: Instellingen → Systeem → Ontwikkelaarsopties → zet **USB-foutopsporing** aan.
   2. Zoek het IP-adres van de tv: Instellingen → Netwerk en internet → je wifi-netwerk.
   3. Download op je pc **SDK Platform-Tools** voor Windows van developer.android.com (zoek op "platform-tools"), pak het uit en open in die map een opdrachtvenster (in de adresbalk van Verkenner `cmd` typen en Enter).
   4. Typ `adb connect 192.168.1.30` (met het IP-adres van je tv). Op de tv verschijnt een vraag: kies **Altijd toestaan** en **OK**.
   5. Typ `adb shell appops set nl.crijns.ollieonair SYSTEM_ALERT_WINDOW allow`
   6. Klaar. In de app-instellingen staat nu een groen vinkje. USB-foutopsporing mag je daarna weer uitzetten.

Let op: de app opent dan elke keer als de tv aangaat, ook als je eigenlijk Netflix wilde kijken. Even op Home drukken en je bent er weer uit. Zet het vinkje uit als je dat niet wilt.

## Automatisch bijwerken

Vanaf deze versie werkt de app zichzelf bij. Hij kijkt bij het openen en elke 12 uur op GitHub of er een nieuwere versie is, downloadt die en installeert hem. Daarna opent de jukebox vanzelf weer. Een nieuwe versie maken is dus alleen nog: de bestanden op GitHub zetten en wachten op het groene vinkje. Het versienummer telt GitHub zelf op.

Eenmalig nodig:
1. Installeer **deze** versie nog één keer zelf (Downloader of `adb install -r ollie-on-air.apk`).
2. Geef de app toestemming om updates te installeren: houd OK 2 seconden ingedrukt en kies **Toestemming voor updates**, of vanaf je pc:
   `adb shell appops set nl.crijns.ollieonair REQUEST_INSTALL_PACKAGES allow`
3. De eerste update vraagt Android nog om bevestiging ("Wil je deze app updaten?" → **Installeren**). Daarna mag de app zichzelf meestal zonder vraag bijwerken.

In de instellingen zie je het versienummer en kun je met **Zoeken naar update** meteen kijken.

Let op: het tv-scherm, de thema's en de bediening zitten in de jukebox op de server. Die werken al vanzelf bij, daarvoor hoeft de app niet bijgewerkt te worden.

## Bediening

- **OK 2 seconden ingedrukt houden:** instellingen (ander adres).
- **Terug:** vanaf de bediening terug naar het tv-scherm; op het tv-scherm twee keer drukken om de app te sluiten.
- Het ⚙-knopje rechtsonder op het tv-scherm werkt nog steeds.
- Is de server niet bereikbaar, dan toont de app dat en probeert hij het elke 15 seconden opnieuw.

## Tips

- Zet in de **YouTube-app** op de tv "Automatisch afspelen" uit, dan start er na een karaokenummer niet nog een video.
- Een nieuwe versie installeer je gewoon over de oude heen (zelfde stappen). Je instellingen blijven bewaard.
- Het sleutelbestand `app/ollie.keystore` zorgt dat updates over de oude versie heen passen. Het beveiligt verder niets, dus het geeft niet dat het in een openbare repository staat.
