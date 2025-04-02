
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

public class user_am extends JPanel {

    private static final String USER_FILE = "TXT/users.txt";
    private JTabbedPane tabbedPane;
    private JPanel userInfoPanel;
    private JPanel userListPanel;

    // User Info Components
    private JTextField usernameField;
    private JTextField passwordField;
    private JTextField roleField;
    private JTextField contactField;
    private JTextField emailField;
    private JButton addButton;

    public user_am() {
        setLayout(new BorderLayout());

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 18));

        userInfoPanel = createUserInfoPanel();
        tabbedPane.addTab("User Info", userInfoPanel);

        userListPanel = new JPanel(); // Blank user list panel
        tabbedPane.addTab("User List", userListPanel);

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createUserInfoPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JLabel usernameLabel = new JLabel("Username:");
        usernameField = new JTextField(15);

        JLabel passwordLabel = new JLabel("Password:");
        passwordField = new JTextField(15);

        JLabel roleLabel = new JLabel("Role:");
        roleField = new JTextField(15);

        JLabel contactLabel = new JLabel("Contact:");
        contactField = new JTextField(15);

        JLabel emailLabel = new JLabel("Email:");
        emailField = new JTextField(15);

        addButton = new JButton("Add User");

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(usernameLabel, gbc);
        gbc.gridx = 1;
        panel.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(passwordLabel, gbc);
        gbc.gridx = 1;
        panel.add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(roleLabel, gbc);
        gbc.gridx = 1;
        panel.add(roleField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(contactLabel, gbc);
        gbc.gridx = 1;
        panel.add(contactField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        panel.add(emailLabel, gbc);
        gbc.gridx = 1;
        panel.add(emailField, gbc);

        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(addButton, gbc);

        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addUser();
            }
        });

        return panel;
    }

    private void addUser() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String role = roleField.getText();
        String contact = contactField.getText();
        String email = emailField.getText();

        if (username.isEmpty() || password.isEmpty() || role.isEmpty() || contact.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields must be filled!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int newUserId = generateNewUserId();
        saveUserData(newUserId, username, password, role, contact, email);

        // Clear fields after adding
        usernameField.setText("");
        passwordField.setText("");
        roleField.setText("");
        contactField.setText("");
        emailField.setText("");
    }

    private int generateNewUserId() {
        int maxUserId = 1000;
        try (BufferedReader br = new BufferedReader(new FileReader(USER_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length > 0) {
                    try {
                        int userId = Integer.parseInt(data[0].trim());
                        if (userId > maxUserId) {
                            maxUserId = userId;
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading user file: " + e.getMessage());
        }
        return maxUserId + 1;
    }

    private void saveUserData(int userId, String username, String password, String role, String contact, String email) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(USER_FILE, true))) {
            bw.write(userId + "," + username + "," + password + "," + role + "," + contact + "," + email);
            bw.newLine();
        } catch (IOException e) {
            System.err.println("Error writing to user file: " + e.getMessage());
        }
    }
}
