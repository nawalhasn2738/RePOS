package sg.edu.nus.iss.se24_2ft.unit1.ca.bootstrap;

import sg.edu.nus.iss.se24_2ft.unit1.ca.category.CategoryManager;
import sg.edu.nus.iss.se24_2ft.unit1.ca.customer.member.MemberManager;
import sg.edu.nus.iss.se24_2ft.unit1.ca.discount.DiscountManager;
import sg.edu.nus.iss.se24_2ft.unit1.ca.product.ProductManager;
import sg.edu.nus.iss.se24_2ft.unit1.ca.transaction.TransactionManager;
import sg.edu.nus.iss.se24_2ft.unit1.ca.vendor.VendorManager;
import java.util.Objects;

/** Concrete composition, not a service or repository: legacy managers still own their I/O. */
public final class StoreContext {
    private final CategoryManager categories;
    private final VendorManager vendors;
    private final ProductManager products;
    private final MemberManager members;
    private final DiscountManager discounts;
    private final TransactionManager transactions;

    private StoreContext(StorePaths paths) {
        categories = new CategoryManager(paths.categories().toString());
        vendors = new VendorManager(paths.directory().toString(), categories);
        products = new ProductManager(paths.products().toString(), categories);
        members = new MemberManager(paths.members().toString());
        discounts = new DiscountManager(paths.discounts().toString());
        transactions = new TransactionManager(paths.transactions().toString(), products, members);
    }

    public static StoreContext load(StorePaths paths) {
        return new StoreContext(Objects.requireNonNull(paths, "paths"));
    }
    public CategoryManager categories() { return categories; }
    public VendorManager vendors() { return vendors; }
    public ProductManager products() { return products; }
    public MemberManager members() { return members; }
    public DiscountManager discounts() { return discounts; }
    public TransactionManager transactions() { return transactions; }
}
