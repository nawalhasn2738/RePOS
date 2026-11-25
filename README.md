# RePOS — Legacy POS Reengineering

RePOS is a Software Engineering semester project by **Nawal, Iman, and Hamna**. We are reengineering an existing Java Point-of-Sale system while preserving its original implementation for comparison.

- **RePOS repository:** https://github.com/nawalhasn2738/RePOS.git
- **Original repository:** https://github.com/yangzai/unit1-ca
- **Preserved original commit:** `87bee5fed6e44cf87fd91a05de42a6817ae81002`
- **Baseline provenance:** [docs/legacy/BASELINE.md](docs/legacy/BASELINE.md)

## Original system

The legacy project is the **University Souvenir Store Application**, a Java Swing/AWT desktop application for store staff. After login, seven tabs provide checkout, category/member/product creation, inventory replenishment, discounts and reports.

The source uses comma-delimited text files, in-memory collections and Swing table models. Receipt output is a dialog plus console text. This description comes from static source inspection; successful compilation, GUI operation and test results have not yet been established.

Read the [system overview](docs/legacy/system-overview.md) and [functional documentation](docs/legacy/functional-documentation.md) for actual workflows and known defects. [Setup and execution](docs/legacy/setup-and-execution.md) explains the original environment and why the preserved checkout is not a prepared runtime.

## Objectives and current progress

Our objectives are to understand the existing system, preserve accepted functionality, clarify documentation, identify risks and restructure implementation/data through reviewable steps. Relational persistence and responsibility separation are **planned work**, not implemented capabilities.

| Reengineering phase | Current progress |
|---|---|
| 1. Inventory Analysis | Completed and published in commit `d13f8c1`; five inventory documents describe assets, dependencies, risks and priorities. |
| 2. Document Restructuring | This README and four legacy guides are prepared for review; not yet committed or pushed. |
| 3. Reverse Engineering | Dedicated phase work remains pending; inventory-stage source reconnaissance is available. |
| 4. Code Restructuring | Not implemented. |
| 5. Data Restructuring | Not implemented; original persistence remains text-file based. |
| 6. Forward Engineering | Not implemented. |

The original system remains unchanged. `reengineered/` and root `tests/` currently contain placeholders; original tests remain in `legacy/test/`. No database migration, architecture improvement or new feature is claimed complete.

## Repository structure

```text
RePOS/
├── legacy/                    # Complete, preserved original baseline
├── reengineered/              # Reserved for future implementation
├── docs/
│   ├── inventory/             # Phase 1 asset, dependency and risk analysis
│   ├── legacy/                # Provenance and restructured original-system guides
│   ├── reverse-engineering/   # Reserved for later phase documentation
│   ├── architecture/          # Reserved for later design decisions
│   ├── refactoring/           # Reserved for later restructuring records
│   ├── testing/               # Reserved for later verification evidence
│   └── handover/              # Reserved for later operational handover
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

Original material is retained in [legacy/README.md](legacy/README.md) and [the original Word report](legacy/submission/docs/report.doc). The report's contents remain unverified and have not been rewritten. The new guides explain inspected evidence; they do not replace unknown original requirements with assumptions.
