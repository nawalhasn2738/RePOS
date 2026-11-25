# RePOS: inventory risk assessment and reengineering priorities

Task 1.3. Assessment date: 2026-11-25. Baseline: `legacy/`, original commit `87bee5fed6e44cf87fd91a05de42a6817ae81002`.

## 1. Executive summary

This document consolidates the four existing inventory documents into **19 distinct risks**, **seven ranked work areas**, **10 preservation workflow groups** and **10 unresolved behavior decisions**. The register proposes **one Critical, 13 High and five Medium priorities; none is rated Low**. These are justified planning judgments, not measured failure frequencies or approved team commitments.

The principal integrity concern is independent sale/stock/loyalty writes (R-02). History reload defects (R-03), absent historical financial fields (R-04) and import/relationship handling (R-05/R-17) are separate issues: a successful database import alone will not solve all of them. Authentication implementation requires replacement (R-10), while valid user-facing login behavior remains required.

The existing inventory's **24 Reusable / 57 Requires modification / 2 Replace / 0 Obsolete / 7 Needs investigation** classifications concern all 90 baseline files, not risk counts. Do not reinterpret them as 90 implementation tasks. Small customer contracts, useful callbacks, existing Swing containers and comparison fixtures are reuse candidates; their suitability is not runtime-certified. The two Replace recommendations identify StoreKeeper/StoreKeeperManager plaintext authentication, not permission to delete original files. IDE metadata, scripts, sample data, tools and the report remain comparison evidence.

**Relational database migration is a planned reengineering direction requested for this task; the legacy system uses text files.** No database engine, schema, new feature or UI redesign is selected here. The proposed responsibility separation in the dependency map remains a target, not an existing architecture.

This assessment uses source/configuration inspection only. No legacy build, application, test, package script or bundled executable was run. No mitigation or workflow is claimed complete. All 19 risk states are **open/unverified**, and all proposed owners and ratings remain subject to team review.

### Assessment rules and confidence

- **Likelihood Low:** a narrow/unlikely condition is required under the contemplated change; **Medium:** a plausible conditional trigger exists but frequency/exposure is unknown; **High:** an ordinary relevant operation or planned restructuring directly encounters the source-confirmed issue. These are qualitative estimates, not percentages.
- **Impact Low:** local cosmetic/administrative effect; **Medium:** workflow/testability/maintainability disruption with a limited recovery scope; **High:** potentially wrong sale, credentials, lost/misrepresented data, or inability to establish a trustworthy executable baseline.
- **Priority:** Medium/High likelihood with High impact normally gives High; High likelihood with Medium impact also gives High; Medium/Medium gives Medium. Low combinations would receive Low or Medium according to consequence; no current risk was assigned one without evidence.
- **Critical override:** R-02 is a provisional integrity acceptance gate because conflicting durable sale/stock/loyalty state must block release/migration acceptance. This is not a claim of an observed production incident, exposure to real customers or a mathematically derived criticality score.
- **Provisional:** all likelihood, impact and priority judgments are provisional because execution, exposure, intended business rules and fault frequencies are not established. Each risk explains its trigger/uncertainty. Source-confirmed mechanisms remain facts even where consequences await reproduction.
- **Ownership:** each risk has exactly one proposed accountable owner using the user-supplied split. Shared is used for the explicitly shared regression/integration concern. Recipients/reviewers in handovers do not become duplicate implementation owners.

## 2. Consolidated risk register

Evidence references H1-H8/M1-M10/L1-L2 refer to the existing inventory findings; D/W references and numbered sections refer to the dependency map. Exact paths and methods appear in each risk and/or the evidence catalog in section 7. Repeated mechanisms are consolidated; related risks are separated only where mitigation and verification differ.

| ID | Concise title | Category | Likelihood (provisional) | Impact (provisional) | Priority (provisional) | Proposed owner |
|---|---|---|---|---|---|---|
| R-01 | Functional equivalence lacks an agreed behavior contract | Functional | Medium | High | High | Nawal |
| R-02 | Sale updates lack an all-or-nothing persistence boundary | Data integrity and persistence | Medium | High | Critical | Hamna |
| R-03 | History loading loses customer identity and report lines | Data integrity and persistence | High | High | High | Hamna |
| R-04 | Legacy sales omit historical financial details | Data integrity and persistence | High | High | High | Hamna |
| R-05 | File parsing and identifier handling can lose relationships | Data integrity and persistence | Medium | High | High | Hamna |
| R-06 | Invalid user values can reach business operations | Functional and security | Medium | High | High | Iman |
| R-07 | Money and loyalty rules are not agreed or fully characterized | Functional | Medium | High | High | Iman |
| R-08 | Date eligibility and report boundaries are ambiguous | Functional | Medium | High | High | Iman |
| R-09 | Stock mutation depends on a table model being initialized | Architecture and coupling | Medium | High | High | Nawal |
| R-10 | Plaintext staff credentials cannot be reused as authentication | Security | Medium | High | High | Iman |
| R-11 | Mixed responsibilities and source cycles obstruct safe separation | Architecture and coupling | High | Medium | High | Nawal |
| R-12 | Mutable aliases and global helpers bypass controlled operations | Architecture and coupling | Medium | Medium | Medium | Iman |
| R-13 | Regression confidence is insufficient for meaningful change | Testing and verification | High | High | High | Shared |
| R-14 | Fixture writes can contaminate the preserved workspace | Testing and verification | Medium | Medium | Medium | Hamna |
| R-15 | Build/run environment cannot yet be reproduced from bundled assets | Build and environment | High | High | High | Nawal |
| R-16 | Packaging recipes can omit assets or clean the wrong location | Build and environment | Medium | Medium | Medium | Nawal |
| R-17 | Vendor/category relationships can be lost during consolidation | Data integrity and persistence | Medium | High | High | Hamna |
| R-18 | Receipt/output behavior can be mistaken for hardware integration | Functional | Medium | Medium | Medium | Iman |
| R-19 | Documentation and asset provenance gaps can misdirect handover | Documentation and operational | Medium | Medium | Medium | Nawal |

### R-01: Functional equivalence lacks an agreed behavior contract

- **Evidence:** `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/StoreApplication.java`, main() wires seven feature tabs; CheckoutPanel.getBottomPanel() coordinates completion; legacy/submission/docs/report.doc remains unread in the inventory.
- **Existing finding cross-reference:** Inventory M4/M5/M10; dependency map D04/W01-W09.
- **Assessment rationale / uncertainty:** Source workflows exist, but intended behavior and accepted defect corrections are not yet approved; likelihood is a forecast of change-related drift, not an observed regression.
- **Consequence if not addressed:** Reengineering may silently remove workflows, add unverified features or copy defects as requirements.
- **Recommended mitigation:** Record source-observed workflows and decision cases below; reconcile the original report when readable; obtain Shared acceptance of preserved versus corrected behavior before implementation.
- **Proposed owner:** Nawal.
- **Suggested phase/task:** Document Restructuring and Reverse Engineering: behavior specification and decision log.
- **Verification method (future, not executed):** Later map every preserved workflow to acceptance examples; Shared review records accepted exceptions and confirms no unapproved scope expansion.

### R-02: Sale updates lack an all-or-nothing persistence boundary

- **Evidence:** `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/transaction/TransactionManager.java`, addTransaction()/store(); `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/customer/member/MemberManager.java`, debitLoyaltyPoint()/creditLoyaltyPoint()/store(); `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/product/ProductManager.java`, deductQuantity()/store(). Separate writes precede transaction storage; IOException is printed; stock-error fallback deducts remaining stock.
- **Existing finding cross-reference:** Inventory H2; dependency map D08/D12.
- **Assessment rationale / uncertainty:** Failure requires an I/O or stock-update problem not reproduced here. Critical is a provisional integrity gate override: a completed sale must not leave conflicting stock, loyalty and history records.
- **Consequence if not addressed:** Partial sale state or recorded quantity different from actual stock deduction can survive a failure; UI success need not reflect durable completion.
- **Recommended mitigation:** Define one durable completion/rollback contract for sale, stock and loyalty; Hamna owns persistence transaction boundaries and failure reporting, with the service consuming that contract rather than issuing independent commits. Decide retry behavior before integration.
- **Proposed owner:** Hamna.
- **Suggested phase/task:** Data Restructuring: transaction boundary and failure semantics; Forward Engineering: persistence integration.
- **Verification method (future, not executed):** Later inject a failure at each update boundary; verify either all effects or none, durable reload consistency, and approved retry behavior. No such execution occurred in this task.

### R-03: History loading loses customer identity and report lines

- **Evidence:** `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/transaction/TransactionManager.java`, load() resolves member lookup for PUBLIC instead of member IDs; the existing-transaction branch returns before extending the flattened report list. legacy/data-sample/Transactions.dat includes two distinct lines for transaction 1.
- **Existing finding cross-reference:** Inventory H1; dependency map section 8 and D16.
- **Assessment rationale / uncertainty:** Given the supplied multi-line/member sample and a load operation, the faulty branches are directly present. Actual GUI results have not been executed.
- **Consequence if not addressed:** Reloaded member associations and visible historical line counts can be wrong; later storage can propagate misidentified customers.
- **Recommended mitigation:** Define corrected history reconstruction from raw rows, preserving original identifiers and every line; distinguish corrected output from the flawed legacy display.
- **Proposed owner:** Hamna.
- **Suggested phase/task:** Reverse Engineering: characterize loader; Data Restructuring: importer/query correction.
- **Verification method (future, not executed):** Later compare raw four-line/three-transaction sample with loaded IDs, customers and product lines; reload migrated records and reconcile all lines, not only transaction counts.

### R-04: Legacy sales omit historical financial details

- **Evidence:** `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/transaction/TransactionItem.java`, toString() saves only transaction/product/customer IDs, quantity and date; `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/transaction/Transaction.java`, addTransactionItem()/toStringStream() uses referenced product prices and does not persist payment, discount or point movements.
- **Existing finding cross-reference:** Inventory H5; dependency map D07/D15.
- **Assessment rationale / uncertainty:** The missing fields are confirmed; information loss is certain when independent original financial reconstruction is required. The intended historical-report requirement remains provisional.
- **Consequence if not addressed:** Migration may fabricate totals or recalculate old sales from current prices, misrepresenting historical facts.
- **Recommended mitigation:** Preserve raw rows and provenance; record unavailable historical values explicitly; define which fields future sales must capture. Do not infer original amounts from current product prices without labeling them as estimates and team approval.
- **Proposed owner:** Hamna.
- **Suggested phase/task:** Data Restructuring: historical field policy and relational mapping.
- **Verification method (future, not executed):** Later reconcile migrated source fields exactly, prove unavailable values remain marked unknown, and verify new sale snapshots survive product-price changes.

### R-05: File parsing and identifier handling can lose relationships

- **Evidence:** `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/util/Util.java`, splitCsv() uses unescaped comma splitting; `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/product/ProductManager.java`, load() indexes fields and parses ID suffixes unchecked; `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/category/CategoryManager.java`, addCategory()/getCategory() inserts retained case but uppercases lookup.
- **Existing finding cross-reference:** Inventory H6; dependency map D09/D12/D15.
- **Assessment rationale / uncertainty:** Triggers require malformed/separator-containing input or inconsistent IDs; no prevalence is known. Parser and case-handling mechanisms are source-confirmed.
- **Consequence if not addressed:** Rows may be skipped, misparsed or fail loading; category/product/customer links can be lost during relational migration.
- **Recommended mitigation:** Create an explicit import validation/mapping policy with row-level diagnostics, duplicate handling and preserved raw inputs; resolve normalization/collision decisions before enforcing relational keys.
- **Proposed owner:** Hamna.
- **Suggested phase/task:** Reverse Engineering: file contract; Data Restructuring: validated import and relationship mapping.
- **Verification method (future, not executed):** Later test malformed field counts, commas/newlines, duplicate and mixed-case IDs, invalid suffixes and missing references; reconcile accepted/rejected rows and keys without silent dropping.

### R-06: Invalid user values can reach business operations

- **Evidence:** `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/gui/ProductPanel.java`, newProductPanel() defaults malformed numbers to zero; `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/product/Product.java`, constructor/deductQuantity() lacks general nonnegative checks; `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/discount/DiscountManager.java`, addDiscount() mainly checks duplicate codes; `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/gui/checkout/ConfirmPaymentDialog.java`, getPaymentPanel() parses payment without an explicit finite-value check.
- **Existing finding cross-reference:** Inventory H6; dependency map D04/D05.
- **Assessment rationale / uncertainty:** Invalid input is a plausible trigger; no exploit or bad transaction has been demonstrated. Missing authoritative validation is confirmed, distinct from file-import parsing in R-05.
- **Consequence if not addressed:** Negative/out-of-range/non-finite values or misleading fallback defaults can affect stock, payment or discount calculations.
- **Recommended mitigation:** Specify validated operation inputs centrally; reject invalid values before mutation and return useful errors. Preserve verified valid-input behavior, not silent bad-input defaults. No SQL-injection vulnerability is asserted because no SQL code exists in the baseline.
- **Proposed owner:** Iman.
- **Suggested phase/task:** Code Restructuring and Forward Engineering: service/domain validation.
- **Verification method (future, not executed):** Later verify negative/zero limits, overflow boundaries, duplicate IDs/barcodes, percentage limits and NaN/infinity rejection at service and GUI entry points; invalid requests must not change state.

### R-07: Money and loyalty rules are not agreed or fully characterized

- **Evidence:** `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/transaction/TransactionManager.java`, constants/addTransaction() defines 10 dollars per credited point and 5 dollars per redeemed point, with credits based on pre-discount subtotal; `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/customer/member/Member.java`, constructor uses -1; `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/discount/DiscountManager.java`, getMaxDiscount() uses that sentinel for first/subsequent purchase.
- **Existing finding cross-reference:** Inventory H8; dependency map D05/D06/D11.
- **Assessment rationale / uncertainty:** The constants and double arithmetic are facts; intended rates, credit basis and rounding are unresolved. No claim is made that every floating-point calculation is wrong.
- **Consequence if not addressed:** An apparently helpful change can alter paid totals, point balances or first-purchase eligibility.
- **Recommended mitigation:** Obtain approved examples for discount choice, conversion, redemption cap, rounding and first-purchase transition; implement one policy authority used by payment and completion. Preserve current rates as observed behavior until explicitly changed.
- **Proposed owner:** Iman.
- **Suggested phase/task:** Reverse Engineering: business decision examples; Code Restructuring/Forward Engineering: money and loyalty policy.
- **Verification method (future, not executed):** Later use approved boundary examples for public/first/subsequent customers, discount/no discount, redemption/overpayment and point balance after reload; compare domain and UI results.

### R-08: Date eligibility and report boundaries are ambiguous

- **Evidence:** `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/discount/Discount.java`, isDiscountAvailable() uses current Date; `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/util/Util.java`, DATE_FORMAT/addDate()/parseDateOrDefault() uses a mutable lenient formatter and Calendar.HOUR=23; `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/gui/ReportPanel.java`, createTransactionReportPanel() filters inclusive timestamp comparisons.
- **Existing finding cross-reference:** Inventory H8; dependency map D11/D19.
- **Assessment rationale / uncertainty:** Boundary/timezone/invalid-date inputs are plausible but unexecuted; the intended day-versus-timestamp policy is unknown. This rates eligibility/report outcomes, not the shared-state issue separately owned in R-12.
- **Consequence if not addressed:** Discounts or report inclusion may change at boundaries or invalid dates may become unintended eligibility.
- **Recommended mitigation:** Agree date/timezone, inclusive range and invalid-date behavior; isolate an explicit clock/date policy and consistent parsing while preserving accepted date semantics.
- **Proposed owner:** Iman.
- **Suggested phase/task:** Reverse Engineering: date decisions; Code Restructuring/Forward Engineering: date policy.
- **Verification method (future, not executed):** Later verify exact start/end, same-day reports, end-of-day transactions, invalid dates and approved timezone using controlled time; test data retains original dates.

### R-09: Stock mutation depends on a table model being initialized

- **Evidence:** `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/product/ProductManager.java`, constructor/deductQuantity() can call understockTableModel.fireTableRowsInserted() while null; `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/StoreApplication.java`, main() initializes that model before normal checkout.
- **Existing finding cross-reference:** Inventory H3; dependency map D13/D14.
- **Assessment rationale / uncertainty:** Exposure is conditional on standalone/restructured use before view initialization; normal startup mitigates it. No universal checkout crash is asserted.
- **Consequence if not addressed:** Separating stock operations from GUI setup can introduce exceptions after in-memory mutation and before storage.
- **Recommended mitigation:** Remove the view-initialization precondition during Nawal's initial restructuring; keep stock rules unchanged, and move/bridge notifications at the presentation boundary.
- **Proposed owner:** Nawal.
- **Suggested phase/task:** Code Restructuring: stock operation/table-adapter seam.
- **Verification method (future, not executed):** Later cross the threshold with and without an initialized view; verify quantity, persistence outcome and any emitted notification independently of GUI construction.

### R-10: Plaintext staff credentials cannot be reused as authentication

- **Evidence:** `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/storekeeper/StoreKeeper.java`, constructor/getPassword() retains/exposes a password String; `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/storekeeper/StoreKeeperManager.java`, load()/login() reads plaintext and compares equality. legacy/data-sample/Storekeepers.dat contains sample credential pairs.
- **Existing finding cross-reference:** Inventory H4; asset inventory Replace entries; dependency map section 12.
- **Assessment rationale / uncertainty:** Credential exposure likelihood depends on future deployment/data access, not known here. Plaintext handling is confirmed; these sample credentials are not asserted to access an external service.
- **Consequence if not addressed:** Reusing the implementation and sample accounts unchanged carries avoidable credential disclosure risk.
- **Recommended mitigation:** Replace credential verification/representation with a reviewed password-storage approach; agree account reset/migration and login behavior. Iman owns authentication implementation; Hamna's relational work consumes its credential-record contract rather than designing a second authentication system.
- **Proposed owner:** Iman.
- **Suggested phase/task:** Forward Engineering: authentication replacement; Data Restructuring handoff: credential record contract.
- **Verification method (future, not executed):** Later verify valid/invalid login, absence of raw passwords in derived storage/logs, approved reset/migration and no unintended role/account-management feature expansion.

### R-11: Mixed responsibilities and source cycles obstruct safe separation

- **Evidence:** `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/transaction/Transaction.java`, getRedeemPointValue()/getTableModel() depends on TransactionManager policy and Swing; `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/transaction/TransactionManager.java`, load()/addTransaction()/store()/getTableModel() combines storage and coordination; TransactionItem holds a parent reference. Five managers also construct Swing models.
- **Existing finding cross-reference:** Inventory M8; dependency map D04-D08/D12-D15/D20 and section 8.
- **Assessment rationale / uncertainty:** Restructuring will encounter these confirmed responsibilities/access boundaries. It is a source cycle, not evidence of runtime recursion; parent/line association is not automatically defective.
- **Consequence if not addressed:** Moving classes mechanically can break package-private mutation, duplicate rules or keep storage/view dependencies inside domain/services.
- **Recommended mitigation:** Define responsibility and interface boundaries first; Nawal owns initial seam/table-adapter/bootstrap restructuring, while Iman owns business/service implementation and Hamna owns data adapters. Remove the reverse manager-policy dependency without discarding justified domain associations.
- **Proposed owner:** Nawal.
- **Suggested phase/task:** Document Restructuring/Reverse Engineering: boundary design; Code Restructuring: initial structure.
- **Verification method (future, not executed):** Later review dependency direction, compile/access boundaries and public contracts; verify no domain-to-Swing/concrete-persistence edge and rerun approved workflow comparisons.

### R-12: Mutable aliases and global helpers bypass controlled operations

- **Evidence:** `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/product/ProductManager.java`, getProductList() and `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/transaction/Transaction.java`, getTransactionItemList() return internal lists; CategoryManager/DiscountManager/VendorManager expose similar lists; Util.DATE_FORMAT is shared mutable state. MemberManager/Vendor list copies are shallow.
- **Existing finding cross-reference:** Dependency map D18/D19 and shared-state findings.
- **Assessment rationale / uncertainty:** Uncontrolled mutation/concurrent formatter use is possible but not observed. Immutability of conversion constants and the effectively read-only PublicCustomer identity are not scored as faults.
- **Consequence if not addressed:** External mutation may desynchronize collections, maps, notifications or persistence; formatter settings can affect other consumers.
- **Recommended mitigation:** Have Iman define invariant-preserving domain/service operations and read contracts; scope mutable formatting state. Nawal applies those contracts to view adapters without independently changing domain behavior.
- **Proposed owner:** Iman.
- **Suggested phase/task:** Code Restructuring: controlled domain state and helper contracts.
- **Verification method (future, not executed):** Later test that returned views cannot bypass mutations, object/collection invariants hold and independently configured date formatting does not leak settings.

### R-13: Regression confidence is insufficient for meaningful change

- **Evidence:** `legacy/test/sg/edu/nus/iss/se24_2ft/unit1/ca/TransactionManagerTest.java`, testAddTransaction(), submits an empty transaction; `legacy/test/sg/edu/nus/iss/se24_2ft/unit1/ca/ProductManagerTest.java`, testGeneratePurchaseOrder(), passes null; `legacy/test/sg/edu/nus/iss/se24_2ft/unit1/ca/StoreKeeperManagerTest.java`, testInitData(), is empty. Existing tests contain 45 @Test annotations, not 45 proven outcomes.
- **Existing finding cross-reference:** Inventory M1; dependency map section 7.
- **Assessment rationale / uncertainty:** Core workflows and failure cases lack strong assertions; execution status remains unknown. High likelihood concerns undetected change-related regressions, not a measured defect rate.
- **Consequence if not addressed:** Changes can appear successful while breaking real sale, replenishment, reload or reporting behavior.
- **Recommended mitigation:** Shared owns the functional-equivalence/regression suite and integration evidence; keep existing tests, add approved acceptance and boundary cases as changes occur. Domain/service and persistence implementers supply their own component evidence to that suite.
- **Proposed owner:** Shared.
- **Suggested phase/task:** Reverse Engineering: characterization; continuous Code/Data Restructuring verification; Forward Engineering: final regression.
- **Verification method (future, not executed):** Later record test discovery/execution/results, expected defect cases versus approved corrections, and end-to-end comparison of every preservation workflow. Never substitute annotation counts for pass results.

### R-14: Fixture writes can contaminate the preserved workspace

- **Evidence:** `legacy/test/util/TestUtil.java`, DATA_PATH/putData()/deleteData(), writes/deletes shared relative test/*.dat files and prints I/O failures.
- **Existing finding cross-reference:** Inventory M2; dependency map test helper and construction-order findings.
- **Assessment rationale / uncertainty:** Trigger requires running tests in the baseline or sharing fixture paths; no test was run here and no contamination is observed.
- **Consequence if not addressed:** Baseline execution can create/remove files; overlapping tests can interfere or hide setup failures.
- **Recommended mitigation:** Hamna owns isolated fixture/data setup and integrity checks; use a separately authorized execution copy/temporary paths later and fail explicitly on setup errors. Shared suite consumes these fixtures.
- **Proposed owner:** Hamna.
- **Suggested phase/task:** Reverse Engineering: isolated data/fixture prerequisite before any execution; Forward Engineering: integration fixtures.
- **Verification method (future, not executed):** Later prove each run uses isolated writable data, parallel fixtures do not collide, setup errors fail visibly, and original legacy hashes remain unchanged.

### R-15: Build/run environment cannot yet be reproduced from bundled assets

- **Evidence:** `legacy/unit1-ca.iml` references IDE-relative JUnit 4.12/Hamcrest 1.3; `legacy/.classpath` uses a JUnit container; `legacy/.idea/misc.xml` specifies Java 8; `legacy/submission/setenv.bat` hard-codes a local JDK. StoreApplication.main() expects relative data files.
- **Existing finding cross-reference:** Inventory H7/M6/M9; dependency map section 7.
- **Assessment rationale / uncertainty:** External dependencies/working-directory assumptions are confirmed; compilation failure is not. Missing prepared data/output is intentional, not lost source.
- **Consequence if not addressed:** The team cannot establish a trustworthy executable comparison baseline or reproduce reported failures.
- **Recommended mitigation:** Nawal owns a documented reproducible toolchain/launch specification in future work, including encoding, dependencies and working directory; prepare only an isolated writable runtime with Hamna's data fixtures.
- **Proposed owner:** Nawal.
- **Suggested phase/task:** Reverse Engineering: minimal reproducible execution prerequisite; Forward Engineering: maintained build/run guide.
- **Verification method (future, not executed):** Later reproduce compilation, test discovery and launch in a clean authorized workspace using documented versions; record actual outputs and prove legacy remains untouched.

### R-16: Packaging recipes can omit assets or clean the wrong location

- **Evidence:** `legacy/submission.sh` copies only test/sg, omits test/util/TestUtil.java and cleans sunmission/data/*; `legacy/submission.bat` copies the full test tree. `legacy/submission/compile.bat` removes/recreates output and contains classses/tools.jar/2>>&1 assumptions.
- **Existing finding cross-reference:** Inventory M6/M7; dependency map operational API/config evidence.
- **Assessment rationale / uncertainty:** Failure depends on invoking these recipes; none was executed. Platform recipes are source-confirmed non-equivalent.
- **Consequence if not addressed:** A future handover archive may lack required helpers or carry stale data; unsafe cleanup is unsuitable for a preserved baseline.
- **Recommended mitigation:** Nawal owns a safe derived packaging specification with complete inputs and controlled output roots, distinct from R-15's compiler/launch environment. Keep all original recipes as evidence.
- **Proposed owner:** Nawal.
- **Suggested phase/task:** Forward Engineering: packaging/handover after reproducible build.
- **Verification method (future, not executed):** Later inspect packaged contents against an approved manifest, include helper/resources, launch from the package and verify cleanup targets only disposable outputs.

### R-17: Vendor/category relationships can be lost during consolidation

- **Evidence:** `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/vendor/VendorManager.java`, load() derives category from filenames and merges by vendor name. The five category-specific vendor files have identical content but distinct category suffixes; all exact source paths are listed in E24.
- **Existing finding cross-reference:** Inventory M3/M4; dependency map D10 and vendor integration findings.
- **Assessment rationale / uncertainty:** Risk is conditional on consolidation/relational migration; source relationships and same-name assumptions are confirmed. No current data loss is claimed.
- **Consequence if not addressed:** Deduplicating files or vendors by content/name alone can drop category links or conflicting descriptions.
- **Recommended mitigation:** Hamna owns explicit vendor identity and category-association mapping, preserving every source relationship; vendor-ordering scope decisions remain part of R-01, not a new implementation assignment.
- **Proposed owner:** Hamna.
- **Suggested phase/task:** Data Restructuring: vendor/category relational mapping and reconciliation.
- **Verification method (future, not executed):** Later compare all category-vendor pairs before/after migration, test conflicting same-name descriptions and preserve approved associations without treating duplicate bytes as redundant meaning.

### R-18: Receipt/output behavior can be mistaken for hardware integration

- **Evidence:** `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/gui/checkout/ReceiptDialog.java`, setVisible()/getReceiptText() calls inherited static ReceiptPrinter.print(); `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/printer/Printer.java`, print() uses System.out. AdhesiveLabelPrinter.getTag() is a disconnected null-returning stub.
- **Existing finding cross-reference:** Inventory M5; dependency map D17 and printer reference findings.
- **Assessment rationale / uncertainty:** Risk concerns future scope/equivalence, not a demonstrated output failure. Physical printer/label requirements remain unverified.
- **Consequence if not addressed:** Team may remove working console receipts or implement unrequested hardware/labels, and output errors can remain tied to dialog display.
- **Recommended mitigation:** Iman owns an agreed receipt-output operation/format contract preserving current dialog/console results; retain the label stub as evidence until a team scope decision. No hardware implementation is authorized.
- **Proposed owner:** Iman.
- **Suggested phase/task:** Reverse Engineering: output acceptance; Forward Engineering: receipt/output operation.
- **Verification method (future, not executed):** Later compare receipt fields/totals/text and visibility-triggered output against approved expectations; explicitly document any change in output timing/destination and absence of assumed hardware features.

### R-19: Documentation and asset provenance gaps can misdirect handover

- **Evidence:** `legacy/README.md` lacks a complete operational/test guide; `legacy/submission/docs/report.doc` remains semantically unverified; inventory records bundled tools and uncertain IDE consumer files without establishing future suitability.
- **Existing finding cross-reference:** Inventory M10/L1/L2; asset inventory Needs investigation entries.
- **Assessment rationale / uncertainty:** Potential missing requirements, licensing/provenance and tooling relevance are unresolved, not confirmed incompatibilities or legal violations.
- **Consequence if not addressed:** The team may distribute unreviewed tools, lose useful configuration evidence or make unsupported functional claims.
- **Recommended mitigation:** Nawal owns reading/reconciling original documentation when tooling is available, maintaining evidence/decision/setup notes and checking tool/source provenance before future redistribution. Preserve IDE metadata; cosmetic cleanup is deferred.
- **Proposed owner:** Nawal.
- **Suggested phase/task:** Document Restructuring: operational/evidence guide; Forward Engineering: handover/provenance review.
- **Verification method (future, not executed):** Later trace guide steps to reproducible outputs and decisions, inspect report contents without editing, and record verified redistribution/tooling information without inferring terms from filenames.

### Consolidation boundaries

- R-02 concerns coherent new sale completion; R-03 concerns existing history reconstruction; R-04 concerns information never stored. Do not treat a fix to one as evidence that the others are resolved.
- R-05 owns file/import diagnostics and identity mapping; R-06 owns authoritative operation-input validation. A database constraint is not a substitute for either.
- R-07 owns monetary/point outcomes; R-08 owns calendar/eligibility boundaries; R-12 owns uncontrolled state access. Shared formatter state is a dependency observation, not an independently demonstrated concurrency failure.
- R-09 is the specific stock/view temporal defect; R-11 covers broader responsibility and cycle restructuring.
- R-13 owns suite/equivalence confidence; R-14 owns fixture isolation; R-15 owns executable-environment reproducibility; R-16 owns package contents/cleanup. Their acceptance outputs differ.
- Vendor relationship migration (R-17), output equivalence (R-18) and missing provenance/operational evidence (R-19) are not excuses to implement new features. Scope decisions feed the single behavior contract in R-01.
- Inventory L1/L2 cosmetic/test-name/empty-profile concerns are absorbed into R-19/R-13 and deferred; they are not inflated into additional high-priority risks.

## 3. Ranked reengineering priorities

The following ranking follows the requested order. It is a value/risk ranking, **not a command to postpone prerequisite tests or environment verification until the end**. Minimal executable-baseline preparation and characterization support all changes from the start; sustained test automation and polished tooling/documentation remain explicit ranked deliverables.

| Rank | Work area | Preserved outcome and risk basis | Entry dependency / what must precede it | Required handover or acceptance evidence |
|---|---|---|---|---|
| 1 | **Functionality that must be preserved** | Agree PR-01 through PR-10 and decisions B-01 through B-10; R-01/R-07/R-08/R-18/R-19. Preserve valid workflows and distinguish accepted defect corrections from accidental differences. | Read source/inventory and original documentation where accessible; obtain Shared behavior review. Later execution examples require isolated build/data prerequisites G1. | Approved behavior/decision matrix, examples and explicit non-goals; no unsupported CRUD/hardware/vendor integration claims. |
| 2 | **Data integrity and transaction correctness** | Resolve R-02/R-03/R-04 and stock/validation consequences in R-05/R-06/R-09. Sale effects, customer links and all history lines must be coherent. | Rank 1 accepted rules; raw source mapping and failure scenarios. Completion semantics must be agreed before implementing durable updates. | Approved all-or-nothing/error/retry contract; expected reload/reference cases; explicit policy for missing historical fields. |
| 3 | **Persistence and relational database migration** | Hamna maps records, relationships, constraints, snapshots, imports and reconciliation: R-02 through R-05/R-14/R-17; consumes credential contract from R-10. | Ranks 1-2 decisions, source record inventory, isolated fixtures, and jointly reviewed service/storage interface specification. Rank 4 boundary **design** can support this before any large refactor. | Proposed relational model and mapping signed off; later reconciled import counts/keys, rollback/retry evidence and unchanged originals. No engine or schema is implemented here. |
| 4 | **Separation of responsibilities and dependency cycles** | Nawal establishes seams; Iman implements business/services; Hamna provides adapters. R-09/R-11/R-12. Remove domain-to-manager-policy/Swing/storage coupling without discarding justified relationships. | Behavior contract and regression examples; initial domain/storage contracts agreed with rank 3 design. Do not mechanically relocate package-private cooperating classes. | Clear service/domain/data/UI contracts, access strategy and reviewable dependency changes; preserve callback indirection where useful. |
| 5 | **Security and input validation** | Replace plaintext authentication, validate operation inputs and settle date/money rules: R-06/R-07/R-08/R-10/R-12. | Rank 1 input/account decisions and rank 4 operation contracts; credential field contract must be settled before rank 3 schema is frozen. | Verified valid/invalid login, no raw future passwords, rejected bad requests without mutation and approved financial/date outcomes. Security design starts early; plaintext is never accepted as a releasable intermediate result. |
| 6 | **Automated tests and regression coverage** | Shared owns suite/integration/equivalence, with isolated data from Hamna and component evidence from each implementer: R-13/R-14; verifies all behavior/data/security risks. | Minimal G1 environment and fixtures; approved expected results distinguish bug-compatible characterization from corrected acceptance tests. | Existing-test results, new critical-path/failure/boundary results and PR-01 through PR-10 traceability. Tests are added/run alongside changes, not after all implementation is finished. |
| 7 | **Build/run/documentation improvements** | Nawal maintains reproducible setup, packaging, operational/decision notes and handover: R-15/R-16/R-19. Shared owns final presentation. | Minimal build/launch specification is an early prerequisite; final packaging/docs depend on verified integrated behavior and migration outputs. | Fresh-workspace reproduction, complete safe distribution, run/reset/recovery guide and recorded limitations. Do not run original destructive recipes against legacy. |

**Release/acceptance gates override rank position:** unresolved sale integrity, authentication or serious validation defects cannot be shipped merely because their work area ranks later. The Critical rating applies to R-02's acceptance consequence, not an instruction to implement before agreeing what a correct sale means.

## 4. Preservation requirements and unresolved behavior decisions

### Preservation interpretation

“Preserve” means retain the verified capability and accepted valid-input results. It does **not** require preserving plaintext storage, reversed history membership, omitted report lines, accidental exceptions or silent invalid-input fallbacks. Defect corrections need explicit expected results and traceable team decisions; until agreed, record observed and intended behavior separately.

“Management” below means the **implemented add/list operations and associated stock/point changes**. Startup wires no general edit/delete screens for category/member/product/discount records. No refund, payment gateway, barcode-scanner, external vendor-order or hardware-printer workflow is inferred.

The evidence catalog provides exact paths for E01-E25; methods named below were inspected directly. All later verification examples are proposed and have not run.

| ID | Workflow to preserve | Actual source-observed behavior / evidence | Required preserved outcome and decision boundary | Related risks / decision IDs |
|---|---|---|---|---|
| PR-01 | Staff login and entry | StoreApplication.main(), LoginPanel.getButtonPanel()/isSuccess(), StoreKeeperManager.login() (E01/E12). Modal login delegates credentials; unsuccessful/cancelled entry does not open feature tabs. | Staff can enter with accepted credentials and receive failure feedback otherwise. Preserve workflow, replace plaintext implementation; no new roles/accounts UI assumed. | R-01/R-10; B-07/B-09 |
| PR-02 | Category add/list | CategoryPanel constructor Add/Reset and CategoryManager.addCategory()/getTableModel() (E02). ID must be nonblank, unique and length three; lookup uppercases while insertion retains case. | Add/list categories and maintain product associations; agree ID normalization and alphabet/name constraints. Do not present case inconsistency as a correct rule. | R-05/R-06/R-11; B-01/B-09 |
| PR-03 | Member registration/list | MemberPanel constructor, MemberManager.addMember(), Member constructor (E03). Nonblank unique ID; initial loyalty -1; table shows name, ID and points. | Register/list members and maintain identities/loyalty; decide explicit first-purchase representation and valid-input rules. | R-05/R-07/R-12; B-01/B-02/B-09 |
| PR-04 | Product add/list | ProductPanel.newProductPanel(), ProductManager.addProduct(), Product fields (E04). Requires category; barcode uniqueness; generated categoryId/sequence ID; stores description, quantity, price, integer barcode, threshold, order quantity. | Preserve fields, valid creation/list and category/barcode links; preserve product-ID checkout. Malformed numbers becoming zero is a defect/decision case, not a requirement. | R-05/R-06/R-12; B-01/B-09 |
| PR-05 | Discount add/list and selection | DiscountPanel.newDiscountPanel(), DiscountManager.addDiscount()/getMaxDiscount(), Discount.isDiscountAvailable() (E05). Highest eligible percentage wins, not stacking; M/A audience, dates/period and special first/subsequent member codes apply. Manager returns false for duplicate codes; startup's void binding loses that feedback. | Create/list definitions and apply approved eligibility; agree duplicate-error feedback, percentage/date validity and sentinel semantics. Do not silently add stacking. | R-06/R-07/R-08; B-01/B-02/B-03/B-09 |
| PR-06 | Inventory and immediate replenishment | InventoryPanel constructor, Product.isUnderstock()/restock(), ProductManager.generatePurchaseOrder() (E04/E14). Understock is quantity <= threshold; selected rows confirmed; configured order quantity is added immediately. | Preserve understock list, selection/confirmation, replenishment and updates. No external purchase-order document, delivery tracking or vendor selection is established. | R-02/R-09/R-17; B-04/B-06 |
| PR-07 | Basket and member selection | CheckoutPanel.getItemPanel()/getBottomPanel()/newTransactionRequested(), Transaction.addTransactionItem() (E06/E08). Product-ID lookup, positive GUI quantity, duplicate-product merging, stock-limit checks, optional member; empty membership is public/null; Reset starts a new basket. | Preserve valid basket/member/public flows, totals and reset; agree cancellation/reopen and invalid-input behavior without allowing unintended stock/point mutation. | R-01/R-06/R-07/R-12; B-01/B-02/B-04 |
| PR-08 | Payment, loyalty and completed sale | ConfirmPaymentDialog.getPaymentPanel()/balanceChanged(), Transaction totals, TransactionManager.addTransaction(), MemberManager point methods (E03/E07/E08/E09). Recorded amount plus redemption covers discounted total; negative balance is change; current constants give one point per 10 pre-discount dollars (floor) and 5 dollars per redeemed point. Confirmed sale assigns ID/date, changes points/stock, then completion/understock UI follows. | Preserve accepted payment/change/point capability; team must approve rates/rounding/credit basis and first-purchase transition. Completion must mean coherent durable success; current partial-write/fallback behavior is not automatically acceptable. | R-02/R-06/R-07/R-09/R-10; B-02/B-04/B-07 |
| PR-09 | Transaction history and reports | TransactionManager.load()/getTableModel(), ReportPanel constructor/createTransactionReportPanel()/set*TableModel() (E09/E11/E17). Four tabs: category, product, member and transaction-line reports. Date filters use >= start and <= end comparisons. Sample has four lines for three transaction IDs; loader currently misidentifies customers and loses flattened rows. | Preserve history access, report types, fields and approved date inclusion; corrected member links/all-line completeness require accepted expected results. Historical sale amounts absent from source stay explicitly unknown. No sales aggregation/export is inferred. | R-03/R-04/R-08/R-13; B-03/B-05 |
| PR-10 | Optional receipt and post-sale feedback | CheckoutPanel.getBottomPanel(), ReceiptDialog.getReceiptPanel()/getReceiptText()/setVisible(), Printer.print() (E06/E15). Completed sale offers receipt-and-return or return; dialog displays items, subtotal/discount, cash/change and member points; showing it prints text to console. Purchased understock items trigger an alert. | Retain accepted receipt fields, optional output and stock feedback; agree destination/timing. The label stub and physical printing remain unverified scope. | R-01/R-18; B-06/B-08 |

### Team decisions required before treating behavior as a target

All B-items are **open**. Shared reviews/accepts behavior decisions; the preparer below gathers evidence and a proposed answer, not duplicate implementation ownership.

| ID | Decision | Source-observed issue / choice to make | Preparer → decision review | Gate / affected risk |
|---|---|---|---|---|
| B-01 | Valid identifiers and input constraints | Three-character category ID versus uppercase lookup; blank names; numeric defaults/ranges; barcode representation; duplicate feedback; field escaping. Agree valid domain inputs and import normalization separately. | Iman (operation rules), with Hamna supplying import-collision evidence → Shared | G2/G3; R-05/R-06 |
| B-02 | Money and loyalty contract | Approve or intentionally change 10-dollar earning basis, 5-dollar redemption rate, pre-discount credits, rounding, -1 sentinel and first/subsequent transition. Commented old formulas are not active requirements. | Iman → Shared | G2; R-07 |
| B-03 | Dates and report ranges | Agree timezone, calendar-day versus timestamp meaning, start/end inclusivity, ALWAYS and invalid-date handling. | Iman → Shared | G2/G3; R-08 |
| B-04 | Durable sale, shortage, cancellation and retries | Define accepted failure/rollback/retry outcomes and exactly when UI may say completed. Decide whether stock shortfall rejects a sale rather than current deduct-remaining fallback. Characterize cancel/reopen without inventing refund functionality. | Hamna (durability contract), consuming Iman operation examples → Shared | G2/G3; R-02/R-09 |
| B-05 | Historical repair and unknown values | Approve corrected PUBLIC/member resolution, all-line reporting, duplicate/missing references and representation of unavailable historical money/payment/points. Do not fabricate missing facts. | Hamna → Shared | G2/G3; R-03/R-04/R-05 |
| B-06 | Replenishment/vendor scope | Retain immediate stock arrival or explicitly authorize a later requirement change; distinguish stored vendor associations from an unimplemented ordering workflow. | Nawal → Shared | G2; R-01/R-17 |
| B-07 | Staff identity/credential migration | Preserve login capability; decide reset versus approved migration, synthetic fixture accounts and credential record contract. No plaintext future accounts or invented role system. | Iman → Shared | G3 before credential schema freeze; R-10 |
| B-08 | Receipt/label output scope | Confirm console/dialog equivalence and output timing; decide whether hardware/labels are out of scope or separately authorized additions. | Nawal (scope evidence), consuming Iman output examples → Shared | G2; R-01/R-18 |
| B-09 | Management scope | Confirm add/list and implemented stock/point operations; general update/delete/refund features are not established. | Nawal → Shared | G2; R-01 |
| B-10 | Fixture identities and provenance | Named sample members/vendors/staff come from upstream; synthetic status and redistribution suitability are not independently established. Decide derived demonstration data and review original documentation/tools. | Nawal (provenance), consuming Hamna fixture map → Shared | G1/G2 for derived fixtures; G6 before distribution; R-14/R-19 |

## 5. Team ownership and handover matrix

The user-supplied split is authoritative. The assignments below are proposals within it, not claims that teammates have accepted or completed work.

| Proposed owner | Accountable risk IDs | Major concern / owned implementation boundary | Handover to | Concrete handover content / acceptance expectation |
|---|---|---|---|---|
| Nawal | R-01, R-09, R-11, R-15, R-16, R-19 | Legacy analysis, architecture contracts, documentation, minimal build/run specification, initial table-adapter/bootstrap seams and safe packaging. No parallel ownership of Iman's business policy or Hamna's repositories/importers. | Iman and Hamna | Source-backed behavior/decision catalog; dependency/access map; agreed interfaces; executable-baseline instructions; initial structure with stock operation independent of view initialization; later setup/package guide. |
| Iman | R-06, R-07, R-08, R-10, R-12, R-18 | Business rules, input validation, application/service orchestration, authentication and receipt-output operation. Consume persistence/completion contracts; do not implement a second importer/transaction boundary. | Hamna for data contracts; Shared for equivalence | Validated operation inputs/results/errors, money/date/loyalty examples, credential record needs, explicit sale-completion calls and output expectations; component verification evidence. |
| Hamna | R-02, R-03, R-04, R-05, R-14, R-17 | Relational data model, persistence boundaries/adapters, historical mapping/import/reconciliation, isolated fixtures and integration data quality. Do not duplicate Iman's service-level policy. | Iman for persistence interfaces; Shared for integration evidence | Record/relationship map, constraints and unknown-value policy; atomic completion and error/retry contract; fixture isolation; row/key/association reconciliation; component verification evidence. |
| Shared | R-13 | Integration, approved functional equivalence, final regression, team behavior decisions and presentation. Shared is explicit for these joint activities, not a second owner of every component implementation. | Whole team / final reviewers | Preservation-to-test matrix, recorded expected defect corrections, integrated pass/fail evidence, final demo/presentation and unresolved-risk disclosures. |

### Interface-specific handovers

| Handover | Single implementation owner | Input required | Output and receiver |
|---|---|---|---|
| Architecture seams and package-private access strategy | Nawal | PR requirements and Iman/Hamna contract needs | Contract/access proposal → Iman/Hamna; they review but do not co-implement the same seam task. |
| Sale application operation | Iman | B-02/B-03/B-04 and Hamna's durability API | Validated completion command/result/error coordination → Hamna adapter and Shared integration suite. |
| Atomic persistence completion | Hamna | Iman's sale/stock/loyalty operation contract | Durable write/rollback/retry behavior → Iman; the service does not issue independent repository commits. |
| Historical/relational migration | Hamna | B-01/B-05/B-07/B-10 and unmodified sample files | Mapping/reconciliation report, accepted rejects and missing-value markers → Shared review. |
| Business/date/security validation | Iman | Shared-approved examples and storage-field contract | Rule/authentication implementation and component examples → Hamna constraints and Shared regression. |
| Fixture/data isolation | Hamna | Minimal build/run layout and accepted demonstration-data policy | Disposable deterministic fixtures → all component checks and Shared suite. |
| Preservation/equivalence suite and integration | Shared | Component evidence from each single owner and approved correction decisions | End-to-end acceptance evidence → all team members. |
| Operational handover and presentation | Nawal owns guide/package; Shared owns presentation/integrated sign-off | Verified integrated build/data/testing results | Reproducible guide/package from Nawal; presentation and final acceptance from Shared. These are distinct outputs. |

No person is assigned ownership based on guessed skill, availability or prior completion. Named assignments follow only the split given in this task.

## 6. Dependencies and suggested execution order

The G-labels below are **suggested gates for later approved work**, not newly approved numbered project tasks, schedules or completion claims. No build/test/implementation starts as part of Task 1.3.

| Gate | Suggested work and owner | Must exist before starting | Exit evidence / work unlocked |
|---|---|---|---|
| G0: review assessment | Nawal prepares this assessment; Shared reviews risks, scope and ownership | Existing inventory and source evidence | Accepted priority/owner proposals and explicit unknowns; authorization for subsequent investigation. |
| G1: establish safe execution prerequisites | Nawal specifies minimal toolchain/working directory; Hamna specifies isolated writable fixtures | G0; no changes to legacy; reviewed fixture provenance constraints | Later actual compile/launch/test-discovery outputs from a disposable copy and before/after legacy integrity checks. Enables characterization; not a claim those checks currently pass. |
| G2: characterize and decide behavior | Shared owns comparison/equivalence cases; Nawal maintains decisions; Iman supplies rules; Hamna supplies reload/data cases | G1 for execution, source evidence for preparatory examples | PR-01 through PR-10 examples and approved B decisions relevant to the next change. Known defects have separate observed and accepted-corrected expectations. |
| G3: define contracts and relational mapping | Nawal owns boundary/access contracts; Iman owns operation/rule contract; Hamna owns relational/import/durability design | G2 decisions on identifiers, financial rules, history, durability and credentials | Agreed interface/record contracts, proposed constraints and unknown-value mapping; each output has one owner. No need to finish all UI restructuring before schema design, or all schema implementation before service-contract design. |
| G4: implement small verified increments | Nawal initial structural seams; Iman domain/services/security; Hamna persistence/importers | G3 contract for that increment; G1 fixtures and G2 regression expectations | Each increment preserves accepted workflows and adds component/regression evidence. Keep rule and persistence work coordinated through contracts, not overlapping ownership. |
| G5: integrate and rehearse migration/failure recovery | Shared owns integration/equivalence; Hamna owns reconciliation/durability component evidence | Relevant G4 components and unresolved decisions closed for those paths | Verified all-or-nothing sale effects, complete customer/line history, accepted rejects/unknowns, relationships, authentication and report/output equivalence. No migration/cutover acceptance with R-02 unresolved. |
| G6: operational handover and presentation | Nawal guide/package/provenance; Shared final tests/presentation | G5 evidence plus remaining Critical/High acceptance decisions resolved or explicitly bounded by agreed scope | Reproducible clean setup/package, recovery/reset instructions, final test/demo evidence and documented residual risks. No silent waiver of credential or data-integrity failures. |

### Dependencies that must not be reversed

- **Behavior decisions before financial/schema freeze:** unknown loyalty/date/history rules are not fixed by choosing SQL types. Agree B-02 through B-05/B-07 before freezing relevant columns and service behavior.
- **Contracts before parallel implementation:** Iman and Hamna can prepare designs in parallel, but the service's success/error/commit contract must agree with persistence behavior before integration.
- **Characterization before correction:** preserve a record of observed defective behavior, then test the explicitly approved correction. Do not demand byte-for-byte bug compatibility or silently change expected results.
- **Isolation before execution:** fixture/build activity must not write to legacy. Minimal environment/test preparation occurs early despite ranks 6-7; full automation/tooling improves continuously.
- **Migration rehearsal before acceptance:** compare record counts **and** identities, line membership, keys, quantities, points and vendor/category pairs. Source has 57 sample rows across 11 files, including four transaction lines for three IDs; counts alone cannot prove semantic fidelity.
- **New writes versus old unknowns:** future sale records may carry approved financial snapshots, while original history lacks those fields. Do not backfill fabricated historical values to make a constraint pass.
- **Security is not deferred to a release cleanup:** agree credential representation before related schema work and validate requests before accepting state changes.

## 7. Evidence references

All paths below are exact repository-relative paths. E-identifiers resolve the preservation and decision tables to source files/methods; inventory finding references are supporting context, not replacements for source verification. The binary report remains unread and licensing/provenance remains uncertain; neither is treated as established requirements or clearance.

| Ref | Exact path(s) | Relevant class/method or evidence |
|---|---|---|
| E01 | `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/StoreApplication.java` | main(): login, manager construction, callbacks, seven feature tabs; report models. |
| E02 | `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/category/CategoryManager.java`; `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/gui/CategoryPanel.java` | addCategory()/getCategory()/store()/getTableModel(); Add/Reset listener. |
| E03 | `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/customer/member/Member.java`; `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/customer/member/MemberManager.java`; `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/gui/MemberPanel.java` | Member constructor (-1); addMember(), debitLoyaltyPoint(), creditLoyaltyPoint(), store(); registration/list. |
| E04 | `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/product/Product.java`; `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/product/ProductManager.java`; `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/gui/ProductPanel.java` | isUnderstock()/restock()/deductQuantity(); load(), addProduct(), generatePurchaseOrder(), table models; newProductPanel(). |
| E05 | `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/discount/Discount.java`; `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/discount/DiscountManager.java`; `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/gui/DiscountPanel.java` | isDiscountAvailable(), addDiscount(), getMaxDiscount(), newDiscountPanel(). |
| E06 | `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/gui/checkout/CheckoutPanel.java` | getItemPanel(), getBottomPanel(), newTransactionRequested(); optional member/payment/output/alerts. |
| E07 | `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/gui/checkout/ConfirmPaymentDialog.java` | getPaymentPanel(), balanceChanged(), isConfirmed(); payment/redemption controls. |
| E08 | `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/transaction/Transaction.java` | addTransactionItem(), getRedeemPointValue(), total/balance methods, getTableModel(), toStringStream(). |
| E09 | `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/transaction/TransactionManager.java` | load(), addTransaction(), store(), getTableModel(); conversion constants. |
| E10 | `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/transaction/TransactionItem.java` | parent/product/quantity; getParent(), setParent(), toString() five-field history row. |
| E11 | `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/gui/ReportPanel.java` | constructor tabs, createTransactionReportPanel(), set*TableModel(); inclusive date filters. |
| E12 | `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/storekeeper/StoreKeeper.java`; `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/storekeeper/StoreKeeperManager.java`; `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/gui/LoginPanel.java` | getPassword(), load(), login(); modal credential input and isSuccess(). |
| E13 | `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/util/Util.java` | splitCsv(), numeric/date fallbacks, DATE_FORMAT, addDate(), formatDollar(). |
| E14 | `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/gui/InventoryPanel.java` | Selected-row purchase confirmation; InventoryPanelListener dispatch. |
| E15 | `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/gui/checkout/ReceiptDialog.java`; `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/printer/Printer.java`; `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/printer/ReceiptPrinter.java`; `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/printer/AdhesiveLabelPrinter.java` | getReceiptText()/setVisible(); static console print; null tag implementations and disconnected label stub. |
| E16 | `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/vendor/VendorManager.java`; `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/vendor/Vendor.java` | load() filename category mapping/name merge; getVendorListByCategoryId(); category associations. |
| E17 | `legacy/data-sample/Transactions.dat` | Four transaction lines, three IDs; first transaction has two distinct products. |
| E18 | `legacy/data-sample/Members.dat`; `legacy/data-sample/Storekeepers.dat` | Member sentinel/identities and plaintext sample staff credentials; not external secrets or verified real accounts. |
| E19 | `legacy/test/sg/edu/nus/iss/se24_2ft/unit1/ca/TransactionManagerTest.java`; `legacy/test/sg/edu/nus/iss/se24_2ft/unit1/ca/TransactionTest.java`; `legacy/test/sg/edu/nus/iss/se24_2ft/unit1/ca/ProductManagerTest.java`; `legacy/test/sg/edu/nus/iss/se24_2ft/unit1/ca/StoreKeeperManagerTest.java`; `legacy/test/sg/edu/nus/iss/se24_2ft/unit1/ca/DiscountTest.java` | Weak sale assertions, null replenishment, empty initialization and fixed-date test; no execution claimed. |
| E20 | `legacy/test/util/TestUtil.java` | DATA_PATH fixture filenames; putData()/deleteData() write/delete relative files. |
| E21 | `legacy/unit1-ca.iml`; `legacy/.classpath`; `legacy/.idea/misc.xml`; `legacy/.idea/encodings.xml` | JUnit/Hamcrest IDE dependencies, source/output roots, Java 8 and UTF-8 intent. |
| E22 | `legacy/submission/compile.bat`; `legacy/submission/run.bat`; `legacy/submission/setenv.bat` | Direct compiler/launch commands, output cleanup and machine-specific environment. |
| E23 | `legacy/submission.bat`; `legacy/submission.sh`; `legacy/tools/7z.exe`; `legacy/tools/7z.dll` | Different packaging inputs and cleanup; bundled tools not executed or certified. |
| E24 | `legacy/data-sample/VendorsCLO.dat`; `legacy/data-sample/VendorsDIA.dat`; `legacy/data-sample/VendorsMUG.dat`; `legacy/data-sample/VendorsSNA.dat`; `legacy/data-sample/VendorsSTA.dat` | Identical contents with different category filenames; preserve association semantics. |
| E25 | `legacy/README.md`; `legacy/submission/docs/report.doc`; `legacy/.gitignore`; `legacy/src/META-INF/MANIFEST.MF`; `legacy/unit1-ca.eml`; `legacy/unit1-ca.userlibraries`; `legacy/.idea/copyright/profiles_settings.xml` | Historical setup, unread report, ignored live outputs, entry point and IDE metadata with unresolved consumers/relevance. |
| E26 | `docs/inventory/asset-inventory.md`; `docs/inventory/inventory-summary.md`; `docs/inventory/inventory-findings.md`; `docs/inventory/dependency-map.md` | Read-only inputs: file classification/counts; H/M/L findings; D/W relationships, cycles and proposed separation. |

### Verification scope and residual uncertainty

This document's checks cover risk-field completeness, unique IDs/owners, priority/owner totals, preservation/decision/evidence references, exact path existence, whitespace and before/after file hashes. They do not execute the proposed mitigation verification methods.

Baseline byte preservation and existing-document hashes are checked independently of the current Git status, because earlier inventory documents are already untracked. Only `docs/inventory/reengineering-priorities.md` may be new/changed by this task. No risk is closed by creating this assessment.
