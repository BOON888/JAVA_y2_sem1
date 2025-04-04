import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

public class item_v extends JPanel {

    private static final String ITEM_FILE = "TXT/items.txt";
    private static final int MARGIN = 20; // Define the margin size
    private static final Font GLOBAL_FONT = new Font("Arial", Font.PLAIN, 16);
    private static final Font HEADER_FONT = new Font("Arial", Font.BOLD, 18);
    private static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 25); // Font for the title

    private static class Item {
        private String id, code, name, category;
        private double price;
        private int quantity;

        public Item(String id, String code, String name, String category, double price, int quantity) {
            this.id = id;
            this.code = code;
            this.name = name;
            this.category = category;
            this.price = price;
            this.quantity = quantity;
        }

        public String getId() { return id; }
        public String getCode() { return code; }
        public String getName() { return name; }
        public String getCategory() { return category; }
        public double getPrice() { return price; }
        public int getQuantity() { return quantity; }
    }

    public item_v() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(0, 0, MARGIN, 0)); // Apply margin to the main panel

        // Add a title label
        JLabel titleLabel = new JLabel("Item List",SwingConstants.LEFT);
        titleLabel.setFont(TITLE_FONT);
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.add(titleLabel, BorderLayout.WEST);
        add(titleLabel, BorderLayout.NORTH);

        ArrayList<Item> items = loadItems();

        String[] columnNames = {"ID", "Code", "Name", "Category", "Price", "Quantity"};
        Object[][] data = new Object[items.size()][6];
        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            data[i][0] = item.getId();
            data[i][1] = item.getCode();
            data[i][2] = item.getName();
            data[i][3] = item.getCategory();
            data[i][4] = String.format("%.2f", item.getPrice()); // Format price to 2 decimal places
            data[i][5] = item.getQuantity();
        }

        DefaultTableModel model = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model);
        table.setFillsViewportHeight(true);
        table.setFont(GLOBAL_FONT);
        table.getTableHeader().setFont(HEADER_FONT);
        table.setRowHeight(30);

        JScrollPane scrollPane = new JScrollPane(table);

        // Adjust column widths based on content
        int maxColumnWidth = 250; // Increased max width
        for (int i = 0; i < table.getColumnCount(); i++) {
            int width = 0;
            for (int row = 0; row < table.getRowCount(); row++) {
                TableCellRenderer renderer = table.getCellRenderer(row, i);
                Component comp = table.prepareRenderer(renderer, row, i);
                width = Math.max(comp.getPreferredSize().width, width);
            }
            TableCellRenderer headerRenderer = table.getColumnModel().getColumn(i).getHeaderRenderer();
            if (headerRenderer == null) {
                headerRenderer = table.getTableHeader().getDefaultRenderer();
            }
            Component headerComp = headerRenderer.getTableCellRendererComponent(table, table.getColumnName(i), false, false, 0, i);
            width = Math.max(width, headerComp.getPreferredSize().width);
            width = Math.min(width + 10, maxColumnWidth);
            table.getColumnModel().getColumn(i).setPreferredWidth(width);
        }

        add(scrollPane, BorderLayout.CENTER);
    }

    private ArrayList<Item> loadItems() {
        ArrayList<Item> items = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(ITEM_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] attributes = line.split("\\|"); // Changed split delimiter to "|"
                if (attributes.length == 6) {
                    try {
                        String id = attributes[0].trim();
                        String code = attributes[1].trim();
                        String name = attributes[2].trim();
                        String category = attributes[3].trim();
                        double price = Double.parseDouble(attributes[4].trim());
                        int quantity = Integer.parseInt(attributes[5].trim());
                        items.add(new Item(id, code, name, category, price, quantity));
                    } catch (NumberFormatException e) {
                        System.err.println("Error parsing line: " + line);
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(null, "Error parsing data in items file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    System.err.println("Skipping invalid line: " + line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error reading items file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return items;
    }

    /*
    public static void main(String[] args) {
        JFrame frame = new JFrame("Item List");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.add(new item_v());
        frame.setVisible(true);
    }
    */
}