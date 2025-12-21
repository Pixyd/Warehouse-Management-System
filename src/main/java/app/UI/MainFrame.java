package app.UI;

import model.Product;
import service.WarehouseService;
import util.SimpleLogger;

import javax.swing.*;
import java.awt.BorderLayout;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;


public class MainFrame extends JFrame {

    private final WarehouseService service = new WarehouseService();
    private ProductPanel productPanel;

    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor();

    // 🔴 Tracks products already shown in low-stock alert
    private final Set<Integer> alertedLowStockIds =
            ConcurrentHashMap.newKeySet();

    public MainFrame() {
        setTitle("Warehouse Management System");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        productPanel = new ProductPanel(service);
        add(productPanel, BorderLayout.CENTER);

        // Check every 20 seconds
        scheduler.scheduleAtFixedRate(this::checkLowStock, 5, 20, TimeUnit.SECONDS);
    }

    private void checkLowStock() {
        try {
            List<Product> low = service.findLowStock();

            // 🔁 Reset alerts if stock becomes normal again
            Set<Integer> currentLowIds = new HashSet<>();
            for (Product p : low) {
                currentLowIds.add(p.getProductId());
            }
            alertedLowStockIds.retainAll(currentLowIds);

            // 🔔 Build alert ONLY for newly low products
            StringBuilder sb = new StringBuilder();
            for (Product p : low) {
                if (!alertedLowStockIds.contains(p.getProductId())) {
                    sb.append(p.getSku())
                            .append(" - ")
                            .append(p.getName())
                            .append(" qty=")
                            .append(p.getQuantity())
                            .append("\n");
                    alertedLowStockIds.add(p.getProductId());
                }
            }

            if (sb.length() > 0) {
                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(
                                this,
                                "Low stock items:\n" + sb,
                                "Low stock alert",
                                JOptionPane.WARNING_MESSAGE
                        )
                );
            }

        } catch (Exception e) {
            SimpleLogger.error("Low stock check failed: " + e.getMessage());
        }
    }
}
