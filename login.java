
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

public class login extends JPanel {

    public login(frame mainFrame) { // Accept main frame
        setLayout(new BorderLayout());

        // Title Label
        JLabel titleLabel = new JLabel("WELCOME TO LOGIN", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 50));
        add(titleLabel, BorderLayout.CENTER);

        // Creating buttons
        JButton amButton = new JButton("AM");
        JButton fmButton = new JButton("FM");
        JButton imButton = new JButton("IM");
        JButton smButton = new JButton("SM");
        JButton pmButton = new JButton("PM");

        // Adjust button font size and preferred size
        Font buttonFont = new Font("Arial", Font.BOLD, 15);
        amButton.setFont(buttonFont);
        fmButton.setFont(buttonFont);
        imButton.setFont(buttonFont);
        smButton.setFont(buttonFont);
        pmButton.setFont(buttonFont);

        amButton.setPreferredSize(new Dimension(100, 30));  // Set preferred size for buttons
        fmButton.setPreferredSize(new Dimension(100, 30));
        imButton.setPreferredSize(new Dimension(100, 30));
        smButton.setPreferredSize(new Dimension(100, 30));
        pmButton.setPreferredSize(new Dimension(100, 30));

        // Adding action listeners to buttons 
        amButton.addActionListener(new NavigationListener(mainFrame, new am(mainFrame)));
        fmButton.addActionListener(new NavigationListener(mainFrame, new fm(mainFrame)));
        imButton.addActionListener(new NavigationListener(mainFrame, new im(mainFrame)));
        smButton.addActionListener(new NavigationListener(mainFrame, new sm(mainFrame)));
        pmButton.addActionListener(new NavigationListener(mainFrame, new pm(mainFrame)));

        // Panel for buttons at the bottom
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());
        buttonPanel.add(amButton);
        buttonPanel.add(fmButton);
        buttonPanel.add(imButton);
        buttonPanel.add(smButton);
        buttonPanel.add(pmButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private static class NavigationListener implements ActionListener {

        private final frame mainFrame;
        private final JPanel nextPanel;

        public NavigationListener(frame mainFrame, JPanel nextPanel) {
            this.mainFrame = mainFrame;
            this.nextPanel = nextPanel;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            mainFrame.switchPanel(nextPanel); // Switch panel inside main frame
        }
    }

    // Main method
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            frame mainFrame = new frame();
            mainFrame.switchPanel(new login(mainFrame)); // Set login as default panel
        });
    }
}
