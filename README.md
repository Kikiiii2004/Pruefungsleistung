## 📊 Akzeptanztests & JGiven-HTML-Report (Aufgabe 3.3)

Die funktionalen Abnahmekriterien (Szenarien `AT-1` bis `AT-6`) wurden vollständig mittels des BDD-Frameworks **JGiven** implementiert.

### Report-Generierung & Lokalisation
Der interaktive HTML-Report wird bei jedem vollständigen Build-Vorgang automatisiert erzeugt. Um den Report zu generieren und zu betrachten, führen Sie folgende Befehle im Projektverzeichnis aus:


### Report-Dashboard & Tag-Übersicht

Nachfolgend finden Sie den visuellen Nachweis des generierten JGiven-Dashboards inklusive der geforderten Tag-Klassifizierungen:

![JGiven HTML Report Dashboard](jgiven-dashboard.png)

```bash
# Führt alle Tests aus und generiert den HTML-Report im target-Ordner
mvn clean verify