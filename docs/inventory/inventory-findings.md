# RePOS: inventory findings and priorities

Inventory date: 2026-11-25. Scope: all 90 files in the preserved legacy baseline, original commit `87bee5fed6e44cf87fd91a05de42a6817ae81002`.

## Evidence and decision rules

Verified facts below come from file contents, full recursive enumeration, content hashes and reference searches across production/test source plus `StoreApplication.main()` wiring. Implications are static interpretations, not reproduced failures. Recommended actions are for later approved work: this task changes only the three inventory documents.

No build script, test, application GUI or bundled binary was executed; no dependencies were installed. The Word report was inventoried as a binary document but not semantically decoded. No finding depends on invented report contents.

Priority meanings:

- **High:** directly affects functionality, data integrity, authentication security, or the ability to reproduce/verify the system.
- **Medium:** affects maintainability, testability, development workflow or unresolved feature integration.
- **Low:** administrative/cosmetic clarity with no established direct runtime impact.

Classification and priority answer different questions: a useful asset may require modification at high priority, and a Needs investigation asset is not automatically obsolete.

## Prioritized findings

| ID | Priority | Finding | Why this priority |
|---|---|---|---|
| H1 | High | Transaction history reload is not faithful | Persisted customer identity and report completeness affect actual history. |
| H2 | High | Sale completion spans independent writes | Failure can leave sale, stock and loyalty records inconsistent. |
| H3 | High | Stock deduction has a model-initialization hazard | Stock mutation can throw before persistence when model is absent. |
| H4 | High | Plaintext authentication assets need replacement | Plaintext credential handling affects authentication security. |
| H5 | High | Stored sales omit original financial state | Absent historical financial fields constrain trustworthy migration. |
| H6 | High | Delimited records and input validation can undermine data integrity | Parsing/validation defects can invalidate stored data and references. |
| H7 | High | Reproducible build/test prerequisites are incomplete | Reengineering needs a reproducible build and test baseline. |
| H8 | High | Monetary, loyalty and time-dependent rules need characterization | Money, points and eligibility rules directly change sale outcomes. |
| M1 | Medium | Existing test assets are useful but weak | Weak assertions reduce confidence in later changes. |
| M2 | Medium | Test helper writes share relative fixture locations | Fixture side effects impede safe/repeatable testing. |
| M3 | Medium | Vendor fixtures are byte-identical but not semantically redundant | Consolidation must preserve category associations. |
| M4 | Medium | Vendor module is loaded but disconnected from feature operations | Integration scope is unclear but loading already exists. |
| M5 | Medium | Printing abstraction contains incomplete/disconnected behavior | Output requirements and partial abstractions affect maintainability. |
| M6 | Medium | Build/launch scripts contain portability and safety assumptions | Environment/script assumptions impede developer workflow. |
| M7 | Medium | Windows and Unix distribution recipes are not equivalent | Distribution recipes can package different/incomplete test sets. |
| M8 | Medium | Business logic, file access and Swing models are coupled | Cross-responsibility dependencies make isolated changes difficult. |
| M9 | Medium | Live data and generated outputs are absent by design, not missing source | Prepared execution setup must be separated from source preservation. |
| M10 | Medium | Documentation and binary-tool provenance remain incomplete | Documentation and provenance gaps need resolution before reliance. |
| L1 | Low | Administrative metadata has overlapping roles but is not proven obsolete | Administrative setup can be reviewed after execution prerequisites. |
| L2 | Low | Comment and test-name cleanup is secondary to behavior | Clarity cleanup does not resolve the higher-priority behavior risks. |

### H1: Transaction history reload is not faithful (High)

**Verified evidence:** `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/transaction/TransactionManager.java`, `load()`, resolves a member only when the recorded ID equals PUBLIC; non-PUBLIC IDs become `PublicCustomer`. The existing-transaction branch adds the item then returns before updating the manager-level `transactionItemList`. `legacy/data-sample/Transactions.dat` contains two distinct product rows for transaction 1.

**Implication / verification limit:** Source-confirmed branch/list behavior implies loss of member association and incomplete flattened report rows after loading multi-product history. Runtime reproduction is pending.

**Recommended action:** Characterize sample reload before migration; verify all line items and customer IDs against persisted records. Correct behavior only during later authorized code work.

### H2: Sale completion spans independent writes (High)

**Verified evidence:** `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/transaction/TransactionManager.java`, `addTransaction()`, debits/credits loyalty, deducts products and then calls `store()`. `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/customer/member/MemberManager.java` point operations and `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/product/ProductManager.java`, `deductQuantity()`, each call their own `store()`. The stores call `Files.write()` and catch/print `IOException`. Transaction stock-error handling attempts deduction of all remaining stock rather than rejecting the recorded quantity.

**Implication / verification limit:** No atomic commit/rollback is implemented in this path. Partial failures can leave point, product and transaction files inconsistent; exact failure behavior has not been executed.

**Recommended action:** Prioritize isolated characterization of complete/failed sales and reconciliation rules; define durable completion requirements before changing storage.

### H3: Stock deduction has a model-initialization hazard (High)

**Verified evidence:** `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/product/ProductManager.java`, `deductQuantity()`, calls `understockTableModel.fireTableRowsInserted()` without a null check when a product first becomes understocked. The constructor initializes that model to null. `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/StoreApplication.java`, `main()`, initializes it through `getUnderstockTableModel()` before checkout.

**Implication / verification limit:** Standalone use can dereference a null model; the normal startup wiring mitigates this specific path. It is not evidence that every GUI sale crashes.

**Recommended action:** Add future boundary cases with/without table initialization; preserve stock threshold semantics while correcting the dependency.

### H4: Plaintext authentication assets need replacement (High)

**Verified evidence:** `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/storekeeper/StoreKeeper.java` stores a password String and exposes `getPassword()`; `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/storekeeper/StoreKeeperManager.java`, `load()`/`login()`, reads the password field and compares equality. `legacy/data-sample/Storekeepers.dat` contains two plaintext sample credential records; `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/gui/LoginPanel.java` reads a password into String form.

**Implication / verification limit:** This is source-confirmed plaintext handling, not a claim that the sample credentials grant access to an external service. Those two production implementations are classified Replace; sample credentials require migration treatment.

**Recommended action:** Define credential requirements and replacement strategy; preserve sample baseline, avoid treating sample records as real future accounts, and verify how existing staff identities migrate.

### H5: Stored sales omit original financial state (High)

**Verified evidence:** `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/transaction/TransactionItem.java`, `toString()`, emits transaction ID, product ID, customer ID, quantity and date. `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/transaction/Transaction.java` marks discount/redemption/payment/credited points as not persisted; `addTransactionItem()` calculates subtotal from the referenced Product price. `legacy/data-sample/Transactions.dat` follows that five-field format.

**Implication / verification limit:** Original financial details cannot be reconstructed independently from those stored fields. Current product records influence loaded totals. Migration must distinguish unavailable facts from inferred values.

**Recommended action:** Map old fields and relationships explicitly; preserve original rows and label historical values that cannot be recovered. Do not invent prices, payment or discount history.

### H6: Delimited records and input validation can undermine data integrity (High)

**Verified evidence:** `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/util/Util.java`, `splitCsv()`, uses `split(",", -1)`; entity serializers such as `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/product/Product.java`, `toString()`, join without escaping. Loaders such as `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/product/ProductManager.java`, `load()`, index fields directly and parse the ID suffix unchecked. `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/gui/ProductPanel.java`, `newProductPanel()`, defaults bad numbers to zero. `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/category/CategoryManager.java` uppercases lookup IDs but retains inserted case.

**Implication / verification limit:** Embedded separators, malformed rows, invalid numeric values or inconsistent category case can produce corrupt/ignored/unresolvable records. These are source risks; actual malformed-file outcomes remain unexecuted.

**Recommended action:** Prioritize data validation and round-trip cases; preserve raw records and document any future normalization/migration rules.

### H7: Reproducible build/test prerequisites are incomplete (High)

**Verified evidence:** The complete file enumeration contains no Maven/Gradle/Ant descriptor or wrapper and no JAR files. `legacy/unit1-ca.iml` references IDE-relative JUnit 4.12 and Hamcrest 1.3; `legacy/.classpath` uses the Eclipse JUnit 4 container. `legacy/.idea/misc.xml` specifies Java 8; `legacy/submission/setenv.bat` hard-codes JDK 1.8.0_77. No explicit test runner script or suite was found.

**Implication / verification limit:** A clean environment cannot reproduce the test setup from bundled files alone. No compile/test failure is asserted because none was attempted in this task.

**Recommended action:** Record/verify the toolchain and externally provided libraries in future authorized setup work. First reproduce compilation and existing tests in a disposable copy, not in legacy.

### H8: Monetary, loyalty and time-dependent rules need characterization (High)

**Verified evidence:** `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/transaction/Transaction.java` uses double totals; `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/transaction/TransactionManager.java` defines DOLLAR_TO_POINT=10 and POINT_TO_DOLLAR=5 and credits from pre-discount subtotal. `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/customer/member/Member.java` starts points at -1; `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/discount/DiscountManager.java`, `getMaxDiscount()`, interprets that value as first purchase. `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/discount/Discount.java` uses current Date; `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/util/Util.java`, `addDate()`, sets Calendar.HOUR to 23.

**Implication / verification limit:** Implemented constants and sentinel meanings are confirmed, but intended business rules, rounding and date-boundary correctness are unverified. They directly affect sale/loyalty outcomes.

**Recommended action:** Review original requirements; characterize first/subsequent discounts, redemption caps, rounding and date edges before changing values or formats.

### M1: Existing test assets are useful but weak (Medium)

**Verified evidence:** `legacy/test/sg/edu/nus/iss/se24_2ft/unit1/ca/TransactionManagerTest.java`, `testAddTransaction()`, submits an empty Transaction; `legacy/test/sg/edu/nus/iss/se24_2ft/unit1/ca/TransactionTest.java` checks item additions largely through exception/non-null outcomes. `legacy/test/sg/edu/nus/iss/se24_2ft/unit1/ca/ProductManagerTest.java`, `testGeneratePurchaseOrder()`, passes null. `legacy/test/sg/edu/nus/iss/se24_2ft/unit1/ca/StoreKeeperManagerTest.java`, `testInitData()`, is empty and `testLogin()` injects state through reflection. `legacy/test/sg/edu/nus/iss/se24_2ft/unit1/ca/DiscountTest.java` depends on a fixed 2016 date and current time.

**Implication / verification limit:** The 15 test classes and 45 annotations do not establish coverage of actual sales, stock replenishment or failure recovery. No tests were run. Inspection also found no GUI/report/receipt/Util-specific test classes.

**Recommended action:** Retain all examples; improve assertions, real sale fixtures, table notifications, invalid-input, persistence and clock-boundary cases after isolated execution is available.

### M2: Test helper writes share relative fixture locations (Medium)

**Verified evidence:** `legacy/test/util/TestUtil.java` defines DATA_PATH="test" and shared Category/Products/Members/Discounts/Transactions/Storekeepers/vendor filenames. `putData()` writes and `deleteData()` removes those paths; I/O exceptions are printed. Multiple manager test classes use these constants.

**Implication / verification limit:** Running from legacy would write inside the preserved baseline. Shared filenames also create potential interference if tests run concurrently; concurrency failure has not been reproduced.

**Recommended action:** Use a disposable working copy or later temporary-path adaptation. Keep the original helper and embedded fixture strings as baseline evidence.

### M3: Vendor fixtures are byte-identical but not semantically redundant (Medium)

**Verified evidence:** The SHA-256 comparison found exactly one duplicate-content group: `legacy/data-sample/VendorsCLO.dat`, `legacy/data-sample/VendorsDIA.dat`, `legacy/data-sample/VendorsMUG.dat`, `legacy/data-sample/VendorsSNA.dat` and `legacy/data-sample/VendorsSTA.dat`. All hash to ce2b030472ba1eaf5ee706865c281492205c395f3a5ac7c3143108263e9064d4. Each contains four name/description records. `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/vendor/VendorManager.java`, `load()`, extracts the category ID from the filename and merges Vendor objects by name.

**Implication / verification limit:** Deleting a duplicate file would remove a category-to-vendor association. A future consolidated format may represent repeated names once while retaining all category relationships.

**Recommended action:** Preserve all five files; if later migrating vendors, verify identity, descriptions and category associations before consolidating records.

### M4: Vendor module is loaded but disconnected from feature operations (Medium)

**Verified evidence:** `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/StoreApplication.java`, `main()`, constructs `vendorManager` and does not refer to that variable again. Production/test reference search finds `getVendorListByCategoryId()` used in `legacy/test/sg/edu/nus/iss/se24_2ft/unit1/ca/VendorManagerTest.java`, not a feature panel. `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/gui/InventoryPanel.java` has a vendor-selection TODO; `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/product/ProductManager.java`, `generatePurchaseOrder()`, immediately calls `restock()` on selected products.

**Implication / verification limit:** The vendor module is not globally unused: it loads data at startup and has tests. What is missing is demonstrated integration with the inventory workflow; actual external ordering must not be claimed.

**Recommended action:** Clarify intended vendor/purchase behavior from original documentation; preserve models, loader and fixtures while deciding future requirements.

### M5: Printing abstraction contains incomplete/disconnected behavior (Medium)

**Verified evidence:** Reference searches across legacy/src and legacy/test found no caller/instantiation of `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/printer/AdhesiveLabelPrinter.java` outside its declaration. Both printer subclasses' `getTag()` return null. `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/gui/checkout/ReceiptDialog.java`, `setVisible()`, calls ReceiptPrinter.print(); `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/printer/Printer.java`, `print()`, is static and writes System.out, with tag/instance-ID output commented.

**Implication / verification limit:** Adhesive labels are an unverified requirement; ReceiptPrinter is referenced even though its override is outside the active static output path. No physical printer integration is implemented in these files.

**Recommended action:** Retain console receipts for comparison. Investigate label/output requirements before completing, simplifying or retiring printer abstractions.

### M6: Build/launch scripts contain portability and safety assumptions (Medium)

**Verified evidence:** `legacy/submission/compile.bat` removes/recreates classes, writes logs, refers to tools.jar and misspells classses in CLASSPATH; CompileMini contains 2>>&1 redirection. `legacy/submission/setenv.bat` replaces PATH with a machine-specific JDK bin directory. `legacy/submission/run.bat` assumes compiled classes and data under its working directory.

**Implication / verification limit:** Script behavior is not verified. Some statements affect only inherited environment because javac supplies explicit -cp; the classpath typo alone is not proof of compilation failure.

**Recommended action:** Keep scripts as command evidence; verify safe isolated build/launch equivalents later. Do not execute baseline cleanup or bundled tooling during inventory.

### M7: Windows and Unix distribution recipes are not equivalent (Medium)

**Verified evidence:** `legacy/submission.bat` copies the complete test directory; `legacy/submission.sh` copies only test/sg and omits test/util/TestUtil.java. The shell script also cleans sunmission/data/* rather than submission/data/* and does not explicitly create all destination roots. Both target a se24_F02.zip assignment package.

**Implication / verification limit:** Unix distribution can omit an imported test helper, and stale data cleanup is suspect. The two scripts overlap in purpose but cannot be treated as interchangeable duplicates.

**Recommended action:** Define expected package contents and verify a safe packaging process in future work; retain both historical recipes.

### M8: Business logic, file access and Swing models are coupled (Medium)

**Verified evidence:** `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/category/CategoryManager.java`, `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/product/ProductManager.java`, `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/customer/member/MemberManager.java`, `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/discount/DiscountManager.java`, `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/transaction/TransactionManager.java` each expose AbstractTableModel implementations while loading/storing records. `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/transaction/Transaction.java` also owns a table model. `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/gui/checkout/CheckoutPanel.java` coordinates payment and completion.

**Implication / verification limit:** These confirmed dependencies complicate isolated testing and changes to storage/presentation. This finding does not assign a design-pattern label or authorize a new UI layer.

**Recommended action:** Document responsibilities and interaction contracts first; use tests to guide later authorized restructuring.

### M9: Live data and generated outputs are absent by design, not missing source (Medium)

**Verified evidence:** `legacy/README.md` instructs copying data-sample to data; `legacy/.gitignore` excludes /data/, out/bin and generated submission directories. The recursive enumeration found no live legacy/data directory or compiled application output. `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/StoreApplication.java`, `main()`, reads data/... relative paths; `.idea/artifacts/jar_with_data.xml` expects a live data directory.

**Implication / verification limit:** The preserved checkout is not a ready-to-run prepared distribution. Missing runtime copies/output should not be fabricated in the baseline or counted as lost assets.

**Recommended action:** Specify the runtime working directory and derive writable data only in a separately authorized execution workspace.

### M10: Documentation and binary-tool provenance remain incomplete (Medium)

**Verified evidence:** `legacy/README.md` provides data setup and Git workflow but no complete build/test guide. `legacy/submission/docs/report.doc` is a 185856-byte OLE compound file; its semantic content was not decoded. `legacy/tools/7z.exe` and `legacy/tools/7z.dll` have PE signatures and version resources identifying 7-Zip 15.14 / Igor Pavlov; `legacy/submission.bat` invokes the executable. No dedicated LICENSE/COPYING/NOTICE file appears in the baseline enumeration.

**Implication / verification limit:** Requirements may exist in the unread report; absence of a dedicated license file does not prove absence of licensing terms embedded elsewhere. Tool version metadata does not certify current safety or distribution rights.

**Recommended action:** Read the report without editing; investigate original requirements and tool/source licensing/provenance before future redistribution or reliance. No install or executable run is required for this inventory.

### L1: Administrative metadata has overlapping roles but is not proven obsolete (Low)

**Verified evidence:** `legacy/.project` and `legacy/.classpath` describe Eclipse; `legacy/.idea/modules.xml`, `misc.xml`, `compiler.xml`, `encodings.xml` and `legacy/unit1-ca.iml` describe IntelliJ. `legacy/unit1-ca.userlibraries` has no library entries; `legacy/.idea/copyright/profiles_settings.xml` has an empty default profile. `legacy/unit1-ca.eml` is XML with output/exclusion settings, not email.

**Implication / verification limit:** Multiple IDE views repeat project information but also capture useful JDK, dependency, encoding and packaging intent. Exact .eml consumer and ongoing need for empty profiles remain unresolved.

**Recommended action:** Retain all metadata; review active IDE/import needs later rather than classifying as obsolete by extension.

### L2: Comment and test-name cleanup is secondary to behavior (Low)

**Verified evidence:** `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/customer/PublicCustomer.java` and `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/customer/member/Member.java` contain commented-out loyalty code. `legacy/test/sg/edu/nus/iss/se24_2ft/unit1/ca/TransactionItemTest.java`, `testGetQuantity()` and `testGetProduct()`, assert the opposite accessor names. `legacy/test/sg/edu/nus/iss/se24_2ft/unit1/ca/StoreKeeperTest.java`, `testGetPassword()`, partly checks the name.

**Implication / verification limit:** These are concrete clarity issues; commented code is not an implemented feature. Behavior/security/data work has higher priority.

**Recommended action:** Clarify names/comments only during later authorized cleanup; preserve the original as evidence.

## Preservation and migration boundaries

All 90 files remain baseline assets, regardless of future classification. Preserve source, tests, test helper, sample records, IDE files, scripts, manifest, report and tools together. Do not delete the identical vendor fixtures or “fix” whitespace in legacy.

Migration candidates requiring explicit interpretation include:

- Product IDs and category associations: `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/product/ProductManager.java`, `load()` and generated `categoryId/sequence` IDs.
- Member identity and -1 loyalty sentinel: `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/customer/member/Member.java` and `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/discount/DiscountManager.java`, `getMaxDiscount()`.
- Customer links and multi-line sale history: `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/transaction/TransactionManager.java`, `load()`, and `legacy/data-sample/Transactions.dat`.
- Missing financial history: `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/transaction/TransactionItem.java`, `toString()`.
- Staff authentication: `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/storekeeper/StoreKeeperManager.java` and `legacy/data-sample/Storekeepers.dat`.
- Vendor/category relationships encoded by five filenames: `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/vendor/VendorManager.java`, `load()`.

These are migration considerations, not an implementation plan or an architecture redesign.

## Unresolved investigations

1. Does the binary report define intended redemption rates, purchase orders, vendor selection or label printing? Its contents are not yet known.
2. What exact JDK/IDE/library setup successfully builds and runs the original tests? Static configuration is not execution evidence.
3. Are all sample identities/credentials fictional and suitable for derived public fixtures? Their origin is the upstream repository, not independently verified consent or synthetic-data provenance.
4. Which tool consumes `legacy/unit1-ca.eml`, and do the empty user-library/copyright profiles serve an import workflow?
5. What licensing/provenance applies to original source and bundled archiver distribution?
6. What are the actual failure/round-trip outcomes for the high-priority source findings when exercised in an isolated workspace?

## Recommended investigation sequence

Review this inventory first. Then, with separate authorization, inspect the Word report, establish a disposable execution copy, reproduce compilation/tests with verified dependencies, and characterize H1-H8. Retain observed outputs before deciding later restructuring or migration changes. Do not infer an external ordering system, hardware printer integration, new database or new UI architecture from the asset names.

The complete file register and calculated classification/type totals are in [asset inventory](asset-inventory.md) and [inventory summary](inventory-summary.md).
