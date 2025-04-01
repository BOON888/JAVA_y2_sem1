import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

public class po_v extends JPanel {

    private static final String PO_FILE = "TXT/po.txt"; // Path to the PO file

    private static class PurchaseOrder {
        private String poId, prId, supplierId, status, approvedBy;
        private int quantityOrdered;
        private String orderDate, deliveryDate;

        // Constructor
        public PurchaseOrder(String poId, String prId, String supplierId, int quantityOrdered, String orderDate, String deliveryDate, String status, String approvedBy) {
            this.poId = poId;
            this.prId = prId;
            this.supplierId = supplierId;
            this.quantityOrdered = quantityOrdered;
            this.orderDate = orderDate;
            this.deliveryDate = deliveryDate;
            this.status = status;
            this.approvedBy = approvedBy;
        }

        // Getter methods for each field
        public String getPoId() { return poId; }
        public String getPrId() { return prId; }
        public String getSupplierId() { return supplierId; }
        public int getQuantityOrdered() { return quantityOrdered; }
        public String getOrderDate() { return orderDate; }
        public String getDeliveryDate() { return deliveryDate; }
        public String getStatus() { return status; }
        public String getApprovedBy() { return approvedBy; }
    }

    public po_v() {
        setLayout(new BorderLayout());

        // Load purchase orders from the file
        ArrayList<PurchaseOrder> purchaseOrders = loadPurchaseOrders();

        // Sort the purchase orders by PO ID in descending order
        Collections.sort(purchaseOrders, (po1, po2) -> Integer.compare(Integer.parseInt(po2.getPoId()), Integer.parseInt(po1.getPoId())));

        // Title for the table
        JLabel titleLabel = new JLabel("PO List", SwingConstants.LEFT);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.add(titleLabel, BorderLayout.WEST);
        add(titlePanel, BorderLayout.NORTH);

        // Define the column names for the table
        String[] columnNames = {"PO ID", "PR ID", "Supplier ID", "Qty Ordered", "Order Date", "Delivery Date", "Status", "Approved By"};

        // Prepare data for the table
        Object[][] data = new Object[purchaseOrders.size()][8];
        for (int i = 0; i < purchaseOrders.size(); i++) {
            PurchaseOrder po = purchaseOrders.get(i);
            data[i][0] = po.getPoId();
            data[i][1] = po.getPrId();
            data[i][2] = po.getSupplierId();
            data[i][3] = po.getQuantityOrdered();
            data[i][4] = po.getOrderDate();
            data[i][5] = po.getDeliveryDate();
            data[i][6] = po.getStatus();
            data[i][7] = po.getApprovedBy();
        }

        // Create a table model and prevent editing of the cells
        DefaultTableModel model = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;  // Disable editing of cells
            }
        };

        // Create JTable
        JTable table = new JTable(model);
        table.setFillsViewportHeight(true);

        // Add the table to a scroll pane
        JScrollPane scrollPane = new JScrollPane(table);

        // Adjust the column widths
        int maxColumnWidth = 200;
        for (int i = 0; i < table.getColumnCount(); i++) {
            int width = 0;
            for (int row = 0; row < table.getRowCount(); row++) {
                TableCellRenderer renderer = table.getCellRenderer(row, i);
                Component comp = table.prepareRenderer(renderer, row, i);
                width = Math.max(comp.getPreferredSize().width, width);
            }
            width = Math.min(width + 10, maxColumnWidth);  // Set a max width
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
                String[] attributes = line.split(",");
                String poId = attributes[0];
                String prId = attributes[1];
                String supplierId = attributes[2];
                int quantityOrdered = Integer.parseInt(attributes[3]);
                String orderDate = attributes[4];
                String deliveryDate = attributes[5];
                String status = attributes[6];
                String approvedBy = attributes[7];

                purchaseOrders.add(new PurchaseOrder(poId, prId, supplierId, quantityOrdered, orderDate, deliveryDate, status, approvedBy));
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error reading purchase orders file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return purchaseOrders;
    }

}