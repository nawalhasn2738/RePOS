# Legacy system overview

This guide describes the preserved **original system**, not the planned reengineered implementation. Evidence is static source/configuration inspection; runtime success is unverified. See [baseline provenance](BASELINE.md), [setup and execution](setup-and-execution.md), [functional documentation](functional-documentation.md) and [documentation gaps](documentation-gaps.md).

## Purpose and users

The [original README](../../legacy/README.md) identifies the project as the University Souvenir Store Application. [MainFrame](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/gui/MainFrame.java) uses that window title; [LoginPanel](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/gui/LoginPanel.java) presents staff login.

The evidenced application user is a **storekeeper/staff member** operating a desktop UI. Customers can be members or public customers represented in checkout; there is no customer-facing application or role/permission administration workflow established by the source.

## Startup and seven tabs

[StoreApplication.main()](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/StoreApplication.java) loads staff credentials and displays a modal login. If login does not succeed, it exits. After success it constructs category, vendor, product, member, discount and transaction managers, wires callbacks/models, then displays MainFrame.

| Tab label | Source class | Source-observed purpose |
|---|---|---|
| Checkout | [CheckoutPanel](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/gui/checkout/CheckoutPanel.java) | Basket entry, optional membership, payment, receipt choice and understock alerts. |
| New Category | [CategoryPanel](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/gui/CategoryPanel.java) | Add categories and view existing records. |
| New Member | [MemberPanel](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/gui/MemberPanel.java) | Register members and view names, IDs and loyalty balances. |
| New Product | [ProductPanel](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/gui/ProductPanel.java) | Add category-linked products and view stock/price fields. |
| Inventory | [InventoryPanel](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/gui/InventoryPanel.java) | Select understock products and confirm immediate replenishment. |
| Discount | [DiscountPanel](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/gui/DiscountPanel.java) | Add and list discount definitions. |
| Reports | [ReportPanel](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/gui/ReportPanel.java) | Category, product, transaction-line and member tables; transaction date filter. |

These are implemented input/list operations, not evidence of general edit/delete, refunds, payment gateways or external ordering.

## Major modules

All production packages share the prefix `sg.edu.nus.iss.se24_2ft.unit1.ca`; this organization does not establish architectural layers.

| Existing package/module | Important classes and responsibilities |
|---|---|
| Root/startup and `gui` | StoreApplication, MainFrame, feature panels and callback interfaces construct/connect the UI and operations. |
| `category` | [Category](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/category/Category.java) and [CategoryManager](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/category/CategoryManager.java): identity, registry, file loading/storage and table model. |
| `product` | [Product](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/product/Product.java) and [ProductManager](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/product/ProductManager.java): category association, generated IDs, barcode uniqueness, stock and replenishment. |
| `customer`, `customer.member` | [Customer](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/customer/Customer.java), [PublicCustomer](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/customer/PublicCustomer.java), [Member](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/customer/member/Member.java) and [MemberManager](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/customer/member/MemberManager.java): customer identity, registration and loyalty balances. |
| `discount` | [Discount](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/discount/Discount.java) and [DiscountManager](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/discount/DiscountManager.java): definitions, time/member eligibility and highest-percentage selection. |
| `transaction` | [Transaction](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/transaction/Transaction.java), [TransactionItem](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/transaction/TransactionItem.java) and [TransactionManager](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/transaction/TransactionManager.java): basket, totals, sale completion, history and report rows. |
| `storekeeper` | [StoreKeeper](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/storekeeper/StoreKeeper.java) and [StoreKeeperManager](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/storekeeper/StoreKeeperManager.java): staff records and plaintext credential comparison. |
| `vendor` | [Vendor](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/vendor/Vendor.java) and [VendorManager](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/vendor/VendorManager.java): category-specific vendor loading; not wired into inventory operations. |
| `gui.checkout`, `printer` | Payment/receipt dialogs; [Printer.print()](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/printer/Printer.java) writes console output. Printer subclasses contain incomplete tags. |
| `util` | [Util](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/util/Util.java): delimiter splitting, fallback parsing, date arithmetic and money formatting. |

Managers combine rules, direct file access and Swing models. Transaction also owns a table model and depends on a TransactionManager conversion constant. Read the [dependency map](../inventory/dependency-map.md) for source-level edges and cycles; its proposed target separation is not yet implemented.

## Workflow and data flow

1. Staff login reads working-directory-relative `data/Storekeepers.dat`.
2. Managers load category/product/member/discount/history files and category-specific vendor files into collections.
3. Add operations validate some inputs, update collections/tables and rewrite their text file.
4. Checkout builds a Transaction from product IDs and quantities, optionally attaches a member and selects an eligible discount.
5. Confirmed payment triggers loyalty updates, stock deduction and transaction storage, then optional receipt and understock feedback.
6. Reports display manager-backed tables; historical transactions are reloaded through current product/member records.

Persistence is comma-delimited text, with no SQL/database implementation identified. Sale-related writes are independent; there is no evidenced atomic transaction boundary. History loading also has customer/line defects. These are limitations to investigate/correct with approved expectations, not behavior silently endorsed by this guide.

## Baseline and planned system

The inventory counts **40 production Java files**, **15 JUnit classes** and **one test helper** within 90 original files. Compilation and the 45 annotated tests have not been executed in this documentation work.

RePOS plans to preserve accepted workflows while improving responsibilities and persistence. No replacement service layer, relational database or redesigned UI has been implemented. Use [reengineering priorities](../inventory/reengineering-priorities.md) for proposed owners, risks and open decisions rather than inferring future capabilities from this overview.
