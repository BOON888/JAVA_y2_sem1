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
import javax.swing.AbstractCellEditor;
import javax.swing.table.TableCellEditor;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import javax.swing.RowFilter;

public class po_e extends JPanel {
    private static final String PO_FILE = "TXT/po.txt";
    private JTabbedPane tabbedPane;

    // --- PO Info Components (Original) ---
    private JTextField prIDField, itemIDField, supplierIDField, quantityField, orderDateField;
    private JComboBox<String> orderByDropdown, receivedByDropdown, approvedByDropdown;
    private JButton addButton;
    // --------------------------------------

    // --- PO List Components (Modified) ---
    private JTable poTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JButton searchButton;
    private TableRowSorter<DefaultTableModel> sorter;
    private JPanel detailsPanel; // Panel to show details below table
    // Labels & Fields for the details panel
    private JLabel detailPoIdLabel, detailPrIdLabel, detailItemIdLabel, detailSupplierIdLabel,
                   detailQuantityLabel, detailOrderDateLabel, detailOrderByLabel,
                   detailReceivedByLabel, detailApprovedByLabel, detailStatusLabel;
    // Store full data read from file, corresponding to table model rows
    private List<String[]> fullPoData;
    // --------------------------------------

    public po_e() {
        setLayout(new BorderLayout());
        fullPoData = new ArrayList<>(); // Initialize the list to hold full data

        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("PO Info", createPOInfoPanel()); // Original
        tabbedPane.addTab("PO List", createPOListPanel()); // Modified

        tabbedPane.setFont(new Font("Arial", Font.BOLD, 20));
        add(tabbedPane, BorderLayout.CENTER);
    }

    // =================================================
    // --- ORIGINAL createPOInfoPanel Method ---
    // =================================================
    private JPanel createPOInfoPanel() {
        // *** This method remains exactly as in your original code ***
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
                new JLabel("Order By:"),
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

        orderByDropdown = new JComboBox<>(new String[]{"Purchase Manager", "Administrator"});
        receivedByDropdown = new JComboBox<>(new String[]{"Inventory Manager"});
        approvedByDropdown = new JComboBox<>(new String[]{"Financial Manager"});

        JComboBox<?>[] combos = {orderByDropdown, receivedByDropdown, approvedByDropdown};

        for (int i = 0; i < 3; i++) {
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
        addButton.addActionListener(e -> addPurchaseOrder());
        panel.add(addButton, gbc);

        gbc.gridy++;
        gbc.weighty = 1.0; // Filler
        panel.add(new JLabel(""), gbc);

        return panel;
    }
    // =================================================
    // --- END ORIGINAL createPOInfoPanel Method ---
    // =================================================


    // =================================================
    // --- NEW createPOListPanel Method ---
    // =================================================
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

        // --- Details Panel (Below Table - Initially Empty) ---
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

        // Create labels for displaying details
        detailPoIdLabel = createDetailLabel();
        detailPrIdLabel = createDetailLabel();
        detailItemIdLabel = createDetailLabel();
        detailSupplierIdLabel = createDetailLabel();
        detailQuantityLabel = createDetailLabel();
        detailOrderDateLabel = createDetailLabel();
        detailOrderByLabel = createDetailLabel();
        detailReceivedByLabel = createDetailLabel();
        detailApprovedByLabel = createDetailLabel();
        detailStatusLabel = createDetailLabel();

        // Add Labels and Value Labels to detailsPanel
        addDetailRow(detailsPanel, gbc, 0, "PO ID:", detailPoIdLabel);
        addDetailRow(detailsPanel, gbc, 1, "PR ID:", detailPrIdLabel);
        addDetailRow(detailsPanel, gbc, 2, "Item ID:", detailItemIdLabel);
        addDetailRow(detailsPanel, gbc, 3, "Supplier ID:", detailSupplierIdLabel);
        addDetailRow(detailsPanel, gbc, 4, "Quantity:", detailQuantityLabel);
        addDetailRow(detailsPanel, gbc, 5, "Order Date:", detailOrderDateLabel);
        addDetailRow(detailsPanel, gbc, 6, "Order By:", detailOrderByLabel);
        addDetailRow(detailsPanel, gbc, 7, "Received By:", detailReceivedByLabel);
        addDetailRow(detailsPanel, gbc, 8, "Approved By:", detailApprovedByLabel);
        addDetailRow(detailsPanel, gbc, 9, "Status:", detailStatusLabel);

        tablePanel.add(detailsPanel, BorderLayout.SOUTH); // Add details panel below table
        //--------------------------------------------

        listPanel.add(tablePanel, BorderLayout.CENTER); // Add combined table/details panel

        loadPurchaseOrders(); // Load data initially
        clearDetailsPanel(); // Ensure details are empty at start

        return listPanel;
    }
    // =================================================
    // --- END NEW createPOListPanel Method ---
    // =================================================

    // Helper to create styled labels for the details panel
    private JLabel createDetailLabel() {
        JLabel label = new JLabel("---"); // Initial text
        label.setFont(new Font("Arial", Font.PLAIN, 14));
        label.setPreferredSize(new Dimension(200, 20)); // Give it some preferred width
        label.setMinimumSize(new Dimension(100, 20));
        label.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0)); // Left padding
        return label;
    }

    // Helper to add a label and its value label to the details panel
    private void addDetailRow(JPanel panel, GridBagConstraints gbc, int y, String labelText, JLabel valueLabel) {
        JLabel titleLabel = new JLabel(labelText);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.weightx = 0.0; // Title label doesn't expand
        gbc.fill = GridBagConstraints.NONE;
        panel.add(titleLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0; // Value label expands
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(valueLabel, gbc);
    }


    // Load data: Reads full data, stores it, populates only PO ID in table
    private void loadPurchaseOrders() {
        tableModel.setRowCount(0); // Clear table
        fullPoData.clear();        // Clear stored full data
        File file = new File(PO_FILE);
        if (!file.exists()) {
            System.err.println("PO file not found: " + PO_FILE + ". Creating.");
             try { // Try to create file/directory
                 File parentDir = file.getParentFile();
                 if (parentDir != null && !parentDir.exists()) { parentDir.mkdirs(); }
                 file.createNewFile();
             } catch (IOException | SecurityException ioe) {
                  JOptionPane.showMessageDialog(this, "Error creating PO file: " + ioe.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
             }
            clearDetailsPanel(); // Clear details even if file not found
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                 lineNumber++;
                if (line.trim().isEmpty()) continue;

                String[] data = line.split("\\|");
                // Expecting 10 columns in file: POID|PRID|ItemID|SuppID|Qty|Date|OrdBy|RecBy|AppBy|Status
                if (data.length >= 10) { // Need all 10 fields for full details
                     // Store the full data row
                     fullPoData.add(data);
                     // Add ONLY PO ID to the visible table model
                     tableModel.addRow(new Object[]{data[0], null}); // data[0] is PO ID, null for Actions
                } else {
                    System.err.println("Skipping malformed line #" + lineNumber + " in " + PO_FILE + " (expected 10 fields, got " + data.length + "): " + line);
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error loading purchase orders: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
        clearDetailsPanel(); // Clear details after loading
    }

    // Add PO: Saves full data to file, reloads table/data
    private void addPurchaseOrder() {
        // --- Validation (same as before) ---
        String prID = prIDField.getText().trim();
        String itemID = itemIDField.getText().trim();
        String supplierID = supplierIDField.getText().trim();
        String quantityStr = quantityField.getText().trim();
        String orderDate = orderDateField.getText().trim();

        if (prID.isEmpty() || itemID.isEmpty() || supplierID.isEmpty() || quantityStr.isEmpty() || orderDate.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all required fields!", "Input Error", JOptionPane.ERROR_MESSAGE); return;
        }
        int quantity;
        try { quantity = Integer.parseInt(quantityStr); if (quantity <= 0) throw new NumberFormatException(); }
        catch (NumberFormatException e) { JOptionPane.showMessageDialog(this, "Quantity must be a positive number.", "Input Error", JOptionPane.ERROR_MESSAGE); return; }
        if (!orderDate.matches("\\d{2}-\\d{2}-\\d{4}")) { JOptionPane.showMessageDialog(this, "Order Date must be in DD-MM-YYYY format.", "Input Error", JOptionPane.ERROR_MESSAGE); return; }
        // --- End Validation ---

        String orderBy = mapRoleToID((String) orderByDropdown.getSelectedItem());
        String receivedBy = mapRoleToID((String) receivedByDropdown.getSelectedItem());
        String approvedBy = mapRoleToID((String) approvedByDropdown.getSelectedItem());
        String status = "Pending"; // Default status

        int poID = generatePOID();
         if (poID < 0) { JOptionPane.showMessageDialog(this, "Could not generate PO ID.", "Error", JOptionPane.ERROR_MESSAGE); return; }
        String formattedPOID = String.format("%04d", poID);

        // File structure: POID|PRID|ItemID|SuppID|Qty|Date|OrdBy|RecBy|AppBy|Status
        String newPO = String.join("|", formattedPOID, prID, itemID, supplierID, String.valueOf(quantity), orderDate, orderBy, receivedBy, approvedBy, status);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(PO_FILE, true))) {
            writer.write(newPO);
            writer.newLine();
            JOptionPane.showMessageDialog(this, "Purchase Order (PO ID: " + formattedPOID + ") added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);

             // Clear input fields
             prIDField.setText(""); itemIDField.setText(""); supplierIDField.setText("");
             quantityField.setText(""); orderDateField.setText("");
             orderByDropdown.setSelectedIndex(0); receivedByDropdown.setSelectedIndex(0); approvedByDropdown.setSelectedIndex(0);

            loadPurchaseOrders(); // Reload data (this will also clear details panel)
            tabbedPane.setSelectedIndex(1); // Switch to PO List tab

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving purchase order: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }


     // View PO: Called by button editor, displays details in the lower panel
     public void viewPO(int modelRowIndex) {
         if (modelRowIndex >= 0 && modelRowIndex < fullPoData.size()) {
            String[] data = fullPoData.get(modelRowIndex); // Get full data for this row
            displayDetails(data); // Populate the details panel
         } else {
             System.err.println("viewPO called with invalid model row index: " + modelRowIndex);
             clearDetailsPanel(); // Clear details if index is bad
         }
     }

    // Helper method to populate the details panel fields
    private void displayDetails(String[] data) {
        if (data == null || data.length < 10) {
            System.err.println("displayDetails called with invalid data.");
            clearDetailsPanel();
            return;
        }
        // Indices match the order in the file:
        // 0:POID, 1:PRID, 2:ItemID, 3:SuppID, 4:Qty, 5:Date, 6:OrdBy, 7:RecBy, 8:AppBy, 9:Status
        detailPoIdLabel.setText(getSafeData(data, 0));
        detailPrIdLabel.setText(getSafeData(data, 1));
        detailItemIdLabel.setText(getSafeData(data, 2));
        detailSupplierIdLabel.setText(getSafeData(data, 3));
        detailQuantityLabel.setText(getSafeData(data, 4));
        detailOrderDateLabel.setText(getSafeData(data, 5));
        detailOrderByLabel.setText(getSafeData(data, 6)); // Consider mapping ID back to Role Name if needed
        detailReceivedByLabel.setText(getSafeData(data, 7));
        detailApprovedByLabel.setText(getSafeData(data, 8));
        detailStatusLabel.setText(getSafeData(data, 9));
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
        detailPrIdLabel.setText("---");
        detailItemIdLabel.setText("---");
        detailSupplierIdLabel.setText("---");
        detailQuantityLabel.setText("---");
        detailOrderDateLabel.setText("---");
        detailOrderByLabel.setText("---");
        detailReceivedByLabel.setText("---");
        detailApprovedByLabel.setText("---");
        detailStatusLabel.setText("---");
    }


     // Search: Filters table based on PO ID (column 0)
     private void searchPO() {
         String searchText = searchField.getText().trim();
         if (searchText.isEmpty()) {
             sorter.setRowFilter(null); // Clear filter
         } else {
             // Filter based on PO ID column (index 0) only, case-insensitive
             try {
                 // Use Pattern.quote to treat search text literally (avoid regex issues)
                 RowFilter<DefaultTableModel, Object> rf = RowFilter.regexFilter("(?i)" + Pattern.quote(searchText), 0);
                 sorter.setRowFilter(rf);
             } catch (java.util.regex.PatternSyntaxException e) {
                 JOptionPane.showMessageDialog(this,"Invalid search pattern","Search Error", JOptionPane.ERROR_MESSAGE);
                 sorter.setRowFilter(null);
             }
         }
         clearDetailsPanel(); // Clear details when searching
     }


    // Delete PO: Called by button editor, removes from file, model, and full data list
    public void deletePO(int modelRowIndex) {
         if (modelRowIndex >= 0 && modelRowIndex < tableModel.getRowCount() && modelRowIndex < fullPoData.size()) {
            // Get PO ID from the model (it's the primary key)
            String poIDToDelete = tableModel.getValueAt(modelRowIndex, 0).toString();
            // Get PR ID from the stored full data for confirmation message
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
                if (deletePOFromFile(poIDToDelete)) {
                    // Remove from the stored full data list *first*
                    fullPoData.remove(modelRowIndex);
                    // Then remove from the visible table model
                    tableModel.removeRow(modelRowIndex);
                    JOptionPane.showMessageDialog(this, "PO " + poIDToDelete + " deleted successfully.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
                    clearDetailsPanel(); // Clear details panel after deletion
                } // Error message shown in deletePOFromFile
            }
        } else {
             System.err.println("deletePO called with invalid model row index: " + modelRowIndex);
        }
    }


    // --- Unchanged Helper Methods ---
    private String mapRoleToID(String role) {
        // (Same as before)
         return switch (role) {
            case "Purchase Manager" -> "1004";
            case "Inventory Manager" -> "1003";
            case "Financial Manager" -> "1002";
            case "Administrator" -> "1001";
            default -> "Unknown";
        };
    }

    private int generatePOID() {
        // (Same as before, returns -1 on error)
        int maxID = 0;
        File file = new File(PO_FILE);
         if (!file.exists()) return 1;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                 if (line.trim().isEmpty()) continue;
                String[] data = line.split("\\|");
                if (data.length > 0 && !data[0].trim().isEmpty()) {
                    try {
                        int currentID = Integer.parseInt(data[0].trim());
                        if (currentID > maxID) maxID = currentID;
                    } catch (NumberFormatException ignored) {}
                }
            }
        } catch (IOException e) {
             JOptionPane.showMessageDialog(this, "Error reading PO file for ID generation: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace(); return -1;
        }
        return maxID + 1;
    }

    private boolean deletePOFromFile(String poIDToDelete) {
        // (Same as before, using temp file)
         File inputFile = new File(PO_FILE);
        File tempFile = null;
        try { tempFile = File.createTempFile("temp_po_", ".txt", inputFile.getParentFile()); }
        catch (IOException e) { JOptionPane.showMessageDialog(this, "Could not create temporary file: " + e.getMessage(), "File Error", JOptionPane.ERROR_MESSAGE); return false; }

        boolean deleted = false, found = false;
         if (!inputFile.exists()) { JOptionPane.showMessageDialog(this, "PO file not found.", "Error", JOptionPane.ERROR_MESSAGE); if (tempFile.exists()) tempFile.delete(); return false; }

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] data = line.split("\\|");
                 if (data.length > 0 && data[0].equals(poIDToDelete)) { found = true; }
                 else { writer.write(line); writer.newLine(); }
            }
        } catch (IOException e) { JOptionPane.showMessageDialog(this, "Error processing PO file for deletion: " + e.getMessage(), "File Error", JOptionPane.ERROR_MESSAGE); if (tempFile.exists()) tempFile.delete(); return false; }

        if (!found) { JOptionPane.showMessageDialog(this, "Could not find PO ID " + poIDToDelete + " in the file.", "Not Found", JOptionPane.WARNING_MESSAGE); if (tempFile.exists()) tempFile.delete(); return false; }

         try { // Replace original with temp
             if (!inputFile.delete()) { System.gc(); Thread.sleep(100); if (!inputFile.delete()) throw new IOException("Could not delete original file: " + inputFile.getAbsolutePath()); }
             if (!tempFile.renameTo(inputFile)) { // Fallback: copy content
                 try (InputStream in = new FileInputStream(tempFile); OutputStream out = new FileOutputStream(inputFile)) { byte[] buf = new byte[8192]; int len; while ((len = in.read(buf)) > 0) out.write(buf, 0, len); deleted = true; }
                 catch (IOException copyEx) { throw new IOException("Could not rename temp file and failed to copy back.", copyEx); }
                 finally { if (deleted && tempFile.exists()) tempFile.delete(); }
             } else { deleted = true; }
         } catch (IOException | SecurityException | InterruptedException e) { JOptionPane.showMessageDialog(this, "Error updating PO file: " + e.getMessage(), "File Error", JOptionPane.ERROR_MESSAGE); deleted = false; }
         if (!deleted && tempFile.exists()) { System.err.println("Error occurred during file replace, temp file might still exist: "+tempFile.getAbsolutePath()); }

        return deleted;
    }

    // --- Main method for testing ---
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception e) { System.err.println("Couldn't set system look and feel."); }

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Purchase Order Management");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.getContentPane().add(new po_e());
            frame.setMinimumSize(new Dimension(800, 650)); // Increased height for details panel
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}


// ========================================================================
// Helper classes for rendering and editing buttons in the JTable
// (Keep ActionButtonsPanel, ActionButtonsRenderer, ActionButtonsEditor same as before)
// ========================================================================

class ActionButtonsPanel extends JPanel { /* ... Same as previous ... */
    public final JButton viewButton;
    public final JButton deleteButton;
    private final List<ActionListener> viewActionListeners = new ArrayList<>();
    private final List<ActionListener> deleteActionListeners = new ArrayList<>();

    public ActionButtonsPanel() {
        super(new FlowLayout(FlowLayout.CENTER, 5, 2));
        setOpaque(true);
        viewButton = new JButton("View"); viewButton.setMargin(new Insets(2, 5, 2, 5)); viewButton.setFocusable(false);
        deleteButton = new JButton("Delete"); deleteButton.setMargin(new Insets(2, 5, 2, 5)); deleteButton.setFocusable(false);
        viewButton.addActionListener(this::fireViewActionPerformed);
        deleteButton.addActionListener(this::fireDeleteActionPerformed);
        add(viewButton); add(deleteButton);
    }
    public void addViewActionListener(ActionListener listener) { if (!viewActionListeners.contains(listener)) viewActionListeners.add(listener); }
    public void addDeleteActionListener(ActionListener listener) { if (!deleteActionListeners.contains(listener)) deleteActionListeners.add(listener); }
    protected void fireViewActionPerformed(ActionEvent event) { ActionEvent newEvent = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "view", event.getWhen(), event.getModifiers()); for (ActionListener listener : new ArrayList<>(viewActionListeners)) listener.actionPerformed(newEvent); }
    protected void fireDeleteActionPerformed(ActionEvent event) { ActionEvent newEvent = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "delete", event.getWhen(), event.getModifiers()); for (ActionListener listener : new ArrayList<>(deleteActionListeners)) listener.actionPerformed(newEvent); }
    @Override public void setBackground(Color bg) { super.setBackground(bg); if (viewButton != null && !viewButton.isBackgroundSet()) viewButton.setBackground(bg); if (deleteButton != null && !deleteButton.isBackgroundSet()) deleteButton.setBackground(bg); }
}

class ActionButtonsRenderer extends ActionButtonsPanel implements TableCellRenderer { /* ... Same as previous ... */
    public ActionButtonsRenderer() { super(); setName("Table.cellRenderer"); }
    @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) { if (isSelected) { setForeground(table.getSelectionForeground()); setBackground(table.getSelectionBackground()); } else { setForeground(table.getForeground()); setBackground(UIManager.getColor("Button.background")); } return this; }
}

class ActionButtonsEditor extends AbstractCellEditor implements TableCellEditor { /* ... Same as previous ... */
    private final ActionButtonsPanel panel = new ActionButtonsPanel();
    private final JTable table;
    private final po_e parentPanel;
    private transient ActionListener viewListener, deleteListener;
    private int editingModelRow = -1;
    public ActionButtonsEditor(JTable table, po_e parentPanel) {
        this.table = table; this.parentPanel = parentPanel;
        viewListener = e -> { if (editingModelRow != -1) { parentPanel.viewPO(editingModelRow); fireEditingStopped(); } };
        deleteListener = e -> { if (editingModelRow != -1) { int rowToDelete = editingModelRow; parentPanel.deletePO(rowToDelete); /* Table model listener should handle stop */ SwingUtilities.invokeLater(this::fireEditingStopped); } }; // Stop editing after action
        panel.addViewActionListener(viewListener); panel.addDeleteActionListener(deleteListener);
    }
    @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) { this.editingModelRow = table.convertRowIndexToModel(row); panel.setBackground(table.getSelectionBackground()); panel.setForeground(table.getSelectionForeground()); return panel; }
    @Override public Object getCellEditorValue() { return null; }
    @Override public boolean stopCellEditing() { editingModelRow = -1; return super.stopCellEditing(); }
    @Override public void cancelCellEditing() { editingModelRow = -1; super.cancelCellEditing(); }
}