package dao;

import model.Product;
import util.SimpleLogger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductDaoImpl implements ProductDao {
    @Override
    public Product create(Product p) throws SQLException {
        String sql = "INSERT INTO product (sku,name,description,price,quantity,min_stock,supplier_id) VALUES (?,?,?,?,?,?,?)";
        try (Connection c = DbManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getSku());
            ps.setString(2, p.getName());
            ps.setString(3, p.getDescription());
            ps.setDouble(4, p.getPrice());
            ps.setInt(5, p.getQuantity());
            ps.setInt(6, p.getMinStock());
            if (p.getSupplierId()==null) ps.setNull(7, Types.INTEGER);
            else ps.setInt(7, p.getSupplierId());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) p.setProductId(rs.getInt(1));
            c.commit();
            SimpleLogger.info("Product created: " + p);
            return p;
        } catch (SQLException e) {
            SimpleLogger.error("Create product failed: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public Optional<Product> findById(int id) throws SQLException {
        String sql = "SELECT * FROM product WHERE product_id=?";
        try (Connection c = DbManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            Product p = null;
            if (rs.next()) {
                p = map(rs);
            }
            c.commit();
            return Optional.ofNullable(p);
        }
    }

    @Override
    public Optional<Product> findBySku(String sku) throws SQLException {
        String sql = "SELECT * FROM product WHERE sku=?";
        try (Connection c = DbManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, sku);
            ResultSet rs = ps.executeQuery();
            Product p = null;
            if (rs.next()) p = map(rs);
            c.commit();
            return Optional.ofNullable(p);
        }
    }

    @Override
    public List<Product> findAll() throws SQLException {
        String sql = "SELECT * FROM product ORDER BY name";
        try (Connection c = DbManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Product> list = new ArrayList<>();
            while (rs.next()) list.add(map(rs));
            c.commit();
            return list;
        }
    }

    @Override
    public void update(Product p) throws SQLException {
        String sql = "UPDATE product SET sku=?,name=?,description=?,price=?,quantity=?,min_stock=?,supplier_id=? WHERE product_id=?";
        try (Connection c = DbManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, p.getSku());
            ps.setString(2, p.getName());
            ps.setString(3, p.getDescription());
            ps.setDouble(4, p.getPrice());
            ps.setInt(5, p.getQuantity());
            ps.setInt(6, p.getMinStock());
            if (p.getSupplierId()==null) ps.setNull(7, Types.INTEGER);
            else ps.setInt(7, p.getSupplierId());
            ps.setInt(8, p.getProductId());
            ps.executeUpdate();
            c.commit();
            SimpleLogger.info("Product updated: " + p);
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM product WHERE product_id=?";
        try (Connection c = DbManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            c.commit();
        }
    }

    @Override
    public void changeQuantity(int productId, int delta, String txType, String note) throws SQLException {
        try (Connection c = DbManager.getConnection()) {
            int current;
            try (PreparedStatement ps = c.prepareStatement("SELECT quantity FROM product WHERE product_id=?")) {
                ps.setInt(1, productId);
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) throw new SQLException("Product not found");
                current = rs.getInt("quantity");
            }
            int updated = current + delta;
            if (updated < 0) throw new SQLException("Insufficient stock");
            try (PreparedStatement upd = c.prepareStatement("UPDATE product SET quantity=? WHERE product_id=?")) {
                upd.setInt(1, updated);
                upd.setInt(2, productId);
                upd.executeUpdate();
            }
            try (PreparedStatement tx = c.prepareStatement(
                    "INSERT INTO inventory_transaction (product_id, change, tx_type, note) VALUES (?,?,?,?)")) {
                tx.setInt(1, productId);
                tx.setInt(2, delta);
                tx.setString(3, txType);
                tx.setString(4, note);
                tx.executeUpdate();
            }
            c.commit();
            SimpleLogger.info("Quantity changed for product " + productId + " delta=" + delta);
        }
    }

    private Product map(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setProductId(rs.getInt("product_id"));
        p.setSku(rs.getString("sku"));
        p.setName(rs.getString("name"));
        p.setDescription(rs.getString("description"));
        p.setPrice(rs.getDouble("price"));
        p.setQuantity(rs.getInt("quantity"));
        p.setMinStock(rs.getInt("min_stock"));
        int sup = rs.getInt("supplier_id");
        if (rs.wasNull()) p.setSupplierId(null);
        else p.setSupplierId(sup);
        return p;
    }
}
