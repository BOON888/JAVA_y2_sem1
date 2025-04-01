
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class inventory_e extends JPanel {

    private static final String INVENTORY_FILE = "TXT/inventory.txt";
    private static final String ITEMS_FILE = "TXT/items.txt";

    private JTabbedPane tabbedPane;
    private JPanel inventoryInfoPanel;
    private JPanel inventoryListPanel;

    // Inventory Info Components
    private JComboBox<String> itemIdInfoComboBox;
    private JTextField lastUpdatedInfoField;
    private JTextField rQuantityInfoField;
    private JTextField updateByInfoField;
    private JButton addButton;

    // Inventory List Components (Top)
    private DefaultTableModel inventoryListTableModelTop;
    private JTable inventoryListTableTop;
    private JScrollPane inventoryListScrollPaneTop;

    // Inventory List Components (Bottom - Update)
    private JTextField inventoryIdUpdateField;
    private JTextField itemIdUpdateField;
    private JTextField stockLevelUpdateField;
    private JTextField lastUpdatedUpdateField;
    private JTextField rQuantityUpdateField;
    private JTextField updateByUpdateField;
    private JComboBox<String> statusUpdateComboBox;
    private JButton updateButton;

    private List<InventoryRecord> inventoryRecords = new ArrayList<>();
    private Map<String, ItemDetails> itemDetailsMap = new HashMap<>();

    public inventory_e() {
        setLayout(new BorderLayout());

        tabbedPane = new JTabbedPane();
        Font tabTitleFont = new Font("Arial", Font.BOLD, 20);
        tabbedPane.setFont(tabTitleFont);

        inventoryInfoPanel = createInventoryInfoPanel();
        tabbedPane.addTab("Inventory Info", inventoryInfoPanel);

        inventoryListPanel = createInventoryListPanel();
        tabbedPane.addTab("Inventory List", inventoryListPanel);

        add(tabbedPane, BorderLayout.CENTER);

        loadItemDetailsForDropdown();  // Load item details specifically for the dropdown
        loadInventoryData();
        populateInventoryListTableTop();
    }

    private JPanel createInventoryInfoPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JLabel itemIdLabel = new JLabel("Item ID:");
        DefaultComboBoxModel<String> comboBoxModel = new DefaultComboBoxModel<>();

        for (Map.Entry<String, ItemDetails> entry : itemDetailsMap.entrySet()) {
            ItemDetails details = entry.getValue();
            comboBoxModel.addElement(String.format("%s (%s - %s)", details.getItemId(), details.getItemName(), details.getCategory()));
        }

        itemIdInfoComboBox = new JComboBox<>(comboBoxModel);
        itemIdInfoComboBox.setMaximumRowCount(10); // Set maximum rows to show before scroll

        JLabel lastUpdatedLabel = new JLabel("Last Updated:");
        lastUpdatedInfoField = new JTextField(15);
        JLabel rQuantityLabel = new JLabel("Received Quantity:");
        rQuantityInfoField = new JTextField(15);
        JLabel updateByLabel = new JLabel("Update By:");
        updateByInfoField = new JTextField(15);
        addButton = new JButton("Add");

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(itemIdLabel, gbc);
        gbc.gridx = 1;
        panel.add(itemIdInfoComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(lastUpdatedLabel, gbc);
        gbc.gridx = 1;
        panel.add(lastUpdatedInfoField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(rQuantityLabel, gbc);
        gbc.gridx = 1;
        panel.add(rQuantityInfoField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(updateByLabel, gbc);
        gbc.gridx = 1;
        panel.add(updateByInfoField, gbc);

        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(addButton, gbc);

        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedItem = (String) itemIdInfoComboBox.getSelectedItem();
                if (selectedItem != null) {
                    String itemId = selectedItem.split(",")[0].trim();  // Extract Item ID

                    String lastUpdated = lastUpdatedInfoField.getText();
                    int rQuantity = Integer.parseInt(rQuantityInfoField.getText().isEmpty() ? "0" : rQuantityInfoField.getText());
                    String updateBy = updateByInfoField.getText();

                    InventoryRecord newRecord = new InventoryRecord(generateNewInventoryId(), itemId, 0, lastUpdated, rQuantity, updateBy, "Pending");
                    inventoryRecords.add(newRecord);
                    populateInventoryListTableTop();

                    itemIdInfoComboBox.setSelectedIndex(-1);  // Optionally deselect after adding
                    lastUpdatedInfoField.setText("");
                    rQuantityInfoField.setText("");
                    updateByInfoField.setText("");
                }
            }
        });

        return panel;
    }

    private JPanel createInventoryListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Top Panel - List with View and Delete
        JPanel topPanel = new JPanel(new BorderLayout());
        inventoryListTableModelTop = new DefaultTableModel(new Object[]{"Inventory ID", "Actions"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 1; // Only the Actions column should not be directly editable
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return (columnIndex == 1) ? ButtonPanel.class : super.getColumnClass(columnIndex);
            }
        };
        inventoryListTableTop = new JTable(inventoryListTableModelTop);
        inventoryListTableTop.getColumn("Actions").setCellRenderer(new ButtonRenderer());
        inventoryListTableTop.getColumn("Actions").setCellEditor(new ButtonEditor(inventoryListTableTop));
        inventoryListScrollPaneTop = new JScrollPane(inventoryListTableTop);
        topPanel.add(inventoryListScrollPaneTop, BorderLayout.CENTER);
        panel.add(topPanel, BorderLayout.NORTH);

        // Bottom Panel - Update Details
        JPanel bottomPanel = new JPanel(new GridBagLayout());
        bottomPanel.setBorder(BorderFactory.createTitledBorder("Inventory List"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JLabel inventoryIdLabel = new JLabel("Inventory ID:");
        inventoryIdUpdateField = new JTextField(15);
        inventoryIdUpdateField.setEditable(false); // Typically not editable for update

        JLabel itemIdLabelUpdate = new JLabel("Item ID:");
        itemIdUpdateField = new JTextField(15);
        JLabel stockLevelLabel = new JLabel("Stock Level:");
        stockLevelUpdateField = new JTextField(15);
        JLabel lastUpdatedLabelUpdate = new JLabel("Last Updated:");
        lastUpdatedUpdateField = new JTextField(15);
        JLabel rQuantityLabelUpdate = new JLabel("R Quantity:");
        rQuantityUpdateField = new JTextField(15);
        JLabel updateByLabelUpdate = new JLabel("Update By:");
        updateByUpdateField = new JTextField(15);
        JLabel statusLabel = new JLabel("Status:");
        statusUpdateComboBox = new JComboBox<>(new String[]{"Pending", "Verified"});
        updateButton = new JButton("Update");

        gbc.gridx = 0;
        gbc.gridy = 0;
        bottomPanel.add(inventoryIdLabel, gbc);
        gbc.gridx = 1;
        bottomPanel.add(inventoryIdUpdateField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        bottomPanel.add(itemIdLabelUpdate, gbc);
        gbc.gridx = 1;
        bottomPanel.add(itemIdUpdateField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        bottomPanel.add(stockLevelLabel, gbc);
        gbc.gridx = 1;
        bottomPanel.add(stockLevelUpdateField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        bottomPanel.add(lastUpdatedLabelUpdate, gbc);
        gbc.gridx = 1;
        bottomPanel.add(lastUpdatedUpdateField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        bottomPanel.add(rQuantityLabelUpdate, gbc);
        gbc.gridx = 1;
        bottomPanel.add(rQuantityUpdateField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        bottomPanel.add(updateByLabelUpdate, gbc);
        gbc.gridx = 1;
        bottomPanel.add(updateByUpdateField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 6;
        bottomPanel.add(statusLabel, gbc);
        gbc.gridx = 1;
        bottomPanel.add(statusUpdateComboBox, gbc);

        gbc.gridx = 1;
        gbc.gridy = 7;
        gbc.anchor = GridBagConstraints.EAST;
        bottomPanel.add(updateButton, gbc);

        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String inventoryId = inventoryIdUpdateField.getText();
                for (InventoryRecord record : inventoryRecords) {
                    if (record.getInventoryId().equals(inventoryId)) {
                        record.setItemId(itemIdUpdateField.getText());
                        record.setStockLevel(Integer.parseInt(stockLevelUpdateField.getText().isEmpty() ? "0" : stockLevelUpdateField.getText()));
                        record.setLastUpdated(lastUpdatedUpdateField.getText());
                        record.setReorderQuantity(Integer.parseInt(rQuantityUpdateField.getText().isEmpty() ? "0" : rQuantityUpdateField.getText()));
                        record.setUpdatedBy(updateByUpdateField.getText());
                        record.setStatus((String) statusUpdateComboBox.getSelectedItem());
                        populateInventoryListTableTop(); // Refresh the top table
                        break;
                    }
                }
                clearUpdateFields();
            }
        });

        panel.add(bottomPanel, BorderLayout.SOUTH);
        return panel;
    }

    private void populateInventoryListTableTop() {
        inventoryListTableModelTop.setRowCount(0);
        for (InventoryRecord record : inventoryRecords) {
            inventoryListTableModelTop.addRow(new Object[]{record.getInventoryId(), new ButtonPanel(record)});
        }
    }

    private void loadInventoryData() {
        inventoryRecords.clear();
        try (BufferedReader br = new BufferedReader(new FileReader(INVENTORY_FILE))) {
            String line;
            br.readLine(); // Skip the header line
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 6) { // Adjust the check if you have more columns now
                    try {
                        String itemId = data[0].trim();
                        int currentStock = Integer.parseInt(data[3].trim());
                        String lastUpdated = data[5].trim();
                        int reorderLevel = (data.length > 4) ? Integer.parseInt(data[4].trim()) : 0;
                        String updatedBy = (data.length > 6) ? data[6].trim() : "";
                        String status = (data.length > 7) ? data[7].trim() : "Pending";

                        InventoryRecord record = new InventoryRecord(generateNewInventoryId(), itemId, currentStock, lastUpdated, reorderLevel, updatedBy, status);
                        inventoryRecords.add(record);
                    } catch (NumberFormatException e) {
                        System.err.println("Error parsing data in line (inventory.txt): " + line);
                    }
                } else {
                    System.err.println("Skipping invalid line in inventory.txt: " + line);
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error reading inventory file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadItemDetailsForDropdown() {
        itemDetailsMap.clear();
        try (BufferedReader br = new BufferedReader(new FileReader(ITEMS_FILE))) {
            String line;
            br.readLine(); // Skip the header line
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 4) {
                    String supplierId = data[0].trim();  // Read supplier_id (not used for dropdown)
                    String itemId = data[1].trim();      // Read item_id
                    String itemName = data[2].trim();    // Read item_name
                    String category = data[3].trim();  // Read category

                    itemDetailsMap.put(itemId, new ItemDetails(itemId, itemName, category));
                } else {

                    System.err.println("Skipping invalid line in items.txt: " + line + ". Expected at least supplier_id, item_id, item_name, category.");
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error reading items file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveInventoryData() {
        // Implement saving data to the file if needed
    }

    private String generateNewInventoryId() {
        return String.valueOf(System.currentTimeMillis()); // Simple unique ID generation
    }

    private void populateUpdateFields(InventoryRecord record) {
        inventoryIdUpdateField.setText(record.getInventoryId());
        itemIdUpdateField.setText(record.getItemId());
        stockLevelUpdateField.setText(String.valueOf(record.getStockLevel()));
        lastUpdatedUpdateField.setText(record.getLastUpdated());
        rQuantityUpdateField.setText(String.valueOf(record.getReorderQuantity()));
        updateByUpdateField.setText(record.getUpdatedBy());
        statusUpdateComboBox.setSelectedItem(record.getStatus());
    }

    private void clearUpdateFields() {
        inventoryIdUpdateField.setText("");
        itemIdUpdateField.setText("");
        stockLevelUpdateField.setText("");
        lastUpdatedUpdateField.setText("");
        rQuantityUpdateField.setText("");
        updateByUpdateField.setText("");
        statusUpdateComboBox.setSelectedIndex(0);
    }

    private static class InventoryRecord {

        private String inventoryId;
        private String itemId;
        private int stockLevel;
        private String lastUpdated;
        private int reorderQuantity;
        private String updatedBy;
        private String status;

        public InventoryRecord(String inventoryId, String itemId, int stockLevel, String lastUpdated, int reorderQuantity, String updatedBy, String status) {
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

        public String getUpdatedBy() {
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

        public void setUpdatedBy(String updatedBy) {
            this.updatedBy = updatedBy;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

    private static class ItemDetails {

        private String itemId;
        private String itemName;
        private String category;

        public ItemDetails(String itemId, String itemName, String category) {
            this.itemId = itemId;
            this.itemName = itemName;
            this.category = category;
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
    }

    private class ButtonPanel extends JPanel {

        private JButton viewButton;
        private JButton deleteButton;
        private InventoryRecord record;

        public ButtonPanel(InventoryRecord record) {
            this.record = record;
            setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
            viewButton = new JButton("View");
            deleteButton = new JButton("Delete");
            add(viewButton);
            add(deleteButton);

            viewButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    populateUpdateFields(record);
                    tabbedPane.setSelectedIndex(1); // Switch to Inventory List tab (bottom)
                }
            });

            deleteButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int confirm = JOptionPane.showConfirmDialog(
                            inventory_e.this,
                            "Are you sure you want to delete Inventory ID: " + record.getInventoryId() + "?",
                            "Confirm Delete",
                            JOptionPane.YES_NO_OPTION);
                    if (confirm == JOptionPane.YES_OPTION) {
                        inventoryRecords.remove(record);
                        populateInventoryListTableTop();
                        clearUpdateFields();
                    }
                }
            });
        }
    }

    private class ButtonRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            return (JPanel) value;
        }
    }

    private class ButtonEditor extends DefaultCellEditor {

        private JPanel panel;

        public ButtonEditor(JTable table) {
            super(new JTextField());
            setClickCountToStart(1);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            panel = (JPanel) value;
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return null;
        }
    }

}
