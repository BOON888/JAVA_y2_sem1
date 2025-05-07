import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.table.TableCellEditor;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class po_e extends JPanel {
    private static final String PO_FILE = "TXT/po.txt";
    private JTabbedPane tabbedPane;
    private po_e_c poController = new po_e_c(); // 创建 po_e_c 的实例

    // --- PO Info Components (Modified - Order By Removed from Input) ---
    private JTextField prIDField, itemIDField, supplierIDField, quantityField, orderDateField;
    private JComboBox<String> receivedByDropdown, approvedByDropdown;
    private JButton addButton;
    private String loggedInUser = "LoggedInUser"; // Placeholder for logged-in user
    // ------------------------------------------------------------------

    // --- PO List Components (Unchanged - Order By Remains) ---
    private JTable poTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JButton searchButton;
    private TableRowSorter<DefaultTableModel> sorter;
    private JPanel detailsPanel; // Panel to show details below table
    private JTextField editPrIdField, editItemIdField, editSupplierIdField, editQuantityField, editOrderDateField;
    private JComboBox<String> editOrderByDropdown, editReceivedByDropdown, editApprovedByDropdown, editStatusDropdown;
    private JLabel detailPoIdLabel; // PO ID will remain a label
    private List<String[]> fullPoData;
    private String currentPoIdForEdit = null; // Track the PO ID being edited
    // ---------------------------------------------------------

    public po_e() {
        setLayout(new BorderLayout());
        fullPoData = new ArrayList<>();

        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("PO Info", createPOInfoPanel());
        tabbedPane.addTab("PO List", createPOListPanel());

        tabbedPane.setFont(new Font("Arial", Font.BOLD, 20));
        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createPOInfoPanel() {
        JPanel panel = new JPanel(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        JLabel[] labels = {
                new JLabel("PR ID:"),
                new JLabel("Item ID:"),
                new JLabel("Supplier ID:"),
                new JLabel("Quantity Ordered:"),
                new JLabel("Order Date (DD-MM-YYYY):"),
                new JLabel("Received By:"),
                new JLabel("Approved By:")
        };

        prIDField = new JTextField(15);
        itemIDField = new JTextField(15);
        supplierIDField = new JTextField(15);
        quantityField = new JTextField(15);
        orderDateField = new JTextField(15);

        JTextField[] fields = { prIDField, itemIDField, supplierIDField, quantityField, orderDateField };

        for (int i = 0; i < 5; i++) {
            labels[i].setFont(new Font("Arial", Font.BOLD, 16));
            gbc.gridx = 0;
            gbc.gridy = i;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 0;
            panel.add(labels[i], gbc);

            gbc.gridx = 1;
            gbc.weightx = 1.0;
            panel.add(fields[i], gbc);
        }

        receivedByDropdown = new JComboBox<>(new String[]{"Inventory Manager"});
        approvedByDropdown = new JComboBox<>(new String[]{"Financial Manager"});

        JComboBox<?>[] combos = {receivedByDropdown, approvedByDropdown};

        for (int i = 0; i < 2; i++) {
            labels[i + 5].setFont(new Font("Arial", Font.BOLD, 16));
            gbc.gridx = 0;
            gbc.gridy = i + 5;
             gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 0;
            panel.add(labels[i + 5], gbc);

            gbc.gridx = 1;
            gbc.weightx = 1.0;
            panel.add(combos[i], gbc);
        }

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.weightx = 0;
        addButton = new JButton("Add Purchase Order");
        addButton.setFont(new Font("Arial", Font.BOLD, 16));
        addButton.addActionListener(e -> {
            String prID = prIDField.getText().trim();
            String itemID = itemIDField.getText().trim();
            String supplierID = supplierIDField.getText().trim();
            String quantityStr = quantityField.getText().trim();
            String orderDate = orderDateField.getText().trim();
            String orderBy = loggedInUser; // Use the logged-in user
            String receivedBy = mapRoleToID((String) receivedByDropdown.getSelectedItem());
            String approvedBy = mapRoleToID((String) approvedByDropdown.getSelectedItem());

            if (poController.addPurchaseOrder(prID, itemID, supplierID, quantityStr, orderDate, orderBy, receivedBy, approvedBy)) {
                loadPurchaseOrders(); // 重新加载数据
                tabbedPane.setSelectedIndex(1); // 切换到 PO List 选项卡
                // 清空输入字段
                prIDField.setText(""); itemIDField.setText(""); supplierIDField.setText("");
                quantityField.setText(""); orderDateField.setText("");
                receivedByDropdown.setSelectedIndex(0); approvedByDropdown.setSelectedIndex(0);
            }
        });

        panel.add(addButton, gbc);

        gbc.gridy++;
        gbc.weighty = 1.0; // Filler
        panel.add(new JLabel(""), gbc);

        return panel;
    }

    private JPanel createPOListPanel() {
        JPanel listPanel = new JPanel(new BorderLayout(10, 10));
        listPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- Search Panel (Top Left - PO ID only) ---
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchPanel.add(new JLabel("Search by PO ID:")); // Label updated
        searchField = new JTextField(15);
        searchButton = new JButton("Search");
        searchButton.setFont(new Font("Arial", Font.BOLD, 12));
        searchButton.addActionListener(e -> searchPO()); // Trigger search
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        listPanel.add(searchPanel, BorderLayout.NORTH);
        // -------------------------------------------

        // --- Table Panel (Center - PO ID and Actions only) ---
        JPanel tablePanel = new JPanel(new BorderLayout()); // Panel to hold table and details

        String[] columnNames = {"PO ID", "Actions"}; // Only 2 columns
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 1; // Only Actions column (index 1) is editable
            }
             @Override
            public Class<?> getColumnClass(int columnIndex) {
                 if (columnIndex == 1) return JPanel.class; // Actions column uses JPanel
                 return String.class; // PO ID column uses String
            }
        };
        poTable = new JTable(tableModel);
        poTable.setRowHeight(35);
        poTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        poTable.setFont(new Font("Arial", Font.PLAIN, 12));

        // Sorter for filtering PO ID column
        sorter = new TableRowSorter<>(tableModel);
        poTable.setRowSorter(sorter);

        // --- Custom Renderer and Editor for Actions Column ---
        TableColumn actionsColumn = poTable.getColumnModel().getColumn(1); // Index 1 is Actions
        actionsColumn.setCellRenderer(new ActionButtonsRenderer());
        actionsColumn.setCellEditor(new ActionButtonsEditor(poTable, this));
        actionsColumn.setMinWidth(140);
        actionsColumn.setPreferredWidth(150);
        poTable.getColumnModel().getColumn(0).setPreferredWidth(100); // PO ID column width

        poTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN); // Let Actions column take remaining space

        JScrollPane scrollPane = new JScrollPane(poTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER); // Add table scroll pane to center
        //--------------------------------------------

        // --- Details Panel (Below Table - Order By Remains) ---
        detailsPanel = new JPanel(new GridBagLayout());
        detailsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(10, 0, 0, 0), // Top margin
                BorderFactory.createTitledBorder(
                        BorderFactory.createEtchedBorder(), "Purchase Order Details",
                        TitledBorder.LEFT, TitledBorder.TOP,
                        new Font("Arial", Font.BOLD, 14)
                )
        ));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 8, 4, 8);
        gbc.anchor = GridBagConstraints.WEST;

        // Create editable fields for displaying details (Order By Remains)
        detailPoIdLabel = new JLabel("---");
        detailPoIdLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        detailPoIdLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));

        editPrIdField = createDetailTextField();
        editItemIdField = createDetailTextField();
        editSupplierIdField = createDetailTextField();
        editQuantityField = createDetailTextField();
        editOrderDateField = createDetailTextField();
        editOrderByDropdown = new JComboBox<>(new String[]{"Purchase Manager", "Administrator"});
        editReceivedByDropdown = new JComboBox<>(new String[]{"Inventory Manager"});
        editApprovedByDropdown = new JComboBox<>(new String[]{"Financial Manager"});
        editStatusDropdown = new JComboBox<>(new String[]{"Pending", "Approved", "Rejected"});

        // Add Labels and Editable Fields to detailsPanel (Order By Remains)
        addDetailRow(detailsPanel, gbc, 0, "PO ID:", detailPoIdLabel);
        addDetailRow(detailsPanel, gbc, 1, "PR ID:", editPrIdField);
        addDetailRow(detailsPanel, gbc, 2, "Item ID:", editItemIdField);
        addDetailRow(detailsPanel, gbc, 3, "Supplier ID:", editSupplierIdField);
        addDetailRow(detailsPanel, gbc, 4, "Quantity:", editQuantityField);
        addDetailRow(detailsPanel, gbc, 5, "Order Date:", editOrderDateField);
        addDetailRow(detailsPanel, gbc, 6, "Order By:", editOrderByDropdown);
        addDetailRow(detailsPanel, gbc, 7, "Received By:", editReceivedByDropdown);
        addDetailRow(detailsPanel, gbc, 8, "Approved By:", editApprovedByDropdown);
        addDetailRow(detailsPanel, gbc, 9, "Status:", editStatusDropdown);

        tablePanel.add(detailsPanel, BorderLayout.SOUTH); // Add details panel below table
        //--------------------------------------------

        // --- Update Button Panel (Bottom) ---
        JPanel updateButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT)); // Align to the right
        JButton updateButton = new JButton("Update");
        updateButton.setFont(new Font("Arial", Font.BOLD, 16));
        updateButton.addActionListener(e -> updateSelectedPO());
        updateButtonPanel.add(updateButton);
        listPanel.add(updateButtonPanel, BorderLayout.SOUTH); // Add button panel to the bottom
        // --------------------------------------

        listPanel.add(tablePanel, BorderLayout.CENTER); // Add combined table/details panel

        loadPurchaseOrders(); // Load data initially
        clearDetailsPanel(); // Ensure details are empty at start

        return listPanel;
    }

    // Helper to create styled text fields for the details panel
    private JTextField createDetailTextField() {
        JTextField textField = new JTextField(15);
        textField.setFont(new Font("Arial", Font.PLAIN, 14));
        textField.setPreferredSize(new Dimension(200, 25));
        textField.setMinimumSize(new Dimension(100, 25));
        return textField;
    }

    // Helper to add a label and its component to the details panel
    private void addDetailRow(JPanel panel, GridBagConstraints gbc, int y, String labelText, JComponent component) {
        JLabel titleLabel = new JLabel(labelText);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.weightx = 0.0; // Title label doesn't expand
        gbc.fill = GridBagConstraints.NONE;
        panel.add(titleLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0; // Component expands
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(component, gbc);
    }


    // Load data: Reads full data, stores it, populates only PO ID in table
    private void loadPurchaseOrders() {
        tableModel.setRowCount(0); // Clear table
        fullPoData.clear();         // Clear stored full data
        List<String[]> dataList = poController.loadPurchaseOrders();
        for (String[] data : dataList) {
            if (data.length >= 1) {
                tableModel.addRow(new Object[]{data[0], null}); // data[0] is PO ID, null for Actions
                fullPoData.add(data);
            }
        }
        clearDetailsPanel(); // Clear details after loading
    }

    // View PO: Called by button editor, displays details in the editable panel
    public void viewPO(int modelRowIndex) {
        if (modelRowIndex >= 0 && modelRowIndex < fullPoData.size()) {
            String[] data = fullPoData.get(modelRowIndex); // Get full data for this row
            currentPoIdForEdit = data[0]; // Store the PO ID being edited
            displayDetailsForEdit(data); // Populate the editable fields
        } else {
            System.err.println("viewPO called with invalid model row index: " + modelRowIndex);
            clearDetailsPanel(); // Clear details if index is bad
        }
    }

    // Helper method to populate the editable fields in the details panel
    private void displayDetailsForEdit(String[] data) {
        if (data == null || data.length < 10) {
            System.err.println("displayDetailsForEdit called with invalid data.");
            clearDetailsPanel();
            return;
        }
        // Indices match the order in the file:
        // 0:POID, 1:PRID, 2:ItemID, 3:SuppID, 4:Qty, 5:Date, 6:OrdBy, 7:RecBy, 8:AppBy, 9:Status
        detailPoIdLabel.setText(getSafeData(data, 0));
        editPrIdField.setText(getSafeData(data, 1));
        editItemIdField.setText(getSafeData(data, 2));
        editSupplierIdField.setText(getSafeData(data, 3));
        editQuantityField.setText(getSafeData(data, 4));
        editOrderDateField.setText(getSafeData(data, 5));editOrderByDropdown.setSelectedItem(mapIDToRole(getSafeData(data, 6)));
        editReceivedByDropdown.setSelectedItem(mapIDToRole(getSafeData(data, 7)));
        editApprovedByDropdown.setSelectedItem(mapIDToRole(getSafeData(data, 8)));
        editStatusDropdown.setSelectedItem(getSafeData(data, 9));
    }

    // Helper method to safely get data from array, returning "N/A" if index is bad
    private String getSafeData(String[] data, int index) {
        if (data != null && index >= 0 && index < data.length && data[index] != null) {
            return data[index];
        }
        return "N/A";
    }


    // Helper method to clear all fields in the details panel
    private void clearDetailsPanel() {
        detailPoIdLabel.setText("---");
        editPrIdField.setText("");
        editItemIdField.setText("");
        editSupplierIdField.setText("");
        editQuantityField.setText("");
        editOrderDateField.setText("");
        editOrderByDropdown.setSelectedIndex(0);
        editReceivedByDropdown.setSelectedIndex(0);
        editApprovedByDropdown.setSelectedIndex(0);
        editStatusDropdown.setSelectedIndex(0);
        currentPoIdForEdit = null;
    }

    public void deletePO(int modelRowIndex) {
        if (modelRowIndex >= 0 && modelRowIndex < tableModel.getRowCount() && modelRowIndex < fullPoData.size()) {
            String poIDToDelete = tableModel.getValueAt(modelRowIndex, 0).toString();
            String prIDInfo = "N/A";
            String[] rowData = fullPoData.get(modelRowIndex);
            if (rowData != null && rowData.length > 1) {
                prIDInfo = rowData[1];
            }

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete PO ID: " + poIDToDelete + " (PR ID: " + prIDInfo + ")?",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                if (poController.deletePurchaseOrder(poIDToDelete)) {
                    fullPoData.remove(modelRowIndex);
                    tableModel.removeRow(modelRowIndex);
                    JOptionPane.showMessageDialog(this, "PO " + poIDToDelete + " deleted successfully.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
                    clearDetailsPanel();
                }
            }
        } else {
            System.err.println("deletePO called with invalid model row index: " + modelRowIndex);
        }
    }

    private void searchPO() {
        String searchText = searchField.getText().trim();
        if (searchText.isEmpty()) {
            sorter.setRowFilter(null); // Show all rows
        } else {
            RowFilter<DefaultTableModel, Object> rf = null;
            try {
                rf = RowFilter.regexFilter("^" + Pattern.quote(searchText), 0); // Search only in PO ID column (index 0)
            } catch (java.util.regex.PatternSyntaxException e) {
                return;
            }
            sorter.setRowFilter(rf);
        }
    }

    private String mapRoleToID(String role) {
        switch (role) {
            case "Inventory Manager": return "IM";
            case "Financial Manager": return "FM";
            default: return "";
        }
    }

    private String mapIDToRole(String id) {
        switch (id) {
            case "PM": return "Purchase Manager";
            case "AD": return "Administrator";
            case "IM": return "Inventory Manager";
            case "FM": return "Financial Manager";
            default: return "N/A";
        }
    }

    // Custom renderer for the Actions column
    private static class ActionButtonsRenderer extends JPanel implements TableCellRenderer {
        private final JButton viewButton = new JButton("View");
        private final JButton deleteButton = new JButton("Delete");

        public ActionButtonsRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
            add(viewButton);
            add(deleteButton);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (isSelected) {
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
                viewButton.setForeground(table.getSelectionForeground());
                deleteButton.setForeground(table.getSelectionForeground());
            } else {
                setBackground(table.getBackground());
                setForeground(table.getForeground());
                viewButton.setForeground(table.getForeground());
                deleteButton.setForeground(Color.RED); // Make delete button red
            }
            return this;
        }
    }

    // Custom editor for the Actions column
    private static class ActionButtonsEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {
        private final JPanel panel;
        private final JButton viewButton;
        private final JButton deleteButton;
        private int currentRow;
        private final JTable table;
        private final po_e poPanel;

        public ActionButtonsEditor(JTable table, po_e poPanel) {
            this.table = table;
            this.poPanel = poPanel;
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
            viewButton = new JButton("View");
            deleteButton = new JButton("Delete");
            deleteButton.setForeground(Color.RED); // Make delete button red
            viewButton.addActionListener(this);
            deleteButton.addActionListener(this);
            panel.add(viewButton);
            panel.add(deleteButton);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            currentRow = row;
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return null; // No meaningful value to return
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == viewButton) {
                poPanel.viewPO(table.convertRowIndexToModel(currentRow));
            } else if (e.getSource() == deleteButton) {
                poPanel.deletePO(table.convertRowIndexToModel(currentRow));
            }
            fireEditingStopped(); // Make sure editing stops
        }
    }

    public void updateSelectedPO() {
        if (currentPoIdForEdit == null) {
            JOptionPane.showMessageDialog(this, "Please view a Purchase Order before updating.", "No PO Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String updatedPrId = editPrIdField.getText();
        String updatedItemId = editItemIdField.getText();
        String updatedSupplierId = editSupplierIdField.getText();
        String updatedQuantity = editQuantityField.getText();
        String updatedOrderDate = editOrderDateField.getText();
        String updatedOrderBy = mapRoleToID((String) editOrderByDropdown.getSelectedItem()); // Keep for editing
        String updatedReceivedBy = mapRoleToID((String) editReceivedByDropdown.getSelectedItem());
        String updatedApprovedBy = mapRoleToID((String) editApprovedByDropdown.getSelectedItem());
        String updatedStatus = (String) editStatusDropdown.getSelectedItem();

        String[] updatedData = {currentPoIdForEdit, updatedPrId, updatedItemId, updatedSupplierId, updatedQuantity,
                               updatedOrderDate, updatedOrderBy, updatedReceivedBy, updatedApprovedBy, updatedStatus};

        if (poController.updatePurchaseOrder(currentPoIdForEdit, updatedData)) {
            JOptionPane.showMessageDialog(this, "Purchase Order " + currentPoIdForEdit + " updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadPurchaseOrders();
            clearDetailsPanel();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to update Purchase Order " + currentPoIdForEdit + ".", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Purchase Order Management");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.getContentPane().add(new po_e());
            frame.setSize(900, 700);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    // Method to set the logged-in user (you'd call this after successful login)
    public void setLoggedInUser(String username) {
        this.loggedInUser = username;
    }
}