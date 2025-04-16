import java.io.Serializable;

public class Item implements Serializable {
    private String id;
    private String name;
    private String supplierId;
    private String supplierName; // New field for supplier name
    private String category;
    private double price;
    private int stockQuantity;

    public Item(String id, String name, String supplierId, String supplierName, String category, double price, int stockQuantity) {
        this.id = id;
        this.name = name;
        this.supplierId = supplierId;
        this.supplierName = supplierName;
        this.category = category;
        this.price = price;
        this.stockQuantity = stockQuantity;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getSupplierId() { return supplierId; }
    public String getSupplierName() { return supplierName; }
    public String getCategory() { return category; }
    public double getPrice() { return price; }
    public int getStockQuantity() { return stockQuantity; }

    // Setters
    public void setName(String name) { this.name = name; }
    public void setSupplierId(String supplierId) { this.supplierId = supplierId; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public void setCategory(String category) { this.category = category; }
    public void setPrice(double price) { this.price = price; }
    public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }

    @Override
    public String toString() {
        return id + "|" + name + "|" + supplierId + "|" + supplierName + "|" + category + "|" + price + "|" + stockQuantity;
    }
}