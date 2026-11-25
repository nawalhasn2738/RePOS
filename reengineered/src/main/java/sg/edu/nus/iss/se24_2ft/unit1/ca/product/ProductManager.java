package sg.edu.nus.iss.se24_2ft.unit1.ca.product;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import sg.edu.nus.iss.se24_2ft.unit1.ca.category.Category;
import sg.edu.nus.iss.se24_2ft.unit1.ca.category.CategoryManager;
import sg.edu.nus.iss.se24_2ft.unit1.ca.util.Util;


public class ProductManager {
    private String filename;
    private CategoryManager categoryManager;
    private List<Product> productList;
    private Map<String, Product> productMap;
    private Map<Integer, Product> barcodeMap;
    private List<Product> understockProductList;
    private Map<String, Integer> maxSubIdMap;

    private final List<ProductCatalogListener> listeners = new ArrayList<>();

    public void addListener(ProductCatalogListener listener) {
        listeners.add(Objects.requireNonNull(listener, "listener"));
    }

    public void removeListener(ProductCatalogListener listener) {
        listeners.remove(listener);
    }

    public List<Product> getUnderstockProducts() {
        return Collections.unmodifiableList(understockProductList);
    }

    public ProductManager(String filename, CategoryManager categoryManager) {
        this.filename = filename;
        this.categoryManager = categoryManager;

        productList = new ArrayList<>();
        productMap = new HashMap<>();
        barcodeMap = new HashMap<>();
        understockProductList = new ArrayList<>();
        maxSubIdMap = new HashMap<>();

        load();
    }

    private void load() {
        try (Stream<String> stream = Files.lines(Paths.get(filename))) {
            stream.map(Util::splitCsv).forEach(a -> {
                String id = a[0], name = a[1], description = a[2];
                double price = Util.parseDoubleOrDefault(a[4], 0);
                int quantity = Util.parseIntOrDefault(a[3], 0),
                        barcode = Util.parseIntOrDefault(a[5], 0),
                        threshold = Util.parseIntOrDefault(a[6], 0),
                        orderQuantity = Util.parseIntOrDefault(a[7], 0);

                Product product = new Product(name, description, quantity,
                        price, barcode, threshold, orderQuantity);

                //if id already exist, skip
                id = id != null ? id.trim() : null;
                if (productMap.containsKey(id)) return;

                if (barcodeMap.containsKey(barcode)) return;

                String[] idArray = id != null ? id.split("/") : new String[] {"", ""};
                String categoryId = idArray[0];
                Category category = categoryManager.getCategory(categoryId);

                //if category does not exist, skip
                if (category == null) return;

                int current = Integer.parseInt(idArray[1]);
                int max = maxSubIdMap.getOrDefault(idArray[0], 0);
                if (max < current) maxSubIdMap.put(categoryId, current);

                addProduct(category, product, id);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Product getProduct(String id) { return productMap.get(id); }

    public List<Product> getProductList() { return productList; }

    public void addProduct(Category category, Product product) {
        if (category == null)
            throw new IllegalArgumentException("Category is not valid");
        if (product == null)
            throw new IllegalArgumentException("Product is not valid");

        if (barcodeMap.containsKey(product.getBarcode()))
            throw new IllegalArgumentException("Bar code: " + product.getBarcode() + " already existed in the system");

        String categoryId = category.getId();
        if (categoryId == null)
            throw new IllegalArgumentException("Category ID is empty. Please input again");

        int subId = maxSubIdMap.getOrDefault(categoryId, 0) + 1;
        maxSubIdMap.put(categoryId, subId);
        String id = categoryId + '/' + subId;

        addProduct(category, product, id);

        store();
    }

    private void addProduct(Category category, Product product, String id) {
        //Check if bar code already existed
        product.setId(id);
        product.setCategory(category);
        productList.add(product);
        productMap.put(id, product);
        barcodeMap.put(product.getBarcode(), product);

        int productIndex = productList.size() - 1;
        int understockIndex = -1;
        if (product.isUnderstock()) {
            understockProductList.add(product);
            understockIndex = understockProductList.size() - 1;
        }
        for (ProductCatalogListener listener : new ArrayList<>(listeners))
            listener.productAdded(productIndex, understockIndex);
    }

    public void deductQuantity(String id, int quantity) {
        Product product = productMap.get(id);
        if (product == null)
            throw new IllegalArgumentException("No such product.");
        if (!product.deductQuantity(quantity))
            throw new IllegalArgumentException("Exceed inventory quantity.");

        int productIndex = productList.indexOf(product);
        int understockIndex = understockProductList.indexOf(product);
        boolean enteredUnderstock = understockIndex == -1 && product.isUnderstock();
        if (enteredUnderstock) {
            understockProductList.add(product);
            understockIndex = understockProductList.size() - 1;
        }
        for (ProductCatalogListener listener : new ArrayList<>(listeners))
            listener.quantityChanged(productIndex, understockIndex, enteredUnderstock);

        store();
    }

    public void generatePurchaseOrder(List<Integer> understockIndexList) {
        //No change to table, short circuit
        if (understockIndexList == null || understockIndexList.size() == 0) return;

        //Assume immediate stock arrival
        understockIndexList.forEach(i -> understockProductList.get(i).restock());
        understockProductList = productList.stream()
                .filter(Product::isUnderstock)
                .collect(Collectors.toList());

        for (ProductCatalogListener listener : new ArrayList<>(listeners))
            listener.inventoryRebuilt();

        store();
    }

    private void store() {
        Stream<String> stream = productList.stream()
                .sorted(Comparator.comparing(Product::getId))
                .map(Product::toString);

        try {
            Files.write(Paths.get(filename), (Iterable<String>) stream::iterator);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
