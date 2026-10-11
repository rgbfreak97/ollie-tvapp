# Ollie on Air – tv-app voor Chromecast met Google TV

Een eigen app die het tv-scherm van de jukebox schermvullend opent. In plaats van TV Bro:

- **Geen "Druk op OK voor geluid"**: video's starten meteen met geluid.
- **Geen screensaver** zolang de app openstaat.
- **Eigen icoon** tussen je apps, geen adresbalk of cursor.
- **Nummers "via YouTube"** openen in de echte YouTube-app op je tv (ingelogd met Premium = geen reclame). Klaar met zingen? Druk op **Terug**, en daarna op **OK** voor de volgende zanger.

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

## Bediening

- **OK 2 seconden ingedrukt houden:** instellingen (ander adres).
- **Terug:** vanaf de bediening terug naar het tv-scherm; op het tv-scherm twee keer drukken om de app te sluiten.
- Het ⚙-knopje rechtsonder op het tv-scherm werkt nog steeds.
- Is de server niet bereikbaar, dan toont de app dat en probeert hij het elke 15 seconden opnieuw.

## Tips

- Zet in de **YouTube-app** op de tv "Automatisch afspelen" uit, dan start er na een karaokenummer niet nog een video.
- Een nieuwe versie installeer je gewoon over de oude heen (zelfde stappen). Je instellingen blijven bewaard.
- Het sleutelbestand `app/ollie.keystore` zorgt dat updates over de oude versie heen passen. Het beveiligt verder niets, dus het geeft niet dat het in een openbare repository staat.
