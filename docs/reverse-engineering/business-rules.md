# Business rules reconstructed from source

Baseline `87bee5fed6e44cf87fd91a05de42a6817ae81002`; static inspection, 2026-11-25. “Actual” means executable statements in source, not approved business requirements or a passed runtime test. All formulas below describe ordinary finite, nonnegative inputs unless explicitly stated; overflow, NaN/infinity and invalid inputs are not silently assumed safe.

Related: [workflow traces](workflow-reconstruction.md), [architecture](architecture-reconstruction.md), [data/ER proposal](data-reconstruction.md), [opportunities](refactoring-opportunities.md). Open decisions remain B-01 through B-10 in [reengineering priorities](../inventory/reengineering-priorities.md).

## Identity, registration and access rules

| Rule ID | Actual rule and boundary | Source / method evidence |
|---|---|---|
| BR-01 | Staff access requires exact map lookup and stored password equality; login success gates startup. StoreKeeper trims record fields, but loader key uses raw name token. No hashing/roles are implemented. | [StoreKeeperManager.load()/login()](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/storekeeper/StoreKeeperManager.java); [StoreKeeper constructor](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/storekeeper/StoreKeeper.java); [main()](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/StoreApplication.java). |
| BR-02 | New category requested ID is trimmed, nonblank, unique and length three. No alphabetic-only check. Lookup uppercases; insertion does not. | [Category constructor](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/category/Category.java); [addCategory()/getCategory()/load()](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/category/CategoryManager.java). |
| BR-03 | Member ID is trimmed, nonblank and unique; new loyalty balance is -1. Loader reads stored balance, default -1. No comprehensive name validation. | [Member constructor](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/customer/member/Member.java); [addMember()/load()](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/customer/member/MemberManager.java). |
| BR-04 | Product requires accepted category/non-null product/unique barcode; next ID = category ID + '/' + (maximum loaded/generated suffix + 1). Barcode is int. Load skips duplicate IDs/barcodes and missing categories; suffix parsing is unchecked and does not enforce an exact two-component ID grammar. | [load()/addProduct()](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/product/ProductManager.java); [fields/constructor](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/product/Product.java). |
| BR-05 | Public identity is PUBLIC; checkout uses null when no member chosen; serialization/reporting substitutes public identity when customer is null. | [getId()](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/customer/PublicCustomer.java); [getBottomPanel()](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/gui/checkout/CheckoutPanel.java); [toString()](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/transaction/TransactionItem.java). |

## Product quantities and basket rules

| Rule ID | Actual rule / formula | Source / method evidence |
|---|---|---|
| BR-06 | Understock iff quantity <= threshold, including equality. Restock only when understock: quantity' = quantity + orderQuantity; it is immediate and may remain understock afterward. | [isUnderstock()/restock()](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/product/Product.java); [generatePurchaseOrder()](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/product/ProductManager.java). |
| BR-07 | Product.deductQuantity(q) refuses only if available < q; otherwise available' = available − q. Negative q is not rejected there and can increase stock. Manager adds product existence checking, notifications and file store. | Product.deductQuantity(); ProductManager.deductQuantity(). |
| BR-08 | GUI requires quantity > 0. Before ID assignment, Transaction accepts valid identified products, merges repeated product quantities and rejects per-product aggregate > available unless isLoading=true. Domain method has no universal positive-quantity check. | [getItemPanel()](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/gui/checkout/CheckoutPanel.java); [addTransactionItem() overloads](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/transaction/Transaction.java). |
| BR-09 | Basket addition updates subtotal by incoming quantity × current product price. Stock is not reserved or deducted until completion. Repeated distinct TransactionItem objects for one product are merged into the existing item. | Transaction.addTransactionItem(); [addTransaction()](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/transaction/TransactionManager.java). |
| BR-10 | Non-null transaction ID locks most public setters and new line additions. This is an application convention, not full immutability: item list is exposed, referenced entities/dates are mutable, and setId() is package-private. | Transaction.addTransactionItem(), setCustomer(), setDate(), setDiscount(), setRedeemPoint(), setPayment(), getTransactionItemList(). |

## Discount eligibility, priority and dates

Evidence: [Discount constructor/isDiscountAvailable()/toString()](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/discount/Discount.java), [DiscountManager.load()/addDiscount()/getMaxDiscount()](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/discount/DiscountManager.java), [Util.parseDateOrDefault()/addDate()](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/util/Util.java).

| Rule ID | Actual rule |
|---|---|
| BR-11 | Code is trimmed/uppercased; duplicate insertion returns false. Manager does not comprehensively validate blank code, percentage, date or duration. Store orders by code. |
| BR-12 | If start == null, available immediately regardless of period. Otherwise future start is ineligible; period < 0 has no end; else eligible while computed end is not before current Date. |
| BR-13 | Util.addDate sets Calendar.HOUR to 23, minutes/seconds to 59, then adds period calendar dates; it does not use HOUR_OF_DAY or explicitly reset milliseconds. Do not simplify this to a proven “end of day” rule. |
| BR-14 | Availability is checked first. All-audience discounts are eligible for any customer. Member-only discounts require instanceof Member; MEMBER_FIRST requires balance == -1; MEMBER_SUBSEQ requires balance != -1; other member-only codes have no additional special-code restriction. |
| BR-15 | Choose max percentage among eligible definitions; no eligible discount → null/zero discount amount. No stacking or explicit secondary tie-priority rule. Negative/out-of-range percentages are not filtered as a valid-range invariant. |
| BR-16 | Invalid start parsing can default to null (always), invalid duration to -1 (unbounded), invalid percentage to zero. SimpleDateFormat is shared/lenient; date formatting is yyyy-MM-dd. These are implementation fallbacks, not confirmed intended requirements. |

The sample dated discounts are historical 2014 definitions. Tests add their own fixture definitions. Availability today must not be inferred from a historical test expectation or rewritten sample dates.

## Checkout money and payment formulas

Let S be accumulated subtotal, p the selected percentage (zero if none), R the integer redeemPoint count, P payment and T discounted total.

Evidence: Transaction.getSubtotal()/getDiscountAmount()/getSubtotalAfterDiscount()/getRedeemPointValue()/getBalance(); [ConfirmPaymentDialog.getPaymentPanel()/balanceChanged()](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/gui/checkout/ConfirmPaymentDialog.java); TransactionManager.addTransaction().

| Rule ID | Actual formula / condition |
|---|---|
| BR-17 | Discount amount D = S × p / 100; T = S − D. Monetary state is double, not decimal/exact-money arithmetic. |
| BR-18 | Redemption value V = R × TransactionManager.POINT_TO_DOLLAR = R × 5; balance B = T − P − V. B < 0 is displayed as change of abs(B). |
| BR-19 | GUI redemption maximum = min(max(member balance,0), int(T/5)); integer conversion truncates toward zero (floor for nonnegative T). It rejects entered R < 0 or above maximum. Transaction.setRedeemPoint() itself checks only not locked and R >= 0. |
| BR-20 | Completion computes integer V; if V < 0 sets dollar value to zero; if V > T replaces R with int(T/5) and recomputes V. Payment+V < T throws “Insufficient Amount”. Finite/nonnegative/range invariants are incomplete; overflow protection is not established by the V < 0 fallback. |
| BR-21 | Payment GUI rejects parse failure, negative amounts and textual decimal fractions longer than two places; transaction setter rejects negative/locked values. No explicit finite-value guard. Display uses Util.formatDollar(): dollar prefix and two decimal places via String.format, not a change to stored precision. |
| BR-22 | Confirm initially enabled iff B <= 0; balanceChanged() applies same test. Conditional calls to balanceChanged() can leave stale UI state (see defects). Manager coverage validation remains a separate check after confirmation. |

**Illustrative arithmetic only, not an executed test:** S=100, p=20, R=2, P=75 gives D=20, T=80, V=10, B=-5 and displayed change 5. Source credit formula gives 10 points, not 7 or 8; requirements still need approval.

## Loyalty balance updates

Evidence: MemberManager.debitLoyaltyPoint()/creditLoyaltyPoint(); [Member.removeLoyaltyPoint()/addLoyaltyPoint()](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/customer/member/Member.java); TransactionManager.addTransaction().

| Rule ID | Actual update |
|---|---|
| BR-23 | Only instanceof Member triggers point debit/credit. Unknown member ID throws. Debit R=0 returns before validation of available points or file write; otherwise manager rejects current balance < R. |
| BR-24 | For a normal nonnegative balance L and valid R, post-debit balance is L−R; member entity method alone caps over-removal, but manager normally rejects it first. |
| BR-25 | Credited points C = int(S / DOLLAR_TO_POINT) = int(S/10), using pre-discount subtotal regardless of cash paid/redemption. For finite nonnegative S this is floor. |
| BR-26 | Member.addLoyaltyPoint(C) replaces a negative existing balance with C, otherwise adds C. A first-purchase -1 member with zero redemption becomes C, including C=0. Normal accepted final balance is L−R+C. Credit writes the member file; nonzero debit writes it earlier separately. |

The source still contains commented Member formulas redeeming multiples of 100 points for 5 dollars; active TransactionManager logic values **one point at 5 dollars**. This contradiction is documentation/requirements evidence, not authorization to choose a different conversion.

## Transaction IDs, rows and reports

| Rule ID | Actual rule | Source evidence |
|---|---|---|
| BR-27 | maxId starts at zero, rises to maximum first-row transaction ID while loading, then new accepted API calls use ++maxId. Raw invalid ID/quantity parse defaults to zero. No stored durable sequence or concurrency control. | TransactionManager constructor/load()/addTransaction(). |
| BR-28 | Lines are grouped by transaction ID; first row supplies customer/date; later rows assume same details. Items are added with loading=true then IDs assigned after load. Existing stock/points are not replayed. | TransactionManager.load(); Transaction.addTransactionItem(item,true). |
| BR-29 | Each persisted line is transaction ID, product ID, customer ID/PUBLIC, quantity, yyyy-MM-dd date. Transactions write in list encounter order; each transaction's lines sort lexically by product ID. No actual chronological sort is performed despite store() comment. | TransactionManager.store(); Transaction.toStringStream(); [TransactionItem.toString()](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/transaction/TransactionItem.java). |
| BR-30 | Empty Transaction can pass manager coverage check with zero amounts; it consumes in-memory ID but serializes no row. UI ordinarily disables payment for empty basket; API restriction is absent. | TransactionManager.addTransaction(); Transaction constructor/toStringStream(); CheckoutPanel getItemPanel()/newTransactionRequested(). |
| BR-31 | Historical subtotal is rebuilt using current referenced product prices. Original discount/payment/redemption/credit fields are not persisted; no financial total/receipt can independently be recovered from the five stored fields. | Transaction.addTransactionItem()/fields; TransactionItem.toString(). |
| BR-32 | Reports show category/product/member state and transaction-line fields, not revenue/profit aggregation. Transaction filter implements start <= row Date <= end via combined RowFilters. Bounds initialize from first/last report rows, not min/max; end is not normalized to full-day end. | [ReportPanel.createTransactionReportPanel()/setTransactionTableModel()](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/gui/ReportPanel.java); manager getTableModel(). |
| BR-33 | Product report “Reorder Quantity” displays threshold; “Order Quantity” displays configured orderQuantity. Basket/receipt table shows quantity, ID, description and current unit price, not a stored line-total snapshot. | ProductManager.getTableModel()/getUnderstockTableModel(); Transaction.getTableModel(). |

## Defective, contradictory or uncertain behavior — not endorsed rules

| Concern | Source-confirmed mechanism | What remains to verify / decide |
|---|---|---|
| History misidentification / missing report lines | TransactionManager.load() uses reversed PUBLIC/member branch; later distinct product items miss flattened list append. | Approve corrected customer/line expectations; do not copy the bad report as acceptance truth. |
| Sale atomicity / stock shortfall | Member writes, per-product writes, then history write; stores print errors; fallback may deduct only remaining stock. | Completion/rollback/retry and shortage policy; no fault run performed. |
| Stock depends on GUI model | ProductManager.deductQuantity() has unguarded new-understock table notification; main() initializes model normally. | Standalone stock operations and table-independent behavior. |
| Payment Confirm stale state | Payment listener compares current balance to construction-time balance; redeem listener parses balanceLabel text with zero fallback and can skip exact-zero update. | Reverting payment and exact-zero redemption UI cases; these are source-derived concerns, not observed GUI failures. |
| Cancel/customer-change carries state | Dialog sets payment/redemption on shared Transaction before isConfirmed; failed confirmation returns without restoring fields. setCustomer() does not clear redemption/payment. Completion counts V even for public/null customer while skipping member debit. | Characterize cancel/reopen and member→public changes; agree invalid-state rejection. No successful exploit or bypass is claimed. |
| Mixed units in clamp | Transaction.setDiscount() compares point count directly with dollar total before clamping count; manager later caps dollar value. | Agree a single policy and characterize discount/redemption edits. |
| First-purchase and time rules | -1 sentinel, active versus commented point formula, Calendar.HOUR=23, fallback ALWAYS behavior. | Shared-approved examples for B-02/B-03; do not “correct” constants without decision. |
| Validation / mutability | GUI checks exceed domain checks; exposed list/date/entity references and non-finite inputs can bypass assumptions. | Define input/invariant contracts; aliases and formatter concurrency are not runtime-proven defects. |

Relevant methods are linked above and in [workflow WF-07/WF-08](workflow-reconstruction.md). Existing tests do not establish these boundaries: TransactionManagerTest submits an empty transaction; ProductManagerTest's purchase case passes null; DiscountTest uses a fixed 2016 date. See [test trace table](workflow-reconstruction.md) for exact test paths.

No rule is changed by this document. Intended versus observed values, date semantics, historical unknowns, credentials, output scope and add/list-only management remain open team decisions.
