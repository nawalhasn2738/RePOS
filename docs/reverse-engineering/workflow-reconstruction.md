# Workflow reconstruction

Baseline commit `87bee5fed6e44cf87fd91a05de42a6817ae81002`; static source trace, 2026-11-25. **No workflow or test was executed.** Arrows below are call order within inspected handlers/managers, not successful runtime traces. Callback targets are bound in [StoreApplication.main()](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/StoreApplication.java). Runtime paths such as data/Products.dat describe required files, not files created in the preserved checkout.

Related: [architecture](architecture-reconstruction.md), [rules](business-rules.md), [data and write order](data-reconstruction.md), [opportunities](refactoring-opportunities.md).

## WF-01: staff login

**Entry/evidence:** [LoginPanel.getButtonPanel() LOGIN listener](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/gui/LoginPanel.java); [StoreKeeperManager constructor/load()/login()](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/storekeeper/StoreKeeperManager.java); [StoreKeeper constructor/getPassword()](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/storekeeper/StoreKeeper.java); StoreApplication.main().

**Order:** main → new StoreKeeperManager → load → Files.lines → Util.splitCsv → new StoreKeeper/map insertion → new anonymous LoginPanel → setVisible(true) (modal). LOGIN → overridden login(name,password) → manager.login() → map lookup/password.equals(). Success sets success=true and disposes; startup checks isSuccess() before constructing other managers.

**Reads/modifies/files:** reads data/Storekeepers.dat into name-keyed map; duplicate raw names overwrite entries. Model constructor trims name/password but manager map key uses the original name token. Dialog changes success state; this path has no data write and does not attach logged-in staff to transactions.

**Rules/errors:** matching staff name/password is required; no hash, role or authorization model. Incorrect credentials show a message; closing without success leads main to System.exit(0). load() prints IOException; malformed short rows can throw unchecked exceptions. Exact whitespace/case acceptance should be characterized, not inferred as normalized login.

## WF-02: category and product management

### Category addition

**Entry/evidence:** [CategoryPanel constructor Add listener](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/gui/CategoryPanel.java); [Category constructor/setId()/toString()](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/category/Category.java); [CategoryManager.addCategory()/store()](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/category/CategoryManager.java).

**Order:** Add → new Category(requested ID,name) (trim) → CategoryPanelListener.addCategoryRequested → bound manager.addCategory → require nonblank/unique/length-three ID → setId → map/list insertion → optional fireTableRowsInserted → store → sorted Category.toString() lines → Files.write(data/Category.dat).

**Data/rules/errors:** input fields, category registry and shared table change; store rewrites all categories sorted by ID. Three characters are required, not explicitly letters. Lookup uppercases while insertion retains case. Panel catches IllegalArgumentException, shows error and clears input even on rejection. I/O failure is only printed; mutation is not rolled back. Load skips duplicate/invalid-length categories; full row validation is absent.

### Product addition

**Entry/evidence:** [ProductPanel.newProductPanel() Add listener](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/gui/ProductPanel.java); [Product constructor](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/product/Product.java); [ProductManager.addProduct()/private addProduct()/store()](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/product/ProductManager.java); CategoryManager.getCategory().

**Order:** Add → Util numeric parsers (fallback zero) → new Product → ProductPanelListener.addProductRequested(cid,p) → startup lambda resolves category → manager.addProduct(category,p) → validate category/product/barcode/category ID → maxSubIdMap+1 → private addProduct assigns ID/category, inserts list/map/barcode map and understock list → table events → store sorted by Product ID.

**Data/rules/errors:** reads category registry and barcode/sequence maps; writes data/Products.dat. ID is categoryId/sequence, barcode unique. No general nonnegative price/quantity/order validation. ProductPanel collects IllegalArgumentException messages; resets fields only with no errors. Unknown category/duplicate barcode is rejected, while malformed numbers can silently become zero. File errors are printed. Loader parses existing ID suffix and skips duplicate IDs/barcodes or missing categories; not all malformed ID failures are caught.

## WF-03: member management

**Entry/evidence:** [MemberPanel constructor Add listener](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/gui/MemberPanel.java); [Member constructor/setId()/toString()](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/customer/member/Member.java); [MemberManager.addMember()/load()/store()](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/customer/member/MemberManager.java).

**Order:** Add → new Member(id,name) (trim; balance -1) → MemberPanelListener → bound addMember → nonblank/unique requested ID → setId → list/map insertion → table notification → store sorted by member ID.

**Data/files:** data/Members.dat is name,ID,loyalty; loader sets the stored balance through setLoyaltyPoint(), defaulting a malformed integer to -1. Table shows Name, Member ID, Loyalty Point. Returned member objects are shared with checkout. Debit/credit execution is WF-07, not an independent user-facing balance-edit screen.

**Errors/limitations:** panel catches IllegalArgumentException then clears fields even after rejection; no comprehensive name validation. load/store IOException is printed. -1 is interpreted as first-purchase state, not spendable credit. add/list operations do not establish general update/delete.

## WF-04: discount creation and selection

**Entry/evidence:** [DiscountPanel.newDiscountPanel() Add listener](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/gui/DiscountPanel.java); [Discount constructor/isDiscountAvailable()/toString()](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/discount/Discount.java); [DiscountManager.addDiscount()/getMaxDiscount()/load()/store()](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/discount/DiscountManager.java).

**Creation order:** collect code/description/date/period/percentage/audience → parse date (default null), period (default -1), percentage (default zero) → new Discount (uppercase/trim code) → void DiscountPanelListener → bound addDiscount → duplicate returns false, otherwise assign code/list/map/event → rewrite data/Discounts.dat sorted by code.

**Selection order:** CheckoutPanel payment action → getDiscount(customer) anonymous override → manager.getMaxDiscount → filter availability → accept all-audience or apply Member-only/special-code rules → max percentage → Discount or null → Transaction.setDiscount().

**Data/rules:** reads definitions, customer concrete type, loyalty sentinel and system time. Does not deduct inventory or write on selection. MEMBER_FIRST is eligible only for -1 members and MEMBER_SUBSEQ only for other member balances when those definitions are member-only. Other member-only codes require Member; all-audience definitions bypass special member checks. No stacking/explicit tie-priority rule is encoded.

**Errors/limitations:** duplicate boolean is discarded by startup's void callback, so no reliable duplicate feedback. Invalid date may become always; invalid period may become unbounded. Fractional-period GUI handling can call Float.parseFloat() without general exception handling. Percent bounds are not enforced by the manager.

## WF-05: inventory updates and replenishment

**Entry/evidence:** [InventoryPanel constructor Generate Purchase Order listener](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/gui/InventoryPanel.java); [ProductManager.getUnderstockTableModel()/generatePurchaseOrder()/deductQuantity()](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/product/ProductManager.java); [Product.isUnderstock()/restock()/deductQuantity()](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/product/Product.java).

**Replenishment order:** selected JTable rows → reject empty selection with message → confirmation → InventoryPanelListener.purchaseOrderRequested(indices) → bound manager.generatePurchaseOrder → no-op for null/empty list → selected understock products restock() → rebuild understock list from all products → fireTableDataChanged on models → store data/Products.dat.

**Stock-deduction order (sale path):** TransactionManager.addTransaction → manager.deductQuantity(id,q) → resolve Product → Product.deductQuantity(q) → update product-table quantity event/understock membership and event → store product file. Repeats per sale item.

**Data/rules:** understock means quantity <= threshold. Restock adds configured orderQuantity once per selected index only if still understock. No reservation occurs when merely adding a basket line. Immediate arrival is assumed; no order record, vendor request or shipment path exists. VendorManager constructor loads files but no operation is wired here.

**Errors/limitations:** manager rejects absent product/insufficient stock via IllegalArgumentException. New understock insertion can dereference a null understock model outside normal startup initialization. Negative quantity/order values are not universally rejected at the domain API. Invalid row indices and direct file failures lack a structured operation error contract.

## WF-06: basket and checkout

**Entry/evidence:** [CheckoutPanel constructor/getItemPanel()/getBottomPanel()/newTransactionRequested()](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/gui/checkout/CheckoutPanel.java); [Transaction.addTransactionItem()/setCustomer()](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/transaction/Transaction.java); [TransactionItem constructor/setParent()/addQuantity()](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/transaction/TransactionItem.java).

**Order:** constructor/newTransactionRequested → new Transaction and basket TableModel. Add Item → uppercase entered product ID → require positive GUI quantity → abstract getProduct resolved by ProductManager → new TransactionItem → Transaction.addTransactionItem → validate product/ID and available quantity; merge repeated product quantity or attach parent/new list/map entry → table event → add incoming quantity × current product price to subtotal → refresh labels/enable payment.

Membership button → input member ID → abstract getMember resolved by MemberManager → invalid nonempty ID message, otherwise Transaction.setCustomer(member or null). Empty means public. Reset → new transaction/table, clear member/subtotal, disable payment.

**Data/files:** basket holds live Product/Member references plus item list/map, subtotal and payment/redemption state. No stock/member/history file is written merely by Add Item, membership selection or Reset.

**Errors/limitations:** GUI catches item IllegalArgumentException and displays errors; Transaction does not itself require quantity > 0, so GUI checks are not a complete domain invariant. Most public transaction setters refuse changes after ID assignment; exposed mutable item list and mutable references weaken that lock. No line-removal/refund flow is identified.

## WF-07: payment and loyalty points

**Entry/evidence:** CheckoutPanel.getBottomPanel() Proceed to Payment listener; [ConfirmPaymentDialog constructor/getPaymentPanel()/balanceChanged()/isConfirmed()](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/gui/checkout/ConfirmPaymentDialog.java); [Transaction setters/total methods](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/transaction/Transaction.java); [TransactionManager.addTransaction()](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/transaction/TransactionManager.java); [MemberManager.debitLoyaltyPoint()/creditLoyaltyPoint()](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/customer/member/MemberManager.java); Member.removeLoyaltyPoint()/addLoyaltyPoint().

**Order:**

1. Select discount from current customer, call Transaction.setDiscount(), construct modal payment dialog over the **same** transaction.
2. Payment button parses amount, rejects negative or more than two textual decimal places, sets transaction.payment. Redeem button computes min of nonnegative member balance and integer-truncated discounted-total/5, validates entered integer, sets transaction.redeemPoint.
3. UI updates balance/Confirm state through balanceChanged() conditionally. Confirm only sets isConfirmed=true and disposes; other close/cancel leaves false.
4. Checkout returns immediately if not confirmed. Otherwise setDate(new Date()) and invoke bound TransactionManager.addTransaction().
5. Manager caps redemption's dollar value, rejects insufficient payment+redemption; if Member, debit requested points (zero is an early return), then credit int(subtotal/10) and record credited amount.
6. Manager deducts stock and records/stores sale as WF-08.
7. Checkout shows completion/receipt option, purchased-understock alert and resets basket.

**Data/files:** dialog edits shared transaction before confirmation. Member debit and credit can each rewrite data/Members.dat; stock deduction rewrites data/Products.dat; final history write is data/Transactions.dat. Public/null customer skips member updates, but manager still includes transaction.redeemPoint in coverage; it does not independently require redemption to belong to a Member.

**Errors/limitations:** parsing/range messages cover some bad input, not finite-value validation. MemberManager throws on unknown member/insufficient requested points. Checkout's completion callback has no general catch around addTransaction(); IOException inside stores is swallowed/printed. No rollback of dialog edits occurs when closing without confirmation.

**Source-derived UI/state concerns, not reproduced:** payment handler compares the new balance against the balance captured when constructing the dialog; returning to that original balance can skip an update and leave stale Confirm state. Redeem handler parses the nonnumeric balanceLabel text with default zero; reaching exact zero may skip balanceChanged(). Cancelling payment then changing customer retains payment/redemption state in Transaction. These deserve dedicated characterization; do not assert every GUI path fails or that a particular exploit has been demonstrated.

## WF-08: transaction persistence and history

**Entry/evidence:** runtime entry is WF-07's completion callback; restart entry is TransactionManager constructor → load(). [TransactionManager.load()/addTransaction()/getTableModel()/store()](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/transaction/TransactionManager.java); [Transaction.addTransactionItem(item,true)/setId()/toStringStream()](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/transaction/Transaction.java); [TransactionItem.toString()](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/transaction/TransactionItem.java).

**Completion order:** coverage check → member debit/write then credit/write (when member; zero debit skips write) → per-item stock deduction/write → setId(++maxId) → transaction list/map insertion → append item references to flattened reporting list → event → store all transactions, flattening each transaction's product-ID-sorted lines. Each line stores transaction ID, product ID, customer ID/PUBLIC, quantity, yyyy-MM-dd date.

**Reload order:** read data/Transactions.dat line → split/fallback parse ID and quantity → product lookup/new item → if transaction exists, add item with isLoading=true then return. For first row, choose customer/date, create transaction, add item, update maxId/list/map and copy its current item references to flattened list. After all rows, setId() on each transaction so later additions are locked.

**Actual reconstruction defects:** PUBLIC triggers member lookup and non-PUBLIC IDs become PublicCustomer (reversed). Later **distinct-product** lines enter the transaction's own list but not the manager's flattened report list; repeated same-product lines can update an already shared item instead. Only the first row's customer/date is used per transaction; conflicts are assumed away.

**Data/errors:** load does not re-deduct stock/points; stock-limit checks are bypassed with isLoading=true. Unknown product errors are caught/printed when adding; a group can still be registered with no valid lines. Malformed field counts/parses are not generally caught. Stored unit price/discount/payment/point history is absent; loaded subtotal uses current product prices. Empty transactions can be accepted through the manager API but emit no rows; GUI ordinarily prevents empty payment. IDs are in-memory/load-derived, not a durable sequence or concurrency-safe allocator.

## WF-09: reports and date filtering

**Entry/evidence:** [ReportPanel constructor/createTransactionReportPanel()/setTransactionTableModel()](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/gui/ReportPanel.java); manager getTableModel() methods; TransactionManager.getTableModel(); Util.DATE_FORMAT/formatDateOrDefault().

**Order:** startup supplies cached category/product/member/transaction models → ReportPanel.set*TableModel() → transaction sorter installed → renderer formats last-column Dates → initial start/end fields use first/last existing report-row dates when any. Get Report → read Date field values → if start.after(end), show message; otherwise combine (AFTER or EQUAL start) AND (BEFORE or EQUAL end) RowFilters → sorter.setRowFilter().

**Data/calculations/files:** this is an in-memory table/query filter, not a file write or SQL query. Reports show current categories, products, member balances and transaction **lines**: ID, product ID/name/description, member ID, quantity, date. No revenue/profit/sales-total aggregation is calculated here. Product report's “Reorder Quantity” column returns Product.threshold; its next “Order Quantity” column returns orderQuantity.

**Limitations:** first/last rows are assumed useful date bounds, not calculated min/max; history loader omission affects these rows. Comparisons are inclusive Date timestamps without explicit full-day end normalization. Persisted dates lose time-of-day. Invalid/null dates and formatter/parser behavior need characterization; report rendering does not repair wrong member IDs or history.

## Existing test evidence and gaps

Every test source and helper was inspected, not executed. These static mappings show examples, not successful coverage of the full workflow.

| Workflow / concern | Exact existing tests | What they establish statically / gap |
|---|---|---|
| WF-01 | [StoreKeeperTest](../../legacy/test/sg/edu/nus/iss/se24_2ft/unit1/ca/StoreKeeperTest.java); [StoreKeeperManagerTest](../../legacy/test/sg/edu/nus/iss/se24_2ft/unit1/ca/StoreKeeperManagerTest.java) | Staff/getter checks; reflective login state insertion; testInitData() empty; no GUI path. |
| WF-02 categories | [CategoryTest](../../legacy/test/sg/edu/nus/iss/se24_2ft/unit1/ca/CategoryTest.java); [CategoryManagerTest](../../legacy/test/sg/edu/nus/iss/se24_2ft/unit1/ca/CategoryManagerTest.java) | Fields/add/lookup/list; no case normalization or file-error checks. |
| WF-02 products / WF-05 | [ProductTest](../../legacy/test/sg/edu/nus/iss/se24_2ft/unit1/ca/ProductTest.java); [ProductManagerTest](../../legacy/test/sg/edu/nus/iss/se24_2ft/unit1/ca/ProductManagerTest.java) | Getters, IDs/add/lookup; purchase-order test passes null, not a real replenishment; no stock/model-boundary suite. |
| WF-03 | [MemberTest](../../legacy/test/sg/edu/nus/iss/se24_2ft/unit1/ca/MemberTest.java); [MemberManagerTest](../../legacy/test/sg/edu/nus/iss/se24_2ft/unit1/ca/MemberManagerTest.java) | Fields/register/list; no meaningful debit/credit/reload assertions. |
| WF-04 | [DiscountTest](../../legacy/test/sg/edu/nus/iss/se24_2ft/unit1/ca/DiscountTest.java); [DiscountManagerTest](../../legacy/test/sg/edu/nus/iss/se24_2ft/unit1/ca/DiscountManagerTest.java) | Availability/serialization/add/duplicate/member-public selection; fixed-date/system-clock assumptions. |
| WF-06/WF-07/WF-08 | [TransactionItemTest](../../legacy/test/sg/edu/nus/iss/se24_2ft/unit1/ca/TransactionItemTest.java); [TransactionTest](../../legacy/test/sg/edu/nus/iss/se24_2ft/unit1/ca/TransactionTest.java); [TransactionManagerTest](../../legacy/test/sg/edu/nus/iss/se24_2ft/unit1/ca/TransactionManagerTest.java) | Item getters and additions; manager test submits empty Transaction, not a paid member sale; no dialog-state or fault/reload fidelity checks. |
| Vendor associations | [VendorTest](../../legacy/test/sg/edu/nus/iss/se24_2ft/unit1/ca/VendorTest.java); [VendorManagerTest](../../legacy/test/sg/edu/nus/iss/se24_2ft/unit1/ca/VendorManagerTest.java) | Model/category associations and category-list presence; not vendor ordering. |
| Fixtures | [TestUtil](../../legacy/test/util/TestUtil.java) | Shared fixture strings/relative paths; setup writes/delete files; unsafe in preserved baseline. |

There are 15 test classes, 45 @Test annotations and one helper. Reports, dialogs, receipt output and Util have no dedicated test classes found. The suggested verification cases remain future work under [priorities](../inventory/reengineering-priorities.md), not tests added in this phase.
