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
```

### 4.1 Daten-Export & Schemaspezifikation

Die Serialisierungskomponente ist über das Entwurfsmuster *Strategy* mittels des Interfaces `AnalyticsExporter` implementiert. Dies ermöglicht die Entkopplung der Simulationslogik von den Dateiformaten.

#### 4.1.1 CSV-Export-Schema (Flachstruktur für Zeitreihen)
Der `CsvExporter` überführt verschachtelte Strukturen tabellarisch in ein unmaskiertes Flachdateiformat.
* **Trennzeichen:** Semikolon (`;`)
* **Zeilenumbruch:** Standard-Linefeed (`\n`)

**Strukturübersicht:**
```text
MetricType;Iteration;KeyIdentifier;Value
<String>;<Integer>;<String>;<Double|Long|String>
```

#### 4.1.2 JSON-Export-Schema (Hierarchisch & Nativ)
Der `JsonExporter` überführt den `AnalyticsReport` reflexionsfrei ohne Verwendung externer Frameworks in ein valides JSON-Dokument. Das zugrundeliegende Struktur-Schema ist wie folgt definiert:

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "title": "AnalyticsReport",
  "type": "object",
  "properties": {
    "generatedAt": { "type": "string", "format": "date-time" },
    "seed": { "type": "integer" },
    "config": {
      "type": "object",
      "properties": {
        "dimensions": { "type": "integer" },
        "populationSize": { "type": "integer" },
        "maxIterations": { "type": "integer" }
      }
    },
    "byAnalyzer": {
      "type": "object",
      "additionalProperties": { "type": "object" }
    }
  },
  "required": ["generatedAt", "seed", "config", "byAnalyzer"]
}
```