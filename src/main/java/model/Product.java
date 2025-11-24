package model;

public class Product {
    private int productId;
    private String sku;
    private String name;
    private String description;
    private double price;
    private int quantity;
    private int minStock;
    private Integer supplierId;

    public Product() {}

    public Product(String sku, String name, double price, int quantity, int minStock) {
        this.sku = sku;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.minStock = minStock;
    }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public int getMinStock() { return minStock; }
    public void setMinStock(int minStock) { this.minStock = minStock; }
    public Integer getSupplierId() { return supplierId; }
    public void setSupplierId(Integer supplierId) { this.supplierId = supplierId; }

    @Override
    public String toString() {
        return sku + " - " + name;
    }
}
