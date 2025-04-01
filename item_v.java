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
        setBorder(new EmptyBorder(MARGIN, MARGIN, MARGIN, MARGIN)); // Apply margin to the main panel

        ArrayList<Item> items = loadItems();

        String[] columnNames = {"ID", "Code", "Name", "Category", "Price", "Quantity"};
        Object[][] data = new Object[items.size()][6];
        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            data[i][0] = item.getId();
            data[i][1] = item.getCode();
            data[i][2] = item.getName();
            data[i][3] = item.getCategory();
            data[i][4] = item.getPrice();
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

        JScrollPane scrollPane = new JScrollPane(table);

        int maxColumnWidth = 200;
        for (int i = 0; i < table.getColumnCount(); i++) {
            int width = 0;
            for (int row = 0; row < table.getRowCount(); row++) {
                TableCellRenderer renderer = table.getCellRenderer(row, i);
                Component comp = table.prepareRenderer(renderer, row, i);
                width = Math.max(comp.getPreferredSize().width, width);
            }
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
                String[] attributes = line.split(",");
                String id = attributes[0];
                String code = attributes[1];
                String name = attributes[2];
                String category = attributes[3];
                double price = Double.parseDouble(attributes[4]);
                int quantity = Integer.parseInt(attributes[5]);
                items.add(new Item(id, code, name, category, price, quantity));
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