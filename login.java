
import java.awt.*;
import javax.swing.*;

public class login extends JPanel {

    public login(frame mainFrame) {
        setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("WELCOME TO LOGIN", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 50));
        add(titleLabel, BorderLayout.CENTER);

        // Button declarations
        JButton amButton = createRoleButton("AM", mainFrame);
        JButton fmButton = createRoleButton("FM", mainFrame);
        JButton imButton = createRoleButton("IM", mainFrame);
        JButton smButton = createRoleButton("SM", mainFrame);
        JButton pmButton = createRoleButton("PM", mainFrame);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(amButton);
        buttonPanel.add(fmButton);
        buttonPanel.add(imButton);
        buttonPanel.add(smButton);
        buttonPanel.add(pmButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JButton createRoleButton(String role, frame mainFrame) {
        JButton button = new JButton(role);
        button.setFont(new Font("Arial", Font.BOLD, 15));
        button.setPreferredSize(new Dimension(100, 30));

        button.addActionListener(e -> {
            String password = JOptionPane.showInputDialog(null, "Enter password for " + role + ":");

            if (password != null) {
                login_c loginCheck = new login_c(role, password);

                if (loginCheck.authenticate()) {
                    JPanel rolePanel = getRolePanel(role, mainFrame);
                    if (rolePanel != null) {
                        mainFrame.switchPanel(rolePanel);
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Invalid password or role. Try again.", "Login Failed", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        return button;
    }

    private JPanel getRolePanel(String role, frame mainFrame) {
        switch (role) {
            case "AM":
                return new am(mainFrame);
            case "FM":
                return new fm(mainFrame);
            case "IM":
                return new im(mainFrame);
            case "SM":
                return new sm(mainFrame);
            case "PM":
                return new pm(mainFrame);
            default:
                return null;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            frame mainFrame = new frame();
            mainFrame.switchPanel(new login(mainFrame));
        });
    }
}
