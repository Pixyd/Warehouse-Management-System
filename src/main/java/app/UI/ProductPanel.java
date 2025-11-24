package app.UI;

import model.Product;
import service.WarehouseService;
import util.SimpleLogger;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.List;

public class ProductPanel extends JPanel {
    private final WarehouseService service;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;

    public ProductPanel(WarehouseService service) {
        this.service = service;
        setLayout(new BorderLayout());
        initComponents();
        loadProducts();
    }

    private void initComponents() {
        JPanel top = new JPanel(new BorderLayout());
        txtSearch = new JTextField();
        JButton btnSearch = new JButton("Search");
        btnSearch.addActionListener(e -> doSearch());
        top.add(txtSearch, BorderLayout.CENTER);
        top.add(btnSearch, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        // Note: last column "_id" holds the real DB product_id and will be hidden in the view
        tableModel = new DefaultTableModel(new String[]{"No.", "SKU", "Name", "Price", "Qty", "Min Stock", "_id"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setFillsViewportHeight(true);

        // Add tooltip that shows DB id when hovering rows (safe: checks model size)
        table.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int viewRow = table.rowAtPoint(e.getPoint());
                if (viewRow < 0) {
                    table.setToolTipText(null);
                    return;
                }
                int modelRow = table.convertRowIndexToModel(viewRow);
                if (tableModel.getColumnCount() <= 6) {
                    table.setToolTipText(null);
                    return;
                }
                Object val = tableModel.getValueAt(modelRow, 6); // model column 6 holds productId
                table.setToolTipText("DB id: " + val);
            }
        });

        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel();
        JButton btnAdd = new JButton("Add");
        btnAdd.addActionListener(e -> showAddDialog());
        JButton btnEdit = new JButton("Edit");
        btnEdit.addActionListener(e -> showEditDialog());
        JButton btnDelete = new JButton("Delete");
        btnDelete.addActionListener(e -> deleteSelected());
        JButton btnIn = new JButton("Receive");
        btnIn.addActionListener(e -> changeStock(1));
        JButton btnOut = new JButton("Dispatch");
        btnOut.addActionListener(e -> changeStock(-1));
        bottom.add(btnAdd);
        bottom.add(btnEdit);
        bottom.add(btnDelete);
        bottom.add(btnIn);
        bottom.add(btnOut);
        add(bottom, BorderLayout.SOUTH);
    }

    private void loadProducts() {
        SwingWorker<List<Product>, Void> w = new SwingWorker<>() {
            @Override
            protected List<Product> doInBackground() throws Exception {
                return service.listAll();
            }

            @Override
            protected void done() {
                try {
                    List<Product> list = get();
                    refreshTable(list);
                } catch (Exception e) {
                    SimpleLogger.error("Load products failed: " + e.getMessage());
                }
            }
        };
        w.execute();
    }

    // Shows a visible index (1..N) and stores DB id in hidden model column
    private void refreshTable(List<Product> list) {
        tableModel.setRowCount(0);
        int idx = 1;
        for (Product p : list) {
            tableModel.addRow(new Object[]{
                    idx++,               // visible number
                    p.getSku(),
                    p.getName(),
                    p.getPrice(),
                    p.getQuantity(),
                    p.getMinStock(),
                    p.getProductId()     // hidden DB id in the model
            });
        }

        // Hide the last (productId) column in the view so UI shows only the numbered index.
        // Must run after model populated. We check counts to avoid exceptions.
        if (tableModel.getColumnCount() >= 7 && table.getColumnCount() > 6) {
            try {
                // remove view column index 6 (the 7th) - model still has it
                table.removeColumn(table.getColumnModel().getColumn(6));
            } catch (Exception ignored) {
                // already removed or something else; safe to ignore
            }
        }

        // Ensure header shows "No." as the first column
        if (table.getColumnCount() > 0) {
            try {
                table.getColumnModel().getColumn(0).setHeaderValue("No.");
                table.getTableHeader().repaint();
            } catch (Exception ignored) {}
        }
    }

    private void doSearch() {
        String q = txtSearch.getText().trim();
        if (q.isEmpty()) {
            loadProducts();
            return;
        }
        SwingWorker<List<Product>, Void> w = new SwingWorker<>() {
            @Override
            protected List<Product> doInBackground() {
                return service.searchByName(q);
            }

            @Override
            protected void done() {
                try {
                    refreshTable(get());
                } catch (Exception e) {
                    SimpleLogger.error("Search failed: " + e.getMessage());
                }
            }
        };
        w.execute();
    }

    private void showAddDialog() {
        ProductFormDialog dlg = new ProductFormDialog(null);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
        Product p = dlg.getProduct();
        if (p != null) {
            SwingWorker<Void, Void> w = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    service.createProduct(p);
                    return null;
                }

                @Override
                protected void done() {
                    loadProducts();
                }
            };
            w.execute();
        }
    }

    private void showEditDialog() {
        int selectedDbId = getSelectedProductId();
        if (selectedDbId < 0) return;

        SwingWorker<Void, Void> w = new SwingWorker<>() {
            Product p;

            @Override
            protected Void doInBackground() throws Exception {
                // fetch latest product from service / DAO
                for (Product pr : service.listAll())
                    if (pr.getProductId() == selectedDbId) {
                        p = pr;
                        break;
                    }
                return null;
            }

            @Override
            protected void done() {
                try {
                    if (p == null) return;
                    ProductFormDialog dlg = new ProductFormDialog(p);
                    dlg.setLocationRelativeTo(ProductPanel.this);
                    dlg.setVisible(true);
                    Product updated = dlg.getProduct();
                    if (updated != null) {
                        SwingWorker<Void, Void> w2 = new SwingWorker<>() {
                            @Override
                            protected Void doInBackground() throws Exception {
                                service.updateProduct(updated);
                                return null;
                            }

                            @Override
                            protected void done() {
                                loadProducts();
                            }
                        };
                        w2.execute();
                    }
                } catch (Exception ex) {
                    SimpleLogger.error("Edit dialog failed: " + ex.getMessage());
                }
            }
        };
        w.execute();
    }

    // helper to get DB product id from the selected row. returns -1 if none selected or not available.
    private int getSelectedProductId() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) return -1;
        int modelRow = table.convertRowIndexToModel(viewRow); // safe with sorting/filtering
        if (tableModel.getColumnCount() <= 6) return -1;
        Object val = tableModel.getValueAt(modelRow, 6); // model column 6 holds productId
        if (val instanceof Integer) return (Integer) val;
        if (val instanceof Number) return ((Number) val).intValue();
        try {
            return Integer.parseInt(String.valueOf(val));
        } catch (Exception e) {
            return -1;
        }
    }

    // ======= deleteSelected() — shows visible No. in dialog, deletes by DB id (safe) =======
    private void deleteSelected() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) return;

        int modelRow = table.convertRowIndexToModel(viewRow);

        // Safe: check model has enough columns before reading
        if (tableModel.getColumnCount() <= 6) {
            JOptionPane.showMessageDialog(this, "Data not loaded yet", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // The visible "No." value is stored at model column 0
        Object noVal = tableModel.getValueAt(modelRow, 0);
        String visibleNumber = (noVal != null) ? String.valueOf(noVal) : String.valueOf(viewRow + 1);

        // Get the real DB id (stored in model column 6)
        Object idVal = tableModel.getValueAt(modelRow, 6);
        int selectedDbId;
        if (idVal instanceof Number) selectedDbId = ((Number) idVal).intValue();
        else {
            try {
                selectedDbId = Integer.parseInt(String.valueOf(idVal));
            } catch (Exception ex) {
                selectedDbId = -1;
            }
        }
        if (selectedDbId < 0) {
            JOptionPane.showMessageDialog(this, "Cannot determine product id", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int ok = JOptionPane.showConfirmDialog(this,
                "Delete product No. " + visibleNumber + " ?",
                "Confirm", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;

        // make final copy for inner SwingWorker
        final int idToDelete = selectedDbId;

        SwingWorker<Void, Void> w = new SwingWorker<>() {
            Exception error = null;

            @Override
            protected Void doInBackground() {
                try {
                    service.deleteProduct(idToDelete); // use final local var
                } catch (java.sql.SQLException e) {
                    error = e;
                    SimpleLogger.error("SQL error during delete: " + e.getMessage());
                } catch (Exception e) {
                    error = e;
                    SimpleLogger.error("Unexpected error during delete: " + e.getMessage());
                }
                return null;
            }

            @Override
            protected void done() {
                if (error != null) {
                    JOptionPane.showMessageDialog(ProductPanel.this,
                            "Failed to delete product: " + error.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    loadProducts();
                }
            }
        };
        w.execute();
    }
    // =========================================================================

    private void changeStock(int sign) {
        int selectedDbId = getSelectedProductId();
        if (selectedDbId < 0) { JOptionPane.showMessageDialog(this, "Select a product"); return; }

        String qtyStr = JOptionPane.showInputDialog(this, "Quantity to " + (sign > 0 ? "add" : "remove"));
        if (qtyStr == null) return;
        try {
            int q = Integer.parseInt(qtyStr);
            SwingWorker<Void, Void> w = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    service.changeStock(selectedDbId, sign * q, sign > 0 ? "receive" : "dispatch", "UI operation");
                    return null;
                }

                @Override
                protected void done() {
                    loadProducts();
                }
            };
            w.execute();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid number");
        }
    }
}