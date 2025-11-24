package app;

import app.UI.MainFrame;
import dao.DbManager;
import util.SimpleLogger;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SimpleLogger.info("Starting Warehouse Management System");
        try {
            DbManager.initDatabase();
        } catch (Exception e) {
            SimpleLogger.error("DB init failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
