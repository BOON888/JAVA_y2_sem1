import javax.swing.*;
import javax.swing.border.EmptyBorder;
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


public class pr_e extends JPanel {

    private static final String PR_FILE = "TXT/pr.txt";
    private static final String PR_INFO_CARD = "INFO";
    private static final String PR_LIST_CARD = "LIST";

    // --- Main Layout ---
    private CardLayout cardLayout;
    private JPanel cardPanel; // Panel holding the different views (Info, List)
    private JPanel topButtonPanel; // Panel for PR Info/PR List buttons

    // --- PR Info Components ---
    private JTextField itemIDField, supplierIDField, quantityField, requiredDateField;
    private JComboBox<String> raisedByDropdown;
    private JButton addPrButton;

    // --- PR List Components ---
    private JTable prTable; // Top table (PR ID, Actions)
    private JTable prDetailsTable; // Bottom table (Full details)
    private DefaultTableModel tableModel; // Model for prTable
    private DefaultTableModel detailsTableModel; // Model for prDetailsTable
    private JButton updateButton;
    private JTextField searchField;
    private JButton searchButton;
    private TableRowSorter<DefaultTableModel> sorter; // Sorter for prTable
    private List<String[]> fullPrData; // To store all data read from file

    public pr_e() {
        setLayout(new BorderLayout(0, 5)); // Main layout with vertical gap
        fullPrData = new ArrayList<>();

        createTopButtons(); // Create the PR Info/PR List buttons
        createCardPanel(); // Create the panel that switches between views
        createPrInfoPanel(); // Create the content for the PR Info view
        createPrListPanel(); // Create the content for the PR List view

        add(topButtonPanel, BorderLayout.NORTH);
        add(cardPanel, BorderLayout.CENTER);

        loadPRData(); // Load initial data
        showCard(PR_INFO_CARD); // Show PR Info page initially
    }

    private void createTopButtons() {
        topButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        topButtonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));

        JButton infoButton = new JButton("PR Info");
        JButton listButton = new JButton("PR List");

        infoButton.addActionListener(e -> showCard(PR_INFO_CARD));
        listButton.addActionListener(e -> {
            loadPRData(); // Reload data when switching to list view
            showCard(PR_LIST_CARD);
        });

        topButtonPanel.add(infoButton);
        topButtonPanel.add(listButton);
    }

    private void createCardPanel() {
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
    }

    private void showCard(String cardName) {
        cardLayout.show(cardPanel, cardName);
    }

    // =================================================
    // --- PR Info Panel Creation ---
    // =================================================
    private void createPrInfoPanel() {
        JPanel infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Labels and Fields for PR Info
        JLabel itemLabel = new JLabel("Item ID:");
        itemIDField = new JTextField(15);

        JLabel supplierLabel = new JLabel("Supplier ID:");
        supplierIDField = new JTextField(15);

        JLabel quantityLabel = new JLabel("Quantity Requested:");
        quantityField = new JTextField(15);

        JLabel dateLabel = new JLabel("Required Date (DD-MM-YYYY):");
        requiredDateField = new JTextField(15); // Consider JDatePicker

        JLabel raisedByLabel = new JLabel("Raised By:");
        raisedByDropdown = new JComboBox<>(new String[]{"Sales Manager", "Administrator"}); // Add roles as needed

        // Layout components using GridBagLayout
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0; infoPanel.add(itemLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0; infoPanel.add(itemIDField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0; infoPanel.add(supplierLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0; infoPanel.add(supplierIDField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.0; infoPanel.add(quantityLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 1.0; infoPanel.add(quantityField, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.0; infoPanel.add(dateLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 3; gbc.weightx = 1.0; infoPanel.add(requiredDateField, gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0.0; infoPanel.add(raisedByLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 4; gbc.weightx = 1.0; infoPanel.add(raisedByDropdown, gbc);

        // Add PR Button
        addPrButton = new JButton("Add Purchase Requisition");
        addPrButton.setFont(new Font("Arial", Font.BOLD, 14));
        addPrButton.addActionListener(e -> addPR());
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.weightx = 0;
        gbc.insets = new Insets(20, 8, 8, 8); // More top margin for button
        infoPanel.add(addPrButton, gbc);

        // Filler to push components up
        gbc.gridy = 6; gbc.weighty = 1.0;
        infoPanel.add(new JLabel(""), gbc);

        cardPanel.add(infoPanel, PR_INFO_CARD); // Add this panel to the CardLayout
    }

    // =================================================
    // --- PR List Panel Creation ---
    // =================================================
    private void createPrListPanel() {
        JPanel listPanel = new JPanel(new BorderLayout(10, 10));
        listPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- Search Panel ---
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchPanel.add(new JLabel("Search by PR ID:"));
        searchField = new JTextField(15);
        searchButton = new JButton("Search");
        searchButton.setFont(new Font("Arial", Font.BOLD, 12));
        searchButton.addActionListener(e -> searchPR());
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        listPanel.add(searchPanel, BorderLayout.NORTH);
        // --------------------

        // --- Center Panel holding both tables ---
        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 0, 10)); // 2 rows, 1 column, vertical gap

        // --- Top Table (PR List) ---
        String[] columnNames = {"PR ID", "Actions"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int row, int column) { return column == 1; } // Actions editable
            @Override public Class<?> getColumnClass(int columnIndex) { return columnIndex == 1 ? JPanel.class : String.class; }
        };
        prTable = new JTable(tableModel);
        prTable.setRowHeight(35);
        prTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        sorter = new TableRowSorter<>(tableModel);
        prTable.setRowSorter(sorter);
        // Set up Actions column
        TableColumn actionsCol = prTable.getColumnModel().getColumn(1);
        actionsCol.setCellRenderer(new ButtonRenderer());
        actionsCol.setCellEditor(new ButtonEditor()); // Using adapted ButtonEditor
        actionsCol.setMinWidth(140);
        actionsCol.setPreferredWidth(150);
        prTable.getColumnModel().getColumn(0).setPreferredWidth(100); // PR ID width
        prTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

        JScrollPane topScrollPane = new JScrollPane(prTable);
        topScrollPane.setBorder(BorderFactory.createTitledBorder("Purchase Requisitions"));
        centerPanel.add(topScrollPane);
        // -------------------------

        // --- Bottom Table (PR Details) ---
        String[] detailsColumnNames = {"PR ID", "Item ID", "Supplier ID", "Quantity Requested", "Required Date", "Raised By", "Status"};
        detailsTableModel = new DefaultTableModel(detailsColumnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Allow editing for Item ID, Supplier ID, Quantity, Required Date, Raised By
                // Columns 0 (PR ID) and 6 (Status) are NOT editable.
                return column > 0 && column < 6;
            }
        };
        prDetailsTable = new JTable(detailsTableModel);
        prDetailsTable.setRowHeight(30);
        prDetailsTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        prDetailsTable.setFont(new Font("Arial", Font.PLAIN, 12));
        // Add ComboBox for Raised By column if needed for editing
        // TableColumn raisedByCol = prDetailsTable.getColumnModel().getColumn(5);
        // raisedByCol.setCellEditor(new DefaultCellEditor(new JComboBox<>(new String[]{"1002", "1001"}))); // Use IDs for editing

        JScrollPane detailsScrollPane = new JScrollPane(prDetailsTable);
        detailsScrollPane.setBorder(BorderFactory.createTitledBorder("Selected PR Details (Editable)"));
        centerPanel.add(detailsScrollPane);
        // -----------------------------

        listPanel.add(centerPanel, BorderLayout.CENTER);

        // --- Bottom Button Panel ---
        JPanel bottomButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        updateButton = new JButton("Update Selected PR Details");
        updateButton.addActionListener(e -> updatePRData());
        bottomButtonPanel.add(updateButton);
        listPanel.add(bottomButtonPanel, BorderLayout.SOUTH);
        // ------------------------

        cardPanel.add(listPanel, PR_LIST_CARD); // Add this panel to the CardLayout
    }

    // =================================================
    // --- Data Handling Methods ---
    // =================================================

    private void loadPRData() {
        tableModel.setRowCount(0);
        fullPrData.clear();
        File file = new File(PR_FILE);
        if (!file.exists()) { /* Handle file creation if needed */ return; }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] data = line.split("\\|");
                // File: pr_id|item_id|supplier_id|quantity|req_date|raised_by_id|status
                if (data.length >= 7) { // Need at least 7 fields
                    fullPrData.add(data);
                    tableModel.addRow(new Object[]{data[0], "View/Delete"}); // Add only PR ID to top table
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error reading PR file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
         // Clear details table whenever main list is loaded/reloaded
         if (detailsTableModel != null) {
             detailsTableModel.setRowCount(0);
         }
    }

     private void addPR() {
        // --- Validation ---
        String itemID = itemIDField.getText().trim();
        String supplierID = supplierIDField.getText().trim();
        String quantityStr = quantityField.getText().trim();
        String requiredDate = requiredDateField.getText().trim();
        String raisedByRole = (String) raisedByDropdown.getSelectedItem();

        if (itemID.isEmpty() || supplierID.isEmpty() || quantityStr.isEmpty() || requiredDate.isEmpty() || raisedByRole == null) {
            JOptionPane.showMessageDialog(this, "Please fill all fields!", "Input Error", JOptionPane.ERROR_MESSAGE); return;
        }
        int quantity;
        try { quantity = Integer.parseInt(quantityStr); if (quantity <= 0) throw new NumberFormatException(); }
        catch (NumberFormatException e) { JOptionPane.showMessageDialog(this, "Quantity must be a positive number.", "Input Error", JOptionPane.ERROR_MESSAGE); return; }
        if (!requiredDate.matches("\\d{2}-\\d{2}-\\d{4}")) { JOptionPane.showMessageDialog(this, "Required Date must be DD-MM-YYYY.", "Input Error", JOptionPane.ERROR_MESSAGE); return; }
        // --- End Validation ---

        String raisedByID = mapRoleToID(raisedByRole); // Map role to ID
        String status = "Pending"; // Default status
        int prID = generatePrID();
        if (prID < 0) { JOptionPane.showMessageDialog(this, "Could not generate PR ID.", "Error", JOptionPane.ERROR_MESSAGE); return; }
        String formattedPrID = String.format("%04d", prID);

        // Format: pr_id|item_id|supplier_id|quantity|req_date|raised_by_id|status
        String newPR = String.join("|", formattedPrID, itemID, supplierID, String.valueOf(quantity), requiredDate, raisedByID, status);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(PR_FILE, true))) {
            writer.write(newPR);
            writer.newLine();
            JOptionPane.showMessageDialog(this, "Purchase Requisition (PR ID: " + formattedPrID + ") added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);

            // Clear input fields
            itemIDField.setText(""); supplierIDField.setText(""); quantityField.setText("");
            requiredDateField.setText(""); raisedByDropdown.setSelectedIndex(0);

            loadPRData(); // Reload data in the list view
            showCard(PR_LIST_CARD); // Switch to the list view

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving PR: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void searchPR() {
        String searchId = searchField.getText().trim();
        if (searchId.isEmpty()) {
            sorter.setRowFilter(null); // Clear filter if search is empty
        } else {
            try {
                // Filter PR ID column (index 0) case-insensitive
                RowFilter<DefaultTableModel, Object> rf = RowFilter.regexFilter("(?i)^" + Pattern.quote(searchId) + "$", 0); // Exact match
                // Or starts with: RowFilter.regexFilter("(?i)^" + Pattern.quote(searchId), 0);
                sorter.setRowFilter(rf);
            } catch (java.util.regex.PatternSyntaxException e) {
                JOptionPane.showMessageDialog(this, "Invalid search pattern", "Search Error", JOptionPane.ERROR_MESSAGE);
                sorter.setRowFilter(null);
            }
        }
         // Clear details when searching
         detailsTableModel.setRowCount(0);
    }

    // Called by ButtonEditor's View button
    public void viewPR(String prId) {
        detailsTableModel.setRowCount(0); // Clear previous details
        String[] dataToView = null;
        // Find the full data corresponding to the prId
        for (String[] data : fullPrData) {
            if (data.length > 0 && data[0].equals(prId)) {
                dataToView = data;
                break;
            }
        }

        if (dataToView != null && dataToView.length >= 7) {
            // Populate the details table with the single row found
             detailsTableModel.addRow(new Object[]{
                 dataToView[0], // PR ID
                 dataToView[1], // Item ID
                 dataToView[2], // Supplier ID
                 dataToView[3], // Quantity
                 dataToView[4], // Required Date
                 dataToView[5], // Raised By ID (Consider mapping back to Role Name for display if needed)
                 dataToView[6]  // Status
             });
        } else {
             JOptionPane.showMessageDialog(this, "Could not find details for PR ID: " + prId, "Error", JOptionPane.WARNING_MESSAGE);
        }
    }

     // Called by ButtonEditor's Delete button
     public void deletePR(String prId) {
         int confirm = JOptionPane.showConfirmDialog(this,"Delete PR " + prId + "?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
         if (confirm == JOptionPane.YES_OPTION) {
             if (deleteRecordFromFile(PR_FILE, prId)) {
                 detailsTableModel.setRowCount(0); // Clear details table
                 loadPRData(); // Reload the main table data
                 JOptionPane.showMessageDialog(this, "PR " + prId + " deleted successfully.");
             } else {
                  JOptionPane.showMessageDialog(this, "Error deleting PR " + prId + " from file.", "Error", JOptionPane.ERROR_MESSAGE);
             }
         }
     }

    private void updatePRData() {
        if (prDetailsTable.isEditing()) {
            prDetailsTable.getCellEditor().stopCellEditing(); // Ensure edits are saved to model
        }

        if (detailsTableModel.getRowCount() != 1) {
            JOptionPane.showMessageDialog(this, "Please view a single PR in the details table before updating.", "Update Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Get data from the details table (which should have only 1 row)
        String prId = detailsTableModel.getValueAt(0, 0).toString();
        String updatedItemId = detailsTableModel.getValueAt(0, 1).toString();
        String updatedSupplierId = detailsTableModel.getValueAt(0, 2).toString();
        String updatedQuantity = detailsTableModel.getValueAt(0, 3).toString();
        String updatedRequiredDate = detailsTableModel.getValueAt(0, 4).toString();
        String updatedRaisedBy = detailsTableModel.getValueAt(0, 5).toString(); // This might be Role name or ID depending on editor
        String updatedStatus = detailsTableModel.getValueAt(0, 6).toString(); // Status shouldn't change here usually

        // --- Optional: Add validation for the updated data ---
        if (updatedItemId.isEmpty() || updatedSupplierId.isEmpty() || updatedQuantity.isEmpty() || updatedRequiredDate.isEmpty() || updatedRaisedBy.isEmpty()) {
             JOptionPane.showMessageDialog(this, "Updated fields cannot be empty.", "Validation Error", JOptionPane.ERROR_MESSAGE); return;
        }
        try { int qty = Integer.parseInt(updatedQuantity); if(qty <= 0) throw new NumberFormatException(); }
        catch (NumberFormatException e) { JOptionPane.showMessageDialog(this, "Updated quantity must be a positive number.", "Validation Error", JOptionPane.ERROR_MESSAGE); return; }
        if (!updatedRequiredDate.matches("\\d{2}-\\d{2}-\\d{4}")) { JOptionPane.showMessageDialog(this, "Updated date must be DD-MM-YYYY.", "Validation Error", JOptionPane.ERROR_MESSAGE); return; }
         // Ensure raisedBy is a valid ID if needed
         // String raisedByIdToSave = mapRoleToID(updatedRaisedBy); // Map back if it was displayed as role name
         String raisedByIdToSave = updatedRaisedBy; // Assuming it's already an ID from the table model
        // --- End Validation ---

        // Create the updated line string based on file format
        String updatedLine = String.join("|", prId, updatedItemId, updatedSupplierId, updatedQuantity, updatedRequiredDate, raisedByIdToSave, updatedStatus);

        if (updateRecordInFile(PR_FILE, prId, updatedLine)) {
             JOptionPane.showMessageDialog(this, "PR " + prId + " updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
             detailsTableModel.setRowCount(0); // Clear details table
             loadPRData(); // Reload main table data
        } else {
             JOptionPane.showMessageDialog(this, "Error updating PR " + prId + " in file.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // =================================================
    // --- Helper Methods ---
    // =================================================

    // Generic method to delete a record identified by the first field (ID)
    private boolean deleteRecordFromFile(String filename, String idToDelete) {
        File inputFile = new File(filename);
        File tempFile;
        try { tempFile = File.createTempFile("temp_del_", ".txt", inputFile.getParentFile()); }
        catch (IOException e) { System.err.println("Could not create temporary file: " + e); return false; }

        boolean found = false;
        if (!inputFile.exists()) return false;

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] data = line.split("\\|");
                if (data.length > 0 && data[0].equals(idToDelete)) {
                    found = true; // Mark as found, do not write to temp file
                } else {
                    writer.write(line); writer.newLine();
                }
            }
        } catch (IOException e) { System.err.println("Error processing file for deletion: " + e); if(tempFile.exists()) tempFile.delete(); return false; }

        if (!found) { if(tempFile.exists()) tempFile.delete(); return false; } // ID wasn't in file

        // Replace original file
        try {
            if (!inputFile.delete()) { System.gc(); Thread.sleep(100); if (!inputFile.delete()) throw new IOException("Could not delete original: " + inputFile.getName()); }
            if (!tempFile.renameTo(inputFile)) { /* Add copy fallback if needed */ throw new IOException("Could not rename temp file"); }
            return true; // Success
        } catch (IOException | SecurityException | InterruptedException e) { System.err.println("Error replacing file: " + e); return false; }
    }

    // Generic method to update a record identified by the first field (ID)
     private boolean updateRecordInFile(String filename, String idToUpdate, String newLine) {
         File inputFile = new File(filename);
         File tempFile;
         try { tempFile = File.createTempFile("temp_upd_", ".txt", inputFile.getParentFile()); }
         catch (IOException e) { System.err.println("Could not create temporary file: " + e); return false; }

         boolean found = false;
         if (!inputFile.exists()) return false;

         try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
              BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
             String line;
             while ((line = reader.readLine()) != null) {
                 if (line.trim().isEmpty()) continue;
                 String[] data = line.split("\\|");
                 if (data.length > 0 && data[0].equals(idToUpdate)) {
                     writer.write(newLine); // Write the updated line
                     writer.newLine();
                     found = true;
                 } else {
                     writer.write(line); writer.newLine(); // Write other lines as they are
                 }
             }
         } catch (IOException e) { System.err.println("Error processing file for update: " + e); if(tempFile.exists()) tempFile.delete(); return false; }

         if (!found) { if(tempFile.exists()) tempFile.delete(); return false; } // ID wasn't in file

         // Replace original file
         try {
             if (!inputFile.delete()) { System.gc(); Thread.sleep(100); if (!inputFile.delete()) throw new IOException("Could not delete original: " + inputFile.getName()); }
             if (!tempFile.renameTo(inputFile)) { /* Add copy fallback if needed */ throw new IOException("Could not rename temp file"); }
             return true; // Success
         } catch (IOException | SecurityException | InterruptedException e) { System.err.println("Error replacing file: " + e); return false; }
     }


    // Map role name to user ID (adjust IDs as needed)
    private String mapRoleToID(String role) {
        return switch (role) {
            case "Sales Manager" -> "1002"; // From example data
            case "Administrator" -> "1001"; // Assumption
            // Add other roles as needed
            default -> "UNKNOWN"; // Or handle error
        };
    }

     // Map user ID back to Role Name (example, might be needed for display)
     private String mapIDToRole(String userID) {
         return switch (userID) {
             case "1002" -> "Sales Manager";
             case "1001" -> "Administrator";
             default -> "Unknown (" + userID + ")";
         };
     }

    // Generate next PR ID (ensure it's 4 digits starting from 5001)
    private int generatePrID() {
        int maxID = 5000; // Start checking from 5000
        File file = new File(PR_FILE);
        if (!file.exists()) return 5001; // First ID

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
            JOptionPane.showMessageDialog(this, "Error reading PR file for ID generation: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return -1; // Indicate error
        }
        return maxID + 1;
    }

    // =================================================
    // --- Main Method for Testing ---
    // =================================================
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception e) { System.err.println("Couldn't set system L&F."); }

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Purchase Requisition Management");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.getContentPane().add(new pr_e());
            frame.setMinimumSize(new Dimension(800, 700)); // Adjusted height for two tables + update button
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }


    // =================================================
    // --- Inner Classes for Table Buttons ---
    // =================================================

    // Renders the View/Delete button panel
    class ButtonRenderer extends JPanel implements TableCellRenderer {
        final JButton viewButton = new JButton("View");
        final JButton deleteButton = new JButton("Delete");

        public ButtonRenderer() {
            super(new FlowLayout(FlowLayout.CENTER, 5, 2)); // Center buttons
            setOpaque(true);
            viewButton.setMargin(new Insets(2, 5, 2, 5));
            deleteButton.setMargin(new Insets(2, 5, 2, 5));
            add(viewButton);
            add(deleteButton);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            // Set background based on selection
            if (isSelected) {
                setForeground(table.getSelectionForeground());
                setBackground(table.getSelectionBackground());
            } else {
                setForeground(table.getForeground());
                setBackground(UIManager.getColor("Button.background"));
            }
            return this;
        }
    }

    // Handles clicks on View/Delete buttons in the table cell
    class ButtonEditor extends AbstractCellEditor implements TableCellEditor {
        private final JPanel panel;
        private final JButton viewButton;
        private final JButton deleteButton;
        private String currentPrId; // Store PR ID of the row being edited
        private transient ActionListener viewListener, deleteListener;

        public ButtonEditor() {
            super(); // No need for checkbox arg with AbstractCellEditor
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 2));
            viewButton = new JButton("View");
            deleteButton = new JButton("Delete");
            viewButton.setMargin(new Insets(2, 5, 2, 5));
            deleteButton.setMargin(new Insets(2, 5, 2, 5));

            viewListener = e -> {
                 if (currentPrId != null) {
                     SwingUtilities.invokeLater(() -> { // Ensure actions run after editing stops
                         viewPR(currentPrId);
                     });
                     fireEditingStopped();
                 }
            };

             deleteListener = e -> {
                 if (currentPrId != null) {
                     // Store ID before stopping edit, as table might change
                     String idToDelete = currentPrId;
                      SwingUtilities.invokeLater(() -> {
                         deletePR(idToDelete);
                     });
                     fireEditingStopped();
                 }
             };

            viewButton.addActionListener(viewListener);
            deleteButton.addActionListener(deleteListener);

            panel.add(viewButton);
            panel.add(deleteButton);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            // Get the PR ID from the first column of the current row
            currentPrId = table.getValueAt(row, 0).toString();
            panel.setBackground(table.getSelectionBackground()); // Match selection color
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            // Value stored in the model doesn't change
            return "View/Delete";
        }

         @Override
        public boolean stopCellEditing() {
             currentPrId = null; // Clear stored ID when editing stops
             return super.stopCellEditing();
        }

        @Override
        public void cancelCellEditing() {
             currentPrId = null; // Clear stored ID when editing is cancelled
             super.cancelCellEditing();
        }

         // Clean up listeners when editor is removed (optional but good practice)
         @Override
         protected void fireEditingStopped() {
             super.fireEditingStopped();
             // No need to remove listeners here if using AbstractCellEditor correctly
         }
    }
}