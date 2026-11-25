# Data persistence handover contract

**Status: proposed contract for team review; no repository interface or database implementation exists yet.** Prepared 2026-11-25 against Phase 4 commit `460f737349622fb3e57e8b09025977ebf057a7b6`. This document defines target behavior; it does not declare a migration or business-rule correction complete.

Read with [team integration guide](team-integration-guide.md), [data reconstruction](../reverse-engineering/data-reconstruction.md), [business rules](../reverse-engineering/business-rules.md), [selected refactorings](../refactoring/selected-refactorings.md) and [refactoring evidence](../refactoring/refactoring-evidence.md). Existing rule/risk decisions remain in [reengineering priorities](../inventory/reengineering-priorities.md).

## 1. Existing implementation and insertion points

| Current source | Verified responsibility | Future repository insertion point / compatibility limit |
|---|---|---|
| [StorePaths](../../reengineered/src/main/java/sg/edu/nus/iss/se24_2ft/unit1/ca/bootstrap/StorePaths.java) | Resolves legacy data filenames; no I/O. | Retain for comparison/file import. It is not database configuration. |
| [StoreContext.load()](../../reengineered/src/main/java/sg/edu/nus/iss/se24_2ft/unit1/ca/bootstrap/StoreContext.java) | Constructs category/vendor/product/member/discount/transaction managers using filenames/directory. Their constructors read files. | Composition seam for a later reviewed service/adapter wiring change; current constructors cannot accept repositories. |
| [CategoryManager.load()/store()/addCategory()](../../reengineered/src/main/java/sg/edu/nus/iss/se24_2ft/unit1/ca/category/CategoryManager.java) | Category registry, validation, Files access and Swing model. | Move file mapping to adapters and operation rules to Iman's service; do not merely add SQL beside store(). |
| [ProductManager.load()/store()/addProduct()/deductQuantity()/generatePurchaseOrder()](../../reengineered/src/main/java/sg/edu/nus/iss/se24_2ft/unit1/ca/product/ProductManager.java) | Product/category/barcode/ID state, stock rules and file access; no Swing types now. | ProductRepository backs future product/inventory operations; current class still owns files and concrete CategoryManager. |
| [MemberManager.load()/store()/debitLoyaltyPoint()/creditLoyaltyPoint()](../../reengineered/src/main/java/sg/edu/nus/iss/se24_2ft/unit1/ca/customer/member/MemberManager.java) | Member balances, rules, independent writes and table events. | MemberRepository provides consistent balance changes/movements inside a shared sale write session. |
| [DiscountManager.load()/store()/getMaxDiscount()](../../reengineered/src/main/java/sg/edu/nus/iss/se24_2ft/unit1/ca/discount/DiscountManager.java) | Definitions, selection rules, file writes and table model. | DiscountRepository stores/queries definitions; eligibility/maximum selection remains business policy. |
| [TransactionManager.load()/store()/addTransaction()](../../reengineered/src/main/java/sg/edu/nus/iss/se24_2ft/unit1/ca/transaction/TransactionManager.java) | Loads history, coordinates member/product writes and sales, allocates in-memory IDs, builds table rows. | Replace write coordination with Iman's service over Hamna's unit of work; repository stores sale aggregates/history without performing hidden member/product writes. |
| [StoreKeeperManager.load()/login()](../../reengineered/src/main/java/sg/edu/nus/iss/se24_2ft/unit1/ca/storekeeper/StoreKeeperManager.java); [StoreApplication.main()](../../reengineered/src/main/java/sg/edu/nus/iss/se24_2ft/unit1/ca/StoreApplication.java) | Staff file is read separately before other managers; plaintext comparison gates startup. | StaffRepository supplies restricted credential records to authentication service; do not return plaintext or expose credentials to panels. |
| [VendorManager.load()/getVendorListByCategoryId()](../../reengineered/src/main/java/sg/edu/nus/iss/se24_2ft/unit1/ca/vendor/VendorManager.java) | Reads category-specific files, merges by name; no user-facing vendor operation wired. | VendorRepository supports required data preservation/lookup, not invented purchase-order integration. |
| [StoreFeatures](../../reengineered/src/main/java/sg/edu/nus/iss/se24_2ft/unit1/ca/gui/StoreFeatures.java); [ProductTableModels](../../reengineered/src/main/java/sg/edu/nus/iss/se24_2ft/unit1/ca/gui/ProductTableModels.java) | Concrete managers wired to callbacks; product adapter expects ProductManager and row-index notifications. | Not repository-pluggable today. Nawal adapts presentation/composition to accepted service read models and stable IDs in a later task. |
| [TransactionReportBinding.bind()/applyDateRange()](../../reengineered/src/main/java/sg/edu/nus/iss/se24_2ft/unit1/ca/gui/TransactionReportBinding.java) | Accepts TableModel; rendering/filtering only, no file/manager imports. | Keep independent of DB/JDBC/ORM; a presentation adapter supplies query results. |

All seven managers currently perform file reads; five rewrite files (category/product/member/discount/transaction). Staff/vendor have no normal writer. Sale writes happen independently: member debit/credit, per-item products, then history. Current exceptions may be printed and swallowed. **None of these patterns constitutes the proposed transactional guarantee.**

Existing interfaces are Customer, six GUI callback interfaces and ProductCatalogListener, plus Java AutoCloseable on new adapters. They are not repository contracts. ProductCatalogListener carries table indices and fires before file storage; it must not be treated as a durable-commit event or called by a database adapter.

## 2. Contract conventions and shared value vocabulary

Everything named in sections 2–7 is **proposed**, including interface/DTO/error names; none is a newly created Java type. DTO means a detached, immutable, Java-8-compatible value object, not the Java language record feature.

### Inputs and results

- IDs are typed logical values: CategoryId, ProductId, MemberId, DiscountCode, SaleId, StaffId, VendorId. Preserve source IDs through explicit mapping; storage-generated surrogate IDs must not leak as replacement UI identifiers without agreement.
- Version is an opaque optimistic-concurrency token, not a domain counter. Every update takes the version read in the same operation and returns a fresh snapshot/version.
- Point counts and stock quantities are integers with checked range/overflow handling. Money is BigDecimal plus an explicitly configured currency and MoneyPolicy (precision/scale/rounding). Do not pass double or infer currency from the dollar label.
- Snapshots expose fields/IDs/versions, not live managed entities, mutable lists, Swing TableModels, result sets, lazy ORM proxies or connections. Returned collections are detached/read-only; date/money values must not be mutable aliases.
- find* returns Optional<Snapshot> for an absent valid key. list* returns an empty list for no matches, never null. Required write targets missing → NOT_FOUND. Unsupported/malformed arguments → INVALID_INPUT before mutation.
- List order is deterministic: categories/products/members by their approved logical ID, discounts by code, vendors by stable VendorId. HistoryQuery declares its order explicitly; see P5. No adapter relies on database encounter order.
- Services validate business decisions; repositories additionally enforce shape, range, uniqueness, references, versions and durable invariants. Neither owner silently normalizes identifiers, changes a rule or drops malformed records.
- All writes require a WriteSession from the unit of work below, including catalog insert, staff provisioning and migration. ReadSession exposes read methods only. Per-repository write methods never commit/autocommit or open independent transactions.

### Draft/input contents

| Proposed value | Required content / exclusions |
|---|---|
| NewCategory | Approved logical category ID and name. No generated name/default. |
| NewProduct | Service-formatted ProductId, CategoryId, name/description, opening quantity, Money price, barcode, threshold, orderQuantity. |
| NewMember | MemberId/name, nonnegative opening balance and explicit firstPurchaseEligible state; legacy -1 mapping is an import decision. |
| NewDiscount | Code/description, optional start date, optional unbounded duration, percentage, memberOnly. Boundary/sentinel conversion must be explicit. |
| NewSale | Requested immutable SaleId, optional MemberId/DiscountCode, service occurrence date/time/precision, subtotal/discount/payment/redemption/credit snapshots and MoneyPolicy/rule provenance; normal new writes have complete financial data. |
| NewSaleLine | Stable line sequence within sale, ProductId, positive quantity and accepted unit-price snapshot; lines use the approved aggregation policy. |
| LoyaltyChange | SaleId, redeemedPoints >= 0, earnedPoints >= 0 and firstPurchaseEligibleAfter; service supplies calculated values and passes the expected member version separately. |
| ProvisionedCredential | Stable staff identity/login key, opaque encoded verifier and its metadata from Iman's authentication contract; never a raw password. |
| SourceRecordKey | Import-run ID, original relative filename, line/group ordinal, source hash and original logical keys; traceability, not a fabricated business event. |

Optional dates/durations are explicit values; a misspelled date is not ALWAYS. Source parsing is a migration adapter responsibility, not an application repository input format.

## 3. Unit of work, errors and command replay

### Proposed UnitOfWork / session contract

Conceptual signatures (documentation only):

```text
read(callback(ReadSession) -> T) -> T
write(CommandEnvelope, callback(WriteSession) -> MutationReceipt) -> CommittedReceipt
lookupCommit(commandId) -> COMMITTED(receipt) | NOT_RECORDED | UNKNOWN
```

CommandEnvelope contains a stable commandId, command type and canonical request fingerprint. MutationReceipt identifies affected entity keys/versions and optional SaleId; CommittedReceipt is detached and records the committed result/commit marker. It is not a Swing event or a promise that the view already refreshed.

- Hamna owns session/connection lifetime, atomic commit/rollback, isolation/conditional updates and commit-receipt storage. Iman owns command identity/input, callback operation order and business decisions.
- A write callback can use all seven repository families through **one session/connection and one commit boundary**. No nested independent commits. A thrown error rolls back all writes, including IDs/ledger/receipt records where the engine supports transactional allocation; gaps in reserved IDs are allowed, duplicate IDs are not.
- Persist the command receipt atomically with its business effects. Same commandId and same fingerprint returns the prior committed receipt without rerunning writes; same ID/different fingerprint → DUPLICATE_COMMAND. Concurrent same-key attempts serialize through durable uniqueness/locking; no double stock/loyalty application.
- Fingerprints cover the stable intended request, not transient retry timestamps/random new IDs. Iman freezes request identity across retries. Credential secrets must not be included in logs/receipt payloads.
- Read operations used to calculate/validate a sale occur inside that write session. Product/member version checks or locks prevent stale stock and balance commits. Queries needed for a coherent report run under one read snapshot.
- After a known rollback, report a typed failure and no commit receipt. If connection loss makes commit outcome unknowable, return OUTCOME_UNKNOWN with commandId; UI must not show definite success/failure or submit a new commandId automatically.
- lookupCommit NOT_RECORDED is not proof that another in-flight attempt cannot commit. A retry must still use the same commandId/fingerprint through the deduplicating write boundary. UNKNOWN requires recovery/reconciliation.
- Publish UI changes/refreshes only **after committed success**, outside the DB transaction. UI refresh failure must not roll back or resubmit a committed sale. Durable outbox messaging is not required by current functionality; do not claim crash-safe notifications already exist.

### Typed failures

| Proposed category | Required meaning / caller expectation |
|---|---|
| INVALID_INPUT | Invalid shape/range/money/date/key contract. No durable mutation; service maps useful validation feedback. |
| NOT_FOUND | Required write target absent; find* absence remains Optional.empty(). |
| DUPLICATE_KEY | Existing category/member/product/barcode/discount/staff/association key collision; no overwrite. |
| REFERENCE_CONFLICT | Missing category/member/product/vendor reference or forbidden destructive change; never silently skip a row. |
| VERSION_CONFLICT | Stale entity version; whole operation rolls back. Service must reread/revalidate rather than blindly reuse amounts. |
| INSUFFICIENT_STOCK / INSUFFICIENT_POINTS | Atomic conditional update cannot satisfy requested deduction; do not deduct remaining stock or credit first to hide shortage. |
| DUPLICATE_COMMAND | Command ID reused for different intent. Do not treat as ordinary duplicate product. |
| DATA_INCOMPLETE / DATA_INTEGRITY | Target data cannot support required operation or violates reconstruction assumptions; fail visibly and reconcile. Historical query may instead return a deliberately flagged incomplete financial snapshot. |
| UNAVAILABLE | Data access failed with known failure/rollback. Wrong credentials is a separate authentication outcome, not this error. |
| OUTCOME_UNKNOWN | Commit may have succeeded; reconcile using commandId before telling the user to retry. |

Hamna translates vendor SQL/driver details into these categories and preserves diagnostic causes privately; callers receive no raw SQL, password or connection information. Do not imitate printStackTrace-and-success behavior.

## 4. Seven proposed repository families

Read methods are available in ReadSession and WriteSession; mutations below are WriteSession-only. Proposed operation signatures are intentionally technology-neutral. They describe minimum required persistence operations, not new user-facing CRUD.

### P1 — ProductRepository: products and inventory

**Source basis:** ProductManager.load()/addProduct()/deductQuantity()/generatePurchaseOrder() and unchanged Product arithmetic (paths in section 1 and [Product](../../reengineered/src/main/java/sg/edu/nus/iss/se24_2ft/unit1/ca/product/Product.java)).

| Required operation | Input/output responsibility | Transaction / error expectations |
|---|---|---|
| findById(ProductId), findByBarcode(Barcode) | Optional ProductSnapshot with category ID, all eight legacy fields and version. | Coherent session read; no implicit file loading. |
| listAll(), listUnderstock() | Detached ordered product snapshots; understock predicate is quantity <= threshold. | No JTable row indices in repository API. |
| insert(NewProduct) | Store approved new product; return snapshot/version. Category sequence allocation is P2, ID formatting is service-owned. | Unique logical ID and barcode, existing category, valid configured ranges; no upsert. |
| adjustStock(ProductId, signedDelta, expectedVersion) | Return updated StockSnapshot/version. Sale supplies negative quantity; immediate replenishment supplies accepted positive orderQuantity. Zero delta is a proposed no-op after verifying product existence/version. | Atomic checked addition and resulting stock >= 0; VERSION_CONFLICT/INSUFFICIENT_STOCK rolls back operation; no clamping. |

Iman calculates quantities/decides eligibility for replenishment. Hamna prevents lost updates and underflow; adapter does not automatically reorder stock or write a sale. Proposed zero/range semantics require decision D3 before implementation.

### P2 — CategoryRepository

**Source basis:** CategoryManager.load()/getCategory()/addCategory().

| Required operation | Responsibility | Transaction / boundary |
|---|---|---|
| findById(CategoryId), listAll() | Optional/ordered CategorySnapshot. | Explicit approved ID case/normalization; no silent uppercasing by adapter alone. |
| insert(NewCategory) | Persist accepted logical ID/name; return snapshot/version. | Unique ID and valid required fields. |
| reserveProductSequence(CategoryId) | Return a unique monotonically allocated suffix for that category within a write session. | Atomically allocate, never service MAX+1. Seed from accepted imported IDs; service formats categoryId/suffix. Gaps are allowed; uniqueness is mandatory. |

No delete/rename API is required by preserved category functionality. Sequence allocation does not give Hamna authority to choose a new user-visible ID format.

### P3 — MemberRepository: member identity, balances and movements

**Source basis:** MemberManager.addMember()/debitLoyaltyPoint()/creditLoyaltyPoint() and [Member](../../reengineered/src/main/java/sg/edu/nus/iss/se24_2ft/unit1/ca/customer/member/Member.java).

| Required operation | Responsibility | Transaction / boundary |
|---|---|---|
| findById(MemberId), listAll() | Optional/ordered MemberSnapshot with points, explicit firstPurchaseEligible state and version. | Unknown imported state is flagged; not silently treated as zero. |
| insert(NewMember, optional OpeningProvenance) | Persist registration/import opening state and any required MIGRATION_OPENING audit entry atomically; return snapshot/version. OpeningProvenance identifies the approved opening source, not a SaleId. | Unique ID and approved initial state; no admin balance-edit UI implied. Opening-entry requirements, including zero balance, are D4 decisions. |
| applyLoyalty(MemberId, LoyaltyChange, expectedVersion) | Persist new balance/state and append sale-linked EARNED/REDEEMED movements; return snapshot/version. | Verify pre-credit balance covers redemption; resulting balance = old − redeemed + earned. All rows/state in same session as sale/stock. |
| listMovements(MemberId, movement query) | Detached stable history ordered by committed movement sequence. | Include provenance and kind; opening/import entries must not masquerade as actual old sale earnings. |

Adapter checks nonnegative/in-range amounts and consistency, **not** earning rates, redemption value or first-purchase eligibility policy. Iman supplies those. Unique sale/member/movement-kind keys plus command deduplication prevent repeat sale movements. Zero earned amount can still record an explicit first-purchase transition; whether zero ledger entries are retained is D4, never silently skip the state transition.

### P4 — DiscountRepository

**Source basis:** DiscountManager.load()/addDiscount()/getMaxDiscount() and [Discount.isDiscountAvailable()](../../reengineered/src/main/java/sg/edu/nus/iss/se24_2ft/unit1/ca/discount/Discount.java).

| Required operation | Responsibility | Transaction / boundary |
|---|---|---|
| findByCode(DiscountCode), listAll() | Detached definitions including optional start/unbounded duration, percentage, audience and version. | No adapter dependency on current system clock or Member class. |
| insert(NewDiscount) | Store accepted definition and return snapshot/version. | Unique approved normalized code, configured percent/date constraints. Duplicate is explicit, not discarded boolean. |

Iman filters current eligibility and chooses highest percentage; repositories do not embed MEMBER_FIRST/MEMBER_SUBSEQ sentinel rules, stack discounts, silently map malformed dates to ALWAYS or choose ties. Sale persists accepted applied values so later definition changes do not rewrite old financial truth.

### P5 — TransactionRepository: sale aggregate, lines and history

**Source basis:** TransactionManager.load()/store()/addTransaction(), [Transaction](../../reengineered/src/main/java/sg/edu/nus/iss/se24_2ft/unit1/ca/transaction/Transaction.java) and [TransactionItem.toString()](../../reengineered/src/main/java/sg/edu/nus/iss/se24_2ft/unit1/ca/transaction/TransactionItem.java).

| Required operation | Responsibility | Transaction / boundary |
|---|---|---|
| reserveSaleId() | Return a unique target SaleId, preserving imported logical-ID mapping. | Concurrency-safe allocation; no MAX+1 in service. Gaps permitted. |
| insertAggregate(NewSale, List<NewSaleLine>) | Store immutable header/lines and return SaleSnapshot. | For normal sales require >= 1 line, unique line sequence/approved product aggregation, valid product/member/discount references and complete approved snapshots. No member/stock update hidden here. |
| findById(SaleId) | Optional aggregate snapshot containing **all lines**, customer/public identity, financial completeness/provenance and applied snapshots. | Reconstruct from stored IDs; never substitute current product price or reverse member/PUBLIC meaning. |
| listLineHistory(HistoryQuery) | Ordered detached line-level report rows plus explicit unknown markers. Include IDs, product name/description, customer, quantity, date/precision; financial values only where known. | Query declares inclusive bounds and order; no financial aggregation/export feature is implied. Read under coherent session. |
| importLegacyAggregate(LegacySaleDraft, SourceRecordKeys) | Migration-only insert retaining raw group/line provenance and unavailable financial values. | Separate from normal complete-sale insert; rerunnable by unique source keys, explicit conflict/reject reporting; no stock/points replay. |

HistoryQuery specifies an approved temporalPolicy/zone and deterministic order. LEGACY_ENCOUNTER uses preserved import/line ordinals plus new committed-sale order; TIME_ASC is an explicitly selected alternative, not an accidental default change. For date-only old rows, policy may use configured-zone start-of-day **for comparison only**, while stored occurrence time remains unknown. D2 must be approved before implementing that conversion. Querying incomplete history must not invent zero totals or silently exclude imported rows.

### P6 — StaffRepository: credentials for authentication only

**Source basis:** StoreKeeperManager.load()/login(); [LoginPanel](../../reengineered/src/main/java/sg/edu/nus/iss/se24_2ft/unit1/ca/gui/LoginPanel.java).

| Required operation | Responsibility | Transaction / boundary |
|---|---|---|
| findCredentialByLogin(StaffLogin) | Optional restricted CredentialRecord: stable staff ID/login, opaque verifier and metadata. | Only authentication/application component consumes it; absence is not DB unavailability. No password comparison/return in repository. |
| insertProvisionedCredential(ProvisionedCredential) | Internal provisioning/migration operation; return non-secret staff identity/version. | Unique approved login; no plaintext future storage or automatic reuse of sample accounts. Not a new staff-management screen. |

Iman owns credential-verifier format/algorithm and authentication/reset policy; Hamna stores opaque encoded values and enforces uniqueness. General catalog/report DTOs must exclude verifier fields. Existing staff identity is not captured on transactions, so no sale-cashier FK is invented.

### P7 — VendorRepository: preserved associations, not ordering

**Source basis:** VendorManager.load()/getVendorListByCategoryId() and [Vendor](../../reengineered/src/main/java/sg/edu/nus/iss/se24_2ft/unit1/ca/vendor/Vendor.java).

| Required operation | Responsibility | Transaction / boundary |
|---|---|---|
| findById(VendorId), listByCategory(CategoryId) | Detached vendor definitions and explicit category links, stable order. | Preserve category filenames' semantics even when bytes/names repeat. |
| insertImportedVendor(ImportedVendor, SourceRecordKey) | Migration-only approved vendor identity/name/description. | Do not merge conflicting same-name vendors silently; use approved identity mapping. |
| linkImportedCategory(VendorId, CategoryId, SourceRecordKey) | Migration-only association, unique vendor/category pair. | Missing references → conflict; same source/pair replay is a recognized no-op, not duplicate new business activity. |

No purchase-order, delivery, vendor selection or stock-arrival integration is included. Normal functionality only needs the preserved lookup/data; scope can grow only by explicit later decision.

## 5. Proposed relational requirements

These are target requirements, not existing DDL. A DB engine, ORM and schema migrations remain Hamna's later work after open decisions. Use logical IDs at boundaries; explicit surrogates/mapping may be chosen inside the adapter.

| Proposed entity | Keys / references | Constraints and missing-history treatment |
|---|---|---|
| Category | PK approved category ID | Unique canonical ID, required name per D3; preserve original ID token/provenance. |
| Product | PK ProductId; FK Category; unique barcode under approved representation | Nonnegative quantity/threshold/orderQuantity, exact decimal current price; positive/zero price policy D1/D3. Stock version/conditional update required. |
| Member | PK MemberId | Nonnegative points plus explicit firstPurchaseEligible; source -1 maps only through approved sentinel policy. Other invalid/missing states quarantine or DATA_INCOMPLETE. |
| Discount | PK normalized code | Decimal percentage, audience, optional start/unbounded duration. Percent range and duration/date boundary rules D2/D3. |
| Sale | PK SaleId; optional FK Member/Discount; unique command linkage for new commits | Original customer token and history completeness; new money/payment/redemption/credit snapshots; old unavailable fields nullable/explicit unknown, not zero. Keep date-only precision distinct from timestamp. |
| SaleLine | PK target line ID or (SaleId,lineSequence); FK Sale/Product | Positive normal-sale quantity and approved product aggregation. New price/name/description snapshots preserve receipt truth; old original price/names not recorded as historical snapshots must remain unknown, even though current names may be displayed as labeled lookup values. |
| LoyaltyMovement (new target audit) | PK movement ID; FK Member; optional Sale for import opening; unique sale/member/kind for normal movement | Signed points, kind, committed order, balance-after/provenance. Ledger and Member balance atomically reconcile; no invented historical earnings/redemptions. |
| Staff | PK chosen stable staff key; unique approved login | Opaque verifier/metadata, no raw password. No required sale reference. |
| Vendor | PK target VendorId; original-name mapping | Names need not be globally unique unless approved evidence supports merging. Preserve description conflicts. |
| VendorCategory | Composite unique VendorId/CategoryId; FKs to both | Preserve every accepted source association; no deletion based solely on duplicate file bytes. |
| CommandReceipt (new target infrastructure) | PK commandId, request fingerprint, committed receipt/reference | Written atomically with normal mutation; recovery/replay may not expose secrets. Not a new user-facing workflow. |

No general delete/rename API is included. Foreign keys should prevent orphan history; destructive cascade of sales/ledger must not be enabled as an accidental default. Future catalog changes must not change immutable accepted sale snapshots.

### Money, quantity, date and loyalty specifics

- **Money:** BigDecimal built from source text/accepted decimal input, not new BigDecimal(double). Decimal SQL type with approved precision/scale/currency/rounding (D1); reject overflow rather than truncate. Legacy dollar symbol does not establish currency. Imported current product prices can be mapped; missing old sale amounts cannot.
- **Stock:** preserve quantity <= threshold for understock and configured immediate replenishment. Atomic versioned signed deltas reject underflow. New repository contract does not silently retain legacy negative-quantity bugs or claim those corrections already approved; D3 gates constraints.
- **Sale calculations:** Iman owns highest eligible discount, subtotal/discount/payment/change, point earning/redemption and all date/rounding decisions. Adapter may validate persisted algebra against the approved policy, but must not recompute eligibility/amounts using today's products/discounts.
- **Loyalty:** for accepted new sale, pre-credit balance must cover redemption, final balance = prior − redeemed + earned. Persist both movement intent and resulting balance within the sale transaction. -1 first-purchase source state must not become a negative target balance or a fake -1 movement.
- **Date/time:** store original date and precision; only newly captured exact time is an Instant. Use a configured zone/policy for comparisons; never invent old time-of-day or infer timezone from machine default.
- **Snapshots:** normal new transactions require complete fields agreed for receipt/history. Legacy import mode permits known missing values with provenance, so new NOT NULL constraints must not force fabricated backfill.
- **Validation:** service validates meaning; DB ensures keys/ranges/algebra/versions on every write. Diagnostics must identify conflicting source records without exposing staff secrets.

## 6. Migration, rollback and reconciliation contract

Hamna owns migration/adapters/data quality. The contract describes later tools; none is created here.

1. Hash/snapshot all original source data and import from immutable files into an isolated target, with a unique import-run ID. Never create live data or modify files under legacy.
2. Preserve every raw row and SourceRecordKey; parse positions/formats from [data reconstruction](../reverse-engineering/data-reconstruction.md), not the flawed TransactionManager loader as an oracle.
3. Import categories before products and vendor associations; members before member-linked sales; definitions/staff through approved mappings; sale headers/lines only after referenced identities are mapped. Unknown applied discount IDs stay absent, not guessed. Seed sequence allocators above accepted imported IDs.
4. Map source PUBLIC to public/absent-member explicitly; preserve raw customer token and reject unresolved actual member references. Include all distinct transaction lines, not the flattened report list that omits some.
5. Record missing original financial values as unknown. For a known opening loyalty balance, a proposed MIGRATION_OPENING entry is a synthetic opening snapshot with source provenance and no invented SaleId; it is not historical earning activity. Do not replay imported sales against stock/points. Invalid -1/other sentinel mapping must be approved before import.
6. Preserve all category/vendor pairs. Report accepted, rejected and conflicting rows separately; never silently skip or overwrite.
7. Make re-import deterministic with unique SourceRecordKeys. Same source key/content may be recognized as imported; changed content/key collisions require a report/review, not destructive upsert.
8. Validate counts **and** keys, all sale lines, customer references, quantities, balances, vendor/category pairs and unknown-field flags. Current samples have 11 files/57 rows, four sale lines for three IDs, and five vendor files whose same four names encode 20 pairs. Counts alone do not prove semantic fidelity.
9. Use bounded import transactions/checkpoints with explicit run status. Do not expose a partial run to normal users. Publish/activate only a reconciled run; retain an auditable rollback path.
10. Before cutover, rehearse rollback on an isolated target. Roll back/delete only records proven owned by the import run and only before target business writes depend on them, or restore a verified target backup. Never “rollback” by editing the original files.
11. After accepting live target writes, reverting to old files would lose new sales. Shared must approve recovery/cutover procedure with target backup/reconciliation, not automatic fallback or dual-writing. No production cutover is authorized by this document.

The original files contain no historical loyalty movements, original sale price/payment/discount/points, exact old time-of-day, stable vendor ID, sale cashier ID, credential hash or currency declaration. Unknown is not equivalent to zero, PUBLIC, no-discount or a newly fabricated event.

## 7. Open decisions and acceptance gates

| ID | Decision before affected implementation | Proposed accountable preparer / review |
|---|---|---|
| D1 | Currency, decimal precision/scale/rounding and conversion of current double-facing APIs; preserve accepted financial examples before changing arithmetic. | Iman; Hamna checks storage capacity; Shared approves behavior. |
| D2 | Calendar dates versus exact timestamps, configured zone, inclusive bounds, ALWAYS/unbounded duration, invalid-date handling and HistoryQuery comparison/order policy. | Iman; Hamna supplies old data precision; Shared approves. |
| D3 | ID case/format, barcode representation, nonnegative/positive/zero input constraints, duplicate/import conflict rules and allocation policy. | Iman for operation rules; Hamna for mapping/constraints; distinct outputs reviewed Shared. |
| D4 | -1 sentinel mapping, first-purchase transition, earning/redemption policy and zero-movement audit treatment. | Iman; Hamna maps opening balances; Shared approves. |
| D5 | Historic repair, incomplete-field representation, line aggregation/order and vendor identity conflicts. | Hamna; Shared approves corrected equivalence. |
| D6 | Credential verifier/metadata format, reset/provisioning and synthetic fixture account policy. | Iman; Hamna stores approved representation; Shared approves. |
| D7 | Durable command receipt/fingerprint/version/isolation strategy, outcome-unknown recovery and migration/cutover rollback. | Hamna infrastructure; Iman service command/error contract; Shared acceptance. |
| D8 | Concrete Java package/DTO/service/repository constructor signatures and transition of current manager/table adapters. | Nawal initial wiring/adapters; Iman service APIs; Hamna implementations; Shared integration review. |

This proposal fixes no business defect by declaration. The families/transaction/error boundaries can guide design now; affected schemas/behavior must not be frozen while their decision remains unresolved. [Team integration guide](team-integration-guide.md) defines exact ownership handovers, dependency direction and compatibility checks.
