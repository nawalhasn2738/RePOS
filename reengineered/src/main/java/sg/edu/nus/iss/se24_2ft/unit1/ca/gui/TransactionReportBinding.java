package sg.edu.nus.iss.se24_2ft.unit1.ca.gui;

import sg.edu.nus.iss.se24_2ft.unit1.ca.util.Util;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.Component;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;

/** Presentation binding only: preserves the legacy timestamp filter and row-order bounds. */
public final class TransactionReportBinding {
    private final JTable table;
    private final JFormattedTextField start;
    private final JFormattedTextField end;
    private TableRowSorter<TableModel> sorter;

    public TransactionReportBinding(JTable table, JFormattedTextField start, JFormattedTextField end) {
        this.table = Objects.requireNonNull(table, "table");
        this.start = Objects.requireNonNull(start, "start");
        this.end = Objects.requireNonNull(end, "end");
    }

    public void bind(TableModel model) {
        Objects.requireNonNull(model, "model");
        if (model.getColumnCount() == 0) throw new IllegalArgumentException("Report requires a date column");
        table.setModel(model);
        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        int dateColumn = model.getColumnCount() - 1;
        table.getColumnModel().getColumn(dateColumn).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean selected, boolean focus, int row, int column) {
                if (value instanceof Date) value = Util.formatDateOrDefault((Date) value, null);
                return super.getTableCellRendererComponent(t, value, selected, focus, row, column);
            }
        });
        if (model.getRowCount() == 0) return;
        start.setValue(model.getValueAt(0, dateColumn));
        end.setValue(model.getValueAt(model.getRowCount() - 1, dateColumn));
    }

    public void applyDateRange(Date first, Date last) {
        Objects.requireNonNull(first, "first");
        Objects.requireNonNull(last, "last");
        if (first.after(last))
            throw new IllegalArgumentException("End Date is before Start Date, please correct");
        if (sorter == null) return;
        RowFilter<Object, Object> lower = RowFilter.orFilter(Arrays.asList(
                RowFilter.dateFilter(RowFilter.ComparisonType.AFTER, first),
                RowFilter.dateFilter(RowFilter.ComparisonType.EQUAL, first)));
        RowFilter<Object, Object> upper = RowFilter.orFilter(Arrays.asList(
                RowFilter.dateFilter(RowFilter.ComparisonType.BEFORE, last),
                RowFilter.dateFilter(RowFilter.ComparisonType.EQUAL, last)));
        sorter.setRowFilter(RowFilter.andFilter(Arrays.asList(lower, upper)));
    }
}
