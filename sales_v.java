import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import javax.swing.table.DefaultTableModel;

public class sales_v extends JPanel {
    private static final String SALES_FILE = "TXT/sales_data.txt";
    private static final String ITEMS_FILE = "TXT/items.txt";

    public sales_v() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(800, 500));

        JLabel title = new JLabel("Sales List", JLabel.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        add(title, BorderLayout.NORTH);

        // Load items for name lookup
        ArrayList<String[]> itemsList = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(ITEMS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split("\\|");
                if (data.length >= 6) {
                    itemsList.add(data);
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error loading items", "Error", JOptionPane.ERROR_MESSAGE);
        }

        String[] columns = {"Sales ID", "Item ID", "Item Name", "Date", "Qty Sold", "Remaining", "Sales Person"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);

        try (BufferedReader reader = new BufferedReader(new FileReader(SALES_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split("\\|");
                if (data.length >= 6) {
                    // Find item name
                    String itemName = "Unknown";
                    for (String[] item : itemsList) {
                        if (item[0].equals(data[1])) {
                            itemName = item[1];
                            break;
                        }
                    }

                    model.addRow(new Object[]{
                        data[0].trim(),
                        data[1].trim(),
                        itemName,
                        data[2].trim(),
                        data[3].trim(),
                        data[4].trim(),
                        data[5].trim()
                    });
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading sales", "Error", JOptionPane.ERROR_MESSAGE);
        }
        
        JTable table = new JTable(model);
        table.setFont(new Font("Arial", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        table.setRowHeight(25);
        
        add(new JScrollPane(table), BorderLayout.CENTER);
    }
}