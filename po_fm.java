import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class po_fm extends JFrame {
    public po_fm() {
        // Set up the main frame
        setTitle("Purchase Order Form");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Create main panel with BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create the top panel (PO Form)
        JPanel formPanel = new JPanel(new GridLayout(5, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createTitledBorder("Purchase Order List"));

        // Add components to form panel
        formPanel.add(new JLabel("PO Number:"));
        JTextField poNumberField = new JTextField();
        formPanel.add(poNumberField);

        formPanel.add(new JLabel("Vendor:"));
        JTextField vendorField = new JTextField();
        formPanel.add(vendorField);

        formPanel.add(new JLabel("Order Date:"));
        JTextField orderDateField = new JTextField();
        formPanel.add(orderDateField);

        formPanel.add(new JLabel("Expected Delivery:"));
        JTextField deliveryField = new JTextField();
        formPanel.add(deliveryField);

        JButton submitButton = new JButton("Submit");
        formPanel.add(new JLabel()); // Empty label for spacing
        formPanel.add(submitButton);

        // Create the bottom panel (menu buttons)
        JPanel menuPanel = new JPanel(new GridLayout(1, 5, 10, 0)); // Changed to 5 columns
        menuPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        // Create menu buttons
        JButton itemListBtn = new JButton("Inventory");
        JButton poListBtn = new JButton("PR List");
        JButton inventoryBtn = new JButton("Finance");
        JButton financeRBtn = new JButton("Finance R");
        JButton exitBtn = new JButton("Exit"); // New Exit button

        

        // Style buttons
        Font buttonFont = new Font("Arial", Font.BOLD, 12);
        itemListBtn.setFont(buttonFont);
        poListBtn.setFont(buttonFont);
        inventoryBtn.setFont(buttonFont);
        financeRBtn.setFont(buttonFont);
        exitBtn.setFont(buttonFont);

        // Add buttons to menu panel
        menuPanel.add(itemListBtn);
        menuPanel.add(poListBtn);
        menuPanel.add(inventoryBtn);
        menuPanel.add(financeRBtn);
        menuPanel.add(exitBtn); // Add the Exit button

        // Add panels to main panel
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(menuPanel, BorderLayout.SOUTH);

        // Add action listeners
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Submit functionality here
                JOptionPane.showMessageDialog(po_fm.this, "PO Submitted");
            }
        });

        exitBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        // Add main panel to frame
        add(mainPanel);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new po_fm().setVisible(true);
            }
        });
    }
}