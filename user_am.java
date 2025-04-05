
import java.awt.*;
import java.io.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

public class user_am extends JPanel {

    private static final String USER_FILE = "TXT/users.txt";
    private JTable userTable, userDetailsTable;
    private DefaultTableModel tableModel, detailsTableModel;
    private JButton updateButton;
    private JTextField searchField;
    private JButton searchButton;

    // --- User Info Components ---
    private JTextField usernameField, passwordField, contactField, emailField;
    private JComboBox<String> roleComboBox;
    private JLabel userIdLabel;
    private JButton saveButton;

    private CardLayout cardLayout;
    private JPanel cardPanel;
    private JPanel userListPanel, userInfoPanel;

    public user_am() {
        setLayout(new BorderLayout());
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        // Create both pages (panels)
        userListPanel = createUserListPanel();
        userInfoPanel = createUserInfoPanel();

        // Add panels to CardLayout panel
        cardPanel.add(userListPanel, "UserListPage");
        cardPanel.add(userInfoPanel, "UserInfoPage");

        // Navigation Buttons
        JPanel navigationPanel = new JPanel();
        JButton goToUserListButton = new JButton("Go to User List");
        goToUserListButton.addActionListener(e -> cardLayout.show(cardPanel, "UserListPage"));

        JButton goToUserInfoButton = new JButton("Go to Add New User");
        goToUserInfoButton.addActionListener(e -> cardLayout.show(cardPanel, "UserInfoPage"));

        navigationPanel.add(goToUserListButton);
        navigationPanel.add(goToUserInfoButton);

        // Add the cardPanel and navigation panel to the main layout
        add(navigationPanel, BorderLayout.NORTH);
        add(cardPanel, BorderLayout.CENTER);
    }

    public void viewUser(String userId) {
        detailsTableModel.setRowCount(0); // Clear existing data
        try (BufferedReader br = new BufferedReader(new FileReader(USER_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split("\\|");
                if (data.length >= 6 && data[0].equals(userId)) {
                    detailsTableModel.addRow(new Object[]{data[0], data[1], data[3], data[4], data[5]});
                    break;
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error reading user file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ------------------- User List Panel -------------------
    private JPanel createUserListPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        String[] columnNames = {"ID", "Actions"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 1;
            }
        };

        userTable = new JTable(tableModel);
        userTable.getColumn("Actions").setCellRenderer(new ButtonRenderer());
        userTable.getColumn("Actions").setCellEditor(new ButtonEditor(this));
        userTable.setRowHeight(40);

        JScrollPane scrollPane = new JScrollPane(userTable);
        scrollPane.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.add(scrollPane, BorderLayout.CENTER);

        // Details table
        String[] detailsColumnNames = {"User ID", "Username", "Role", "Contact Number", "Email"};
        detailsTableModel = new DefaultTableModel(detailsColumnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column != 0;
            }
        };
        userDetailsTable = new JTable(detailsTableModel);
        userDetailsTable.setRowHeight(30);
        JScrollPane detailsScrollPane = new JScrollPane(userDetailsTable);
        detailsScrollPane.setBorder(new EmptyBorder(10, 20, 10, 20));

        // Search bar
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchField = new JTextField(15);
        searchButton = new JButton("Search");
        searchButton.addActionListener(e -> searchUser());
        searchPanel.add(new JLabel("Search by User ID: "));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        panel.add(searchPanel, BorderLayout.NORTH);

        // Update button
        updateButton = new JButton("Update");
        updateButton.addActionListener(e -> updateUserData());
        JPanel bottomPanel = new JPanel();
        bottomPanel.add(updateButton);

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(detailsScrollPane, BorderLayout.CENTER);
        southPanel.add(bottomPanel, BorderLayout.SOUTH);
        panel.add(southPanel, BorderLayout.SOUTH);

        loadUserData();
        return panel;
    }

    // ------------------- User Info Panel -------------------
    private JPanel createUserInfoPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Add New User"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        // Labels and fields
        userIdLabel = new JLabel("User ID: " + generateNextUserId());
        usernameField = new JTextField(15);
        passwordField = new JTextField(15);
        contactField = new JTextField(15);
        emailField = new JTextField(15);
        roleComboBox = new JComboBox<>(new String[]{"am", "sm", "fm", "im", "pm"});

        saveButton = new JButton("Save");
        saveButton.addActionListener(e -> saveNewUser());

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("User ID:"), gbc);
        gbc.gridx = 1;
        panel.add(userIdLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        panel.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        panel.add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(new JLabel("Role:"), gbc);
        gbc.gridx = 1;
        panel.add(roleComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(new JLabel("Contact No:"), gbc);
        gbc.gridx = 1;
        panel.add(contactField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        panel.add(emailField, gbc);

        gbc.gridx = 1;
        gbc.gridy++;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(saveButton, gbc);

        return panel;
    }

    // ------------------- Helper Methods -------------------
    private String generateNextUserId() {
        int lastId = 1000;
        try (BufferedReader br = new BufferedReader(new FileReader(USER_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length > 0) {
                    int id = Integer.parseInt(parts[0]);
                    if (id > lastId) {
                        lastId = id;
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("File read error: " + e.getMessage());
        }
        return String.valueOf(lastId + 1);
    }

    private void saveNewUser() {
        String userId = generateNextUserId();
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String role = roleComboBox.getSelectedItem().toString();
        String contact = contactField.getText().trim();
        String email = emailField.getText().trim();

        if (username.isEmpty() || password.isEmpty() || contact.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(USER_FILE, true))) {
            String newUser = userId + "|" + username + "|" + password + "|" + role + "|" + contact + "|" + email;
            bw.write(newUser);
            bw.newLine();
            JOptionPane.showMessageDialog(this, "User added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving user: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Clear form and refresh
        userIdLabel.setText("User ID: " + generateNextUserId());
        usernameField.setText("");
        passwordField.setText("");
        contactField.setText("");
        emailField.setText("");
        roleComboBox.setSelectedIndex(0);
        loadUserData();
    }

    private void loadUserData() {
        tableModel.setRowCount(0);
        try (BufferedReader br = new BufferedReader(new FileReader(USER_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split("\\|");
                if (data.length > 0) {
                    tableModel.addRow(new Object[]{data[0], "Buttons"});
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error reading user file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void searchUser() {
        String searchId = searchField.getText().trim();
        if (searchId.isEmpty()) {
            loadUserData();
            return;
        }

        tableModel.setRowCount(0);
        try (BufferedReader br = new BufferedReader(new FileReader(USER_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split("\\|");
                if (data.length > 0 && data[0].equals(searchId)) {
                    tableModel.addRow(new Object[]{data[0], "Buttons"});
                    break;
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error reading user file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

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
                String[] data = line.split("\\|");
                if (data[0].equals(userId)) {
                    updatedContent.append(userId).append("|").append(updatedUsername).append("|password|")
                            .append(updatedRole).append("|").append(updatedContact).append("|").append(updatedEmail).append("\n");
                } else {
                    updatedContent.append(line).append("\n");
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error reading user file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(inputFile, false))) {
            bw.write(updatedContent.toString().trim());
            JOptionPane.showMessageDialog(this, "User data updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error updating user file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        loadUserData();
    }

    // ------------ Renderer/Editor Classes ------------
    class ButtonRenderer extends JPanel implements TableCellRenderer {

        public ButtonRenderer() {
            setLayout(new FlowLayout(FlowLayout.RIGHT));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            panel.add(new JButton("View"));
            panel.add(new JButton("Delete"));
            return panel;
        }
    }

    class ButtonEditor extends DefaultCellEditor {

        private final JPanel panel;
        private final JButton viewButton;
        private final JButton deleteButton;
        private String userId;

        public ButtonEditor(user_am aThis) {
            super(new JCheckBox());
            panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            viewButton = new JButton("View");
            deleteButton = new JButton("Delete");

            viewButton.addActionListener(e -> {
                fireEditingStopped();
                viewUser(userId);
            });

            deleteButton.addActionListener(e -> {
                fireEditingStopped();
                deleteUser(userId);
            });

            panel.add(viewButton);
            panel.add(deleteButton);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            userId = table.getValueAt(row, 0).toString();
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return "Buttons";
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

                    try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
                        String line;
                        while ((line = br.readLine()) != null) {
                            if (!line.startsWith(userId + "|")) {
                                newContent.append(line).append("\n");
                            }
                        }
                    }

                    try (BufferedWriter bw = new BufferedWriter(new FileWriter(inputFile))) {
                        bw.write(newContent.toString().trim());
                    }

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
