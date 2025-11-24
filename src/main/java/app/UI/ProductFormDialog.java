package app.UI;

import model.Product;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ProductFormDialog extends JDialog {
    private JTextField txtSku = new JTextField(20);
    private JTextField txtName = new JTextField(20);
    private JTextArea txtDesc = new JTextArea(4,20);
    private JTextField txtPrice = new JTextField(10);
    private JTextField txtQty = new JTextField(6);
    private JTextField txtMin = new JTextField(6);
    private Product product;
    private boolean submitted = false;

    public ProductFormDialog(Product p) {
        setModal(true);
        setTitle(p==null? "Add Product" : "Edit Product");
        setSize(400,350);
        setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4,4,4,4);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0; gbc.gridy = 0;
        form.add(new JLabel("SKU:"), gbc);
        gbc.gridx = 1; form.add(txtSku, gbc);

        gbc.gridx = 0; gbc.gridy++;
        form.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1; form.add(txtName, gbc);

        gbc.gridx = 0; gbc.gridy++;
        form.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1; form.add(new JScrollPane(txtDesc), gbc);

        gbc.gridx = 0; gbc.gridy++;
        form.add(new JLabel("Price:"), gbc);
        gbc.gridx = 1; form.add(txtPrice, gbc);

        gbc.gridx = 0; gbc.gridy++;
        form.add(new JLabel("Quantity:"), gbc);
        gbc.gridx = 1; form.add(txtQty, gbc);

        gbc.gridx = 0; gbc.gridy++;
        form.add(new JLabel("Min Stock:"), gbc);
        gbc.gridx = 1; form.add(txtMin, gbc);

        add(form, BorderLayout.CENTER);

        JPanel buttons = new JPanel();
        JButton ok = new JButton("OK");
        JButton cancel = new JButton("Cancel");
        buttons.add(ok); buttons.add(cancel);
        add(buttons, BorderLayout.SOUTH);

        if (p!=null) {
            product = p;
            txtSku.setText(p.getSku());
            txtName.setText(p.getName());
            txtDesc.setText(p.getDescription());
            txtPrice.setText(String.valueOf(p.getPrice()));
            txtQty.setText(String.valueOf(p.getQuantity()));
            txtMin.setText(String.valueOf(p.getMinStock()));
        }

        ok.addActionListener(a -> {
            if (!validateInput()) return;
            if (product==null) product = new Product();
            product.setSku(txtSku.getText().trim());
            product.setName(txtName.getText().trim());
            product.setDescription(txtDesc.getText().trim());
            product.setPrice(Double.parseDouble(txtPrice.getText().trim()));
            product.setQuantity(Integer.parseInt(txtQty.getText().trim()));
            product.setMinStock(Integer.parseInt(txtMin.getText().trim()));
            submitted = true;
            setVisible(false);
            dispose();
        });

        cancel.addActionListener(a -> {
            submitted = false;
            product = null;
            setVisible(false);
            dispose();
        });

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                submitted = false;
                product = null;
            }
        });
    }

    private boolean validateInput() {
        if (txtSku.getText().trim().isEmpty()) { showError("SKU required"); return false; }
        if (txtName.getText().trim().isEmpty()) { showError("Name required"); return false; }
        try {
            Double.parseDouble(txtPrice.getText().trim());
        } catch (NumberFormatException e) { showError("Invalid price"); return false; }
        try {
            Integer.parseInt(txtQty.getText().trim());
        } catch (NumberFormatException e) { showError("Invalid quantity"); return false; }
        try {
            Integer.parseInt(txtMin.getText().trim());
        } catch (NumberFormatException e) { showError("Invalid min stock"); return false; }
        return true;
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Validation", JOptionPane.ERROR_MESSAGE);
    }

    public Product getProduct() {
        return submitted ? product : null;
    }
}
