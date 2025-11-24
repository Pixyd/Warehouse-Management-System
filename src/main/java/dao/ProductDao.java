package dao;

import model.Product;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface ProductDao {
    Product create(Product p) throws SQLException;
    Optional<Product> findById(int id) throws SQLException;
    Optional<Product> findBySku(String sku) throws SQLException;
    List<Product> findAll() throws SQLException;
    void update(Product p) throws SQLException;
    void delete(int id) throws SQLException;
    void changeQuantity(int productId, int delta, String txType, String note) throws SQLException;
}
