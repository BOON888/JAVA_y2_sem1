
import java.awt.*;
import javax.swing.*;

public class supplier_e extends JPanel {

    public supplier_e() {
        setLayout(new BorderLayout());

        // Title Label
        JLabel titleLabel = new JLabel("Hello Supplier", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        add(titleLabel, BorderLayout.CENTER);
    }
}
