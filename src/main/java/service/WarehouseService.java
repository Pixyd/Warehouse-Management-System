package service;

import dao.ProductDao;
import dao.ProductDaoImpl;
import model.Product;
import util.SimpleLogger;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class WarehouseService {
    private final ProductDao productDao = new ProductDaoImpl();
    private final Map<Integer, Product> productCache = new ConcurrentHashMap<>();

    public WarehouseService() {
        try {
            loadCache();
        } catch (SQLException e) {
            SimpleLogger.error("Failed to load cache: " + e.getMessage());
        }
    }

    public void deleteProduct(int id) throws SQLException {
        productDao.delete(id);
        productCache.remove(id);
    }

    public void loadCache() throws SQLException {
        List<Product> all = productDao.findAll();
        productCache.clear();
        for (Product p : all) productCache.put(p.getProductId(), p);
    }

    public List<Product> listAll() {
        return new ArrayList<>(productCache.values());
    }

    public Product createProduct(Product p) throws SQLException {
        Product created = productDao.create(p);
        productCache.put(created.getProductId(), created);
        return created;
    }

    public void updateProduct(Product p) throws SQLException {
        productDao.update(p);
        productCache.put(p.getProductId(), p);
    }

    public void changeStock(int productId, int delta, String type, String note) throws SQLException {
        productDao.changeQuantity(productId, delta, type, note);
        productDao.findById(productId).ifPresent(prod -> productCache.put(productId, prod));
    }

    public List<Product> findLowStock() {
        List<Product> low = new ArrayList<>();
        for (Product p : productCache.values()) {
            if (p.getQuantity() <= p.getMinStock()) low.add(p);
        }
        return low;
    }

    public List<Product> searchByName(String query) {
        String q = query.toLowerCase(Locale.ROOT);
        List<Product> res = new ArrayList<>();
        for (Product p : productCache.values()) {
            if (p.getName().toLowerCase(Locale.ROOT).contains(q) ||
                    p.getSku().toLowerCase(Locale.ROOT).contains(q)) res.add(p);
        }
        return res;
    }
}
