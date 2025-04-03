import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
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

public class inventory_e extends JPanel {

    private static final String INVENTORY_FILE = "TXT/inventory.txt";
    private static final String ITEMS_FILE = "TXT/items.txt";
    private static final String DELIMITER = ",";

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
    private DefaultTableModel inventoryListTableModelTop = new DefaultTableModel(new Object[]{"Inventory ID - (Status)", "Actions"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return column == 1;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return (columnIndex == 1) ? ButtonPanel.class : super.getColumnClass(columnIndex);
        }
    };
    private JTable inventoryListTableTop;
    private JScrollPane inventoryListScrollPaneTop;

    // Inventory List Components (Bottom - Update)
    private JTextField inventoryIdUpdateField;
    private JTextField itemIdUpdateField;
    private JTextField stockLevelUpdateField;
    private JTextField lastUpdatedUpdateField;
    private JTextField rQuantityUpdateField;
    private JTextField updateByUpdateField;
    private JTextField statusUpdateField;
    private JButton updateButton;

    private List<InventoryRecord> inventoryRecords = new ArrayList<>();
    private Map<String, ItemDetails> itemDetailsMap = new HashMap<>();

    public inventory_e() {
        loadItemDetailsForDropdown();
        setLayout(new BorderLayout());

        tabbedPane = new JTabbedPane();
        Font tabTitleFont = new Font("Arial", Font.BOLD, 20);
        tabbedPane.setFont(tabTitleFont);

        inventoryInfoPanel = createInventoryInfoPanel();
        tabbedPane.addTab("Inventory Info", inventoryInfoPanel);

        inventoryListPanel = createInventoryListPanel();
        tabbedPane.addTab("Inventory List", inventoryListPanel);

        add(tabbedPane, BorderLayout.CENTER);

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
            comboBoxModel.addElement(String.format("ID: %s \u00A0(Item: %s - %s)", details.getItemId(), details.getItemName(), details.getCategory()));
        }

        itemIdInfoComboBox = new JComboBox<>(comboBoxModel);
        itemIdInfoComboBox.setMaximumRowCount(15);

        JLabel lastUpdatedLabel = new JLabel("Last Updated:");
        lastUpdatedInfoField = new JTextField("DD/MM/YYYY", 10);
        JLabel rQuantityLabel = new JLabel("Received Quantity:");
        rQuantityInfoField = new JTextField("Num", 4);
        JLabel updateByLabel = new JLabel("Update By:");
        updateByInfoField = new JTextField(30);
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
                    String itemId = selectedItem.split(" / ")[0].substring(4).trim();

                    String lastUpdated = lastUpdatedInfoField.getText();
                    int rQuantity = Integer.parseInt(rQuantityInfoField.getText().isEmpty() ? "0" : rQuantityInfoField.getText());
                    String updateBy = updateByInfoField.getText();

                    InventoryRecord newRecord = new InventoryRecord(generateNewInventoryId(), itemId, 0, lastUpdated, rQuantity, updateBy, "Pending");
                    inventoryRecords.add(newRecord);
                    populateInventoryListTableTop();
                    saveInventoryData();

                    itemIdInfoComboBox.setSelectedIndex(-1);
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
        panel.setBorder(new EmptyBorder(20, 20, 0, 20));

        inventoryListTableTop = new JTable(inventoryListTableModelTop);
        inventoryListTableTop.getColumn("Actions").setCellRenderer(new ButtonRenderer());
        inventoryListTableTop.getColumn("Actions").setCellEditor(new ButtonEditor(inventoryListTableTop));
        inventoryListScrollPaneTop = new JScrollPane(inventoryListTableTop);
        inventoryListScrollPaneTop.setMinimumSize(new Dimension(inventoryListScrollPaneTop.getMinimumSize().width, 50));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(inventoryListScrollPaneTop, BorderLayout.CENTER);

        JButton refreshButton = new JButton("Refresh");
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(refreshButton);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(createInventoryListBottomPanel(), BorderLayout.SOUTH);

        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadInventoryData();
                populateInventoryListTableTop();
                loadItemDetailsForDropdown();
                updateItemIdDropdown();
                inventoryListTableTop.repaint();
            }
        });

        return panel;
    }

    private JPanel createInventoryListBottomPanel() {
        JPanel bottomPanel = new JPanel(new GridBagLayout());
        TitledBorder titledBorder = BorderFactory.createTitledBorder("Inventory Details");
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

        JLabel inventoryIdLabel = new JLabel("Inventory ID:");
        inventoryIdUpdateField = new JTextField(15);
        inventoryIdUpdateField.setEditable(false);

        JLabel itemIdLabelUpdate = new JLabel("Item ID:");
        itemIdUpdateField = new JTextField(15);
        JLabel stockLevelLabel = new JLabel("Stock Level:");
        stockLevelUpdateField = new JTextField(15);
        JLabel lastUpdatedLabelUpdate = new JLabel("Last Updated:");
        lastUpdatedUpdateField = new JTextField(15);
        JLabel rQuantityLabelUpdate = new JLabel("Received Quantity:");
        rQuantityUpdateField = new JTextField(15);
        JLabel updateByLabelUpdate = new JLabel("Update By:");
        updateByUpdateField = new JTextField(15);
        JLabel statusLabel = new JLabel("Status:");
        statusUpdateField = new JTextField(15);
        statusUpdateField.setEditable(false);
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
        bottomPanel.add(statusUpdateField, gbc);

        gbc.gridx = 1;
        gbc.gridy = 7;
        gbc.anchor = GridBagConstraints.EAST;
        bottomPanel.add(updateButton, gbc);

        JButton goBackButton = new JButton("Go Back to Inventory Info");

        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        bottomPanel.add(goBackButton, gbc);

        goBackButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tabbedPane.setSelectedIndex(0);
            }
        });

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
                        populateInventoryListTableTop();
                        saveInventoryData();
                        break;
                    }
                }
                clearUpdateFields();
            }
        });

        return bottomPanel;
    }

    private void populateInventoryListTableTop() {
        inventoryListTableModelTop.setRowCount(0);
        for (InventoryRecord record : inventoryRecords) {
            inventoryListTableModelTop.addRow(new Object[]{record.getInventoryId() + " - (" + record.getStatus() + ")", new ButtonPanel(record)});
        }
    }

    private void loadInventoryData() {
        inventoryRecords.clear();
        try (BufferedReader br = new BufferedReader(new FileReader(INVENTORY_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(DELIMITER);
                if (data.length == 7) {
                    try {
                        String inventoryId = data[0].trim();
                        String itemId = data[1].trim();
                        int currentStock = Integer.parseInt(data[2].trim());
                        String lastUpdated = data[3].trim();
                        int reorderLevel = Integer.parseInt(data[4].trim());
                        String updatedBy = data[5].trim();
                        String status = data[6].trim();

                        InventoryRecord record = new InventoryRecord(inventoryId, itemId, currentStock, lastUpdated, reorderLevel, updatedBy, status);
                        inventoryRecords.add(record);
                    } catch (NumberFormatException e) {
                        System.err.println("Error parsing data in line (inventory.txt): " + line);
                    }
                } else {
                    System.err.println("Skipping invalid line in inventory.txt: " + line + ". Expected 7 columns.");
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error reading inventory file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadItemDetailsForDropdown() {
        itemDetailsMap.clear();
        List<ItemDetails> tempList = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(ITEMS_FILE))) {
            String line;
            String headerLine = br.readLine();
            System.out.println("Header Line (if any): " + headerLine);

            while ((line = br.readLine()) != null) {
                System.out.println("Processing line: " + line);
                String[] data = line.split(DELIMITER);
                System.out.println("Data array length: " + data.length);
                if (data.length == 6) {
                    String itemId = data[0].trim();
                    String itemName = data[1].trim();
                    String supplierId = data[2].trim();
                    String category = data[3].trim();
                    double price = Double.parseDouble(data[4].trim());
                    int stockQuantity = Integer.parseInt(data[5].trim());
                    tempList.add(new ItemDetails(itemId, itemName, category));
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

    private void saveInventoryData() {
        try (FileWriter writer = new FileWriter(INVENTORY_FILE)) {
            for (InventoryRecord record : inventoryRecords) {
                writer.write(record.getInventoryId() + DELIMITER +
                        record.getItemId() + DELIMITER +
                        record.getStockLevel() + DELIMITER +
                        record.getLastUpdated() + DELIMITER +
                        record.getReorderQuantity() + DELIMITER +
                        record.getUpdatedBy() + DELIMITER +
                        record.getStatus() + "\n");
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving inventory file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String generateNewInventoryId() {
        return String.valueOf(System.currentTimeMillis());
    }

    private void populateUpdateFields(InventoryRecord record) {
        inventoryIdUpdateField.setText(record.getInventoryId());
        itemIdUpdateField.setText(record.getItemId());
        stockLevelUpdateField.setText(String.valueOf(record.getStockLevel()));
        lastUpdatedUpdateField.setText(record.getLastUpdated());
        rQuantityUpdateField.setText(String.valueOf(record.getReorderQuantity()));
        updateByUpdateField.setText(record.getUpdatedBy());
        statusUpdateField.setText(record.getStatus());
    }

    private void clearUpdateFields() {
        inventoryIdUpdateField.setText("");
        itemIdUpdateField.setText("");
        stockLevelUpdateField.setText("");
        lastUpdatedUpdateField.setText("");
        rQuantityUpdateField.setText("");
        updateByUpdateField.setText("");
        statusUpdateField.setText("");
    }

    private void updateItemIdDropdown() {
        DefaultComboBoxModel<String> comboBoxModel = new DefaultComboBoxModel<>();
        for (Map.Entry<String, ItemDetails> entry : itemDetailsMap.entrySet()) {
            ItemDetails details = entry.getValue();
            comboBoxModel.addElement(String.format("ID: %s \u00A0(Item: %s - %s)", details.getItemId(), details.getItemName(), details.getCategory()));
        }
        itemIdInfoComboBox.setModel(comboBoxModel);
        itemIdInfoComboBox.setSelectedIndex(-1);
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
            setLayout(new FlowLayout(FlowLayout.LEFT, 7, 0));
            viewButton = new JButton("View");
            deleteButton = new JButton("Delete");

            Dimension buttonSize = new Dimension(90, 14);
            viewButton.setPreferredSize(buttonSize);
            deleteButton.setPreferredSize(buttonSize);

            Font smallerFont = new Font(viewButton.getFont().getName(), Font.PLAIN, 13);
            viewButton.setFont(smallerFont);
            deleteButton.setFont(smallerFont);

            add(viewButton);
            add(deleteButton);

            viewButton.setEnabled(true);
            deleteButton.setEnabled(record.getStatus().equals("Pending"));

            viewButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    System.out.println("View button clicked for Inventory ID: " + record.getInventoryId());
                    System.out.println("inventoryRecords size before populateUpdateFields: " + inventoryRecords.size());
                    populateUpdateFields(record);
                    System.out.println("inventoryRecords size after populateUpdateFields: " + inventoryRecords.size());
                    tabbedPane.setSelectedIndex(1);
                }
            });

            deleteButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (record.getStatus().equals("Pending")) {
                        int confirm = JOptionPane.showConfirmDialog(
                                inventory_e.this,
                                "Are you sure you want to delete Inventory ID: " + record.getInventoryId() + "?",
                                "Confirm Delete",
                                JOptionPane.YES_NO_OPTION);
                        if (confirm == JOptionPane.YES_OPTION) {
                            int rowIndex = inventoryRecords.indexOf(record);
                            inventoryRecords.remove(record);
                            populateInventoryListTableTop();
                            inventoryListTableModelTop.fireTableRowsDeleted(rowIndex, rowIndex);

                            int actionsColumnIndex = 1;
                            Rectangle cellRect = inventoryListTableTop.getCellRect(rowIndex, actionsColumnIndex, true);
                            inventoryListTableTop.repaint(cellRect);

                            clearUpdateFields();
                            saveInventoryData();

                            loadItemDetailsForDropdown();
                            updateItemIdDropdown();

                            loadInventoryData();
                            populateInventoryListTableTop();
                            inventoryListTableTop.repaint();

                            tabbedPane.setSelectedIndex(0);

                            System.out.println("Record deleted. UI refreshed.");
                        }
                    } else {
                        JOptionPane.showMessageDialog(inventory_e.this, "Cannot delete records with status '" + record.getStatus() + "'.", "Delete Not Allowed", JOptionPane.WARNING_MESSAGE);
                    }
                }
            });
        }
    }

    private class ButtonRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            InventoryRecord record = inventoryRecords.get(row);
            if (record != null) {
                return new ButtonPanel(record);
            } else {
                return new JLabel();
            }
        }
    }

    private class ButtonEditor extends DefaultCellEditor {
        public ButtonEditor(JTable table) {
            super(new JTextField());
            setClickCountToStart(1);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            InventoryRecord record = inventoryRecords.get(row);
            if (record != null) {
                return new ButtonPanel(record);
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Inventory Management");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.getContentPane().add(new inventory_e());
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}