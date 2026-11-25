# RePOS — Legacy POS Reengineering

RePOS is a Software Engineering semester project by **Nawal, Iman, and Hamna**. We are reengineering an existing Java Point-of-Sale system while preserving its original implementation for comparison.

- **RePOS repository:** https://github.com/nawalhasn2738/RePOS.git
- **Original repository:** https://github.com/yangzai/unit1-ca
- **Preserved original commit:** `87bee5fed6e44cf87fd91a05de42a6817ae81002`
- **Baseline provenance:** [docs/legacy/BASELINE.md](docs/legacy/BASELINE.md)

## Original system

The legacy project is the **University Souvenir Store Application**, a Java Swing/AWT desktop application for store staff. After login, seven tabs provide checkout, category/member/product creation, inventory replenishment, discounts and reports.

The original source uses comma-delimited text files, in-memory collections and Swing table models. Receipt output is a dialog plus console text. Legacy behavior is documented from source inspection. Compilation and focused tests of the reengineered working copy have succeeded; full desktop operation and application equivalence remain unverified.

Read the [system overview](docs/legacy/system-overview.md) and [functional documentation](docs/legacy/functional-documentation.md) for actual workflows and known defects. [Setup and execution](docs/legacy/setup-and-execution.md) explains the original environment and why the preserved checkout is not a prepared runtime.

## Objectives and current progress

Our objectives are to understand the existing system, preserve accepted functionality, clarify documentation, identify risks and restructure implementation/data through reviewable steps. Initial responsibility separations are implemented in `reengineered/`; relational persistence and the application/service layer remain pending.

| Reengineering phase | Current progress |
|---|---|
| 1. Inventory Analysis | Completed and published in `d13f8c1`: five documents cover assets, dependencies, risks and priorities. |
| 2. Document Restructuring | Completed and published in `3875c48`: root README and four legacy guides. |
| 3. Reverse Engineering | Completed and published in `52a705e`: architecture, workflows, business rules, data reconstruction and refactoring opportunities. |
| 4. Code Restructuring | Nawal's scoped work completed and published in `460f737`: inventory presentation, startup composition and transaction-report binding refactorings, with focused tests. Broader service/persistence restructuring remains pending. |
| 5. Data Restructuring | Handover contracts and proposed persistence architecture documented and published in `297322f`. Database schema, migrations and repository implementations remain pending. |
| 6. Forward Engineering | Pending shared implementation and integration. |

`legacy/` preserves the original application. `reengineered/` contains a working source copy with the three implemented refactorings, seven new production types, a test harness and a PowerShell test script. It still uses the legacy file-backed managers and retains known business/persistence defects; it is not a completed reengineered POS. Original tests remain in `legacy/test/`; root `tests/` is reserved for future project-level tests.

## Validation evidence and focused test command

The [refactoring evidence](docs/refactoring/refactoring-evidence.md) records successful Java 8-target compilation of 48 Java files and **21 focused test scenario groups passed, zero failed**: nine inventory, five composition and seven report-binding scenarios. The independent audit also reproduced this result using Temurin JDK `25.0.5.1`.

From the repository root in PowerShell, with `java` and `javac` on PATH and a JDK supporting `--release`:

```powershell
& ./reengineered/scripts/test.ps1
```

The [test script](reengineered/scripts/test.ps1) compiles with `javac --release 8 -encoding UTF-8` and runs the dependency-free harness headlessly. Build output and synthetic fixtures use temporary directories and are cleaned afterward; no prepared live data folder is required for these tests.

This verifies the Java 8 language/API target, **not execution on a Java 8 VM**. Full end-to-end GUI workflows, complete application equivalence, the original JUnit suite, database integration and failure recovery have not been validated. The current working copy has no prepared live runtime data directory or new packaged application; see [legacy setup and execution](docs/legacy/setup-and-execution.md) for the original environment and data requirements.

## Team contributions and ownership

| Team member | Contribution / assigned responsibility |
|---|---|
| Nawal | Completed legacy analysis, architecture/documentation foundation, reverse engineering, initial code refactoring and integration contracts. |
| Iman | Owns business logic, application and service-layer implementation; these target implementations remain pending. |
| Hamna | Owns the relational data model, migrations and persistence/repository implementation; these target implementations remain pending. |
| Shared | Final integration, regression testing and forward engineering. |

Each teammate should work on their own feature branch and submit a **Pull Request to `main`** after completing and validating their assigned work. Coordinate interface and behavior decisions using the [team integration guide](docs/handover/team-integration-guide.md) and [data persistence contract](docs/handover/data-persistence-contract.md) before integrating service and database changes. These documents describe proposed contracts, not existing Java repository interfaces.

## Repository structure

```text
RePOS/
├── legacy/                    # Complete, preserved original baseline
├── reengineered/              # Working source copy with implemented refactorings
│   ├── src/main/java/         # Application sources and extracted responsibilities
│   ├── src/test/java/         # Focused refactoring test harness
│   └── scripts/test.ps1       # Temporary-output compile/headless test runner
├── docs/
│   ├── inventory/             # Phase 1 asset, dependency and risk analysis
│   ├── legacy/                # Provenance and restructured original-system guides
│   ├── reverse-engineering/   # Reconstructed architecture, workflows, rules and data
│   ├── architecture/          # Reserved for later design decisions
│   ├── refactoring/           # Selected refactorings, evidence and validation limits
│   ├── testing/               # Reserved for later verification evidence
│   └── handover/              # Proposed persistence and team integration contracts
├── tests/                     # Reserved for future project-level tests
└── README.md
```

**Keep `legacy/` untouched:** its source, tests, configuration, sample data, documentation and bundled tools are the comparison baseline. Do not build, create live data or run fixture-writing tests inside it. Any later runtime investigation should use a separately prepared disposable copy.

## Documentation guide

| Start here | Purpose |
|---|---|
| [System overview](docs/legacy/system-overview.md) | Users, seven tabs, modules and startup/workflow map. |
| [Setup and execution](docs/legacy/setup-and-execution.md) | Java/build/test requirements, historical commands, data layout and execution limits. |
| [Functional documentation](docs/legacy/functional-documentation.md) | Source-observed operations, rules, persistence effects and defect boundaries. |
| [Documentation gaps](docs/legacy/documentation-gaps.md) | Missing, outdated, contradictory and unverified information. |
| [Asset inventory](docs/inventory/asset-inventory.md) | All 90 original files and proposed reuse classifications. |
| [Inventory summary](docs/inventory/inventory-summary.md) | Calculated totals and major reuse/modification findings. |
| [Inventory findings](docs/inventory/inventory-findings.md) | Source-backed issues and investigation priorities. |
| [Dependency map](docs/inventory/dependency-map.md) | Existing relationships, cycles and proposed target separation. |
| [Reengineering priorities](docs/inventory/reengineering-priorities.md) | Risk register, preservation decisions, ownership and suggested execution gates. |
| [Architecture reconstruction](docs/reverse-engineering/architecture-reconstruction.md) | Original startup, responsibilities, dependencies and architectural diagrams. |
| [Workflow reconstruction](docs/reverse-engineering/workflow-reconstruction.md) | Source traces for nine workflow groups and existing test gaps. |
| [Business rules](docs/reverse-engineering/business-rules.md) | Observed formulas, eligibility, loyalty and unresolved behavior. |
| [Data reconstruction](docs/reverse-engineering/data-reconstruction.md) | Original file formats, relationships, integrity risks and proposed relational model. |
| [Refactoring opportunities](docs/reverse-engineering/refactoring-opportunities.md) | Proposed improvements, risks and accountable owners. |
| [Selected refactorings](docs/refactoring/selected-refactorings.md) | Nawal's three scoped implementation choices and justification. |
| [Refactoring evidence](docs/refactoring/refactoring-evidence.md) | Before/after responsibilities, tests, results and limitations. |
| [Data persistence contract](docs/handover/data-persistence-contract.md) | Proposed repository operations, transaction guarantees, migration requirements and open decisions. |
| [Team integration guide](docs/handover/team-integration-guide.md) | Proposed dependency flow, compatibility work and ownership handovers. |

Original material is retained in [legacy/README.md](legacy/README.md) and [the original Word report](legacy/submission/docs/report.doc). The report's contents remain unverified and have not been rewritten. The new guides explain inspected evidence; they do not replace unknown original requirements with assumptions.
