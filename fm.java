
import java.awt.*;
import javax.swing.*;

public class fm extends JPanel {

    private JPanel contentPanel;
    private JFrame mainFrame;

    public fm(JFrame frame) {
        this.mainFrame = frame;
        setLayout(new BorderLayout());

        // Title Label
        JLabel titleLabel = new JLabel("WELCOME FINANCE MANAGER", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));

        // Container for dynamic content
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(titleLabel, BorderLayout.CENTER);

        // Panel for bottom buttons
        JPanel bottomPanel = new JPanel(new GridLayout(1, 6)); // 5 buttons + 1 Exit button

        // Creating buttons
        String[] buttonNames = {"PO", "Inventory", "PR List", "Finance", "Finance R"};
        String[] classNames = {"po_fm", "inventory_fm", "pr_v", "finance_e", "finance_r"};

        for (int i = 0; i < buttonNames.length; i++) {
            JButton button = new JButton(buttonNames[i]);
            final String className = classNames[i];
            button.addActionListener(e -> switchContent(className));
            bottomPanel.add(button);
        }

        // Exit button
        JButton exitButton = new JButton("Exit");
        exitButton.addActionListener(e -> exitToLogin());
        bottomPanel.add(exitButton);

        add(contentPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void switchContent(String className) {
        try {
            contentPanel.removeAll();
            Class<?> clazz = Class.forName(className);
            JPanel panel = (JPanel) clazz.getDeclaredConstructor().newInstance();
            contentPanel.add(panel, BorderLayout.CENTER);
            contentPanel.revalidate();
            contentPanel.repaint();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error opening " + className, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exitToLogin() {
        mainFrame.getContentPane().removeAll();
        mainFrame.add(new login((frame) mainFrame));
        mainFrame.revalidate();
        mainFrame.repaint();
    }
}
