# RePOS: inventory summary

Inventory date: 2026-11-25. Baseline commit: `87bee5fed6e44cf87fd91a05de42a6817ae81002`.

## Scope and calculated totals

**90 legacy file assets** were enumerated and inspected at the appropriate level: text content for text assets; signature/size and, for tools, version resources for binary assets. The binary Word report's content remains unverified. “Inspected” does not mean compiled, executed, proven correct or security-audited.

The [file-level register](asset-inventory.md) is the authoritative classification list. Each actual file occurs exactly once; embedded records are not additional assets. The three new inventory documents and existing documentation placeholders are outside the legacy asset count.

| Asset type | File count |
|---|---:|
| Production Java | 40 |
| JUnit test | 15 |
| Test support | 1 |
| Sample data | 11 |
| Build/execution script | 5 |
| IDE configuration | 12 |
| Repository configuration | 1 |
| Manifest | 1 |
| Documentation | 2 |
| Bundled tool | 2 |
| **Total** | **90** |

The Java subtotal is **56 files**: 40 production sources + 15 JUnit test classes + one test helper. The test classes contain **45 `@Test` methods**, including an empty initialization test. These are static counts, not execution results.

| Classification | Count |
|---|---:|
| Reusable | 24 |
| Requires modification | 57 |
| Replace | 2 |
| Obsolete | 0 |
| Needs investigation | 7 |
| **Total** | **90** |

There are **zero Obsolete recommendations**: current evidence does not establish that any file can be discarded from a future application. IDE metadata remains useful evidence even where its future consumer is uncertain. No classification authorizes editing/removing the preserved original.

## Major reusable assets

- Customer identity contracts: `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/customer/Customer.java` (`Customer.getId()`) and `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/customer/PublicCustomer.java` (`getInstance()`, `getId()`) are small active contracts.
- Existing GUI contracts/container: `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/gui/FeaturePanel.java`, `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/gui/MainFrame.java` (`addFeaturePanel()`), and the category/member/product/checkout listener interfaces preserve the current interaction vocabulary. This is not an instruction to create a new UI layer.
- Vendor state: `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/vendor/Vendor.java` preserves name/description/category association; `getCategoryList()` returns a list copy. Disconnected feature wiring does not itself make the data model obsolete.
- Comparison fixtures: `legacy/data-sample/Category.dat`, `legacy/data-sample/Products.dat`, `legacy/data-sample/Discounts.dat` and all five vendor files are reusable examples. Dates and filenames must retain their original meaning.
- Build knowledge: `legacy/src/META-INF/MANIFEST.MF` records the main class; `legacy/.project` records the Eclipse Java builder; `legacy/.idea/encodings.xml` records UTF-8 intent. Other retained metadata provides module/resource relationships.

“Reusable” means little or no adaptation appears necessary from static evidence; it does not guarantee future suitability without tests.

## Major modification or replacement candidates

- **Sale and inventory correctness:** `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/transaction/TransactionManager.java` (`load()`, `addTransaction()`) and `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/product/ProductManager.java` (`deductQuantity()`) contain specific customer-reload, report-row, stock-notification and multi-file completion concerns. See findings H1-H3.
- **Data representation:** `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/transaction/Transaction.java`, `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/transaction/TransactionItem.java` and `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/util/Util.java` need consideration of financial history, comma escaping, date rules and monetary validation. `legacy/data-sample/Transactions.dat` and `Members.dat` need explicit migration interpretation.
- **Authentication replacement:** `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/storekeeper/StoreKeeper.java` and `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/storekeeper/StoreKeeperManager.java` retain/expose/compare plaintext passwords. Replacement is a future security recommendation; both original files remain untouched.
- **Tests and fixture setup:** all 15 JUnit classes and `legacy/test/util/TestUtil.java` retain useful examples but need stronger assertions, boundary cases or isolated fixture handling.
- **Environment and packaging:** all five scripts need adaptation or correction. `legacy/unit1-ca.iml` and `legacy/.classpath` require external IDE-provided test libraries; no JUnit/Hamcrest JAR is bundled.
- **Documentation:** `legacy/README.md` has sample-data setup but lacks complete verified compilation/test instructions. Its original text must remain preserved; future operational documentation belongs outside the baseline.

## Assets needing investigation

- `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/printer/AdhesiveLabelPrinter.java`: Purpose is suggested by name but required workflow is not established. Keep baseline stub; investigate original requirements before implementing or retiring.
- `legacy/.idea/copyright/profiles_settings.xml`: An empty IDE profile does not establish required or obsolete status. Retain baseline; review relevance with future IDE setup.
- `legacy/unit1-ca.eml`: Contains useful path information but active consumer is unverified. Retain and identify intended IDE interoperability workflow before adapting/removing.
- `legacy/unit1-ca.userlibraries`: No current library entries; need for import workflow unestablished. Retain; determine relevance when build/import process is verified.
- `legacy/submission/docs/report.doc`: Purpose suggested by location/name but requirements/content still unverified. Read without editing when compatible tooling is available; do not infer requirements from filename.
- `legacy/tools/7z.exe`: Historical role established, but current provenance/licensing/suitability not fully assessed. Keep baseline binary; verify distribution requirements and tool provenance before future use.
- `legacy/tools/7z.dll`: Historical role established, but current provenance/licensing/suitability not fully assessed. Keep baseline binary; verify distribution requirements and tool provenance before future use.

These seven files are included in the 90 total; their unknowns are not silently treated as successful verification.

## Important observations

1. **Vendor fixture duplication is real but category associations are distinct.** The five `legacy/data-sample/VendorsCLO.dat`, `VendorsDIA.dat`, `VendorsMUG.dat`, `VendorsSNA.dat` and `VendorsSTA.dat` files have the same SHA-256, `ce2b030472ba1eaf5ee706865c281492205c395f3a5ac7c3143108263e9064d4`. `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/vendor/VendorManager.java`, `load()`, derives category ID from each filename. Do not delete duplicate-looking files.
2. **Vendor loading is disconnected from feature operations.** `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/StoreApplication.java`, `main()`, constructs the vendor manager but does not use that variable later. `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/gui/InventoryPanel.java` has a vendor-selection TODO; `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/product/ProductManager.java`, `generatePurchaseOrder()`, immediately replenishes stock.
3. **Printing is partial.** `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/gui/checkout/ReceiptDialog.java`, `setVisible()`, invokes `ReceiptPrinter.print()`, whose inherited static implementation in `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/printer/Printer.java` writes to the console. The adhesive-label class has no callers found in production or tests.
4. **Runtime/build resources are not all present.** `legacy/data/` and build outputs are absent; live data is intentionally excluded by `legacy/.gitignore` and prepared from samples according to `legacy/README.md`. No Maven/Gradle/Ant build descriptor, bundled test JAR or explicit test-run script was found in the 90-file enumeration.
5. **Data assets capture relationships, not a full sale audit.** The 11 sample files contain 57 rows in total. `Transactions.dat` has four lines for three transaction IDs, including a two-product sale. `legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/transaction/TransactionItem.java`, `toString()`, saves only transaction/product/customer IDs, quantity and date; original prices/payments/discounts are absent.
6. **All original assets must remain available for comparison.** Configuration, tests, fixtures, scripts, binary tools and the report are included alongside source. Replacement/modified classifications concern future reuse, not baseline preservation.

## Verification and limitations

Counts were calculated from recursive file enumeration and the one-entry-per-file register; classification counts come from those register entries. Paths must be checked against the actual files, not merely against the reconnaissance report. Detailed verification results are reported with task completion.

At the time of inventory, the committed `legacy/` subtree and original upstream tree both identify `c026b20bcfab3bb29caa31f6f04dd28a90c5ab8c`. A byte-hash snapshot is used to compare baseline files before/after documentation work, in addition to Git checks.

No builds, tests, package scripts or bundled tools were executed; dependencies were not installed. Source-confirmed concerns are distinguished from untested runtime consequences in [inventory findings](inventory-findings.md).
