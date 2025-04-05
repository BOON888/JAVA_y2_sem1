
import java.awt.*;
import javax.swing.*;

public class sales_e extends JPanel {

    public sales_e() {
        setLayout(new BorderLayout());

        // Title Label
        JLabel titleLabel = new JLabel("Hello Sales", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        add(titleLabel, BorderLayout.CENTER);
    }
}
