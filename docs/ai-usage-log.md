# 🤖 AI Usage Log & Reflexion — Mayfly Analytics Framework

Dieses Dokument dokumentiert chronologisch und kritisch den Einsatz von künstlicher Intelligenz (KI) im Rahmen der Bearbeitung von Phase 2 des Projekts.

---

## 🛠️ 1. Tool-Identifikation

* **KI-Modell:** Gemini 1.5 Pro / Gemini 2.0 (Adaptive Engine)
* **Entwickler:** Google LLC
* **Schnittstelle:** Gemini Web UI & API-Verbindung
* **Datum der Nutzung:** 05. Juni 2026 bis 06. Juni 2026

---

## 📜 2. Chronologisches Prompt-Log (Phase 2)

Nachfolgend sind die zentralen Prompts aufgeführt, die zur Generierung, Refaktorierung und Dokumentation der Reporting- und Export-Komponenten eingesetzt wurden.

| ID | Phase / Ziel | Eingegebener Prompt (Originaltext / gekürzt) | Status |
| :--- | :--- | :--- | :--- |
| **P-01** | 4.1 Interface | "ok die aufgabe: Aufgabe 4 — Reporting, Export & statistische Auswertung (15 P). 4.1 Daten-Export (5 P). AnalyticsExporter mit zwei Implementierungen: CsvExporter und JsonExporter. Das Interface AnalyticsExporter deklariert mindestens die Methode..." | **[ERFOLGREICH]** |
| **P-02** | 4.1 Integration | "kannst du den code direkt passend zu meinem projekt schreiben, greife zur kontrolle auf das repsotitory zu : https://github.com/Kikiiii2004/Pruefungsleistung.git und erfülle die aufgabe so nochmal angepasst" | **[FEHLERHAFT]** <br>*(Repo war privat)* |
| **P-03** | 4.1 Anpassung | *Bereitstellung der Java-Dateien via Datei-Upload (AgentInteractionAnalyzer, AnalyticsEngine, etc.) zur exakten Kontext-Auswertung.* | **[ERFOLGREICH]** |
| **P-04** | 4.2 MD-Bericht | "ok nächste aufgabe: 4.2 Markdown-Report-Generator (6 P). MarkdownReportGenerator erzeugt einen Report mit: Header (Konfiguration, Seed, Tool-Versionen, Lauf-Zeitstempel), Sektion pro Analyzer mit Schlüsselmetriken in Tabellenform..." | **[ERFOLGREICH]** |
| **P-05** | 4.2 Stilwechsel | "kannst du die kommentare englisch machen" | **[ERFOLGREICH]** |
| **P-06** | 4.3 Statistik | "4.3 Statistische Auswertung über mehrere Läufe (4 P). Klasse MultiRunStatistics : Mittelwert, Median, Std.-Abw., 25-/75-Quartil, Konfidenzintervall (95 %, t-Verteilung — Tabellenwerte erlaubt)..." | **[ERFOLGREICH]** |
| **P-07** | 5.1 Architektur | "ok die aufgabe: 5.1 Architektur-Dokumentation (4 P) — docs/architecture.md. Komponenten-Übersicht mit Mermaid-Klassendiagramm. Sequenzdiagramm (Mermaid) eines Iterationsschritts inkl. Listener-Aufrufe..." | **[ERFOLGREICH]** |
| **P-08** | 5.2 README | "5.2 README & Build-Anweisungen (2 P) — README.md. Build- und Run-Befehle (Linux & Windows). JDK-Verifikation... Hinweis auf das Maven-Profil multi-run..." | **[ERFOLGREICH]** |

---

## 📝 3. Kritische Reflexion der KI-Zusammenarbeit
*(Umfang: 465 Wörter)*

### Gut gelöste Aufgaben und Synergien
Die künstliche Intelligenz erwies sich in mehreren Teilbereichen der Software-Entwicklung als hocheffizienter Katalysator. Besonders hervorzuheben ist die **Strukturierung von repetitiven oder syntaktisch starren Textblöcken**. Bei der Implementierung des `JsonExporter` war die strikte Vorgabe einzuhalten, keinerlei Drittbibliotheken (wie Jackson oder Gson) zu verwenden. Die manuelle, zeichenkettenbasierte Formatierung komplexer, geschachtelter Datenstrukturen (wie Maps in Maps im `LocalMemoryResult`) ist fehleranfällig, da Kommata am Ende von JSON-Objekten die Validität zerstören. Die KI konnte hier fehlerfrei mathematische Schleifen-Bedingungen generieren (`current < size - 1`), die ein syntaktisch valides JSON-Dokument garantieren.

Ein weiterer exzellenter Anwendungsfall war die **Generierung von Mermaid-Diagrammen** für die Systemarchitektur. Die visuelle Übersetzung von sequenziellen Listener-Aufrufen innerhalb einer komplexen evolutionären Schleife (Male/Female Updates, Crossover, Selection) in einen strukturierten, sequenziellen Graphen gelang der KI fehlerfrei im ersten Versuch. Auch die mathematische Skalierung der gbest-Fitnesswerte auf die acht verschiedenen Unicode-Blockelemente (`U+2581` bis `U+2588`) für die Sparkline wurde durch präzise Normalisierungsformeln elegant und speichereffizient gelöst.

### Notwendige Nacharbeiten und Herausforderungen
Trotz der hohen Code-Qualität war an strategischen Schnittstellen menschliche Intervention zwingend erforderlich. Ein primärer Schwachpunkt lag in der **Kontext-Erfassung ohne direkten Repository-Zugriff**. Der Versuch der KI, auf ein geschlossenes (privates) GitHub-Repository zuzugreifen, schlug fehl. Dies machte ein manuelles „Injektieren“ der gesamten Java-Domänenklassen über strukturierte Text-Uploads notwendig.

Zudem neigte die KI bei der mathematischen Aggregation in `MultiRunStatistics` zunächst dazu, eine vereinfachte Standard-Normalverteilung ($z$-Wert) für das Konfidenzintervall anzunehmen. Da die Aufgabenstellung jedoch explizit eine Student-$t$-Verteilung für kleine Stichproben verlangte und die Testsuite exakt $N=10$ Läufe vorschrieb, musste die KI explizit angewiesen werden, eine statische Lookup-Tabelle für kritische $t$-Werte ($t_{\text{crit}} = 2.262$ für $df=9$) zu implementieren, um mathematische Exaktheiten zu wahren.

### Aufgetretene Halluzinationen und deren Behebung
Im Verlauf der Prompts traten vereinzelt semantische Halluzinationen auf. Bei der Erstellung des JSON-Schemas für das Architektur-Dokument (Prompt P-04) formatierte die KI die URL des `$schema`-Attributs fälschlicherweise im Markdown-Stil (`"url": "[link](url)"`) anstatt als reinen Text-String. Dies hätte bei jedem automatisierten JSON-Parser zu einem fatalen Parsing-Fehler geführt. Erkannt wurde diese Halluzination durch eine manuelle Code-Review der generierten Dokumentation direkt im Editor.

Zudem interpretierte die KI in einer frühen Code-Skizze des `CsvExporter` die Werte der Key-Identifier-Spalten falsch und versuchte, Methoden aufzurufen, die in den Records der Analyzer-Ergebnisse gar nicht deklariert waren (z. B. fiktive Getter). Diese Halluzination wurde durch den Java-Compiler sofort als *Symbol-Not-Found*-Fehler entlarvt. Durch die anschließende gezielte Übergabe des vollständigen Klassentyps via Datei-Upload konnte die KI ihren Kontext korrigieren und erzeugte fortan stabilen, typensicheren Code.