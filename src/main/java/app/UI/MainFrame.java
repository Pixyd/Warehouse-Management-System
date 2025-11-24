package app.UI;

import model.Product;
import service.WarehouseService;
import util.SimpleLogger;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.concurrent.*;

public class MainFrame extends JFrame {
    private WarehouseService service = new WarehouseService();
    private ProductPanel productPanel;

    private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public MainFrame() {
        setTitle("Warehouse Management System");
        setSize(900,600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        productPanel = new ProductPanel(service);
        add(productPanel, BorderLayout.CENTER);

        scheduler.scheduleAtFixedRate(this::checkLowStock, 5, 20, TimeUnit.SECONDS);
    }

    private void checkLowStock() {
        try {
            List<Product> low = service.findLowStock();
            if (!low.isEmpty()) {
                SwingUtilities.invokeLater(() -> {
                    StringBuilder sb = new StringBuilder("Low stock items:\n");
                    for (Product p : low) sb.append(p.getSku()).append(" - ").append(p.getName())
                            .append(" qty=").append(p.getQuantity()).append("\n");
                    JOptionPane.showMessageDialog(this, sb.toString(), "Low stock alert", JOptionPane.WARNING_MESSAGE);
                });
            }
        } catch (Exception e) {
            SimpleLogger.error("Low stock check failed: " + e.getMessage());
        }
    }
}
