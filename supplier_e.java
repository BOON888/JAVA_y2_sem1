import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

public class supplier_e extends JPanel {
    private JTextField nameField, contactField, emailField, addressField, itemField;
    private JTable supplierTable;
    private DefaultTableModel tableModel;
    private static final String FILE_NAME = "TXT/suppliers.txt";
    private int currentId = 3000;

    public supplier_e() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(1280, 450)); // Reduced panel height

        // Title Panel
        JPanel titlePanel = new JPanel();
        JLabel titleLabel = new JLabel("Supplier Management", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titlePanel.add(titleLabel);
        add(titlePanel, BorderLayout.NORTH);

        // Main Panel
        JPanel mainPanel = new JPanel(new GridLayout(1, 2, 10, 10));

        // Left Panel - Supplier Info
        JPanel inputPanel = new JPanel(new GridLayout(6, 2, 5, 5));
        inputPanel.setPreferredSize(new Dimension(280, 180)); // Reduced input panel size
        inputPanel.setBorder(BorderFactory.createTitledBorder("Supplier Info"));
        inputPanel.add(new JLabel("Name:"));
        nameField = new JTextField(15);
        inputPanel.add(nameField);
        inputPanel.add(new JLabel("Contact No.:"));
        contactField = new JTextField(15);
        inputPanel.add(contactField);
        inputPanel.add(new JLabel("Email:"));
        emailField = new JTextField(15);
        inputPanel.add(emailField);
        inputPanel.add(new JLabel("Address:"));
        addressField = new JTextField(15);
        inputPanel.add(addressField);
        inputPanel.add(new JLabel("Item Name:"));
        itemField = new JTextField(15);
        inputPanel.add(itemField);

        // Add button with reduced height
        JButton addButton = new JButton("Add");
        addButton.setPreferredSize(new Dimension(100, 30)); // Reduced button height
        inputPanel.add(addButton);

        // Right Panel - Supplier List
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setPreferredSize(new Dimension(750, 220)); // Adjusted table panel size
        tablePanel.setBorder(BorderFactory.createTitledBorder("Supplier List"));
        tableModel = new DefaultTableModel(new String[]{"ID", "Name", "Contact No.", "Email", "Address", "Item Name"}, 0);
        supplierTable = new JTable(tableModel);
        loadSuppliers();
        tablePanel.add(new JScrollPane(supplierTable), BorderLayout.CENTER);

        // Buttons Panel
        JPanel buttonPanel = new JPanel();
        JButton deleteButton = new JButton("Delete");
        JButton editButton = new JButton("Edit");
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        tablePanel.add(buttonPanel, BorderLayout.SOUTH);

        mainPanel.add(inputPanel);
        mainPanel.add(tablePanel);
        add(mainPanel, BorderLayout.CENTER);

        // Action listeners
        addButton.addActionListener(e -> addSupplier());
        deleteButton.addActionListener(e -> deleteSupplier());
        editButton.addActionListener(e -> editSupplier());
    }

    private void addSupplier() {
        String name = nameField.getText();
        String contact = contactField.getText();
        String email = emailField.getText();
        String address = addressField.getText();
        String item = itemField.getText();

        if (name.isEmpty() || contact.isEmpty() || email.isEmpty() || address.isEmpty() || item.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields must be filled", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        tableModel.addRow(new Object[]{currentId++, name, contact, email, address, item});
        saveSuppliers();

        nameField.setText("");
        contactField.setText("");
        emailField.setText("");
        addressField.setText("");
        itemField.setText("");
    }

    private void deleteSupplier() {
        int selectedRow = supplierTable.getSelectedRow();
        if (selectedRow != -1) {
            tableModel.removeRow(selectedRow);
            saveSuppliers();
        } else {
            JOptionPane.showMessageDialog(this, "Please select a supplier to delete", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editSupplier() {
        int selectedRow = supplierTable.getSelectedRow();
        if (selectedRow != -1) {
            // Gather the selected row data
            String name = tableModel.getValueAt(selectedRow, 1).toString();
            String contact = tableModel.getValueAt(selectedRow, 2).toString();
            String email = tableModel.getValueAt(selectedRow, 3).toString();
            String address = tableModel.getValueAt(selectedRow, 4).toString();
            String item = tableModel.getValueAt(selectedRow, 5).toString();

            // Create a new edit dialog window
            EditSupplierDialog dialog = new EditSupplierDialog(name, contact, email, address, item);
            dialog.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "Please select a supplier to edit", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveSuppliers() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                writer.write(tableModel.getValueAt(i, 0) + "|" + tableModel.getValueAt(i, 1) + "|" +
                        tableModel.getValueAt(i, 2) + "|" + tableModel.getValueAt(i, 3) + "|" +
                        tableModel.getValueAt(i, 4) + "|" + tableModel.getValueAt(i, 5) + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadSuppliers() {
        File file = new File(FILE_NAME);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split("\\|");
                if (data.length == 6) {
                    int id = Integer.parseInt(data[0]);
                    tableModel.addRow(data);
                    currentId = Math.max(currentId, id + 1);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // EditSupplierDialog class for editing supplier info
    private static class EditSupplierDialog extends JDialog {
        private JTextField nameField, contactField, emailField, addressField, itemField;

        public EditSupplierDialog(String name, String contact, String email, String address, String item) {
            setTitle("Edit Supplier");
            setLayout(new GridLayout(6, 2, 5, 5));
            setSize(300, 200);

            nameField = new JTextField(name);
            contactField = new JTextField(contact);
            emailField = new JTextField(email);
            addressField = new JTextField(address);
            itemField = new JTextField(item);

            add(new JLabel("Name:"));
            add(nameField);
            add(new JLabel("Contact No.:"));
            add(contactField);
            add(new JLabel("Email:"));
            add(emailField);
            add(new JLabel("Address:"));
            add(addressField);
            add(new JLabel("Item Name:"));
            add(itemField);

            JButton saveButton = new JButton("Save");
            saveButton.addActionListener(e -> {
                // Here you can update the supplier's information in the file or the table
                dispose();  // Close the dialog after saving
            });
            add(saveButton);

            setLocationRelativeTo(null);
        }
    }
}
