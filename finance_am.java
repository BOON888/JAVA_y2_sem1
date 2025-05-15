
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import finance_am_c.Finance;
import finance_am_c;

public class finance_am extends JFrame {

    JTable table;
    DefaultTableModel model;
    ArrayList<Finance> financeList;

    public finance_am() {
        setTitle("Finance Approval Management");
        setSize(900, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        String[] columnNames = {"Finance ID", "PO ID", "Approval Status", "Payment Status",
            "Payment Date", "Amount", "Verified By"};
        model = new DefaultTableModel(columnNames, 0) {
            public boolean isCellEditable(int row, int col) {
                return col == 2; // Only approvalStatus is editable
            }
        };

        table = new JTable(model);
        table.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(
                new JComboBox<>(new String[]{"approve", "reject"})));

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        JButton btnSave = new JButton("Save Changes");
        btnSave.addActionListener(e -> saveChanges());

        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(e -> loadData());

        JPanel panel = new JPanel();
        panel.add(btnRefresh);
        panel.add(btnSave);
        add(panel, BorderLayout.SOUTH);

        loadData();
        setVisible(true);
    }

    void loadData() {
        model.setRowCount(0);
        financeList = finance_am_c.readFinanceFile("finance.txt");
        for (Finance f : financeList) {
            model.addRow(f.toArray());
        }
    }

    void saveChanges() {
        for (int i = 0; i < table.getRowCount(); i++) {
            financeList.get(i).approvalStatus = model.getValueAt(i, 2).toString();
        }
        finance_am_c.writeFinanceFile("finance.txt", financeList);
        JOptionPane.showMessageDialog(this, "Finance data updated.");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(finance_am::new);
    }
}
