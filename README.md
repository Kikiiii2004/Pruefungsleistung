# 🛸 Mayfly Optimization Suite — Analytics Framework

This repository implements a stateless, decoupled variant of the **Mayfly Swarm Intelligence Optimization Algorithm** featuring real-time telemetry extraction, native serialization strategies, and multi-run statistical evaluation.

---

## 🛠️ Prerequisite: JDK Verification

Before executing build or runtime commands, verify that your local environment is configured with a supported **Java Development Kit (JDK 25)**.

To check your current active Java version, open your terminal and execute:
```bash
java -version
⚠️ Note: Ensure that the output confirms a version $\ge 25$. If a lower version is shown, update your Core JAVA_HOME environment variable to point to a valid JDK 25 installation.🚀 Build & Execution CommandsThe project uses Apache Maven as its build automation tool. Follow the instructions below based on your operating system.🐧 Linux / macOSOpen a shell terminal in the project root directory:Bash# Clean project and run the complete validation suite (including JGiven BDD tests)
mvn clean verify

# Execute the main application loop directly
mvn exec:java -Dexec.mainClass="edu.swarmintelligence.mayfly.Main"
🪟 WindowsOpen PowerShell or Command Prompt (cmd) in the project root directory:PowerShell# Clean project and run the complete validation suite
mvn clean verify

# Execute the main application loop directly
mvn exec:java "-Dexec.mainClass=edu.swarmintelligence.mayfly.Main"
📊 Automated Telemetry & Test ReportingEvery successful full verification compile cycle (mvn clean verify) automatically aggregates multi-layered validation reports into the target/ artifact directory.📍 Report Paths & LocationsReport TypePurpose / DescriptionRelative File Path LocationJGiven BDD ReportScenario-based functional acceptance criteria verification dashboard.target/jgiven-report/html/index.htmlJaCoCo Test CoverageStructural test coverage matrix (Line, Branch, and Mutation checkpoints).target/site/jacoco/index.htmlMayfly Analytics DocumentNatively generated markdown report containing performance charts and sparklines../Mayfly_Analytics_Report.md🧪 Advanced Multi-Run Verification ProfileStochastic optimization metaheuristics exhibit structural variations depending on random initialization states. To thoroughly evaluate algorithm stability and population robustness, a dedicated Maven build profile is configured in the pom.xml.To trigger an aggregated multi-run evaluation series ($N = 10$) featuring the compiled MultiRunStatistics evaluations, execute:Bashmvn -Pmulti-run verify
Profile Architecture:Activates deep structural performance analysis tracking.Generates an extended Mayfly_Analytics_Report.md file appended with a 95% Confidence Interval (CI) table calculated via finite Student-t distributions ($t_{\text{crit}} = 2.262$).📁 Repository Directory LayoutPlaintext.
├── docs/
│   └── architecture.md           # Unified Task 3.3, 4.1, 4.2 & 5.1 System Documentation
├── src/
│   ├── main/
│   │   └── java/
│   │       └── edu/swarmintelligence/mayfly/
│   │           ├── AgentInteractionAnalyzer.java
│   │           ├── AnalyticsEngine.java
│   │           ├── AnalyticsExporter.java        # Strategy Pattern Interface
│   │           ├── AnalyticsReport.java          # In-Memory Telemetry Record
│   │           ├── CsvExporter.java              # Native CSV Serialization Strategy
│   │           ├── JsonExporter.java             # Native Third-Party-Free JSON Exporter
│   │           ├── MarkdownReportGenerator.java  # Sparkline & Mermaid Engine
│   │           ├── MultiRunStatistics.java       # Student-t Distribution Aggregator
│   │           └── Main.java                     # Application Entry Point
│   └── test/
│       └── java/
│           └── bdd/                              # JGiven BDD Verification Suite
│               ├── GivenMayflyConfiguration.java
│               ├── WhenAlgorithmRuns.java
│               ├── ThenAnalyticsReport.java
│               └── MayflyAlgorithmBddTest.java   # AT-1 to AT-7 Acceptance Tests
├── Mayfly_Analytics_Report.md    # Dynamically generated runtime Markdown artifact
├── jgiven-dashboard.png          # Visual proof of Task 3.3 Acceptance
├── pom.xml                       # Maven Configuration (including 'multi-run' profile)
└── README.md                     # This System Overview File