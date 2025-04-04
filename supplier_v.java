import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class supplier_v extends JDialog {
    private JTextField nameField, contactField, emailField, addressField, itemField;
    private boolean saved = false;
    private String[] editedData;

    public supplier_v(JFrame parent, String name, String contact, String email, String address, String item) {
        super(parent, "Edit Supplier Details", true);
        setSize(600, 400);
        setLocationRelativeTo(parent);
        setResizable(false);

        // Main panel with modern styling
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(new Color(240, 240, 240));

        // Form panel with grid layout
        JPanel formPanel = new JPanel(new GridLayout(5, 2, 15, 15));
        formPanel.setBackground(new Color(240, 240, 240));

        // Styled labels
        Font labelFont = new Font("Segoe UI", Font.BOLD, 14);
        Color labelColor = new Color(70, 70, 70);

        // Create form fields with consistent styling
        nameField = createStyledField(name);
        contactField = createStyledField(contact);
        emailField = createStyledField(email);
        addressField = createStyledField(address);
        itemField = createStyledField(item);

        // Add form components
        addFormRow(formPanel, "Supplier Name:", nameField, labelFont, labelColor);
        addFormRow(formPanel, "Contact Number:", contactField, labelFont, labelColor);
        addFormRow(formPanel, "Email Address:", emailField, labelFont, labelColor);
        addFormRow(formPanel, "Physical Address:", addressField, labelFont, labelColor);
        addFormRow(formPanel, "Supplied Items:", itemField, labelFont, labelColor);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Button panel with modern buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        buttonPanel.setBackground(new Color(240, 240, 240));

        JButton saveButton = createStyledButton("Save Changes", new Color(76, 175, 80));
        saveButton.addActionListener(this::saveAction);

        JButton cancelButton = createStyledButton("Cancel", new Color(244, 67, 54));
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private JTextField createStyledField(String text) {
        JTextField field = new JTextField(text);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        field.setBackground(Color.WHITE);
        return field;
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void addFormRow(JPanel panel, String labelText, JTextField field, Font font, Color color) {
        JLabel label = new JLabel(labelText);
        label.setFont(font);
        label.setForeground(color);
        panel.add(label);
        panel.add(field);
    }

    private void saveAction(ActionEvent e) {
        if (validateFields()) {
            saved = true;
            editedData = new String[]{
                nameField.getText().trim(),
                contactField.getText().trim(),
                emailField.getText().trim(),
                addressField.getText().trim(),
                itemField.getText().trim()
            };
            dispose();
        }
    }

    private boolean validateFields() {
        if (nameField.getText().trim().isEmpty()) {
            showValidationError("Supplier name cannot be empty");
            return false;
        }
        if (contactField.getText().trim().isEmpty()) {
            showValidationError("Contact number cannot be empty");
            return false;
        }
        if (emailField.getText().trim().isEmpty() || !emailField.getText().contains("@")) {
            showValidationError("Please enter a valid email address");
            return false;
        }
        return true;
    }

    private void showValidationError(String message) {
        JOptionPane.showMessageDialog(this, message, "Validation Error", JOptionPane.WARNING_MESSAGE);
    }

    public boolean isSaved() {
        return saved;
    }

    public String[] getEditedData() {
        return editedData;
    }
}