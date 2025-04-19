import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class finance_e extends JPanel {

    private static final String INVENTORY_FILE = "TXT/inventory.txt";
    private static final String ITEMS_FILE = "TXT/items.txt";
    private static final String STATUS_PENDING = "Pending";
    private static final String STATUS_DELETED = "Deleted";

    private JTabbedPane tabbedPane;
    private JPanel financeInfoPanel;
    private JPanel financeListPanel;

    private JComboBox<String> poIdInfoComboBox;
    private JTextField paymentStatusInfoField;
    private JTextField paymentDateInfoField;
    private JTextField amountInfoField;
    private JButton addButton;

    private DefaultTableModel financeListTableModelTop = new DefaultTableModel(new Object[]{"Finance ID - (Status)", "Actions"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return column == 1;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return (columnIndex == 1) ? ButtonPanel.class : super.getColumnClass(columnIndex);
        }
    };
    private JTable financeListTableTop;
    private JScrollPane financeListScrollPaneTop;

    private JTextField financeIdUpdateField;
    private JComboBox<String> poIdUpdateComboBox;
    private JTextField paymentStatusUpdateField;
    private JTextField paymentDateUpdateField;
    private JTextField amountUpdateField;
    private JTextField updateByUpdateField;
    private JTextField statusUpdateField;
    private JButton updateButton;

    private List<InventoryRecord> financeRecords = new ArrayList<>();
    private Map<String, ItemDetails> itemDetailsMap = new HashMap<>();

    public finance_e() {
        this(new ArrayList<>(), new HashMap<>());
    }

    public finance_e(List<InventoryRecord> financeRecords, Map<String, ItemDetails> itemDetailsMap) {
        this.financeRecords = financeRecords;
        this.itemDetailsMap = itemDetailsMap;
        loadItemDetailsForDropdown();
        setLayout(new BorderLayout());

        tabbedPane = new JTabbedPane();
        Font tabTitleFont = new Font("Arial", Font.BOLD, 20);
        tabbedPane.setFont(tabTitleFont);

        financeInfoPanel = createFinanceInfoPanel();
        tabbedPane.addTab("Finance Info", financeInfoPanel);

        financeListPanel = createFinanceListPanel();
        tabbedPane.addTab("Finance List", financeListPanel);

        add(tabbedPane, BorderLayout.CENTER);

        loadFinanceData();
        populateFinanceListTableTop();
    }

    private JPanel createFinanceInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;

        JLabel poIdLabel = new JLabel("PO ID:");
        DefaultComboBoxModel<String> poIdComboBoxModel = new DefaultComboBoxModel<>();
        for (Map.Entry<String, ItemDetails> entry : itemDetailsMap.entrySet()) {
            ItemDetails details = entry.getValue();
            poIdComboBoxModel.addElement(String.format("ID: %s \u00A0(Item: %s - %s)", details.getItemId(), details.getItemName(), details.getCategory()));
        }
        poIdInfoComboBox = new JComboBox<>(poIdComboBoxModel);
        poIdInfoComboBox.setMaximumRowCount(15);

        JLabel paymentStatusLabel = new JLabel("Payment Status:");
        paymentStatusInfoField = new JTextField(15);

        JLabel paymentDateLabel = new JLabel("Payment Date:");
        paymentDateInfoField = new JTextField(15);

        JLabel amountLabel = new JLabel("Amount:");
        amountInfoField = new JTextField(15);

        addButton = new JButton("Add");

        gbc.gridx = 0;
        gbc.gridy = 0;
        inputPanel.add(poIdLabel, gbc);
        gbc.gridx = 1;
        inputPanel.add(poIdInfoComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        inputPanel.add(paymentStatusLabel, gbc);
        gbc.gridx = 1;
        inputPanel.add(paymentStatusInfoField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        inputPanel.add(paymentDateLabel, gbc);
        gbc.gridx = 1;
        inputPanel.add(paymentDateInfoField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        inputPanel.add(amountLabel, gbc);
        gbc.gridx = 1;
        inputPanel.add(amountInfoField, gbc);

        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.EAST;
        inputPanel.add(addButton, gbc);

        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedItem = (String) poIdInfoComboBox.getSelectedItem();
                if (selectedItem == null || selectedItem.isEmpty()) {
                    JOptionPane.showMessageDialog(finance_e.this, "Please select a PO ID", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String poId = selectedItem.split(" ")[1];
                String paymentStatus = paymentStatusInfoField.getText().trim();
                String paymentDate = paymentDateInfoField.getText().trim();
                String amountText = amountInfoField.getText().trim();

                if (paymentStatus.isEmpty() || paymentDate.isEmpty() || amountText.isEmpty()) {
                    JOptionPane.showMessageDialog(finance_e.this, "Please fill in all fields", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try {
                    double amount = Double.parseDouble(amountText);
                    String financeId = generateNewFinanceId();
                    
                    InventoryRecord newRecord = new InventoryRecord(
                            financeId,
                            poId,
                            0, // stockLevel not used
                            paymentDate,
                            0, // reorderQuantity not used
                            Integer.parseInt(login_c.currentUserId),
                            paymentStatus
                    );
                    
                    financeRecords.add(newRecord);
                    saveFinanceData();
                    populateFinanceListTableTop();
                    
                    poIdInfoComboBox.setSelectedIndex(-1);
                    paymentStatusInfoField.setText("");
                    paymentDateInfoField.setText("");
                    amountInfoField.setText("");
                    
                    JOptionPane.showMessageDialog(finance_e.this, "Finance record added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(finance_e.this, "Invalid amount format", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        panel.add(inputPanel, BorderLayout.NORTH);
        return panel;
    }

    private JPanel createFinanceListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 20, 0, 20));

        financeListTableTop = new JTable(financeListTableModelTop);
        financeListTableTop.getColumn("Actions").setCellRenderer(new ButtonRenderer());
        financeListTableTop.getColumn("Actions").setCellEditor(new ButtonEditor(financeListTableTop));
        financeListScrollPaneTop = new JScrollPane(financeListTableTop);
        financeListScrollPaneTop.setMinimumSize(new Dimension(financeListScrollPaneTop.getMinimumSize().width, 50));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(financeListScrollPaneTop, BorderLayout.CENTER);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(createFinanceListBottomPanel(), BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createFinanceListBottomPanel() {
        JPanel bottomPanel = new JPanel(new GridBagLayout());
        TitledBorder titledBorder = BorderFactory.createTitledBorder("Finance Details");
        LineBorder topBorder = new LineBorder(Color.BLACK, 2);

        bottomPanel.setBorder(new CompoundBorder(
                new EmptyBorder(0, 0, 10, 0),
                new CompoundBorder(topBorder, new EmptyBorder(5, 0, 5, 0))
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;

        JLabel financeIdLabel = new JLabel("Finance ID:");
        financeIdUpdateField = new JTextField(15);
        financeIdUpdateField.setEditable(false);

        JLabel poIdLabelUpdate = new JLabel("PO ID:");
        DefaultComboBoxModel<String> poIdUpdateComboBoxModel = new DefaultComboBoxModel<>();
        for (Map.Entry<String, ItemDetails> entry : itemDetailsMap.entrySet()) {
            ItemDetails details = entry.getValue();
            poIdUpdateComboBoxModel.addElement(String.format("ID: %s \u00A0(Item: %s - %s)", details.getItemId(), details.getItemName(), details.getCategory()));
        }
        poIdUpdateComboBox = new JComboBox<>(poIdUpdateComboBoxModel);
        poIdUpdateComboBox.setMaximumRowCount(15);

        JLabel paymentStatusLabel = new JLabel("Payment Status:");
        paymentStatusUpdateField = new JTextField(15);

        JLabel paymentDateLabel = new JLabel("Payment Date:");
        paymentDateUpdateField = new JTextField(15);

        JLabel amountLabel = new JLabel("Amount:");
        amountUpdateField = new JTextField(15);

        JLabel updateByLabelUpdate = new JLabel("Update By (User ID):");
        updateByUpdateField = new JTextField(15);
        updateByUpdateField.setEditable(false);

        JLabel statusLabel = new JLabel("Status:");
        statusUpdateField = new JTextField(15);
        statusUpdateField.setEditable(false);

        updateButton = new JButton("Update");

        gbc.gridx = 0;
        gbc.gridy = 0;
        bottomPanel.add(financeIdLabel, gbc);
        gbc.gridx = 1;
        bottomPanel.add(financeIdUpdateField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        bottomPanel.add(poIdLabelUpdate, gbc);
        gbc.gridx = 1;
        bottomPanel.add(poIdUpdateComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        bottomPanel.add(paymentStatusLabel, gbc);
        gbc.gridx = 1;
        bottomPanel.add(paymentStatusUpdateField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        bottomPanel.add(paymentDateLabel, gbc);
        gbc.gridx = 1;
        bottomPanel.add(paymentDateUpdateField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        bottomPanel.add(amountLabel, gbc);
        gbc.gridx = 1;
        bottomPanel.add(amountUpdateField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        bottomPanel.add(updateByLabelUpdate, gbc);
        gbc.gridx = 1;
        bottomPanel.add(updateByUpdateField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 6;
        bottomPanel.add(statusLabel, gbc);
        gbc.gridx = 1;
        bottomPanel.add(statusUpdateField, gbc);

        gbc.gridx = 1;
        gbc.gridy = 7;
        gbc.anchor = GridBagConstraints.EAST;
        bottomPanel.add(updateButton, gbc);

        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String financeId = financeIdUpdateField.getText();
                boolean found = false;
                for (InventoryRecord record : financeRecords) {
                    if (record.getInventoryId().equals(financeId) {
                        found = true;
                        int confirm = JOptionPane.showConfirmDialog(
                                finance_e.this,
                                "Are you sure you want to update Finance ID: " + record.getInventoryId() + "?",
                                "Confirm Update",
                                JOptionPane.YES_NO_OPTION);
                        if (confirm == JOptionPane.YES_OPTION) {
                            String selectedItem = (String) poIdUpdateComboBox.getSelectedItem();
                            if (selectedItem != null) {
                                String poId = selectedItem.split(" ")[1];
                                record.setItemId(poId);
                            }
                            record.setLastUpdated(paymentDateUpdateField.getText());
                            record.setReorderQuantity(0); // Not used
                            record.setUpdatedBy(Integer.parseInt(login_c.currentUserId));
                            record.setStatus(paymentStatusUpdateField.getText());
                            
                            JOptionPane.showMessageDialog(finance_e.this, "Finance updated successfully! Updated By: " + login_c.currentUserId, "Success", JOptionPane.INFORMATION_MESSAGE);
                            clearUpdateFields();
                            populateFinanceListTableTop();
                            saveFinanceData();
                            updatePoIdDropdown();
                        } else {
                            JOptionPane.showMessageDialog(finance_e.this, "Update cancelled.", "Cancelled", JOptionPane.INFORMATION_MESSAGE);
                        }
                        break;
                    }
                }
                clearUpdateFields();
                if (!found) {
                    JOptionPane.showMessageDialog(finance_e.this, "Could not find finance record.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        return bottomPanel;
    }

    public void populateFinanceListTableTop() {
        financeListTableModelTop.setRowCount(0);
        for (InventoryRecord record : financeRecords) {
            financeListTableModelTop.addRow(new Object[]{record.getInventoryId() + " - (" + record.getStatus() + ")", new ButtonPanel(record)});
        }
    }

    private void loadFinanceData() {
        financeRecords.clear();
        try (BufferedReader br = new BufferedReader(new FileReader(INVENTORY_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split("\\|");
                if (data.length == 7) {
                    try {
                        String financeId = data[0].trim();
                        String poId = data[1].trim();
                        int currentStock = Integer.parseInt(data[2].trim());
                        String paymentDate = data[3].trim();
                        int reorderLevel = Integer.parseInt(data[4].trim());
                        int updatedBy = Integer.parseInt(data[5].trim());
                        String status = data[6].trim();

                        InventoryRecord record = new InventoryRecord(financeId, poId, currentStock, paymentDate, reorderLevel, updatedBy, status);
                        financeRecords.add(record);
                    } catch (NumberFormatException e) {
                        System.err.println("Error parsing data in line (inventory.txt): " + line);
                    }
                } else {
                    System.err.println("Skipping invalid line in inventory.txt: " + line + ". Expected 7 columns.");
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error reading finance file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadItemDetailsForDropdown() {
        itemDetailsMap.clear();
        List<ItemDetails> tempList = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(ITEMS_FILE))) {
            String line;

            while ((line = br.readLine()) != null) {
                String[] data = line.split("\\|");
                if (data.length == 6) {
                    String itemId = data[0].trim();
                    String itemName = data[1].trim();
                    String supplierId = data[2].trim();
                    String category = data[3].trim();
                    double price = Double.parseDouble(data[4].trim());
                    int stockQuantity = Integer.parseInt(data[5].trim());
                    tempList.add(new ItemDetails(itemId, itemName, category, stockQuantity));
                } else {
                    System.err.println("Skipping invalid line in items.txt: " + line + ". Expected itemId, ItemName, SupplierId, Category, Price, StockQuantity.");
                }
            }
        } catch (IOException e) {
            System.err.println("IOException caught in loadItemDetailsForDropdown(): " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error reading items file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Collections.sort(tempList, Comparator.comparing(ItemDetails::getItemId));

        itemDetailsMap.clear();
        for (ItemDetails itemDetails : tempList) {
            itemDetailsMap.put(itemDetails.getItemId(), itemDetails);
        }
    }

    public void saveFinanceData() {
        try (FileWriter writer = new FileWriter(INVENTORY_FILE)) {
            for (InventoryRecord record : financeRecords) {
                writer.write(record.getInventoryId() + "|"
                        + record.getItemId() + "|"
                        + record.getStockLevel() + "|"
                        + record.getLastUpdated() + "|"
                        + record.getReorderQuantity() + "|"
                        + record.getUpdatedBy() + "|"
                        + record.getStatus() + "\n");
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving finance file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public String generateNewFinanceId() {
        long maxId = 0;
        for (InventoryRecord record : financeRecords) {
            try {
                long currentId = Long.parseLong(record.getInventoryId());
                maxId = Math.max(maxId, currentId);
            } catch (NumberFormatException e) {
            }
        }
        return String.valueOf(maxId + 1);
    }

    private void populateUpdateFields(InventoryRecord record) {
        financeIdUpdateField.setText(record.getInventoryId());
        for (int i = 0; i < poIdUpdateComboBox.getItemCount(); i++) {
            String item = poIdUpdateComboBox.getItemAt(i);
            if (item.startsWith("ID: " + record.getItemId() + " ")) {
                poIdUpdateComboBox.setSelectedIndex(i);
                break;
            }
        }
        paymentStatusUpdateField.setText(record.getStatus());
        paymentDateUpdateField.setText(record.getLastUpdated());
        amountUpdateField.setText("0"); // Not used in original, placeholder
        updateByUpdateField.setText(String.valueOf(record.getUpdatedBy()));
        statusUpdateField.setText(record.getStatus());
    }

    private void clearUpdateFields() {
        financeIdUpdateField.setText("");
        poIdUpdateComboBox.setSelectedIndex(-1);
        paymentStatusUpdateField.setText("");
        paymentDateUpdateField.setText("");
        amountUpdateField.setText("");
        updateByUpdateField.setText("");
        statusUpdateField.setText("");
    }

    private void updatePoIdDropdown() {
        DefaultComboBoxModel<String> comboBoxModel = new DefaultComboBoxModel<>();
        for (Map.Entry<String, ItemDetails> entry : itemDetailsMap.entrySet()) {
            ItemDetails details = entry.getValue();
            comboBoxModel.addElement(String.format("ID: %s \u00A0(Item: %s - %s)", details.getItemId(), details.getItemName(), details.getCategory()));
        }
        poIdInfoComboBox.setModel(comboBoxModel);
        poIdInfoComboBox.setSelectedIndex(-1);

        DefaultComboBoxModel<String> updateComboBoxModel = new DefaultComboBoxModel<>();
        for (Map.Entry<String, ItemDetails> entry : itemDetailsMap.entrySet()) {
            ItemDetails details = entry.getValue();
            updateComboBoxModel.addElement(String.format("ID: %s \u00A0(Item: %s - %s)", details.getItemId(), details.getItemName(), details.getCategory()));
        }
        poIdUpdateComboBox.setModel(updateComboBoxModel);
        poIdUpdateComboBox.setSelectedIndex(-1);
    }

    public static class InventoryRecord {
        private String inventoryId;
        private String itemId;
        private int stockLevel;
        private String lastUpdated;
        private int reorderQuantity;
        private int updatedBy;
        private String status;

        public InventoryRecord(String inventoryId, String itemId, int stockLevel, String lastUpdated, int reorderQuantity, int updatedBy, String status) {
            this.inventoryId = inventoryId;
            this.itemId = itemId;
            this.stockLevel = stockLevel;
            this.lastUpdated = lastUpdated;
            this.reorderQuantity = reorderQuantity;
            this.updatedBy = updatedBy;
            this.status = status;
        }

        public String getInventoryId() {
            return inventoryId;
        }

        public String getItemId() {
            return itemId;
        }

        public int getStockLevel() {
            return stockLevel;
        }

        public String getLastUpdated() {
            return lastUpdated;
        }

        public int getReorderQuantity() {
            return reorderQuantity;
        }

        public int getUpdatedBy() {
            return updatedBy;
        }

        public String getStatus() {
            return status;
        }

        public void setItemId(String itemId) {
            this.itemId = itemId;
        }

        public void setStockLevel(int stockLevel) {
            this.stockLevel = stockLevel;
        }

        public void setLastUpdated(String lastUpdated) {
            this.lastUpdated = lastUpdated;
        }

        public void setReorderQuantity(int reorderQuantity) {
            this.reorderQuantity = reorderQuantity;
        }

        public void setUpdatedBy(int updatedBy) {
            this.updatedBy = updatedBy;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

    public static class ItemDetails {
        private String itemId;
        private String itemName;
        private String category;
        private int stockQuantity;

        public ItemDetails(String itemId, String itemName, String category, int stockQuantity) {
            this.itemId = itemId;
            this.itemName = itemName;
            this.category = category;
            this.stockQuantity = stockQuantity;
        }

        public String getItemId() {
            return itemId;
        }

        public String getItemName() {
            return itemName;
        }

        public String getCategory() {
            return category;
        }

        public int getStockQuantity() {
            return stockQuantity;
        }
    }

    private class ButtonPanel extends JPanel {
        private JButton viewButton;
        private InventoryRecord record;

        public ButtonPanel(InventoryRecord record) {
            this.record = record;
            setLayout(new FlowLayout(FlowLayout.LEFT, 7, 0));
            viewButton = new JButton("View");

            Dimension buttonSize = new Dimension(90, 14);
            viewButton.setPreferredSize(buttonSize);

            Font smallerFont = new Font(viewButton.getFont().getName(), Font.PLAIN, 13);
            viewButton.setFont(smallerFont);

            add(viewButton);

            viewButton.setEnabled(true);

            viewButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    populateUpdateFields(record);
                    tabbedPane.setSelectedIndex(1);
                }
            });
        }
    }

    private class ButtonRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            InventoryRecord record = financeRecords.get(row);
            if (record != null) {
                ButtonPanel panel = new ButtonPanel(record);
                return panel;
            } else {
                return new JLabel();
            }
        }
    }

    private class ButtonEditor extends DefaultCellEditor {
        private ButtonPanel panel;

        public ButtonEditor(JTable table) {
            super(new JTextField());
            setClickCountToStart(1);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            InventoryRecord record = financeRecords.get(row);
            if (record != null) {
                panel = new ButtonPanel(record);
                return panel;
            } else {
                return new JPanel();
            }
        }

        @Override
        public Object getCellEditorValue() {
            return null;
        }

        @Override
        public boolean isCellEditable(java.util.EventObject e) {
            return true;
        }

        @Override
        public boolean shouldSelectCell(java.util.EventObject anEvent) {
            return false;
        }
    }

    private int getStockLevelFromItems(String itemId) {
        try (BufferedReader br = new BufferedReader(new FileReader(ITEMS_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split("\\|");
                if (data.length == 6 && data[0].trim().equals(itemId)) {
                    return Integer.parseInt(data[5].trim());
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error reading items file or parsing stock level: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Finance Management");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.getContentPane().add(new finance_e());
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}