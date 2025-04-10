import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class inventory_c extends JPanel {

    private static final String INVENTORY_FILE = "TXT/inventory.txt";
    private static final String STATUS_PENDING = "Pending";

    private JComboBox<String> itemIdInfoComboBox;
    private JTextField lastUpdatedInfoField;
    private JTextField rQuantityInfoField;
    private JTextField updateByInfoField;
    private JButton addButton;

    private List<inventory_e.InventoryRecord> inventoryRecords;
    private Map<String, inventory_e.ItemDetails> itemDetailsMap;
    private inventory_e mainPanel;

    public inventory_c(List<inventory_e.InventoryRecord> inventoryRecords, Map<String, inventory_e.ItemDetails> itemDetailsMap, inventory_e mainPanel) {
        this.inventoryRecords = inventoryRecords;
        this.itemDetailsMap = itemDetailsMap;
        this.mainPanel = mainPanel;
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JLabel itemIdLabel = new JLabel("Item ID:");
        DefaultComboBoxModel<String> comboBoxModel = new DefaultComboBoxModel<>();

        for (Map.Entry<String, inventory_e.ItemDetails> entry : itemDetailsMap.entrySet()) {
            inventory_e.ItemDetails details = entry.getValue();
            comboBoxModel.addElement(String.format("ID: %s \u00A0(Item: %s - %s)", details.getItemId(), details.getItemName(), details.getCategory()));
        }

        itemIdInfoComboBox = new JComboBox<>(comboBoxModel);
        itemIdInfoComboBox.setMaximumRowCount(15);

        JLabel lastUpdatedLabel = new JLabel("Last Updated:");
        lastUpdatedInfoField = new JTextField("DD-MM-YYYY", 10);
        JLabel rQuantityLabel = new JLabel("Received Qty:");
        rQuantityInfoField = new JTextField("Number", 4);
        JLabel updateByLabel = new JLabel("Modify By (User ID):");
        updateByInfoField = new JTextField("User ID", 30);
        addButton = new JButton("Add");

        gbc.gridx = 0;
        gbc.gridy = 0;
        add(itemIdLabel, gbc);
        gbc.gridx = 1;
        add(itemIdInfoComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        add(lastUpdatedLabel, gbc);
        gbc.gridx = 1;
        add(lastUpdatedInfoField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        add(rQuantityLabel, gbc);
        gbc.gridx = 1;
        add(rQuantityInfoField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        add(updateByLabel, gbc);
        gbc.gridx = 1;
        add(updateByInfoField, gbc);

        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.EAST;
        add(addButton, gbc);

        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedItem = (String) itemIdInfoComboBox.getSelectedItem();
                if (selectedItem != null) {
                    String itemId = selectedItem.split(" ")[1];
                    String lastUpdated = lastUpdatedInfoField.getText();
                    String rQuantityText = rQuantityInfoField.getText().trim();
                    String updateByText = updateByInfoField.getText().trim();

                    try {
                        int rQuantity = Integer.parseInt(rQuantityText.isEmpty() ? "0" : rQuantityText);
                        int updateBy = Integer.parseInt(updateByText.isEmpty() ? "0" : updateByText);

                        int stockQuantity = 0;
                        if (itemDetailsMap.containsKey(itemId)) {
                            stockQuantity = itemDetailsMap.get(itemId).getStockQuantity();
                        }

                        inventory_e.InventoryRecord newRecord = new inventory_e.InventoryRecord(generateNewInventoryId(), itemId, stockQuantity, lastUpdated, rQuantity, updateBy, STATUS_PENDING);
                        inventoryRecords.add(newRecord);
                        mainPanel.populateInventoryListTableTop();
                        saveInventoryData();

                        lastUpdatedInfoField.setText("DD-MM-YYYY");
                        rQuantityInfoField.setText("Number");
                        updateByInfoField.setText("User ID");

                        JOptionPane.showMessageDialog(inventory_c.this, "Inventory added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);

                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(inventory_c.this, "Invalid input for Received Quantity or Modify By (User ID). Please enter correct numeric data.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
    }

    private String generateNewInventoryId() {
        long maxId = 0;
        for (inventory_e.InventoryRecord record : inventoryRecords) {
            try {
                long currentId = Long.parseLong(record.getInventoryId());
                maxId = Math.max(maxId, currentId);
            } catch (NumberFormatException e) {
            }
        }
        return String.valueOf(maxId + 1);
    }

    private void saveInventoryData() {
        try (FileWriter writer = new FileWriter(INVENTORY_FILE)) {
            for (inventory_e.InventoryRecord record : inventoryRecords) {
                writer.write(record.getInventoryId() + "|"
                        + record.getItemId() + "|"
                        + record.getStockLevel() + "|"
                        + record.getLastUpdated() + "|"
                        + record.getReorderQuantity() + "|"
                        + record.getUpdatedBy() + "|"
                        + record.getStatus() + "\n");
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving inventory file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}