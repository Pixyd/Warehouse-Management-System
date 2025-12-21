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

    // ✅ NEW: Track products already alerted for low stock
    private final Set<Integer> lowStockAlerted =
            ConcurrentHashMap.newKeySet();

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
        lowStockAlerted.remove(id); // cleanup if deleted
    }

    public void loadCache() throws SQLException {
        List<Product> all = productDao.findAll();
        productCache.clear();
        for (Product p : all) {
            productCache.put(p.getProductId(), p);
        }
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
    public boolean changeStock(int productId, int delta, String type, String note) throws SQLException {
        Product before = productCache.get(productId);
        int oldQty = (before != null) ? before.getQuantity() : Integer.MAX_VALUE;

        productDao.changeQuantity(productId, delta, type, note);

        Product updated = productDao.findById(productId).orElse(null);
        if (updated != null) {
            productCache.put(productId, updated);

            // 🔔 Detect threshold crossing
            if (oldQty > updated.getMinStock() &&
                    updated.getQuantity() <= updated.getMinStock()) {
                return true;   // LOW STOCK JUST TRIGGERED
            }
        }
        return false;
    }


    // 🔹 OLD method (unchanged, still usable)
    public List<Product> findLowStock() {
        List<Product> low = new ArrayList<>();
        for (Product p : productCache.values()) {
            if (p.getQuantity() <= p.getMinStock()) {
                low.add(p);
            }
        }
        return low;
    }

    // ✅ NEW method: returns ONLY newly low-stock items (no repeat alerts)
    public List<Product> findNewLowStockItems() {
        List<Product> result = new ArrayList<>();

        for (Product p : productCache.values()) {
            int id = p.getProductId();

            if (p.getQuantity() <= p.getMinStock()) {
                // show alert only once per low-stock event
                if (lowStockAlerted.add(id)) {
                    result.add(p);
                }
            } else {
                // stock restored → allow future alert
                lowStockAlerted.remove(id);
            }
        }
        return result;
    }

    public List<Product> searchByName(String query) {
        String q = query.toLowerCase(Locale.ROOT);
        List<Product> res = new ArrayList<>();
        for (Product p : productCache.values()) {
            if (p.getName().toLowerCase(Locale.ROOT).contains(q) ||
                    p.getSku().toLowerCase(Locale.ROOT).contains(q)) {
                res.add(p);
            }
        }
        return res;
    }
}
