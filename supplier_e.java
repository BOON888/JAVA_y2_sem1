import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.util.*;

public class supplier_e extends JPanel {
    private JTextField nameField, contactField, emailField, addressField, itemField;
    private JTable supplierTable;
    private DefaultTableModel tableModel;
    private static final String FILE_NAME = "TXT/suppliers.txt"; // Your folder path
    private int currentId = 3000;

    public supplier_e() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(1280, 450));

        JPanel titlePanel = new JPanel();
        JLabel titleLabel = new JLabel("Supplier Management", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titlePanel.add(titleLabel);
        add(titlePanel, BorderLayout.NORTH);

        JPanel mainPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        mainPanel.add(createInputPanel());
        mainPanel.add(createTablePanel());
        add(mainPanel, BorderLayout.CENTER);

        loadSuppliers();
    }

    private JPanel createInputPanel() {
        JPanel inputPanel = new JPanel(new GridLayout(6, 2, 5, 5));
        inputPanel.setPreferredSize(new Dimension(280, 180));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Supplier Info"));

        nameField = new JTextField(15);
        contactField = new JTextField(15);
        emailField = new JTextField(15);
        addressField = new JTextField(15);
        itemField = new JTextField(15);

        inputPanel.add(new JLabel("Name:")); inputPanel.add(nameField);
        inputPanel.add(new JLabel("Contact No.:")); inputPanel.add(contactField);
        inputPanel.add(new JLabel("Email:")); inputPanel.add(emailField);
        inputPanel.add(new JLabel("Address:")); inputPanel.add(addressField);
        inputPanel.add(new JLabel("Item Name:")); inputPanel.add(itemField);

        JButton addButton = new JButton("Add");
        addButton.addActionListener(e -> addSupplier());
        inputPanel.add(addButton);

        return inputPanel;
    }

    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setPreferredSize(new Dimension(750, 220));
        tablePanel.setBorder(BorderFactory.createTitledBorder("Supplier List"));

        tableModel = new DefaultTableModel(new String[]{"ID", "Name", "Contact No.", "Email", "Address", "Item Name"}, 0);
        supplierTable = new JTable(tableModel);
        tablePanel.add(new JScrollPane(supplierTable), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton deleteButton = new JButton("Delete");
        deleteButton.addActionListener(e -> deleteSupplier());
        JButton editButton = new JButton("Edit");
        editButton.addActionListener(e -> editSupplier());

        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        tablePanel.add(buttonPanel, BorderLayout.SOUTH);

        return tablePanel;
    }

    private void addSupplier() {
        String name = nameField.getText().trim();
        String contact = contactField.getText().trim();
        String email = emailField.getText().trim();
        String address = addressField.getText().trim();
        String item = itemField.getText().trim();

        if (name.isEmpty() || contact.isEmpty() || email.isEmpty() || address.isEmpty() || item.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields must be filled", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        tableModel.addRow(new Object[]{currentId++, name, contact, email, address, item});
        saveSuppliers();
        clearInputFields();
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
            String name = tableModel.getValueAt(selectedRow, 1).toString();
            String contact = tableModel.getValueAt(selectedRow, 2).toString();
            String email = tableModel.getValueAt(selectedRow, 3).toString();
            String address = tableModel.getValueAt(selectedRow, 4).toString();
            String item = tableModel.getValueAt(selectedRow, 5).toString();

            supplier_v dialog = new supplier_v((JFrame) SwingUtilities.getWindowAncestor(this), name, contact, email, address, item);
            dialog.setVisible(true);

            if (dialog.isSaved()) {
                String[] newData = dialog.getEditedData();
                tableModel.setValueAt(newData[0], selectedRow, 1);
                tableModel.setValueAt(newData[1], selectedRow, 2);
                tableModel.setValueAt(newData[2], selectedRow, 3);
                tableModel.setValueAt(newData[3], selectedRow, 4);
                tableModel.setValueAt(newData[4], selectedRow, 5);
                saveSuppliers();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a supplier to edit", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearInputFields() {
        nameField.setText("");
        contactField.setText("");
        emailField.setText("");
        addressField.setText("");
        itemField.setText("");
    }

    private void saveSuppliers() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_NAME))) {
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                writer.println(
                        tableModel.getValueAt(i, 0) + "|" +
                        tableModel.getValueAt(i, 1) + "|" +
                        tableModel.getValueAt(i, 2) + "|" +
                        tableModel.getValueAt(i, 3) + "|" +
                        tableModel.getValueAt(i, 4) + "|" +
                        tableModel.getValueAt(i, 5));
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving suppliers: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadSuppliers() {
        File file = new File(FILE_NAME);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length == 6) {
                    int id = Integer.parseInt(parts[0]);
                    tableModel.addRow(parts);
                    currentId = Math.max(currentId, id + 1);
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error loading suppliers: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
