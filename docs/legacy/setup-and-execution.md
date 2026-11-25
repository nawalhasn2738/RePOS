# Legacy setup and execution

These notes reconstruct the **original configuration and scripts**. They are not a successfully tested installation recipe. No compilation, application execution, fixture-writing test or packaging script was run for this documentation task.

Keep the preserved baseline untouched. Any later execution must use a separately prepared disposable copy with writable data/output, outside `legacy/`. See [baseline provenance](BASELINE.md), [system overview](system-overview.md) and [documentation gaps](documentation-gaps.md).

## Requirements evidenced by configuration

| Requirement | Verified evidence | Limit |
|---|---|---|
| Java 8-era JDK | [misc.xml](../../legacy/.idea/misc.xml) specifies JDK_1_8 / JDK name 1.8; [setenv.bat](../../legacy/submission/setenv.bat) names jdk1.8.0_77. Lambdas/streams occur in source. | Intended environment, not a proven minimum/maximum compatibility matrix. Neither Java 8 nor newer-JDK execution is certified. |
| Graphical desktop | Swing JFrame/JDialog use in MainFrame/LoginPanel/payment/receipt dialogs. | Headless application operation is not established. |
| Compiler | [compile.bat](../../legacy/submission/compile.bat) invokes javac directly. | No Maven, Gradle or Ant build descriptor/wrapper was found in the inventory. |
| Production APIs | Java standard library, Swing/AWT, NIO files, collections/streams and text/date APIs. | No third-party production Java dependency identified from imports; no runnable artifact was verified. |
| Test libraries | [unit1-ca.iml](../../legacy/unit1-ca.iml) points to IDE-relative JUnit 4.12/Hamcrest 1.3; [.classpath](../../legacy/.classpath) uses Eclipse's JUnit 4 container. | These JARs are not bundled; availability through an installed IDE is unverified. |
| Encoding | [encodings.xml](../../legacy/.idea/encodings.xml) specifies UTF-8. | Original javac script does not explicitly pass -encoding; cross-environment text handling needs verification. |
| Windows script environment | Batch scripts use Windows paths/commands and optionally a Java 1.8 registry lookup. | setenv.bat hard-codes one machine's JDK and replaces PATH with its bin directory. |

## Required working-directory layout

[StoreApplication.main()](../../legacy/src/sg/edu/nus/iss/se24_2ft/unit1/ca/StoreApplication.java) reads `data/...` relative to the launch directory, not from a database or guaranteed bundled classpath resource.

The original [README](../../legacy/README.md) says to copy sample contents into a live `data` directory. In the preserved checkout the live directory is absent intentionally: [.gitignore](../../legacy/.gitignore) excludes it. Do not create it inside the baseline.

A future disposable runtime needs these live filenames copied from [data-sample](../../legacy/data-sample/):

| Live filename(s), relative to that runtime | Records / consumer |
|---|---|
| Category.dat | Category ID/name; CategoryManager. |
| Products.dat | Product ID, description, stock, price, barcode and reorder fields; ProductManager. |
| Members.dat | Name, member ID and loyalty balance; MemberManager. |
| Discounts.dat | Definition, dates/period, percentage and member/all audience; DiscountManager. |
| Storekeepers.dat | Staff names and plaintext sample passwords; StoreKeeperManager. |
| Transactions.dat | Transaction/product/customer IDs, quantity and date; TransactionManager. |
| VendorsCLO.dat, VendorsDIA.dat, VendorsMUG.dat, VendorsSNA.dat, VendorsSTA.dat | Category-associated vendors; VendorManager derives the category from each filename. |

Keep all five vendor filenames even though their contents match. The samples are comparison data, not approved future production accounts or independently verified synthetic identities.

## Original scripts and command context

| Original file | What it does / expected context | Known limitation |
|---|---|---|
| [submission.bat](../../legacy/submission.bat) | From original project root, deletes/repopulates submission src/test/data/classes and packages with bundled 7-Zip. | Mutates directories; do not run in baseline. Copies complete test tree. |
| [submission.sh](../../legacy/submission.sh) | Unix copy/cleanup/ZIP recipe for assignment submission. | sunmission/data cleanup typo; copies test/sg but omits test/util helper; destination assumptions. |
| [submission/compile.bat](../../legacy/submission/compile.bat) | From a prepared submission working directory with src and setenv.bat, compiles to classes and writes compile.log. Defaults to CompileMini; another argument selects CompileAll. | Deletes/recreates output; classses typo in inherited CLASSPATH, tools.jar reference and unusual redirection; actual behavior untested. |
| [submission/run.bat](../../legacy/submission/run.bat) | Calls setenv.bat, then starts StoreApplication from classes. | Requires prepared classes and adjacent live data; no preparation occurs in the launch script. |
| [submission/setenv.bat](../../legacy/submission/setenv.bat) | Sets JAVA_HOME and PATH. | Machine-specific JDK path; overwrites PATH. |

The preserved submission directory contains scripts and documentation, not the generated src/test/data/classes distribution. Original packaging commands are historical evidence, not a safe prerequisite to run in this repository.

### Historical compiler and launcher commands — not executed

CompileMini contains this invocation; `SourcePath_Prefix` is defined by the script as `src\sg\edu\nus\iss\se24_2ft\unit1\ca`:

```bat
javac -d "%cd%\classes" -cp classes -sourcepath src %SourcePath_Prefix%\*.java
```

The script also appends log/redirection operations. CompileAll traverses source directories and invokes javac for each directory's Java files. Neither mode compiles/runs the test suite.

The original launch command is:

```bat
java -cp classes sg.edu.nus.iss.se24_2ft.unit1.ca.StoreApplication
```

[MANIFEST.MF](../../legacy/src/META-INF/MANIFEST.MF) identifies the same main class. [jar_with_data.xml](../../legacy/.idea/artifacts/jar_with_data.xml) describes an IntelliJ artifact containing unit1-ca.jar plus an adjacent data directory; that artifact has not been built or validated.

## Tests and safe investigation prerequisites

The 15 JUnit 4 classes contain 45 @Test methods; this count is not a pass result. No repository test-running script or explicit suite was found. Eclipse/IntelliJ settings indicate intended IDE test support, not successful execution.

[TestUtil](../../legacy/test/util/TestUtil.java) writes/deletes working-directory-relative test files. Manager constructors perform real file reads. Running tests directly from the baseline would mutate its contents, and shared filenames could interfere across runs.

Before a later authorized execution investigation:

1. Prepare a complete disposable copy, including test/util, with dedicated writable live data and outputs.
2. Establish and record the actual JDK, compiler encoding, IDE/library paths and launch directory.
3. Supply verified test libraries through the chosen execution environment; do not infer they are bundled.
4. Capture real compilation/test/GUI results and compare baseline hashes afterward.

No copies, installations or execution were performed here. [Inventory findings](../inventory/inventory-findings.md) and [reengineering priorities](../inventory/reengineering-priorities.md) explain the data-integrity, security and verification issues that still apply even if compilation later succeeds.
