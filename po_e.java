import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.util.Vector;

public class po_e extends JPanel {
    private static final String PO_FILE = "TXT/po.txt";
    private JTabbedPane tabbedPane;
    private JTable poTable;
    private DefaultTableModel tableModel;
    private JTextField prIDField, supplierIDField, itemIDField, quantityField, orderDateField;
    private JComboBox<String> orderByDropdown, receivedByDropdown, approvedByDropdown, statusDropdown;
    private JButton addButton;

    public po_e() {
        setLayout(new BorderLayout());

        tabbedPane = new JTabbedPane(); // ✅ Tabbed interface for PO Info & PO List
        tabbedPane.addTab("PO Info", createPOInfoPanel());
        tabbedPane.addTab("PO List", createPOListPanel());

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createPOInfoPanel() {
        JPanel panel = new JPanel(new GridLayout(10, 2, 10, 10));

        panel.add(new JLabel("PR ID:"));
        prIDField = new JTextField(10);
        panel.add(prIDField);

        panel.add(new JLabel("Supplier ID:"));
        supplierIDField = new JTextField(10);
        panel.add(supplierIDField);

        panel.add(new JLabel("Item ID:"));
        itemIDField = new JTextField(10);
        panel.add(itemIDField);

        panel.add(new JLabel("Quantity Ordered:"));
        quantityField = new JTextField(10);
        panel.add(quantityField);

        panel.add(new JLabel("Order Date (DD-MM-YYYY):"));
        orderDateField = new JTextField(10);
        panel.add(orderDateField);

        panel.add(new JLabel("Order By:"));
        orderByDropdown = new JComboBox<>(new String[]{"Purchase Manager", "Administrator"});
        panel.add(orderByDropdown);

        panel.add(new JLabel("Received By:"));
        receivedByDropdown = new JComboBox<>(new String[]{"Inventory Manager"});
        panel.add(receivedByDropdown);

        panel.add(new JLabel("Approved By:"));
        approvedByDropdown = new JComboBox<>(new String[]{"Financial Manager"});
        panel.add(approvedByDropdown);

        panel.add(new JLabel("Status:"));
        statusDropdown = new JComboBox<>(new String[]{"Pending", "Approved", "Rejected"});
        panel.add(statusDropdown);

        addButton = new JButton("Add Purchase Order");
        addButton.addActionListener(e -> addPurchaseOrder());

        panel.add(new JLabel()); // Placeholder for spacing
        panel.add(addButton);

        return panel;
    }

    private JPanel createPOListPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        String[] columnNames = {"PO ID", "PR ID", "Item ID", "Supplier ID", "Quantity", "Order Date", "Order By", "Received By", "Approved By", "Status"};
        tableModel = new DefaultTableModel(columnNames, 0);
        poTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(poTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        loadPurchaseOrders(); // ✅ Load PO data from "po.txt"

        return panel;
    }

    private void loadPurchaseOrders() {
        try (BufferedReader reader = new BufferedReader(new FileReader(PO_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split("\\|");
                if (data.length == 10) {
                    tableModel.addRow(data);
                } else {
                    System.err.println("Error: Incorrect format in line -> " + line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error loading purchase orders.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addPurchaseOrder() {
        String prID = prIDField.getText().trim();
        String supplierID = supplierIDField.getText().trim();
        String itemID = itemIDField.getText().trim();
        String quantity = quantityField.getText().trim();
        String orderDate = orderDateField.getText().trim();
        String orderBy = orderByDropdown.getSelectedItem().toString();
        String receivedBy = receivedByDropdown.getSelectedItem().toString();
        String approvedBy = approvedByDropdown.getSelectedItem().toString();
        String status = statusDropdown.getSelectedItem().toString();

        if (prID.isEmpty() || supplierID.isEmpty() || itemID.isEmpty() || quantity.isEmpty() || orderDate.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Please fill all required fields!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String newPO = String.join("|", prID, supplierID, itemID, quantity, orderDate, orderBy, receivedBy, approvedBy, status);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(PO_FILE, true))) {
            writer.write(newPO + "\n"); // ✅ Append new PO to file
            JOptionPane.showMessageDialog(null, "Purchase Order added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadPurchaseOrders(); // ✅ Refresh PO List
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error saving purchase order.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}