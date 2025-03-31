
import java.awt.*;
import javax.swing.*;

public class pm extends JPanel {

    private JPanel contentPanel;
    private JFrame mainFrame;

    public pm(JFrame frame) {
        this.mainFrame = frame;
        setLayout(new BorderLayout());

        // Title Label
        JLabel titleLabel = new JLabel("WELCOME PURCHASING MANAGER", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));

        // Container for dynamic content
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(titleLabel, BorderLayout.CENTER);

        // Panel for bottom buttons
        JPanel bottomPanel = new JPanel(new GridLayout(1, 5)); // 4 buttons + 1 Exit button

        // Creating buttons
        String[] buttonNames = {"Item List", "Supplier List", "PR List", "PO"};
        String[] classNames = {"item_v", "supplier_v", "pr_pm", "po_e"};

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
