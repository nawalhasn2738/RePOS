package sg.edu.nus.iss.se24_2ft.unit1.ca.product;

/** Synchronous catalog notifications; no GUI or storage API belongs in this contract. */
public interface ProductCatalogListener {
    void productAdded(int productIndex, int understockIndex);
    void quantityChanged(int productIndex, int understockIndex, boolean enteredUnderstock);
    void inventoryRebuilt();
}
