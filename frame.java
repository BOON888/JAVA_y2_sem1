import javax.swing.*;

public class frame extends JFrame {
    public frame() {
        setTitle("JAVA FRAME");
        setSize(1028, 617);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true); // Ensure frame is visible testing dsjlkfjkdsfjkaskf
    }
    // Method to switch panels
    public void switchPanel(JPanel panel) {
        getContentPane().removeAll();
        getContentPane().add(panel);
        revalidate();
        repaint();
    }


}