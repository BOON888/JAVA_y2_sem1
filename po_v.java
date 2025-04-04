import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

public class po_v extends JPanel {

    private static final String PO_FILE = "TXT/po.txt"; // Path to the PO file

    private static class PurchaseOrder {
        private String poId, prId, itemId, supplierId, status, approvedBy;
        private int quantityOrdered, requestedBy, processedBy;
        private String orderDate;

        // Constructor
        public PurchaseOrder(String poId, String prId, String itemId, String supplierId, int quantityOrdered, String orderDate, int requestedBy, int processedBy, String approvedBy, String status) {
            this.poId = poId;
            this.prId = prId;
            this.itemId = itemId;
            this.supplierId = supplierId;
            this.quantityOrdered = quantityOrdered;
            this.orderDate = orderDate;
            this.requestedBy = requestedBy;
            this.processedBy = processedBy;
            this.approvedBy = approvedBy;
            this.status = status;
        }

        // Getter methods for each field
        public String getPoId() { return poId; }
        public String getPrId() { return prId; }
        public String getItemId() { return itemId; }
        public String getSupplierId() { return supplierId; }
        public int getQuantityOrdered() { return quantityOrdered; }
        public String getOrderDate() { return orderDate; }
        public int getRequestedBy() { return requestedBy; }
        public int getProcessedBy() { return processedBy; }
        public String getApprovedBy() { return approvedBy; }
        public String getStatus() { return status; }
    }

    public po_v() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(0, 0, 20, 0)); // Add 20-pixel bottom margin

        // Load purchase orders from the file
        ArrayList<PurchaseOrder> purchaseOrders = loadPurchaseOrders();

        // Sort the purchase orders by PO ID in descending order
        Collections.sort(purchaseOrders, (po1, po2) -> Integer.compare(Integer.parseInt(po2.getPoId()), Integer.parseInt(po1.getPoId())));

        // Title for the table
        JLabel titleLabel = new JLabel("PO List", SwingConstants.LEFT);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 25)); // Set font size to 25
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.add(titleLabel, BorderLayout.WEST);
        add(titlePanel, BorderLayout.NORTH);

        // Define the column names for the table
        String[] columnNames = {"PO ID", "PR ID", "Item ID", "Supplier ID", "Qty Ordered", "Order Date", "Requested By", "Processed By", "Approved By", "Status"};

        // Prepare data for the table
        Object[][] data = new Object[purchaseOrders.size()][10];
        for (int i = 0; i < purchaseOrders.size(); i++) {
            PurchaseOrder po = purchaseOrders.get(i);
            data[i][0] = po.getPoId();
            data[i][1] = po.getPrId();
            data[i][2] = po.getItemId();
            data[i][3] = po.getSupplierId();
            data[i][4] = po.getQuantityOrdered();
            data[i][5] = po.getOrderDate();
            data[i][6] = po.getRequestedBy();
            data[i][7] = po.getProcessedBy();
            data[i][8] = po.getApprovedBy();
            data[i][9] = po.getStatus();
        }

        // Create a table model and prevent editing of the cells
        DefaultTableModel model = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;   // Disable editing of cells
            }
        };

        // Create JTable
        JTable table = new JTable(model);
        table.setFont(new Font("Arial", Font.PLAIN, 16)); // Set font for table content
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 18)); // Set font for table header
        table.setRowHeight(30); // Increase row height for better readability
        table.setFillsViewportHeight(true);

        // Add the table to a scroll pane
        JScrollPane scrollPane = new JScrollPane(table);

        // Adjust the column widths
        int maxColumnWidth = 250; // Increased max column width
        for (int i = 0; i < table.getColumnCount(); i++) {
            int width = 0;
            for (int row = 0; row < table.getRowCount(); row++) {
                TableCellRenderer renderer = table.getCellRenderer(row, i);
                Component comp = table.prepareRenderer(renderer, row, i);
                width = Math.max(comp.getPreferredSize().width, width);
            }
            width = Math.min(width + 20, maxColumnWidth);   // Increased padding
            table.getColumnModel().getColumn(i).setPreferredWidth(width);
        }

        // Add the scroll pane to the panel
        add(scrollPane, BorderLayout.CENTER);
    }

    private ArrayList<PurchaseOrder> loadPurchaseOrders() {
        ArrayList<PurchaseOrder> purchaseOrders = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(PO_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] attributes = line.split("\\|");
                if (attributes.length == 10) {
                    try {
                        String poId = attributes[0].trim();
                        String prId = attributes[1].trim();
                        String itemId = attributes[2].trim();
                        String supplierId = attributes[3].trim();
                        int quantityOrdered = Integer.parseInt(attributes[4].trim());
                        String orderDate = attributes[5].trim();
                        int requestedBy = Integer.parseInt(attributes[6].trim());
                        int processedBy = Integer.parseInt(attributes[7].trim());
                        String approvedBy = attributes[8].trim();
                        String status = attributes[9].trim();

                        purchaseOrders.add(new PurchaseOrder(poId, prId, itemId, supplierId, quantityOrdered, orderDate, requestedBy, processedBy, approvedBy, status));
                    } catch (NumberFormatException e) {
                        System.err.println("Error parsing number in PO file: " + line);
                    }
                } else {
                    System.err.println("Skipping invalid line in PO file: " + line + ". Expected 10 pipe-separated values.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error reading purchase orders file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return purchaseOrders;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("PO List");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.getContentPane().add(new po_v());
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}