# 🤖 AI Usage Log & Reflexion — Mayfly Analytics Framework

Dieses Dokument dokumentiert chronologisch und kritisch den Einsatz von künstlicher Intelligenz im Rahmen der Bearbeitung von **Phase 2** des Projekts (Aufgaben 3–5).

---

## 1. Tool-Identifikation

| Eigenschaft          | Details                           |
|:---------------------|:----------------------------------|
| **KI-Modell**        | Claude Sonnet 4.6                 |
| **Entwickler**       | Anthropic PBC                     |
| **Schnittstelle**    | Claude.ai Web UI (chat.claude.ai) |
| **Nutzungszeitraum** | 05. Juni 2026 – 06. Juni 2026     |

---

## 2. Chronologisches Prompt-Log (Phase 2)

### Aufgabe 3 — JGiven-Akzeptanztests

| ID       | Zeitstempel      | Ziel                  | Prompt (gekürzt)                                                                                                                                                                        | Status            |
|:---------|:-----------------|:----------------------|:----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:------------------|
| **P-01** | 2026-06-05 10:12 | 3.1 BDD-Szenarien     | *„Kannst du JGiven-Akzeptanztests für den Mayfly-Algorithmus schreiben? Mindestens 6 Szenarien AT-1 bis AT-6 in Given/When/Then-Form mit @Description..."*                              | ✅ **ERFOLGREICH** |
| **P-02** | 2026-06-05 10:45 | 3.2 Stage-Architektur | *„Schreib mir GivenMayflyConfiguration, WhenAlgorithmRuns und ThenAnalyticsReport als wiederverwendbare JGiven-Stage-Klassen mit @ProvidedScenarioState und @ExpectedScenarioState..."* | ✅ **ERFOLGREICH** |
| **P-03** | 2026-06-05 11:20 | 3.4 Multi-Run         | *„Ergänze einen AT-7 Multi-Run-Akzeptanztest mit N=10 Seeds, der Mittelwert und Standardabweichung der finalen Fitness prüft..."*                                                       | ✅ **ERFOLGREICH** |

### Aufgabe 4 — Reporting, Export & Statistik

| ID       | Zeitstempel      | Ziel                   | Prompt (gekürzt)                                                                                                                                                                                       | Status                                                  |
|:---------|:-----------------|:-----------------------|:-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:--------------------------------------------------------|
| **P-04** | 2026-06-05 13:00 | 4.1 Interface          | *„Implementiere AnalyticsExporter mit CsvExporter und JsonExporter ohne Drittbibliotheken. Das Interface deklariert void export(AnalyticsReport report, Writer out) throws IOException..."*            | ✅ **ERFOLGREICH**                                       |
| **P-05** | 2026-06-05 13:30 | 4.1 Repo-Zugriff       | *„Kannst du den Code direkt zu meinem Projekt schreiben? Repository: https://github.com/Kikiiii2004/Pruefungsleistung.git"*                                                                            | ❌ **FEHLERHAFT** — Repo nicht zugänglich ohne Git-Clone |
| **P-06** | 2026-06-05 14:00 | 4.1 Kontext-Injektion  | *[Vollständiger Upload aller Java-Quelldateien als Datei-Anhänge zur Kontext-Bereitstellung]*                                                                                                          | ✅ **ERFOLGREICH**                                       |
| **P-07** | 2026-06-05 15:00 | 4.2 Markdown-Generator | *„Implementiere MarkdownReportGenerator mit: Header-Sektion, Metriktabellen pro Analyzer, Unicode-Sparkline (U+2581–U+2588), Mermaid-Diagramm für Plateaus oder Konvergenzkurve..."*                   | ✅ **ERFOLGREICH**                                       |
| **P-08** | 2026-06-05 15:45 | 4.3 Statistik          | *„Implementiere MultiRunStatistics: Mittelwert, Median, Std.-Abw. (Stichprobenvarianz), Q25/Q75, 95%-Konfidenzintervall via Student-t-Tabellenwerten. Für N=10 muss t_crit=2.262 verwendet werden..."* | ✅ **ERFOLGREICH**                                       |

### Aufgabe 5 — Dokumentation & KI-Reflexion

| ID       | Zeitstempel      | Ziel              | Prompt (gekürzt)                                                                                                                                                                                 | Status            |
|:---------|:-----------------|:------------------|:-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:------------------|
| **P-09** | 2026-06-06 09:00 | 5.1 Architektur   | *„Erstelle docs/architecture.md mit: Mermaid-Klassendiagramm, Sequenzdiagramm eines Iterationsschritts mit Listener-Aufrufen, ADRs für mindestens 3 Architekturentscheidungen..."*               | ✅ **ERFOLGREICH** |
| **P-10** | 2026-06-06 09:45 | 5.2 README        | *„Erstelle README.md mit: Build-Befehlen für Linux und Windows, JDK-Verifikationshinweis, Verzeichnisstruktur, Coverage-Report-Pfad, JGiven-Report-Pfad, Hinweis auf mvn -Pmulti-run verify..."* | ✅ **ERFOLGREICH** |
| **P-11** | 2026-06-06 10:30 | pom.xml Korrektur | *„Die pom.xml verwendet noch Java 21 statt 25. Korrigiere auf maven.compiler.release=25 und füge das multi-run Maven-Profil mit exec-maven-plugin hinzu..."*                                     | ✅ **ERFOLGREICH** |

---

## 3. Markierung KI-generierter Code-Abschnitte

Folgender Code wurde durch KI-Unterstützung erzeugt und ist entsprechend markiert:

- `CsvExporter.java` — Schleifenlogik für korrekte Semikolon-Trennzeichen ohne trailing separators
- `JsonExporter.java` — Zeichenketten-basierte JSON-Serialisierung inkl. Escape-Logik
- `MarkdownReportGenerator.java` — Sparkline-Normalisierungsformel, Mermaid-Diagrammgenerierung
- `MultiRunStatistics.java` — Student-t Lookup-Tabelle, Quantil-Interpolation
- `docs/architecture.md` — Mermaid-Klassen- und Sequenzdiagramme, ADR-Struktur
- `README.md` — Build-Anweisungen, Verzeichnisstruktur

---

## 4. Kritische Reflexion der KI-Zusammenarbeit

*(Umfang: 512 Wörter)*

### Gut gelöste Aufgaben und Synergien

Die künstliche Intelligenz erwies sich in mehreren Teilbereichen als hocheffizienter Katalysator. Besonders hervorzuheben ist die **Generierung syntaktisch strenger, repetitiver Strukturen**. Die native Implementierung des `JsonExporter` ohne externe Bibliotheken wie Jackson oder Gson erforderte präzise Schleifenbedingungen, um valides JSON zu garantieren — insbesondere das korrekte Handling von Kommas vor dem letzten Element einer Liste oder Map (`current < size - 1`). Hier produzierte die KI auf Anhieb korrekten, kompilierbaren Code, der manuell fehleranfällig gewesen wäre.

Ein weiterer exzellenter Anwendungsfall war die **Mermaid-Diagrammgenerierung**. Die Übersetzung eines komplexen, mehrschichtigen Beobachter-Musters (Observer-Pattern mit sealed Events, sequenziellen Listener-Aufrufen in Männchen-, Weibchen- und Mating-Phasen) in einen strukturell korrekten Sequenzgraphen gelang im ersten Versuch fehlerfrei. Ebenso wurde die mathematische Normalisierung der `gbestFitness`-Werte auf acht Unicode-Blockzeichen (`U+2581`–`U+2588`) durch eine elegante Normalisierungsformel korrekt implementiert.

Auch die **JGiven-Stages-Architektur** profitierte erheblich von KI-Unterstützung. Das korrekte Zusammenspiel von `@ProvidedScenarioState`, `@ExpectedScenarioState` und dem Fluent-Self-Type-Pattern (`self()`) erfordert präzises Verständnis des JGiven-Frameworks — die KI generierte alle drei Stage-Klassen strukturell korrekt im ersten Durchgang.

### Notwendige Nacharbeiten und Herausforderungen

Trotz hoher initialer Code-Qualität waren an strategischen Schnittstellen menschliche Eingriffe unumgänglich.

**Kontext-Erfassung ohne Repository-Zugriff (P-05):** Der Versuch, die KI direkt auf das GitHub-Repository zugreifen zu lassen, scheiterte, da keine direkte Git-Integration in der verwendeten Claude-Web-Oberfläche verfügbar war. Dies erforderte ein manuelles Injektieren sämtlicher Java-Quelldateien als Datei-Uploads (P-06), was zeitaufwändig, aber effektiv war.

**Java-Version-Fehler (P-11):** Die KI generierte die initiale `pom.xml` mit `<maven.compiler.source>21</maven.compiler.source>`, obwohl in allen Prompts explizit Java 25 gefordert wurde. Die Aufgabenstellung schreibt `maven.compiler.release=25` vor. Dieser Fehler hätte bei der automatisierten Build-Überprüfung zu einem direkten Punktabzug (-5 P für `--enable-preview`-Äquivalent oder Compiler-Fehler) geführt. Erkannt wurde er durch manuelle Kontrolle der `pom.xml`.

**Multi-Run Maven-Profil:** Das `multi-run`-Profil in der `pom.xml` fehlte vollständig im initialen KI-Output. Die Aufgabenstellung explizit verlangt `mvn -Pmulti-run verify` als ausführbaren Befehl. Erst nach explizitem Nachfragen (P-11) wurde das Profil korrekt mit `exec-maven-plugin` ergänzt.

### Aufgetretene Halluzinationen und deren Behebung

**Halluzination 1 — Konfidenzintervall mit z-Wert statt t-Wert:** In der initialen Skizze von `MultiRunStatistics` verwendete die KI den Standard-Normalverteilungs-z-Wert ($z = 1.96$) für das 95%-Konfidenzintervall. Da die Aufgabenstellung explizit eine Student-t-Verteilung für kleine Stichproben verlangt und die Testsuite $N=10$ Läufe vorschreibt, war dies mathematisch inkorrekt. Die KI musste explizit angewiesen werden, eine statische Lookup-Tabelle für $t_{\text{crit}}$-Werte zu implementieren ($t_{\text{crit}} = 2.262$ für $df=9$).

**Halluzination 2 — Nicht-existente Record-Getter:** Bei einer frühen Version des `CsvExporter` versuchte die KI, Methoden aufzurufen, die in den konkreten `AnalyzerResult`-Records nicht deklariert waren (fiktive Getter wie `getConvergenceRate()`). Diese Halluzination wurde durch den Java-Compiler sofort als `Symbol-Not-Found`-Fehler entlarvt. Durch anschließendes explizites Übergeben der vollständigen Record-Definitionen als Datei-Uploads konnte die KI die korrekten Methoden (`convergenceRateEstimate()`, `plateauSegments()`) identifizieren.

**Halluzination 3 — Falsche JSON-Schema-URL-Formatierung:** Im ersten Entwurf des Architektur-Dokuments formatierte die KI das `$schema`-Attribut im Markdown-Link-Stil (`"url": "[link](url)"`), was in einem echten JSON-Dokument zu einem Parser-Fehler geführt hätte. Erkannt durch manuelle Review der Dokumentation im Editor.

### Fazit

Der KI-Einsatz war in Phase 2 insgesamt produktivitätssteigernd. Wiederholbare, strukturell strenge Aufgaben (JSON-Serialisierung, Mermaid-Diagramme, Boilerplate-Code) wurden korrekt generiert. Bei mathematisch präzisen Anforderungen (t-Verteilung statt z-Verteilung) und Konfigurationsdateien (pom.xml Java-Version, Maven-Profile) war jedoch immer eine sorgfältige manuelle Nachprüfung zwingend notwendig. Blind übernommener KI-Code hätte an zwei kritischen Stellen (pom.xml, MultiRunStatistics) direkt zu Punktabzügen geführt.