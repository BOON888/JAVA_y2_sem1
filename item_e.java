
import java.awt.*;
import javax.swing.*;

public class item_e extends JPanel {

    public item_e() {
        setLayout(new BorderLayout());

        // Title Label
        JLabel titleLabel = new JLabel("Hello Item", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        add(titleLabel, BorderLayout.CENTER);
    }
}
