# Task 4.1: refactoring evidence

Implementation/verification date: 2026-11-25. Selection was written in [selected-refactorings.md](selected-refactorings.md) **before copying or changing production code**. Nawal owns these three initial architecture/presentation refactorings. This is a working port with focused improvements, not a completed reengineered POS or approval of unresolved legacy behavior.

## Scope and files

- 40 baseline production Java files copied to reengineered/src/main/java using their original package names.
- Exactly three imported files changed: StoreApplication.java, product/ProductManager.java and gui/ReportPanel.java.
- The other **37 imported files are byte-identical** to their counterparts under legacy/src.
- Seven new production types, one test harness and one PowerShell build/test script.
- Two new refactoring documents. **51 new files total**: 47 production Java + one test Java + one script + two documents.
- No original tests, sample data, binaries or scripts were copied into an active runtime. Synthetic test fixtures and compiled classes live in temporary directories and are removed by the harness/script.
- No database, schema, dependency download or unrelated business-rule correction was introduced.

The full file manifest is below. Existing .gitkeep placeholders remain untouched.

## RF-1: inventory view/event extraction

**Before:** [legacy ProductManager](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/product/ProductManager.java) owned product state/file operations plus two anonymous AbstractTableModel implementations and direct Swing fireTable* calls. deductQuantity() could enter understock then dereference a null view model.

**After:** [ProductManager](../../reengineered/src/main/java/sg/edu/nus/iss/se24_2ft/unit1/ca/product/ProductManager.java) owns the same stock/list/ID/barcode/text-file operations and emits [ProductCatalogListener](../../reengineered/src/main/java/sg/edu/nus/iss/se24_2ft/unit1/ca/product/ProductCatalogListener.java) callbacks. [ProductTableModels](../../reengineered/src/main/java/sg/edu/nus/iss/se24_2ft/unit1/ca/gui/ProductTableModels.java) owns the two Swing models, a single shared eight-column projection, event translation and subscription disposal.

| Dependency / responsibility | Before | After |
|---|---|---|
| Stock owner → view | Direct javax.swing.table API and nullable model fields | Toolkit-independent listener contract; no Swing or AWT type/import in ProductManager |
| Full/understock projection | Two nearly duplicated anonymous manager models | ProductTableModels owns both and shares ProductRows |
| Table updates | Manager fires table events | Adapter translates add/quantity/rebuild events |
| Lifecycle | View field existence affects valid stock operation | Zero listeners is supported; close() removes adapter subscription |
| File writes | ProductManager.load()/store() | Same owner, format, sorted IDs and write-after-notifications pattern |

**Substantial change:** extracts two projections and their event/lifecycle behavior, removing a real view-initialization precondition. New getUnderstockProducts() exposes an unmodifiable list view; Product objects remain live. Existing getProductList() mutability is deliberately unchanged.

**Behavior preserved:** threshold equality, quantity deduction, immediate orderQuantity replenishment, barcode/ID behavior, column order/labels/types and product file schema. In particular, the legacy “Reorder Quantity” label still shows threshold. The only intentional corrective effect is that entering understock without any view now succeeds and persists rather than failing on a null table model.

**Results:** nine RF-1 tests passed: standalone threshold crossing/write; all eight projected values/labels; insert/update events; product insertion; restock rebuild/events/write; missing/insufficient product rejection without mutation; duplicate barcode/null listener; null/empty replenishment no-op; adapter unsubscribe.

**Limits:** ProductManager still owns file I/O and concrete category lookup; CategoryManager and other managers still have Swing coupling. Notifications are synchronous on the caller thread and precede storage, as the old event/write pattern did. Both catalog lists now reach their final mutation state before the grouped adapter callback; this is an internal observer-timing change, not a new stock rule. Listener exceptions are not isolated; no durability or thread-safety claim is made.

## RF-2: startup and feature composition

**Before:** [legacy StoreApplication.main()](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/StoreApplication.java) mixed relative paths, modal login, manager construction, all panel bindings, window close behavior and display.

**After:**

| Source | Focused responsibility |
|---|---|
| [StorePaths](../../reengineered/src/main/java/sg/edu/nus/iss/se24_2ft/unit1/ca/bootstrap/StorePaths.java) | Pure path resolution; default remains working-directory data; no directory creation or I/O. |
| [StoreContext.load()](../../reengineered/src/main/java/sg/edu/nus/iss/se24_2ft/unit1/ca/bootstrap/StoreContext.java) | Concrete manager construction in original category/vendor/product/member/discount/transaction order; managers retain persistence ownership. |
| [StoreFeatures](../../reengineered/src/main/java/sg/edu/nus/iss/se24_2ft/unit1/ca/gui/StoreFeatures.java) | Constructs/binds seven existing FeaturePanels without a JFrame or LoginPanel; owns product-model adapter lifetime. |
| [StoreWindowFactory](../../reengineered/src/main/java/sg/edu/nus/iss/se24_2ft/unit1/ca/gui/StoreWindowFactory.java) | MainFrame assembly, window-close cleanup/process exit and pack(). |
| [StoreApplication.main()](../../reengineered/src/main/java/sg/edu/nus/iss/se24_2ft/unit1/ca/StoreApplication.java) | Staff load/login first; only after success load StoreContext, create and show the window. |

**Before/after direction:** main → all individual managers/panels becomes launcher → path/context/window composition; feature binding is separated from top-level window/process creation. Context is a concrete bootstrap, **not** a new service layer or a repository facade.

**Substantial change:** extracts environment and manager/window composition while integrating RF-1 adapters into actual product/inventory/report callers. It provides a headless composition seam and explicit handover points without changing Iman's service policy or Hamna's storage design.

**Behavior preserved:** the same seven tab titles/order, four add callbacks, inventory callback, checkout lookups/completion callback, shared report models, staff login before remaining file loads and window-close process exit. Default arguments remain ignored, as before; no new command-line configuration feature is claimed. Source audit confirms the original binding block remains equivalent apart from product adapter access.

**Results:** five RF-2 tests passed: pure paths/defaults/null contracts; configured context read with no fixture rewrite; seven exact feature types/order and immutable panel map; real Category/Member/Product/Discount Add buttons update the bound managers; shared product/inventory/report projections respond to stock changes.

**Limits:** main(), modal login, JFrame display, process exit, checkout/payment dialogs and the entire desktop lifecycle were not executed. The test assembles real JPanel features headlessly on Swing's event-dispatch thread. Context construction still loads real legacy file managers and inherits their errors/defects. close() removes only the new product adapter; it is not a general application shutdown/resource framework.

## RF-3: transaction report binding

**Before:** [legacy ReportPanel](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/gui/ReportPanel.java) combined layout/controls with model attachment, sorting, rendering, date bounds and filter composition.

**After:** [TransactionReportBinding](../../reengineered/src/main/java/sg/edu/nus/iss/se24_2ft/unit1/ca/gui/TransactionReportBinding.java) binds a supplied TableModel to a JTable, owns its sorter/date renderer, initializes first/last-row dates and applies the same inclusive RowFilter composition. [ReportPanel](../../reengineered/src/main/java/sg/edu/nus/iss/se24_2ft/unit1/ca/gui/ReportPanel.java) retains its layout, controls and reversed-range user message, delegating bind/filter operations.

**Before/after direction:** ReportPanel → sorter/rendering/filter construction becomes ReportPanel → presentation binding → Java Swing TableModel/RowFilter. The binding imports no manager or file API and performs no domain/persistence query. It still calls the unchanged Util formatter.

**Substantial change:** extracts a complete model-to-report-view responsibility rather than only a renamed filter helper. Both actual setTransactionTableModel() and Get Report handler use it.

**Behavior preserved:** exact inclusive Date timestamps; no end-of-day expansion; first/last encountered rows rather than min/max; empty model leaves prior field values; rebinding resets sorter/filter; same date format and invalid-range message. Null collaborators/model and zero-column input receive explicit new adapter-contract validation; ordinary seven-column transaction reports are unaffected.

**Results:** seven RF-3 tests passed: inclusive/equal bounds; reversed range preserves current filter; empty/unsorted/rebound models; Date/non-Date rendering; null/zero-column/pre-bind input; no silent full-day expansion; actual ReportPanel Get Report button delegation.

**Limits:** no date/timezone/invalid-date policy correction, revenue aggregation, history repair or database query was added. The inherited first/last bounds and mutable Util.DATE_FORMAT remain. GUI error dialogs were not displayed; reversed-range failure was tested at the extracted API.

## Actual build and test execution

Environment observed: Windows PowerShell **5.1.26100.9818**, Temurin **25.0.5.1**, javac **25.0.5.1**. No dependency was installed.

From the repository root:

```powershell
& ./reengineered/scripts/test.ps1
```

[test.ps1](../../reengineered/scripts/test.ps1) compiles only the reengineered main/test trees with `javac --release 8 -encoding UTF-8`, then runs [RefactoringTests](../../reengineered/src/test/java/repos/refactoring/RefactoringTests.java) using `java -Djava.awt.headless=true`. It exits with failure if compilation or a test fails; explicit assertions do not depend on -ea. Script outputs and synthetic fixture directories are temporary, outside legacy and repository output trees, and cleaned afterward.

Final actual output summary:

```text
javac 25.0.5.1
warning: [options] source value 8 is obsolete and will be removed in a future release
warning: [options] target value 8 is obsolete and will be removed in a future release
warning: [options] To suppress warnings about obsolete options, use -Xlint:-options.
3 warnings
Compiled 48 source files with --release 8
RESULT: 21 tests passed; 0 failed
```

Compilation and all 21 tests succeeded on both executions; the second followed final source cleanup. No further test breadth or original-suite pass is claimed.

| Test group | Tests | Result / side-effect boundary |
|---|---:|---|
| RF-1 inventory adapters and stock integration | 9 | Passed; only synthetic temporary category/product files used. |
| RF-2 context and real feature binding | 5 | Passed; synthetic temporary data, headless JPanel controls; no top-level window. |
| RF-3 report binding | 7 | Passed; in-memory TableModels/JTables, no managers/files. |
| Total | 21 | Passed; no failed tests. |

### Tooling blockers and limits

- **No blocker for this harness:** Java compilation and focused tests ran successfully. JDK 25 reports three Java-8-target obsolescence warnings; these are not errors.
- `--release 8` checks the Java 8 language/API target; **actual Java 8 VM execution is unverified**. The script itself needs a JDK supporting --release, and was tested on JDK 25.
- The repository references JUnit 4.12/Hamcrest 1.3 through IDE configuration. No JUnit 4 JAR or console launcher was found in the repository/checked local Maven cache; some JUnit 6 API/engine artifacts and Hamcrest 3.0 exist there. The new harness intentionally requires only the available JDK. **The original 45 annotated tests were not run**, copied or relabeled as these 21 tests.
- The port has no prepared live data folder or new packaging artifact. Launching the full desktop application still requires separate writable runtime data/setup and manual validation.
- Atomic sale persistence, history/customer defects, plaintext authentication, financial/date rules, payment-state concerns and broader validation remain with their established owners. No new database or sale-service implementation is hidden in this task.

## Preservation and structural verification

Static/diff audit confirms:

1. Only three imported originals differ; the remaining 37 port source files are byte-identical.
2. ProductManager.load(), store(), getProduct(), getProductList() and public addProduct() bodies match baseline apart from whitespace. Stock arithmetic remains in unchanged Product.java; only event plumbing changes around it.
3. Feature callbacks/lookup/report bindings match the original block apart from the product-model adapter calls.
4. ProductManager has no Swing/AWT types; TransactionReportBinding has no manager/file imports; main's credential load/dialog/success check still precede StoreContext.load().
5. All 90 original legacy files and Phase 1–3 documents are checked by before/after SHA-256; no generated .class/JAR/log/cache output is kept in the repository.
6. Nothing is staged, committed or pushed.

Unmodified working-port imports retain upstream whitespace and other known defects; they are not represented as newly cleaned/refactored classes. `git diff --check` has no tracked diff yet. Separate checks cover authored/refactored files; a future staged full-copy whitespace check can report inherited upstream issues, as the original baseline did. Do not confuse preserving those files with claiming the inherited warnings are fixed.

## Complete new-file manifest

Paths below are repository-relative. .gitkeep files are pre-existing and excluded. “Imported unchanged” means byte-identical to original source, not a claim of quality or successful original-suite coverage.

| Path | Provenance / purpose |
|---|---|
| `docs/refactoring/refactoring-evidence.md` | Before/after evidence, results, limits and file manifest |
| `docs/refactoring/selected-refactorings.md` | Selection recorded before implementation; final scope/results linked |
| `reengineered/scripts/test.ps1` | New temporary-output compile/test runner |
| `reengineered/src/main/java/sg/edu/nus/iss/se24_2ft/unit1/ca/bootstrap/StoreContext.java` | New focused production type |
| `reengineered/src/main/java/sg/edu/nus/iss/se24_2ft/unit1/ca/bootstrap/StorePaths.java` | New focused production type |
| `reengineered/src/main/java/sg/edu/nus/iss/se24_2ft/unit1/ca/category/Category.java` | Imported unchanged production source |
| `reengineered/src/main/java/sg/edu/nus/iss/se24_2ft/unit1/ca/category/CategoryManager.java` | Imported unchanged production source |
| `reengineered/src/main/java/sg/edu/nus/iss/se24_2ft/unit1/ca/customer/Customer.java` | Imported unchanged production source |
| `reengineered/src/main/java/sg/edu/nus/iss/se24_2ft/unit1/ca/customer/member/Member.java` | Imported unchanged production source |
| `reengineered/src/main/java/sg/edu/nus/iss/se24_2ft/unit1/ca/customer/member/MemberManager.java` | Imported unchanged production source |
| `reengineered/src/main/java/sg/edu/nus/iss/se24_2ft/unit1/ca/customer/PublicCustomer.java` | Imported unchanged production source |
| `reengineered/src/main/java/sg/edu/nus/iss/se24_2ft/unit1/ca/discount/Discount.java` | Imported unchanged production source |
| `reengineered/src/main/java/sg/edu/nus/iss/se24_2ft/unit1/ca/discount/DiscountManager.java` | Imported unchanged production source |
| `reengineered/src/main/java/sg/edu/nus/iss/se24_2ft/unit1/ca/gui/CategoryPanel.java` | Imported unchanged production source |
| `reengineered/src/main/java/sg/edu/nus/iss/se24_2ft/unit1/ca/gui/CategoryPanelListener.java` | Imported unchanged production source |
| `reengineered/src/main/java/sg/edu/nus/iss/se24_2ft/unit1/ca/gui/checkout/CheckoutPanel.java` | Imported unchanged production source |
| `reengineered/src/main/java/sg/edu/nus/iss/se24_2ft/unit1/ca/gui/checkout/CheckoutPanelListener.java` | Imported unchanged production source |
| `reengineered/src/main/java/sg/edu/nus/iss/se24_2ft/unit1/ca/gui/checkout/ConfirmPaymentDialog.java` | Imported unchanged production source |
| `reengineered/src/main/java/sg/edu/nus/iss/se24_2ft/unit1/ca/gui/checkout/ReceiptDialog.java` | Imported unchanged production source |
| `reengineered/src/main/java/sg/edu/nus/iss/se24_2ft/unit1/ca/gui/DiscountPanel.java` | Imported unchanged production source |
| `reengineered/src/main/java/sg/edu/nus/iss/se24_2ft/unit1/ca/gui/DiscountPanelListener.java` | Imported unchanged production source |
| `reengineered/src/main/java/sg/edu/nus/iss/se24_2ft/unit1/ca/gui/FeaturePanel.java` | Imported unchanged production source |
| `reengineered/src/main/java/sg/edu/nus/iss/se24_2ft/unit1/ca/gui/InventoryPanel.java` | Imported unchanged production source |
| `reengineered/src/main/java/sg/edu/nus/iss/se24_2ft/unit1/ca/gui/InventoryPanelListener.java` | Imported unchanged production source |
| `reengineered/src/main/java/sg/edu/nus/iss/se24_2ft/unit1/ca/gui/LoginPanel.java` | Imported unchanged production source |
| `reengineered/src/main/java/sg/edu/nus/iss/se24_2ft/unit1/ca/gui/MainFrame.java` | Imported unchanged production source |
| `reengineered/src/main/java/sg/edu/nus/iss/se24_2ft/unit1/ca/gui/MemberPanel.java` | Imported unchanged production source |
| `reengineered/src/main/java/sg/edu/nus/iss/se24_2ft/unit1/ca/gui/MemberPanelListener.java` | Imported unchanged production source |
| `reengineered/src/main/java/sg/edu/nus/iss/se24_2ft/unit1/ca/gui/ProductPanel.java` | Imported unchanged production source |
| `reengineered/src/main/java/sg/edu/nus/iss/se24_2ft/unit1/ca/gui/ProductPanelListener.java` | Imported unchanged production source |
| `reengineered/src/main/java/sg/edu/nus/iss/se24_2ft/unit1/ca/gui/ProductTableModels.java` | New focused production type |
| `reengineered/src/main/java/sg/edu/nus/iss/se24_2ft/unit1/ca/gui/ReportPanel.java` | Imported production source; selected responsibility refactored |
| `reengineered/src/main/java/sg/edu/nus/iss/se24_2ft/unit1/ca/gui/StoreFeatures.java` | New focused production type |
| `reengineered/src/main/java/sg/edu/nus/iss/se24_2ft/unit1/ca/gui/StoreWindowFactory.java` | New focused production type |
| `reengineered/src/main/java/sg/edu/nus/iss/se24_2ft/unit1/ca/gui/TransactionReportBinding.java` | New focused production type |
| `reengineered/src/main/java/sg/edu/nus/iss/se24_2ft/unit1/ca/printer/AdhesiveLabelPrinter.java` | Imported unchanged production source |
| `reengineered/src/main/java/sg/edu/nus/iss/se24_2ft/unit1/ca/printer/Printer.java` | Imported unchanged production source |
| `reengineered/src/main/java/sg/edu/nus/iss/se24_2ft/unit1/ca/printer/ReceiptPrinter.java` | Imported unchanged production source |
| `reengineered/src/main/java/sg/edu/nus/iss/se24_2ft/unit1/ca/product/Product.java` | Imported unchanged production source |
| `reengineered/src/main/java/sg/edu/nus/iss/se24_2ft/unit1/ca/product/ProductCatalogListener.java` | New focused production type |
| `reengineered/src/main/java/sg/edu/nus/iss/se24_2ft/unit1/ca/product/ProductManager.java` | Imported production source; selected responsibility refactored |
| `reengineered/src/main/java/sg/edu/nus/iss/se24_2ft/unit1/ca/StoreApplication.java` | Imported production source; selected responsibility refactored |
| `reengineered/src/main/java/sg/edu/nus/iss/se24_2ft/unit1/ca/storekeeper/StoreKeeper.java` | Imported unchanged production source |
| `reengineered/src/main/java/sg/edu/nus/iss/se24_2ft/unit1/ca/storekeeper/StoreKeeperManager.java` | Imported unchanged production source |
| `reengineered/src/main/java/sg/edu/nus/iss/se24_2ft/unit1/ca/transaction/Transaction.java` | Imported unchanged production source |
| `reengineered/src/main/java/sg/edu/nus/iss/se24_2ft/unit1/ca/transaction/TransactionItem.java` | Imported unchanged production source |
| `reengineered/src/main/java/sg/edu/nus/iss/se24_2ft/unit1/ca/transaction/TransactionManager.java` | Imported unchanged production source |
| `reengineered/src/main/java/sg/edu/nus/iss/se24_2ft/unit1/ca/util/Util.java` | Imported unchanged production source |
| `reengineered/src/main/java/sg/edu/nus/iss/se24_2ft/unit1/ca/vendor/Vendor.java` | Imported unchanged production source |
| `reengineered/src/main/java/sg/edu/nus/iss/se24_2ft/unit1/ca/vendor/VendorManager.java` | Imported unchanged production source |
| `reengineered/src/test/java/repos/refactoring/RefactoringTests.java` | New dependency-free focused test harness |
