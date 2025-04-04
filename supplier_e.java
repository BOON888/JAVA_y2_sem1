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
        setPreferredSize(new Dimension(1280, 450));

        // Title Panel
        JPanel titlePanel = new JPanel();
        JLabel titleLabel = new JLabel("Supplier Management", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titlePanel.add(titleLabel);
        add(titlePanel, BorderLayout.NORTH);

        // Main Panel
        JPanel mainPanel = new JPanel(new GridLayout(1, 2, 10, 10));

        // Left Panel - Supplier Info
        JPanel inputPanel = createInputPanel();
        mainPanel.add(inputPanel);

        // Right Panel - Supplier List
        JPanel tablePanel = createTablePanel();
        mainPanel.add(tablePanel);

        add(mainPanel, BorderLayout.CENTER);
    }

    private JPanel createInputPanel() {
        JPanel inputPanel = new JPanel(new GridLayout(6, 2, 5, 5));
        inputPanel.setPreferredSize(new Dimension(280, 180));
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

        JButton addButton = new JButton("Add");
        addButton.setPreferredSize(new Dimension(100, 30));
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
        loadSuppliers();

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
            String id = tableModel.getValueAt(selectedRow, 0).toString();
            String name = tableModel.getValueAt(selectedRow, 1).toString();
            String contact = tableModel.getValueAt(selectedRow, 2).toString();
            String email = tableModel.getValueAt(selectedRow, 3).toString();
            String address = tableModel.getValueAt(selectedRow, 4).toString();
            String item = tableModel.getValueAt(selectedRow, 5).toString();

            supplier_v dialog = new supplier_v(
                (JFrame)SwingUtilities.getWindowAncestor(this),
                name, contact, email, address, item
            );
            dialog.setVisible(true);
            if (dialog.isSaved()) {
                String[] newData = dialog.getEditedData();
                tableModel.setValueAt(newData[0], selectedRow, 1); // name
                tableModel.setValueAt(newData[1], selectedRow, 2); // contact
                tableModel.setValueAt(newData[2], selectedRow, 3); // email
                tableModel.setValueAt(newData[3], selectedRow, 4); // address
                tableModel.setValueAt(newData[4], selectedRow, 5); // item
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
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                writer.write(tableModel.getValueAt(i, 0) + "|" +
                             tableModel.getValueAt(i, 1) + "|" +
                             tableModel.getValueAt(i, 2) + "|" +
                             tableModel.getValueAt(i, 3) + "|" +
                             tableModel.getValueAt(i, 4) + "|" +
                             tableModel.getValueAt(i, 5) + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving suppliers: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
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
            JOptionPane.showMessageDialog(this, "Error loading suppliers: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}