package sg.edu.nus.iss.se24_2ft.unit1.ca.gui;

import sg.edu.nus.iss.se24_2ft.unit1.ca.product.Product;
import sg.edu.nus.iss.se24_2ft.unit1.ca.product.ProductCatalogListener;
import sg.edu.nus.iss.se24_2ft.unit1.ca.product.ProductManager;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/** Owns the two legacy inventory projections and translates catalog events for Swing. */
public final class ProductTableModels implements ProductCatalogListener, AutoCloseable {
    private final ProductManager manager;
    private final AbstractTableModel products;
    private final AbstractTableModel understock;

    public ProductTableModels(ProductManager manager) {
        this.manager = Objects.requireNonNull(manager, "manager");
        products = new ProductRows(manager::getProductList);
        understock = new ProductRows(manager::getUnderstockProducts);
        manager.addListener(this);
    }

    public TableModel products() { return products; }
    public TableModel understock() { return understock; }

    @Override public void productAdded(int productIndex, int understockIndex) {
        products.fireTableRowsInserted(productIndex, productIndex);
        if (understockIndex >= 0) understock.fireTableRowsInserted(understockIndex, understockIndex);
    }

    @Override public void quantityChanged(int productIndex, int understockIndex, boolean enteredUnderstock) {
        if (productIndex >= 0) products.fireTableCellUpdated(productIndex, 3);
        if (understockIndex < 0) return;
        if (enteredUnderstock) understock.fireTableRowsInserted(understockIndex, understockIndex);
        else understock.fireTableCellUpdated(understockIndex, 3);
    }

    @Override public void inventoryRebuilt() {
        understock.fireTableDataChanged();
        products.fireTableDataChanged();
    }

    @Override public void close() { manager.removeListener(this); }

    private static final class ProductRows extends AbstractTableModel {
        private static final String[] COLUMNS = {"ID", "Name", "Description", "Quantity Avl.", "Price",
                "Bar Code", "Reorder Quantity", "Order Quantity"};
        private final Supplier<List<Product>> rows;

        ProductRows(Supplier<List<Product>> rows) { this.rows = rows; }
        @Override public String getColumnName(int column) { return COLUMNS[column]; }
        @Override public int getRowCount() { return rows.get().size(); }
        @Override public int getColumnCount() { return COLUMNS.length; }
        @Override public Object getValueAt(int row, int column) {
            Product p = rows.get().get(row);
            switch (column) {
                case 0: return p.getId();
                case 1: return p.getName();
                case 2: return p.getDescription();
                case 3: return p.getQuantity();
                case 4: return p.getPrice();
                case 5: return p.getBarcode();
                case 6: return p.getThreshold();
                case 7: return p.getOrderQuantity();
                default: return null;
            }
        }
    }
}
