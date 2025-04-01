
import javax.swing.*;

public class frame extends JFrame {

    public frame() {
        setTitle("JAVA FRAME");
        setSize(1920, 1080);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true); // Ensure frame is visible
    }

    // Method to switch panels
    public void switchPanel(JPanel panel) {
        getContentPane().removeAll();
        getContentPane().add(panel);
        revalidate();
        repaint();
    }
}
