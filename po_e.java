import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;

public class po_e extends JPanel {
    private static final String PO_FILE = "TXT/po.txt";
    private JTabbedPane tabbedPane;
    private JTable poTable;
    private DefaultTableModel tableModel;
    private JTextField prIDField, itemIDField, supplierIDField, quantityField, orderDateField;
    private JComboBox<String> orderByDropdown, receivedByDropdown, approvedByDropdown;
    private JButton addButton;

    public po_e() {
        setLayout(new BorderLayout());

        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("PO Info", createPOInfoPanel());
        tabbedPane.addTab("PO List", createPOListPanel());

        tabbedPane.setFont(new Font("Arial", Font.BOLD, 20));
        tabbedPane.setPreferredSize(new Dimension(800, 600));

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createPOInfoPanel() {
        JPanel panel = new JPanel(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        JLabel prIDLabel = new JLabel("PR ID:");
        prIDLabel.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(prIDLabel, gbc);

        gbc.gridx = 1;
        prIDField = new JTextField(15);
        panel.add(prIDField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        JLabel itemIDLabel = new JLabel("Item ID:");
        itemIDLabel.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(itemIDLabel, gbc);

        gbc.gridx = 1;
        itemIDField = new JTextField(15);
        panel.add(itemIDField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        JLabel supplierIDLabel = new JLabel("Supplier ID:");
        supplierIDLabel.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(supplierIDLabel, gbc);

        gbc.gridx = 1;
        supplierIDField = new JTextField(15);
        panel.add(supplierIDField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        JLabel quantityLabel = new JLabel("Quantity Ordered:");
        quantityLabel.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(quantityLabel, gbc);

        gbc.gridx = 1;
        quantityField = new JTextField(15);
        panel.add(quantityField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        JLabel orderDateLabel = new JLabel("Order Date (DD-MM-YYYY):");
        orderDateLabel.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(orderDateLabel, gbc);

        gbc.gridx = 1;
        orderDateField = new JTextField(15);
        panel.add(orderDateField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        JLabel orderByLabel = new JLabel("Order By:");
        orderByLabel.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(orderByLabel, gbc);

        gbc.gridx = 1;
        orderByDropdown = new JComboBox<>(new String[]{"Purchase Manager", "Administrator"});
        panel.add(orderByDropdown, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        JLabel receivedByLabel = new JLabel("Received By:");
        receivedByLabel.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(receivedByLabel, gbc);

        gbc.gridx = 1;
        receivedByDropdown = new JComboBox<>(new String[]{"Inventory Manager"});
        panel.add(receivedByDropdown, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        JLabel approvedByLabel = new JLabel("Approved By:");
        approvedByLabel.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(approvedByLabel, gbc);

        gbc.gridx = 1;
        approvedByDropdown = new JComboBox<>(new String[]{"Financial Manager"});
        panel.add(approvedByDropdown, gbc);

        gbc.gridy++;
        addButton = new JButton("Add Purchase Order");
        addButton.setFont(new Font("Arial", Font.BOLD, 16));
        addButton.addActionListener(e -> addPurchaseOrder());
        panel.add(addButton, gbc);

        return panel;
    }

    private JPanel createPOListPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        String[] columnNames = {"PO ID", "PR ID", "Item ID", "Supplier ID", "Quantity", "Order Date", "Order By", "Received By", "Approved By", "Status"};
        tableModel = new DefaultTableModel(columnNames, 0);
        poTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(poTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        loadPurchaseOrders();

        return panel;
    }

    private void loadPurchaseOrders() {
        try (BufferedReader reader = new BufferedReader(new FileReader(PO_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split("\\|");
                if (data.length == 10) {
                    tableModel.addRow(data);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error loading purchase orders.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addPurchaseOrder() {
        String prID = prIDField.getText().trim();
        String itemID = itemIDField.getText().trim();
        String supplierID = supplierIDField.getText().trim();
        String quantity = quantityField.getText().trim();
        String orderDate = orderDateField.getText().trim();
        String orderBy = mapRoleToID(orderByDropdown.getSelectedItem().toString());
        String receivedBy = mapRoleToID(receivedByDropdown.getSelectedItem().toString());
        String approvedBy = mapRoleToID(approvedByDropdown.getSelectedItem().toString());
        String status = "Pending";

        int poID = generatePOID();

        if (prID.isEmpty() || itemID.isEmpty() || supplierID.isEmpty() || quantity.isEmpty() || orderDate.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Please fill all required fields!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String newPO = String.join("|", String.format("%04d", poID), prID, itemID, supplierID, quantity, orderDate, orderBy, receivedBy, approvedBy, status);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(PO_FILE, true))) {
            writer.write(newPO + "\n");
            JOptionPane.showMessageDialog(null, "Purchase Order added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadPurchaseOrders();
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error saving purchase order.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String mapRoleToID(String role) {
        switch (role) {
            case "Purchase Manager":
                return "1004";
            case "Inventory Manager":
                return "1003";
            case "Financial Manager":
                return "1002";
            default:
                return "Unknown";
        }
    }

    private int generatePOID() {
        int maxID = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(PO_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split("\\|");
                if (data.length > 0) {
                    try {
                        int currentID = Integer.parseInt(data[0]);
                        if (currentID > maxID) {
                            maxID = currentID;
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return maxID + 1;
    }
}