import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
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

public class po_fm extends JPanel {

    private static final String PO_FILE = "TXT/po.txt";
    private static final String ITEMS_FILE = "TXT/items.txt";

    private JTabbedPane tabbedPane;
    private JPanel poListPanel;

    private DefaultTableModel poListTableModelTop = new DefaultTableModel(new Object[]{"PO ID - (Status)", "Actions"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return column == 1;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return (columnIndex == 1) ? ButtonPanel.class : String.class;
        }
    };
    private JTable poListTableTop;
    private JScrollPane poListScrollPaneTop;

    private JTextField poIdUpdateField;
    private JTextField prIdUpdateField;
    private JTextField itemIdUpdateField;
    private JTextField supplierIdUpdateField;
    private JTextField quantityOrderedUpdateField;
    private JTextField orderDateUpdateField;
    private JTextField orderByUpdateField;
    private JTextField receivedByUpdateField;
    private JTextField approvedByUpdateField;
    private JComboBox<String> statusComboBox;
    private JButton updateButton;
    private JButton approveButton;
    private JButton rejectButton;

    private List<PORecord> poRecords = new ArrayList<>();
    private Map<String, ItemDetails> itemDetailsMap = new HashMap<>();

    public po_fm() {
        loadItemDetailsForDropdown();
        setLayout(new BorderLayout());

        tabbedPane = new JTabbedPane();
        Font tabTitleFont = new Font("Arial", Font.BOLD, 20);
        tabbedPane.setFont(tabTitleFont);

        poListPanel = createPOListPanel();
        tabbedPane.addTab("PO List", poListPanel);

        add(tabbedPane, BorderLayout.CENTER);

        loadPOData();
        populatePOListTableTop();
    }

    private JPanel createPOListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 20, 0, 20));

        poListTableTop = new JTable(poListTableModelTop) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (column == 0) {
                    c.setFont(c.getFont().deriveFont(Font.PLAIN));
                    ((JComponent)c).setToolTipText(getValueAt(row, column).toString());
                }
                return c;
            }
        };
        
        poListTableTop.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        TableColumnModel columnModel = poListTableTop.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(300);
        columnModel.getColumn(1).setPreferredWidth(100);
        poListTableTop.setFillsViewportHeight(true);
        
        poListTableTop.getColumn("Actions").setCellRenderer(new ButtonRenderer());
        poListTableTop.getColumn("Actions").setCellEditor(new ButtonEditor(poListTableTop));
        
        poListScrollPaneTop = new JScrollPane(poListTableTop);
        poListScrollPaneTop.setPreferredSize(new Dimension(poListScrollPaneTop.getPreferredSize().width, 200));
        
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(poListScrollPaneTop, BorderLayout.CENTER);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(createPOListBottomPanel(), BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createPOListBottomPanel() {
        JPanel bottomPanel = new JPanel(new GridBagLayout());
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

        JLabel poIdLabel = new JLabel("PO ID:");
        poIdUpdateField = new JTextField(25);
        poIdUpdateField.setEditable(false);

        JLabel prIdLabelUpdate = new JLabel("PR ID:");
        prIdUpdateField = new JTextField(25);

        JLabel itemIdLabelUpdate = new JLabel("Item ID:");
        itemIdUpdateField = new JTextField(25);

        JLabel supplierIdLabelUpdate = new JLabel("Supplier ID:");
        supplierIdUpdateField = new JTextField(25);

        JLabel quantityOrderedLabelUpdate = new JLabel("Quantity Ordered:");
        quantityOrderedUpdateField = new JTextField(25);

        JLabel orderDateLabelUpdate = new JLabel("Order Date:");
        orderDateUpdateField = new JTextField(25);

        JLabel orderByLabelUpdate = new JLabel("Order By (User ID):");
        orderByUpdateField = new JTextField(25);

        JLabel receivedByLabelUpdate = new JLabel("Received By (User ID):");
        receivedByUpdateField = new JTextField(25);

        JLabel approvedByLabelUpdate = new JLabel("Approved By (User ID):");
        approvedByUpdateField = new JTextField(25);

        JLabel statusLabel = new JLabel("Status:");
        statusComboBox = new JComboBox<>(new String[]{"Pending", "Approved", "Rejected"});
        statusComboBox.setEnabled(false);

        updateButton = new JButton("Update");
        approveButton = new JButton("Approve");
        rejectButton = new JButton("Reject");

        gbc.gridx = 0;
        gbc.gridy = 0;
        bottomPanel.add(poIdLabel, gbc);
        gbc.gridx = 1;
        bottomPanel.add(poIdUpdateField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        bottomPanel.add(prIdLabelUpdate, gbc);
        gbc.gridx = 1;
        bottomPanel.add(prIdUpdateField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        bottomPanel.add(itemIdLabelUpdate, gbc);
        gbc.gridx = 1;
        bottomPanel.add(itemIdUpdateField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        bottomPanel.add(supplierIdLabelUpdate, gbc);
        gbc.gridx = 1;
        bottomPanel.add(supplierIdUpdateField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        bottomPanel.add(quantityOrderedLabelUpdate, gbc);
        gbc.gridx = 1;
        bottomPanel.add(quantityOrderedUpdateField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        bottomPanel.add(orderDateLabelUpdate, gbc);
        gbc.gridx = 1;
        bottomPanel.add(orderDateUpdateField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 6;
        bottomPanel.add(orderByLabelUpdate, gbc);
        gbc.gridx = 1;
        bottomPanel.add(orderByUpdateField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 7;
        bottomPanel.add(receivedByLabelUpdate, gbc);
        gbc.gridx = 1;
        bottomPanel.add(receivedByUpdateField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 8;
        bottomPanel.add(approvedByLabelUpdate, gbc);
        gbc.gridx = 1;
        bottomPanel.add(approvedByUpdateField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 9;
        bottomPanel.add(statusLabel, gbc);
        gbc.gridx = 1;
        bottomPanel.add(statusComboBox, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(approveButton);
        buttonPanel.add(rejectButton);
        buttonPanel.add(updateButton);

        gbc.gridx = 1;
        gbc.gridy = 10;
        gbc.anchor = GridBagConstraints.EAST;
        bottomPanel.add(buttonPanel, gbc);

        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String poId = poIdUpdateField.getText();
                boolean found = false;
                
                for (PORecord record : poRecords) {
                    if (record.getPoId().equals(poId)) {
                        found = true;
                        int confirm = JOptionPane.showConfirmDialog(
                                po_fm.this,
                                "Are you sure you want to update PO ID: " + record.getPoId() + "?",
                                "Confirm Update",
                                JOptionPane.YES_NO_OPTION);
                        
                        if (confirm == JOptionPane.YES_OPTION) {
                            try {
                                record.setItemId(itemIdUpdateField.getText());
                                record.setPrId(prIdUpdateField.getText());
                                record.setSupplierId(supplierIdUpdateField.getText());
                                
                                int quantityOrdered = Integer.parseInt(quantityOrderedUpdateField.getText().trim());
                                record.setQuantityOrdered(quantityOrdered);
                                
                                record.setOrderDate(orderDateUpdateField.getText());
                                
                                int orderBy = Integer.parseInt(orderByUpdateField.getText().trim());
                                record.setOrderBy(orderBy);
                                
                                int receivedBy = Integer.parseInt(receivedByUpdateField.getText().trim());
                                record.setReceivedBy(receivedBy);
                                
                                int approvedBy = Integer.parseInt(approvedByUpdateField.getText().trim());
                                record.setApprovedBy(approvedBy);
                                
                                populatePOListTableTop();
                                savePOData();
                                JOptionPane.showMessageDialog(po_fm.this, "PO updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                                
                            } catch (NumberFormatException ex) {
                                JOptionPane.showMessageDialog(po_fm.this, 
                                    "Invalid input for numeric fields. Please enter correct data.", 
                                    "Error", JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                        } else {
                            JOptionPane.showMessageDialog(po_fm.this, "Update cancelled.", "Cancelled", JOptionPane.INFORMATION_MESSAGE);
                        }
                        break;
                    }
                }
                
                clearUpdateFields();
                if (!found) {
                    JOptionPane.showMessageDialog(po_fm.this, "Could not find PO record.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        approveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String poId = poIdUpdateField.getText();
                if (poId.isEmpty()) {
                    JOptionPane.showMessageDialog(po_fm.this, "No PO selected. Please select a PO first.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                int confirm = JOptionPane.showConfirmDialog(
                        po_fm.this,
                        "Are you sure you want to APPROVE PO ID: " + poId + "?",
                        "Confirm Approval",
                        JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    for (PORecord record : poRecords) {
                        if (record.getPoId().equals(poId)) {
                            record.setStatus("Approved");
                            populatePOListTableTop();
                            savePOData();
                            statusComboBox.setSelectedItem("Approved");
                            JOptionPane.showMessageDialog(po_fm.this, "PO approved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                            break;
                        }
                    }
                }
            }
        });

        rejectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String poId = poIdUpdateField.getText();
                if (poId.isEmpty()) {
                    JOptionPane.showMessageDialog(po_fm.this, "No PO selected. Please select a PO first.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                int confirm = JOptionPane.showConfirmDialog(
                        po_fm.this,
                        "Are you sure you want to REJECT PO ID: " + poId + "?",
                        "Confirm Rejection",
                        JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    for (PORecord record : poRecords) {
                        if (record.getPoId().equals(poId)) {
                            record.setStatus("Rejected");
                            populatePOListTableTop();
                            savePOData();
                            statusComboBox.setSelectedItem("Rejected");
                            JOptionPane.showMessageDialog(po_fm.this, "PO rejected successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                            break;
                        }
                    }
                }
            }
        });

        return bottomPanel;
    }

    private void populatePOListTableTop() {
        poListTableModelTop.setRowCount(0);
        for (PORecord record : poRecords) {
            poListTableModelTop.addRow(new Object[]{record.getPoId() + " - (" + record.getStatus() + ")", new ButtonPanel(record)});
        }
    }

    private void loadPOData() {
        poRecords.clear();
        try (BufferedReader br = new BufferedReader(new FileReader(PO_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split("\\|");
                if (data.length == 10) {
                    try {
                        String poId = data[0].trim();
                        String prId = data[1].trim();
                        String itemId = data[2].trim();
                        String supplierId = data[3].trim();
                        int quantityOrdered = Integer.parseInt(data[4].trim());
                        String orderDate = data[5].trim();
                        int orderBy = Integer.parseInt(data[6].trim());
                        int receivedBy = Integer.parseInt(data[7].trim());
                        int approvedBy = Integer.parseInt(data[8].trim());
                        String status = data[9].trim();

                        PORecord record = new PORecord(
                            poId, prId, itemId, supplierId, 
                            quantityOrdered, orderDate, 
                            orderBy, receivedBy, approvedBy, 
                            status
                        );
                        poRecords.add(record);
                    } catch (NumberFormatException e) {
                        System.err.println("Error parsing data in line (po.txt): " + line);
                    }
                } else {
                    System.err.println("Skipping invalid line in po.txt: " + line + ". Expected 10 columns.");
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error reading PO file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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

    private void savePOData() {
        try (FileWriter writer = new FileWriter(PO_FILE)) {
            for (PORecord record : poRecords) {
                writer.write(record.getPoId() + "|"
                        + record.getPrId() + "|"
                        + record.getItemId() + "|"
                        + record.getSupplierId() + "|"
                        + record.getQuantityOrdered() + "|"
                        + record.getOrderDate() + "|"
                        + record.getOrderBy() + "|"
                        + record.getReceivedBy() + "|"
                        + record.getApprovedBy() + "|"
                        + record.getStatus() + "\n");
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving PO file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void populateUpdateFields(PORecord record) {
        poIdUpdateField.setText(record.getPoId());
        prIdUpdateField.setText(record.getPrId());
        itemIdUpdateField.setText(record.getItemId());
        supplierIdUpdateField.setText(record.getSupplierId());
        quantityOrderedUpdateField.setText(String.valueOf(record.getQuantityOrdered()));
        orderDateUpdateField.setText(record.getOrderDate());
        orderByUpdateField.setText(String.valueOf(record.getOrderBy()));
        receivedByUpdateField.setText(String.valueOf(record.getReceivedBy()));
        approvedByUpdateField.setText(String.valueOf(record.getApprovedBy()));
        statusComboBox.setSelectedItem(record.getStatus());
    }

    private void clearUpdateFields() {
        poIdUpdateField.setText("");
        prIdUpdateField.setText("");
        itemIdUpdateField.setText("");
        supplierIdUpdateField.setText("");
        quantityOrderedUpdateField.setText("");
        orderDateUpdateField.setText("");
        orderByUpdateField.setText("");
        receivedByUpdateField.setText("");
        approvedByUpdateField.setText("");
        statusComboBox.setSelectedItem("Pending");
    }

    private static class PORecord {
        private String poId;
        private String prId;
        private String itemId;
        private String supplierId;
        private int quantityOrdered;
        private String orderDate;
        private int orderBy;
        private int receivedBy;
        private int approvedBy;
        private String status;

        public PORecord(String poId, String prId, String itemId, String supplierId, 
                       int quantityOrdered, String orderDate, int orderBy, 
                       int receivedBy, int approvedBy, String status) {
            this.poId = poId;
            this.prId = prId;
            this.itemId = itemId;
            this.supplierId = supplierId;
            this.quantityOrdered = quantityOrdered;
            this.orderDate = orderDate;
            this.orderBy = orderBy;
            this.receivedBy = receivedBy;
            this.approvedBy = approvedBy;
            this.status = status;
        }

        public String getPoId() { return poId; }
        public String getPrId() { return prId; }
        public String getItemId() { return itemId; }
        public String getSupplierId() { return supplierId; }
        public int getQuantityOrdered() { return quantityOrdered; }
        public String getOrderDate() { return orderDate; }
        public int getOrderBy() { return orderBy; }
        public int getReceivedBy() { return receivedBy; }
        public int getApprovedBy() { return approvedBy; }
        public String getStatus() { return status; }

        public void setPrId(String prId) { this.prId = prId; }
        public void setItemId(String itemId) { this.itemId = itemId; }
        public void setSupplierId(String supplierId) { this.supplierId = supplierId; }
        public void setQuantityOrdered(int quantityOrdered) { this.quantityOrdered = quantityOrdered; }
        public void setOrderDate(String orderDate) { this.orderDate = orderDate; }
        public void setOrderBy(int orderBy) { this.orderBy = orderBy; }
        public void setReceivedBy(int receivedBy) { this.receivedBy = receivedBy; }
        public void setApprovedBy(int approvedBy) { this.approvedBy = approvedBy; }
        public void setStatus(String status) { this.status = status; }
    }

    private static class ItemDetails {
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

        public String getItemId() { return itemId; }
    }

    private class ButtonPanel extends JPanel {
        private JButton viewButton;
        private PORecord record;

        public ButtonPanel(PORecord record) {
            this.record = record;
            setLayout(new FlowLayout(FlowLayout.LEFT, 7, 0));
            viewButton = new JButton("View");

            Dimension buttonSize = new Dimension(90, 14);
            viewButton.setPreferredSize(buttonSize);

            Font smallerFont = new Font(viewButton.getFont().getName(), Font.PLAIN, 13);
            viewButton.setFont(smallerFont);

            add(viewButton);

            viewButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    populateUpdateFields(record);
                }
            });
        }
    }

    private class ButtonRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (value instanceof ButtonPanel) {
                return (ButtonPanel) value;
            }
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
            return label;
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
            PORecord record = poRecords.get(row);
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Purchase Order Management");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            
            po_fm panel = new po_fm();
            frame.getContentPane().add(new JScrollPane(panel), BorderLayout.CENTER);
            
            frame.setPreferredSize(new Dimension(1000, 700));
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}