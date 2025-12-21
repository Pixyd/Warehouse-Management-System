package app.UI;

import model.Product;
import service.WarehouseService;
import util.SimpleLogger;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
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

        tableModel = new DefaultTableModel(
                new String[]{"No.", "SKU", "Name", "Price", "Qty", "Min Stock", "_id"}, 0
        ) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setFillsViewportHeight(true);

        // Tooltip for DB id
        table.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int viewRow = table.rowAtPoint(e.getPoint());
                if (viewRow < 0) {
                    table.setToolTipText(null);
                    return;
                }
                int modelRow = table.convertRowIndexToModel(viewRow);
                Object val = tableModel.getValueAt(modelRow, 6);
                table.setToolTipText("DB id: " + val);
            }
        });

        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel();
        JButton btnAdd = new JButton("Add");
        JButton btnEdit = new JButton("Edit");
        JButton btnDelete = new JButton("Delete");
        JButton btnIn = new JButton("Receive");
        JButton btnOut = new JButton("Dispatch");

        btnAdd.addActionListener(e -> showAddDialog());
        btnEdit.addActionListener(e -> showEditDialog());
        btnDelete.addActionListener(e -> deleteSelected());
        btnIn.addActionListener(e -> changeStock(1));
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
                    refreshTable(get());
                } catch (Exception e) {
                    SimpleLogger.error("Load failed: " + e.getMessage());
                }
            }
        };
        w.execute();
    }

    private void refreshTable(List<Product> list) {
        tableModel.setRowCount(0);
        int idx = 1;
        for (Product p : list) {
            tableModel.addRow(new Object[]{
                    idx++,
                    p.getSku(),
                    p.getName(),
                    p.getPrice(),
                    p.getQuantity(),
                    p.getMinStock(),
                    p.getProductId()
            });
        }

        // Hide DB id column
        if (table.getColumnCount() > 6) {
            table.removeColumn(table.getColumnModel().getColumn(6));
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
                    SimpleLogger.error("Search failed");
                }
            }
        };
        w.execute();
    }

    private void showAddDialog() {
        ProductFormDialog dlg = new ProductFormDialog(null);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
        if (dlg.getProduct() != null) {
            try {
                service.createProduct(dlg.getProduct());
                loadProducts();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, e.getMessage());
            }
        }
    }

    private void showEditDialog() {
        int id = getSelectedProductId();
        if (id < 0) return;

        Product p = service.listAll().stream()
                .filter(x -> x.getProductId() == id)
                .findFirst().orElse(null);

        if (p == null) return;

        ProductFormDialog dlg = new ProductFormDialog(p);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);

        if (dlg.getProduct() != null) {
            try {
                service.updateProduct(dlg.getProduct());
                loadProducts();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, e.getMessage());
            }
        }
    }

    private int getSelectedProductId() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) return -1;
        int modelRow = table.convertRowIndexToModel(viewRow);
        return (int) tableModel.getValueAt(modelRow, 6);
    }

    private void deleteSelected() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) return;

        int modelRow = table.convertRowIndexToModel(viewRow);
        int dbId = (int) tableModel.getValueAt(modelRow, 6);
        Object no = tableModel.getValueAt(modelRow, 0);

        int ok = JOptionPane.showConfirmDialog(
                this,
                "Delete product No. " + no + " ?",
                "Confirm",
                JOptionPane.YES_NO_OPTION
        );

        if (ok != JOptionPane.YES_OPTION) return;

        try {
            service.deleteProduct(dbId);
            loadProducts();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    // =================== MODIFIED PART ===================
    private void changeStock(int sign) {
        int selectedDbId = getSelectedProductId();
        if (selectedDbId < 0) {
            JOptionPane.showMessageDialog(this, "Select a product");
            return;
        }

        String qtyStr = JOptionPane.showInputDialog(
                this,
                "Quantity to " + (sign > 0 ? "add" : "remove")
        );
        if (qtyStr == null) return;

        try {
            int q = Integer.parseInt(qtyStr);

            SwingWorker<Void, Void> w = new SwingWorker<>() {
                boolean lowTriggered = false;
                Exception error;

                @Override
                protected Void doInBackground() {
                    try {
                        lowTriggered = service.changeStock(
                                selectedDbId,
                                sign * q,
                                sign > 0 ? "receive" : "dispatch",
                                "UI operation"
                        );
                    } catch (Exception e) {
                        error = e;
                    }
                    return null;
                }

                @Override
                protected void done() {
                    if (error != null) {
                        JOptionPane.showMessageDialog(
                                ProductPanel.this,
                                error.getMessage(),
                                "Error",
                                JOptionPane.ERROR_MESSAGE
                        );
                        return;
                    }

                    loadProducts();

                    if (lowTriggered) {
                        JOptionPane.showMessageDialog(
                                ProductPanel.this,
                                "⚠ Stock has fallen below minimum level!",
                                "Low Stock Alert",
                                JOptionPane.WARNING_MESSAGE
                        );
                    }
                }
            };
            w.execute();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid number");
        }
    }
    // =====================================================
}