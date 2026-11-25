# RePOS — Legacy POS Reengineering

RePOS is a Software Engineering semester project to reengineer an existing legacy Java Point-of-Sale (POS) system through a structured process, preserving the original system for comparison.

**Team:** Nawal, Iman, and Hamna.

**Project repository:** https://github.com/nawalhasn2738/RePOS.git

**Original legacy repository:** https://github.com/yangzai/unit1-ca

## Reengineering phases

1. Inventory Analysis
2. Document Restructuring
3. Reverse Engineering
4. Code Restructuring
5. Data Restructuring
6. Forward Engineering

## Repository structure

- `legacy/`: the preserved original baseline. Its source, directory structure, documentation, tests, configuration, and sample data must remain unchanged for comparison throughout the project. See [baseline provenance](docs/legacy/BASELINE.md) for the original commit.
- `reengineered/`: reserved for future reengineered implementation.
- `docs/`: project documentation, organized into `inventory/`, `legacy/`, `reverse-engineering/`, `architecture/`, `refactoring/`, `testing/`, and `handover/`.
- `tests/`: reserved for future project-level tests; original tests remain in `legacy/test/`.

This foundation does not introduce implementation changes or an architecture redesign.
