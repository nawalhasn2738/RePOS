# Task 4.1: selected refactorings

Selection recorded before implementation, 2026-11-25. Owner: Nawal. The reengineered directory initially contains only .gitkeep. Existing Phase 1–3 documents and every legacy asset remain unchanged.

## RF-1: inventory presentation boundary

Legacy evidence: [ProductManager](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/product/ProductManager.java), getTableModel(), getUnderstockTableModel(), deductQuantity() and generatePurchaseOrder(). It owns stock/file operations plus two Swing models. Entering understock without first constructing a view can dereference a null model.

Extract both product table models and their event translation into a presentation adapter. ProductManager emits toolkit-independent catalog change notifications; no Swing imports or required view initialization remain in that manager. Keep stock arithmetic, category/barcode/ID rules, text files and write order. Removing the view prerequisite is the one intentional corrective effect, directly within FO-06/R-09.

This is a cohesive two-view/event-lifecycle extraction, not a cosmetic class split. Test standalone stock changes, both view projections, insertion/update/rebuild events, invalid operations and adapter disposal.

## RF-2: application composition and startup seams

Legacy evidence: [StoreApplication.main()](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/StoreApplication.java) constructs all managers, embeds paths and wires seven feature tabs after modal login.

Extract working-directory data-path configuration, manager construction context and window assembly into focused bootstrap classes. Keep login before loading the rest of the store, manager construction order, seven tab order, callback targets and default data directory. Main remains a thin launcher. Test configurable paths and context wiring without a window; test feature assembly as headless Swing panels without launching the application.

This moves environment/composition concerns out of one large method and gives Iman/Hamna a concrete handover seam. It does not extract sale policy, replace authentication or redesign persistence. Part of FO-05, within Nawal's initial architecture responsibility.

## RF-3: transaction-report presentation binding

Legacy evidence: [ReportPanel](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/gui/ReportPanel.java), createTransactionReportPanel() and setTransactionTableModel(). One class owns layouts, model attachment, sorter construction, date rendering, initial bounds and filter composition.

Extract a presentation-only transaction report binding that accepts an existing TableModel, owns sorter/rendering and applies the same inclusive Date filter. ReportPanel retains controls/layout and user feedback. Preserve first/last-row initialization, exact timestamp comparisons and invalid-range message; do not correct date/timezone/business rules or history loading.

This creates a reusable/testable model-to-view binding without loading managers or files. It complements FO-05's presentation adapters; it is deliberately distinct from Iman's date policy work (FO-11). Test boundaries, reversed ranges, empty/rebound models, rendering and actual ReportPanel wiring.

## Working-port and validation scope

Copy the existing 40 production Java files into reengineered/src/main/java with their original packages so the three changes integrate with real callers and the complete port compiles. This is a working copy of existing functionality, not implementation of a new whole POS application. Change only the three selected responsibility areas. Do not copy original build scripts or create live data inside legacy. Unrelated source and known business/persistence defects remain as comparison evidence in the port.

Use available Java tooling for compilation and focused tests, with temporary outputs/fixtures. Local inspection found JDK 25 and some JUnit 6 artifacts, but no legacy JUnit 4 JAR or JUnit console launcher in the repository/checked Maven cache. No dependencies were installed. A dependency-free Java harness now executes the focused checks; results and limits are recorded in [refactoring evidence](refactoring-evidence.md). No staging, commit or push is authorized by this task.

## Implemented scope

All three selections are integrated into a compilable working port: 40 copied production files, of which three were refactored, plus seven new production classes/interfaces. The other 37 imports remain byte-identical. The harness has 21 passing focused tests (nine RF-1, five RF-2, seven RF-3). This does not certify the complete desktop application, the original JUnit suite, Java 8 runtime compatibility or the unresolved legacy business/persistence defects.
