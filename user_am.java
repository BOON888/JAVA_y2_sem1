
import java.awt.*;
import java.io.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

public class user_am extends JPanel {

    private static final String USER_FILE = "TXT/users.txt";  // Path to the users file
    private JTable userTable, userDetailsTable;  // Tables for user list and user details
    private DefaultTableModel tableModel, detailsTableModel;  // Table models for the above tables
    private JButton updateButton;  // Button to update user details

    private JTextField searchField;  // Text field for searching users by user ID
    private JButton searchButton;  // Button to initiate the search

    // Constructor for setting up the panel and components
    public user_am() {
        setLayout(new BorderLayout());

        // Setting up the main table for user list with columns ID and Actions
        String[] columnNames = {"ID", "Actions"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 1;  // Only the "Actions" column is editable
            }
        };

        userTable = new JTable(tableModel);
        userTable.getColumn("Actions").setCellRenderer(new ButtonRenderer());  // Renderer to display buttons in the Actions column
        userTable.getColumn("Actions").setCellEditor(new ButtonEditor());  // Editor to handle button actions
        userTable.setRowHeight(40);  // Set row height for readability

        // Adding the user table to the panel in a scroll pane
        JScrollPane scrollPane = new JScrollPane(userTable);
        scrollPane.setBorder(new EmptyBorder(20, 20, 20, 20));  // Adding padding to the scroll pane
        add(scrollPane, BorderLayout.CENTER);

        // Setting up the user details table (displayed when a user is selected)
        String[] detailsColumnNames = {"User ID", "Username", "Role", "Contact Number", "Email"};
        detailsTableModel = new DefaultTableModel(detailsColumnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column != 0;  // Only the details (not the ID) are editable
            }
        };
        userDetailsTable = new JTable(detailsTableModel);
        userDetailsTable.setRowHeight(30);  // Set row height for readability
        JScrollPane detailsScrollPane = new JScrollPane(userDetailsTable);
        detailsScrollPane.setBorder(new EmptyBorder(10, 20, 10, 20));

        // Search bar setup (to search for a user by ID)
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchField = new JTextField(15);  // Text field for entering search ID
        searchButton = new JButton("Search");  // Search button

        // Search button action listener (calls searchUser when clicked)
        searchButton.addActionListener(e -> searchUser());

        searchPanel.add(new JLabel("Search by User ID: "));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        add(searchPanel, BorderLayout.NORTH);  // Add search bar to the top of the panel

        // Update button setup to update selected user details
        updateButton = new JButton("Update");
        updateButton.addActionListener(e -> updateUserData());
        JPanel bottomPanel = new JPanel();
        bottomPanel.add(updateButton);

        // Bottom panel to display the user details table and update button
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(detailsScrollPane, BorderLayout.CENTER);
        southPanel.add(bottomPanel, BorderLayout.SOUTH);
        add(southPanel, BorderLayout.SOUTH);

        loadUserData();  // Load all user data when the panel is initialized
    }

    // Method to load all user data from the file and display it in the user table
    private void loadUserData() {
        tableModel.setRowCount(0);  // Clear existing table data
        try (BufferedReader br = new BufferedReader(new FileReader(USER_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length > 0) {
                    tableModel.addRow(new Object[]{data[0], "Buttons"});  // Add user ID and buttons for actions
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error reading user file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Method to search for a user by user ID
    private void searchUser() {
        String searchId = searchField.getText().trim();  // Get the search ID
        if (searchId.isEmpty()) {
            loadUserData();  // If the search field is empty, reload all user data
            return;
        }

        tableModel.setRowCount(0);  // Clear existing table data

        try (BufferedReader br = new BufferedReader(new FileReader(USER_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length > 0 && data[0].equals(searchId)) {
                    tableModel.addRow(new Object[]{data[0], "Buttons"});  // Display the matching user
                    break;  // Only show one user for the search
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error reading user file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Method to view the details of a selected user
    private void viewUser(String userId) {
        detailsTableModel.setRowCount(0);  // Clear existing details
        try (BufferedReader br = new BufferedReader(new FileReader(USER_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 5 && data[0].equals(userId)) {
                    detailsTableModel.addRow(new Object[]{data[0], data[1], data[3], data[4], data[5]});
                    break;  // Stop once the user is found
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error reading user file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Method to update user data based on the entered details
    private void updateUserData() {
        if (detailsTableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No user selected to update.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String userId = detailsTableModel.getValueAt(0, 0).toString();
        String updatedUsername = detailsTableModel.getValueAt(0, 1).toString();
        String updatedRole = detailsTableModel.getValueAt(0, 2).toString();
        String updatedContact = detailsTableModel.getValueAt(0, 3).toString();
        String updatedEmail = detailsTableModel.getValueAt(0, 4).toString();

        File inputFile = new File(USER_FILE);
        StringBuilder updatedContent = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data[0].equals(userId)) {
                    updatedContent.append(userId).append(",").append(updatedUsername).append(",password,")
                            .append(updatedRole).append(",").append(updatedContact).append(",").append(updatedEmail).append("\n");
                } else {
                    updatedContent.append(line).append("\n");  // Keep other users unchanged
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error reading user file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Writing the updated data back to the file
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(inputFile, false))) {
            bw.write(updatedContent.toString().trim());
            JOptionPane.showMessageDialog(this, "User data updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error updating user file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Renderer for buttons in the Actions column
    class ButtonRenderer extends JPanel implements TableCellRenderer {

        public ButtonRenderer() {
            setLayout(new FlowLayout(FlowLayout.RIGHT));  // Align buttons to the right
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton viewButton = new JButton("View");
            JButton deleteButton = new JButton("Delete");
            panel.add(viewButton);
            panel.add(deleteButton);
            return panel;
        }
    }

    // Editor for handling button clicks in the Actions column
    class ButtonEditor extends DefaultCellEditor {

        private final JPanel panel;
        private final JButton viewButton;
        private final JButton deleteButton;
        private String userId;

        public ButtonEditor() {
            super(new JCheckBox());
            panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            viewButton = new JButton("View");
            deleteButton = new JButton("Delete");

            // Action listeners for buttons
            viewButton.addActionListener(e -> {
                fireEditingStopped();
                viewUser(userId);  // View user details
            });

            deleteButton.addActionListener(e -> {
                fireEditingStopped();
                deleteUser(userId);  // Delete user
            });

            panel.add(viewButton);
            panel.add(deleteButton);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            userId = table.getValueAt(row, 0).toString();  // Get user ID from the row
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return "Buttons";  // Return value for the cell (buttons in this case)
        }

        private void deleteUser(String userId) {
            int confirm = JOptionPane.showConfirmDialog(
                    user_am.this,
                    "Delete user " + userId + "?",
                    "Confirm Deletion",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    File inputFile = new File(USER_FILE);
                    StringBuilder newContent = new StringBuilder();

                    // Read all lines except the one to delete
                    try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
                        String line;
                        while ((line = br.readLine()) != null) {
                            if (!line.startsWith(userId + ",")) {
                                newContent.append(line).append("\n");
                            }
                        }
                    }

                    // Write updated content back to file
                    try (BufferedWriter bw = new BufferedWriter(new FileWriter(inputFile))) {
                        bw.write(newContent.toString().trim());
                    }

                    // Refresh UI
                    detailsTableModel.setRowCount(0);
                    loadUserData();
                    JOptionPane.showMessageDialog(user_am.this, "User deleted successfully");

                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(
                            user_am.this,
                            "Error deleting user: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        }
    }
}
