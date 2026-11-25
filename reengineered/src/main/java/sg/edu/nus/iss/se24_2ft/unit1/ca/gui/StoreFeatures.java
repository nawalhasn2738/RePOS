package sg.edu.nus.iss.se24_2ft.unit1.ca.gui;

import sg.edu.nus.iss.se24_2ft.unit1.ca.category.CategoryManager;
import sg.edu.nus.iss.se24_2ft.unit1.ca.customer.Customer;
import sg.edu.nus.iss.se24_2ft.unit1.ca.customer.member.Member;
import sg.edu.nus.iss.se24_2ft.unit1.ca.customer.member.MemberManager;
import sg.edu.nus.iss.se24_2ft.unit1.ca.discount.Discount;
import sg.edu.nus.iss.se24_2ft.unit1.ca.discount.DiscountManager;
import sg.edu.nus.iss.se24_2ft.unit1.ca.gui.checkout.CheckoutPanel;
import sg.edu.nus.iss.se24_2ft.unit1.ca.product.Product;
import sg.edu.nus.iss.se24_2ft.unit1.ca.product.ProductManager;
import sg.edu.nus.iss.se24_2ft.unit1.ca.transaction.TransactionManager;

import sg.edu.nus.iss.se24_2ft.unit1.ca.bootstrap.StoreContext;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** Owns feature construction/binding; no window, login, business policy or file access here. */
public final class StoreFeatures implements AutoCloseable {
    private final Map<String, FeaturePanel> panels = new LinkedHashMap<>();
    private final ProductTableModels productTables;

    public StoreFeatures(StoreContext context) {
        Objects.requireNonNull(context, "context");
        CategoryManager categoryManager = context.categories();
        ProductManager productManager = context.products();
        MemberManager memberManager = context.members();
        DiscountManager discountManager = context.discounts();
        TransactionManager transactionManager = context.transactions();
        productTables = new ProductTableModels(productManager);
        DiscountPanel discountPanel = new DiscountPanel();
        discountPanel.setTableModel(discountManager.getTableModel());
        discountPanel.addDiscountPanelListener(discountManager::addDiscount);

        CategoryPanel categoryPanel = new CategoryPanel();
        categoryPanel.setTableModel(categoryManager.getTableModel());
        categoryPanel.addCategoryPanelListener(categoryManager::addCategory);

        MemberPanel memberPanel = new MemberPanel();
        memberPanel.setTableModel(memberManager.getTableModel());
        memberPanel.addMemberPaneListener(memberManager::addMember);

        ProductPanel productPanel = new ProductPanel();
        productPanel.setTableModel(productTables.products());
        productPanel.addProductPanelListener(
                (cid, p) -> productManager.addProduct(categoryManager.getCategory(cid), p)
        );

        InventoryPanel inventoryPanel = new InventoryPanel();
        inventoryPanel.setTableModel(productTables.understock());
        inventoryPanel.addInventoryPanelListener(productManager::generatePurchaseOrder);

        CheckoutPanel checkoutPanel = new CheckoutPanel() {
            @Override
            protected Product getProduct(String id) {
                return productManager.getProduct(id);
            }

            @Override
            protected Member getMember(String id) {
                return memberManager.getMember(id);
            }

            @Override
            protected Discount getDiscount(Customer customer) {
                return discountManager.getMaxDiscount(customer);
            }
        };
        checkoutPanel.addCheckoutPanelListener(transactionManager::addTransaction);

        ReportPanel reportPanel = new ReportPanel();
        reportPanel.setCategoryTableModel(categoryManager.getTableModel());
        reportPanel.setProductTableModel(productTables.products());
        reportPanel.setTransactionTableModel(transactionManager.getTableModel());
        reportPanel.setMemberTableModel(memberManager.getTableModel());

        panels.put("Checkout", checkoutPanel);
        panels.put("New Category", categoryPanel);
        panels.put("New Member", memberPanel);
        panels.put("New Product", productPanel);
        panels.put("Inventory", inventoryPanel);
        panels.put("Discount", discountPanel);
        panels.put("Reports", reportPanel);
    }

    public Map<String, FeaturePanel> panels() { return Collections.unmodifiableMap(panels); }
    @Override public void close() { productTables.close(); }
}
