package repos.refactoring;

import sg.edu.nus.iss.se24_2ft.unit1.ca.bootstrap.*;
import sg.edu.nus.iss.se24_2ft.unit1.ca.category.CategoryManager;
import sg.edu.nus.iss.se24_2ft.unit1.ca.gui.*;
import sg.edu.nus.iss.se24_2ft.unit1.ca.product.*;
import sg.edu.nus.iss.se24_2ft.unit1.ca.util.Util;
import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.*;
import java.awt.Component;
import java.awt.Container;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

/** Dependency-free focused tests; throws AssertionError on failure, independently of -ea. */
public final class RefactoringTests {
    private static int passed;
    private interface Checked { void run() throws Exception; }
    private static void run(String name, Checked test) throws Exception {
        test.run();
        passed++;
        System.out.println("PASS " + name);
    }
    private static void equal(Object expected, Object actual) {
        if (!Objects.equals(expected, actual)) throw new AssertionError("Expected " + expected + ", got " + actual);
    }
    private static void yes(boolean value) { if (!value) throw new AssertionError("Condition failed"); }
    private static void rejects(Class<? extends Throwable> type, Checked action) throws Exception {
        try { action.run(); }
        catch (Throwable error) { if (type.isInstance(error)) return; throw new AssertionError("Wrong error", error); }
        throw new AssertionError("Expected " + type.getName());
    }

    public static void main(String[] args) throws Exception {
        // Swing presentation checks execute on the event-dispatch thread, with no top-level window.
        final Throwable[] failure = {null};
        SwingUtilities.invokeAndWait(() -> {
            try { inventory(); composition(); reports(); }
            catch (Throwable error) { failure[0] = error; }
        });
        if (failure[0] != null) throw new AssertionError("Refactoring test failure", failure[0]);
        System.out.println("RESULT: " + passed + " tests passed; 0 failed");
    }

    private static void inventory() throws Exception {
        run("RF1 stock enters understock without any view and persists", () -> {
            try (Fixture f = new Fixture()) {
                ProductManager m = f.products();
                m.deductQuantity("MUG/1", 2);
                equal(10, m.getProduct("MUG/1").getQuantity());
                equal(1, m.getUnderstockProducts().size());
                yes(f.read("Products.dat").contains(",10,2.5,100,10,5"));
                rejects(UnsupportedOperationException.class, () -> m.getUnderstockProducts().clear());
            }
        });
        run("RF1 table projections preserve all eight fields and labels", () -> {
            try (Fixture f = new Fixture(); ProductTableModels views = new ProductTableModels(f.products())) {
                TableModel model = views.products();
                equal(8, model.getColumnCount()); equal(1, model.getRowCount()); equal(0, views.understock().getRowCount());
                Object[] expected = {"MUG/1", "Mug", "Description", 12, 2.5, 100, 10, 5};
                for (int i = 0; i < expected.length; i++) equal(expected[i], model.getValueAt(0, i));
                equal("Reorder Quantity", model.getColumnName(6));
                equal("Order Quantity", model.getColumnName(7));
                equal(null, model.getValueAt(0, 99));
                rejects(IndexOutOfBoundsException.class, () -> model.getValueAt(50, 0));
            }
        });
        run("RF1 quantity and threshold insertion notifications", () -> {
            try (Fixture f = new Fixture()) {
                ProductManager m = f.products();
                try (ProductTableModels views = new ProductTableModels(m)) {
                    List<TableModelEvent> full = new ArrayList<>(), low = new ArrayList<>();
                    views.products().addTableModelListener(full::add); views.understock().addTableModelListener(low::add);
                    m.deductQuantity("MUG/1", 2);
                    equal(TableModelEvent.UPDATE, full.get(0).getType()); equal(3, full.get(0).getColumn());
                    equal(TableModelEvent.INSERT, low.get(0).getType()); equal(0, low.get(0).getFirstRow());
                    m.deductQuantity("MUG/1", 1);
                    equal(TableModelEvent.UPDATE, low.get(1).getType()); equal(9, views.understock().getValueAt(0, 3));
                }
            }
        });
        run("RF1 adding an understock product updates both views", () -> {
            try (Fixture f = new Fixture()) {
                ProductManager m = f.products();
                try (ProductTableModels views = new ProductTableModels(m)) {
                    List<TableModelEvent> events = new ArrayList<>(); views.understock().addTableModelListener(events::add);
                    m.addProduct(f.categories().getCategory("MUG"), new Product("New", "New description", 1, 4, 101, 2, 3));
                    equal("MUG/2", views.products().getValueAt(1, 0)); equal(1, views.understock().getRowCount());
                    equal(TableModelEvent.INSERT, events.get(0).getType()); equal(0, events.get(0).getFirstRow());
                }
            }
        });
        run("RF1 restock rebuild notifications and original immediate arithmetic", () -> {
            try (Fixture f = new Fixture()) {
                ProductManager m = f.products(); m.deductQuantity("MUG/1", 2);
                try (ProductTableModels views = new ProductTableModels(m)) {
                    List<TableModelEvent> events = new ArrayList<>(); views.understock().addTableModelListener(events::add);
                    m.generatePurchaseOrder(Arrays.asList(0));
                    equal(15, m.getProduct("MUG/1").getQuantity()); equal(0, views.understock().getRowCount());
                    equal(Integer.MAX_VALUE, events.get(0).getLastRow());
                    yes(f.read("Products.dat").contains(",15,2.5,100,10,5"));
                }
            }
        });
        run("RF1 missing and insufficient products leave data unchanged", () -> {
            try (Fixture f = new Fixture()) {
                ProductManager m = f.products(); String before = f.read("Products.dat");
                rejects(IllegalArgumentException.class, () -> m.deductQuantity("NONE", 1));
                rejects(IllegalArgumentException.class, () -> m.deductQuantity("MUG/1", 13));
                equal(12, m.getProduct("MUG/1").getQuantity()); equal(before, f.read("Products.dat"));
            }
        });
        run("RF1 duplicate barcode and null listener rejected", () -> {
            try (Fixture f = new Fixture()) {
                ProductManager m = f.products();
                rejects(NullPointerException.class, () -> m.addListener(null));
                rejects(IllegalArgumentException.class, () -> m.addProduct(f.categories().getCategory("MUG"),
                        new Product("Duplicate", "", 1, 1, 100, 1, 1)));
                equal(1, m.getProductList().size());
            }
        });
        run("RF1 null and empty replenishment remain no-ops", () -> {
            try (Fixture f = new Fixture()) {
                ProductManager m = f.products(); String before = f.read("Products.dat");
                m.generatePurchaseOrder(null); m.generatePurchaseOrder(Collections.emptyList());
                equal(before, f.read("Products.dat"));
            }
        });
        run("RF1 closing an adapter unsubscribes notifications", () -> {
            try (Fixture f = new Fixture()) {
                ProductManager m = f.products(); ProductTableModels views = new ProductTableModels(m);
                List<TableModelEvent> events = new ArrayList<>(); views.products().addTableModelListener(events::add);
                views.close(); views.close(); m.deductQuantity("MUG/1", 1);
                equal(0, events.size()); equal(11, m.getProduct("MUG/1").getQuantity());
            }
        });
    }

    private static void composition() throws Exception {
        run("RF2 path configuration is pure and retains legacy filenames", () -> {
            Path absent = Paths.get(System.getProperty("java.io.tmpdir"), "RePOS-absent-" + UUID.randomUUID());
            StorePaths p = new StorePaths(absent);
            equal(absent.resolve("Category.dat"), p.categories()); equal(absent.resolve("Products.dat"), p.products());
            equal(absent.resolve("Members.dat"), p.members()); equal(absent.resolve("Discounts.dat"), p.discounts());
            equal(absent.resolve("Transactions.dat"), p.transactions()); equal(absent.resolve("Storekeepers.dat"), p.storekeepers());
            equal(Paths.get("data"), StorePaths.workingDirectory().directory()); yes(!Files.exists(absent));
            rejects(NullPointerException.class, () -> new StorePaths(null));
            rejects(NullPointerException.class, () -> StoreContext.load(null));
        });
        run("RF2 context loads configured managers without rewriting fixture data", () -> {
            try (Fixture f = new Fixture()) {
                Map<String, String> before = f.snapshot(); StoreContext c = StoreContext.load(new StorePaths(f.root));
                equal("Mugs", c.categories().getCategory("MUG").getName());
                equal(c.categories().getCategory("MUG"), c.products().getProduct("MUG/1").getCategory());
                equal(1, c.vendors().getVendorListByCategoryId("MUG").size());
                equal("M1", c.members().getMember("M1").getId());
                equal(0, c.discounts().getDiscountList().size()); equal(0, c.transactions().getTableModel().getRowCount());
                equal(before, f.snapshot());
            }
        });
        run("RF2 seven feature types and order without a JFrame or login dialog", () -> {
            try (Fixture f = new Fixture(); StoreFeatures features = new StoreFeatures(StoreContext.load(new StorePaths(f.root)))) {
                equal(Arrays.asList("Checkout", "New Category", "New Member", "New Product", "Inventory", "Discount", "Reports"),
                        new ArrayList<>(features.panels().keySet()));
                equal("CheckoutPanel", features.panels().get("Checkout").getClass().getSuperclass().getSimpleName());
                equal(CategoryPanel.class, features.panels().get("New Category").getClass());
                equal(MemberPanel.class, features.panels().get("New Member").getClass());
                equal(ProductPanel.class, features.panels().get("New Product").getClass());
                equal(InventoryPanel.class, features.panels().get("Inventory").getClass());
                equal(DiscountPanel.class, features.panels().get("Discount").getClass());
                equal(ReportPanel.class, features.panels().get("Reports").getClass());
                rejects(UnsupportedOperationException.class, () -> features.panels().clear());
                rejects(NullPointerException.class, () -> new StoreFeatures(null));
            }
        });
        run("RF2 real category member product and discount Add callbacks remain bound", () -> {
            try (Fixture f = new Fixture()) {
                StoreContext c = StoreContext.load(new StorePaths(f.root));
                try (StoreFeatures features = new StoreFeatures(c)) {
                    fill(features.panels().get("New Category"), "TST", "Tests");
                    click(features.panels().get("New Category"), "Add"); yes(c.categories().getCategory("TST") != null);
                    fill(features.panels().get("New Member"), "M2", "Member Two");
                    click(features.panels().get("New Member"), "Add"); equal("Member Two", c.members().getMember("M2").getName());
                    fill(features.panels().get("New Product"), "TST", "New", "Test description", "3", "2.50", "123", "1", "5");
                    click(features.panels().get("New Product"), "Add"); equal(3, c.products().getProduct("TST/1").getQuantity());
                    fill(features.panels().get("Discount"), "TEST", "Test discount", "", "", "10");
                    click(features.panels().get("Discount"), "Add"); equal("TEST", c.discounts().getDiscountList().get(0).getCode());
                    yes(f.read("Category.dat").contains("TST,Tests")); yes(f.read("Members.dat").contains("Member Two,M2,-1"));
                }
            }
        });
        run("RF2 product and inventory and report projections share the extracted adapter", () -> {
            try (Fixture f = new Fixture()) {
                StoreContext c = StoreContext.load(new StorePaths(f.root));
                try (StoreFeatures features = new StoreFeatures(c)) {
                    JTable products = components(features.panels().get("New Product"), JTable.class).get(0);
                    JTable inventory = components(features.panels().get("Inventory"), JTable.class).get(0);
                    yes(components(features.panels().get("Reports"), JTable.class).stream().anyMatch(t -> t.getModel() == products.getModel()));
                    c.products().deductQuantity("MUG/1", 2);
                    equal(10, products.getValueAt(0, 3)); equal(1, inventory.getRowCount()); equal("MUG/1", inventory.getValueAt(0, 0));
                }
            }
        });
    }

    private static void reports() throws Exception {
        run("RF3 exact inclusive start/end and single-instant ranges", () -> {
            ReportFixture f = new ReportFixture(); f.binding.bind(model(999, 1000, 1500, 2000, 2001));
            f.binding.applyDateRange(new Date(1000), new Date(2000)); equal(3, f.table.getRowCount());
            f.binding.applyDateRange(new Date(1500), new Date(1500)); equal(1, f.table.getRowCount());
        });
        run("RF3 reversed range rejects without replacing active filter", () -> {
            ReportFixture f = new ReportFixture(); f.binding.bind(model(1000, 2000, 3000));
            f.binding.applyDateRange(new Date(2000), new Date(2000));
            rejects(IllegalArgumentException.class, () -> f.binding.applyDateRange(new Date(3), new Date(2)));
            equal(1, f.table.getRowCount());
        });
        run("RF3 empty/rebound models retain first-last encounter behavior", () -> {
            ReportFixture f = new ReportFixture(); f.start.setValue(new Date(7)); f.end.setValue(new Date(8));
            f.binding.bind(model()); equal(new Date(7), f.start.getValue()); equal(new Date(8), f.end.getValue());
            f.binding.bind(model(50, 20)); equal(new Date(50), f.start.getValue()); equal(new Date(20), f.end.getValue());
            f.binding.applyDateRange(new Date(20), new Date(50)); equal(2, f.table.getRowCount());
            f.binding.bind(model(100, 200, 300)); equal(3, f.table.getRowCount());
        });
        run("RF3 renderer retains yyyy-MM-dd and accepts non-Date values", () -> {
            ReportFixture f = new ReportFixture(); f.binding.bind(model(1000));
            TableCellRenderer renderer = f.table.getColumnModel().getColumn(1).getCellRenderer();
            JLabel rendered = (JLabel) renderer.getTableCellRendererComponent(f.table, new Date(1000), false, false, 0, 1);
            equal(Util.formatDateOrDefault(new Date(1000), null), rendered.getText());
            rendered = (JLabel) renderer.getTableCellRendererComponent(f.table, "unchanged", false, false, 0, 1);
            equal("unchanged", rendered.getText());
        });
        run("RF3 invalid binding inputs and pre-bind valid no-op", () -> {
            ReportFixture f = new ReportFixture(); f.binding.applyDateRange(new Date(1), new Date(2));
            rejects(NullPointerException.class, () -> f.binding.bind(null));
            rejects(IllegalArgumentException.class, () -> f.binding.bind(new DefaultTableModel()));
            rejects(NullPointerException.class, () -> f.binding.applyDateRange(null, new Date()));
            rejects(NullPointerException.class, () -> new TransactionReportBinding(null, f.start, f.end));
        });
        run("RF3 timestamp filtering does not silently expand to end-of-day", () -> {
            ReportFixture f = new ReportFixture(); f.binding.bind(model(0, 43200000));
            f.binding.applyDateRange(new Date(0), new Date(0)); equal(1, f.table.getRowCount());
        });
        run("RF3 actual ReportPanel Get Report handler delegates to binding", () -> {
            ReportPanel panel = new ReportPanel(); panel.setTransactionTableModel(model(1000, 2000, 3000));
            List<JFormattedTextField> fields = components(panel, JFormattedTextField.class);
            equal(2, fields.size()); fields.get(0).setValue(new Date(2000)); fields.get(1).setValue(new Date(2000));
            click(panel, "Get Report");
            JTable filtered = components(panel, JTable.class).stream().filter(t -> t.getRowSorter() != null).findFirst().get();
            equal(1, filtered.getRowCount()); equal(new Date(2000), filtered.getValueAt(0, 1));
        });
    }

    private static TableModel model(long... dates) {
        DefaultTableModel model = new DefaultTableModel(new Object[]{"ID", "Date"}, 0);
        for (int i = 0; i < dates.length; i++) model.addRow(new Object[]{i, new Date(dates[i])});
        return model;
    }
    private static final class ReportFixture {
        final JTable table = new JTable();
        final JFormattedTextField start = new JFormattedTextField(Util.DATE_FORMAT);
        final JFormattedTextField end = new JFormattedTextField(Util.DATE_FORMAT);
        final TransactionReportBinding binding = new TransactionReportBinding(table, start, end);
    }
    private static <T extends Component> List<T> components(Component root, Class<T> type) {
        List<T> result = new ArrayList<>(); if (type.isInstance(root)) result.add(type.cast(root));
        if (root instanceof Container) for (Component child : ((Container) root).getComponents()) result.addAll(components(child, type));
        return result;
    }
    private static void fill(Component panel, String... values) {
        List<JTextField> fields = components(panel, JTextField.class); equal(values.length, fields.size());
        for (int i = 0; i < values.length; i++) fields.get(i).setText(values[i]);
    }
    private static void click(Component panel, String title) {
        JButton button = components(panel, JButton.class).stream().filter(b -> title.equals(b.getText())).findFirst().orElseThrow(() -> new AssertionError(title));
        button.doClick();
    }
    private static final class Fixture implements AutoCloseable {
        final Path root = Files.createTempDirectory("RePOS-refactoring-fixture-");
        private CategoryManager categories;
        private ProductManager products;
        Fixture() throws Exception {
            write("Category.dat", "MUG,Mugs\n"); write("Products.dat", "MUG/1,Mug,Description,12,2.5,100,10,5\n");
            write("Members.dat", "Test Member,M1,50\n"); write("Discounts.dat", ""); write("Transactions.dat", "");
            write("Storekeepers.dat", "test-user,test-fixture-only\n"); write("VendorsMUG.dat", "Test Vendor,Fixture\n");
        }
        void write(String name, String text) throws Exception { Files.write(root.resolve(name), text.getBytes(StandardCharsets.UTF_8)); }
        String read(String name) throws Exception { return new String(Files.readAllBytes(root.resolve(name)), StandardCharsets.UTF_8); }
        CategoryManager categories() {
            if (categories == null) categories = new CategoryManager(root.resolve("Category.dat").toString()); return categories;
        }
        ProductManager products() {
            if (products == null) products = new ProductManager(root.resolve("Products.dat").toString(), categories()); return products;
        }
        Map<String, String> snapshot() throws Exception {
            Map<String, String> result = new TreeMap<>();
            try (Stream<Path> paths = Files.list(root)) {
                for (Path p : (Iterable<Path>) paths::iterator) result.put(p.getFileName().toString(), read(p.getFileName().toString()));
            }
            return result;
        }
        @Override public void close() throws Exception {
            try (Stream<Path> paths = Files.walk(root)) {
                Path[] entries = paths.sorted(Comparator.reverseOrder()).toArray(Path[]::new);
                for (Path p : entries) {
                    if (!p.toAbsolutePath().normalize().startsWith(root.toAbsolutePath().normalize())) throw new AssertionError("Unsafe cleanup path");
                    Files.delete(p);
                }
            }
        }
    }
}
