# Legacy functional documentation

This guide records **source-observed original behavior**, not runtime certification or an approved redesign. The original remains untouched. Read [system overview](system-overview.md) for navigation, [setup and execution](setup-and-execution.md) for prerequisites and [documentation gaps](documentation-gaps.md) for unresolved requirements.

The seven tabs are wired by [StoreApplication.main()](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/StoreApplication.java). “Management” means the implemented add/list operations and stock/loyalty changes; general edit/delete, refunds, payment gateways and external ordering are not established.

## Staff login

**Evidence:** [LoginPanel.getButtonPanel()/isSuccess()](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/gui/LoginPanel.java), [StoreKeeperManager.load()/login()](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/storekeeper/StoreKeeperManager.java) and StoreApplication.main().

A modal dialog collects staff name/password. The manager loads staff records and compares password strings. Successful login closes the dialog and opens the feature tabs; failure shows feedback, while closing without success causes startup to exit.

**Limitation:** plaintext credential storage/comparison is an implementation to replace, not a security requirement. No staff administration or permission-role workflow is evidenced.

## Categories

**Evidence:** [CategoryPanel constructor](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/gui/CategoryPanel.java), [CategoryManager.addCategory()/getCategory()/store()](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/category/CategoryManager.java).

Add collects category ID/name. The manager rejects blank, duplicate or non-three-character IDs, assigns the accepted ID, updates its list/map and table, then rewrites category data. The panel also provides list and Reset controls.

**Decision needed:** the code checks length, not alphabetic characters. Insertion retains case but lookup uppercases; normalization and name validation need agreed behavior. Do not silently turn this inconsistency into a preserved rule.

## Members

**Evidence:** [MemberPanel constructor](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/gui/MemberPanel.java), [MemberManager.addMember()/debitLoyaltyPoint()/creditLoyaltyPoint()](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/customer/member/MemberManager.java), [Member constructor and point methods](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/customer/member/Member.java).

Add accepts name/member ID, requires a nonblank unique ID and displays member name, ID and loyalty balance. New members start with -1 points; discount selection interprets that as first purchase. Point debit/credit operations update table values and rewrite member data.

**Decision needed:** -1 is a sentinel, not a normal spendable balance. First-purchase transition and valid identity/name rules require explicit agreement before changing the representation.

## Products

**Evidence:** [ProductPanel.newProductPanel()](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/gui/ProductPanel.java), [ProductManager.addProduct()/load()](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/product/ProductManager.java), [Product fields](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/product/Product.java).

| Input/state | Source behavior |
|---|---|
| Category | Must resolve to an existing accepted category. |
| Product ID | Generated as categoryId/next sequence within the manager. |
| Name/description | Stored with the product. |
| Quantity/price | Represent current stock and unit price. |
| Barcode | Integer-valued and checked for uniqueness; checkout uses product ID. |
| Threshold/order quantity | Define understock/replenishment behavior. |

Creation updates product collections/tables and rewrites the product file. The panel lists current records and offers Reset.

**Limitation:** malformed numeric input can default to zero and comprehensive range validation is absent. No scanner integration is established by storing a barcode.

## Discounts

**Evidence:** [DiscountPanel.newDiscountPanel()](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/gui/DiscountPanel.java), [DiscountManager.addDiscount()/getMaxDiscount()](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/discount/DiscountManager.java), [Discount.isDiscountAvailable()/toString()](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/discount/Discount.java).

Definitions contain code, description, start date, duration, percentage and member-only/all audience. Codes are normalized to uppercase by Discount. Duplicate code insertion returns false; the current void callback binding in startup does not convey that result to the panel.

At payment, the manager chooses the **highest-percentage eligible discount**, not a stack of discounts. Availability considers current time and start/period; null start and negative period represent always/unbounded cases. Member-only definitions require a Member; special MEMBER_FIRST and MEMBER_SUBSEQ codes depend on the -1 loyalty sentinel.

**Decisions needed:** percentage limits, invalid-date behavior, date boundaries, duplicate feedback and first/subsequent rules. Source behavior is not proof that the configured rules match original requirements.

## Inventory and replenishment

**Evidence:** [InventoryPanel purchase-order action](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/gui/InventoryPanel.java), [Product.isUnderstock()/restock()](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/product/Product.java), [ProductManager.generatePurchaseOrder()/deductQuantity()](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/product/ProductManager.java).

A product is understocked when quantity <= threshold. The Inventory tab lists those products, requires selected rows and asks for confirmation. The manager immediately adds each selected product's configured order quantity, rebuilds the understock list, notifies tables and stores products.

“Generate Purchase Order” is the button label; the operation does not create an order document or communicate with a vendor. [VendorManager.load()](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/vendor/VendorManager.java) loads category-associated vendors at startup, but the created manager is not connected to this operation. Vendor selection is a TODO.

**Limitation:** deductQuantity() can notify an uninitialized understock table model in standalone use. Normal startup initializes the model before checkout; this is not evidence that every sale crashes.

## Basket and customer selection

**Evidence:** [CheckoutPanel.getItemPanel()/getBottomPanel()/newTransactionRequested()](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/gui/checkout/CheckoutPanel.java), [Transaction.addTransactionItem()](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/transaction/Transaction.java).

1. Enter product ID and a positive quantity; GUI input uppercases the ID.
2. Resolve the product and add a TransactionItem.
3. Repeated products merge quantities; transaction checks reject quantities above available stock.
4. Optionally select a member ID. Invalid nonempty IDs are rejected; an empty selection represents a public customer via null.
5. Reset replaces the in-memory basket and disables payment until an item is added.

A public identity is also represented by [PublicCustomer.getId()](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/customer/PublicCustomer.java) returning PUBLIC when serialization/manager code needs it. Cancellation/reopening and invalid-input boundaries still need runtime characterization; no refund or item-removal workflow is inferred.

## Payment and loyalty

**Evidence:** [ConfirmPaymentDialog.getPaymentPanel()/balanceChanged()](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/gui/checkout/ConfirmPaymentDialog.java), [Transaction total/balance methods](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/transaction/Transaction.java), [TransactionManager constants/addTransaction()](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/transaction/TransactionManager.java).

The payment dialog displays subtotal, discount, discounted total, redemption, amount paid and balance/change. It records payment and optional member-point redemption. Confirmation is enabled when payment/redemption covers the discounted total.

| Implemented calculation | Source rule |
|---|---|
| Discount amount | subtotal × percentage / 100. |
| Redemption value | redeemed points × 5 dollars. |
| Balance | discounted subtotal − payment − redemption value. Negative balance is displayed as change. |
| Earned points | Integer floor of pre-discount subtotal / 10 for members. |
| Redemption limit | Member balance and discounted total constrain points; completion also caps redemption against total. |

These are **actual implementation values**, not verified intended business rules. Money uses double; rounding, non-finite input, first-purchase transition and date/eligibility rules require approved examples. Commented-out older point formulas are not active functionality.

## Sale completion, transactions and persistence

**Evidence:** CheckoutPanel.getBottomPanel(), [TransactionManager.addTransaction()/load()/store()](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/transaction/TransactionManager.java), [TransactionItem.toString()](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/transaction/TransactionItem.java).

After payment confirmation, checkout sets the date and invokes the transaction callback. The manager checks coverage, changes member points, deducts stock, assigns a new ID, updates history/report collections and writes transactions.

The live data is comma-delimited text. Category/product/member/discount/transaction managers rewrite their files; [Util.splitCsv()](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/util/Util.java) provides simple splitting without quoted-field support.

| Confirmed limitation | Consequence / interpretation |
|---|---|
| Member, product and transaction writes are separate; I/O exceptions are printed. | Completion is not an evidenced atomic durable transaction. Failure behavior needs a defined correction contract. |
| Stock-error fallback attempts to deduct remaining stock. | Recorded quantity may diverge from deducted quantity; do not preserve silently. |
| load() reverses PUBLIC/member resolution and omits later distinct items from its flattened report list. | Historical customer associations/report rows can be wrong after reload. |
| Persisted lines contain transaction/product/customer IDs, quantity and date only. | Original prices, discounts, payments and point movements cannot be reconstructed independently. Loaded subtotal uses current referenced product prices. |

The [sample transaction file](../../legacy/data-sample/Transactions.dat) contains four lines for three IDs, including a two-product sale. Future relational migration is planned; no database implementation or migration has occurred.

## Receipts, reports and post-sale feedback

**Evidence:** [ReceiptDialog.getReceiptPanel()/getReceiptText()/setVisible()](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/gui/checkout/ReceiptDialog.java), [Printer.print()](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/printer/Printer.java), [ReportPanel constructor/createTransactionReportPanel()/set*TableModel()](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/gui/ReportPanel.java).

After completion, checkout offers receipt-and-return or return. The dialog shows transaction/items, subtotal/discount, cash/change and applicable member points. Making it visible prints formatted text through the inherited static console print method. Purchased understock products trigger an alert before the basket resets.

Reports provide category, product, member and transaction-line tables. Transaction start/end filters combine inclusive comparisons; the meaning of full calendar-day versus exact timestamp bounds remains to be agreed. Tables use shared manager models. There is no report-export or financial-aggregation implementation identified in ReportPanel.

No physical printer integration is evidenced. AdhesiveLabelPrinter is a disconnected stub, not an implemented label workflow.

## Acceptance and uncertainty

Known defects and unclear rules are tracked in [documentation gaps](documentation-gaps.md) and the Phase 1 [risk register/preservation decisions](../inventory/reengineering-priorities.md). Future work must preserve accepted valid workflows, distinguish observed defects from intended results, and capture actual build/test/runtime evidence. This guide closes documentation organization work only.
