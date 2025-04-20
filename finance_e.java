import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class finance_e extends JPanel {

    private static final String FINANCE_FILE = "TXT/finance.txt";
    private static final String PO_FILE = "TXT/po.txt";
    private static final String STATUS_PENDING = "Pending";
    private static final String STATUS_VERIFIED = "Verified";
    private static final String STATUS_NOT_VERIFIED = "Not Verified";
    private static final String STATUS_PAID = "Paid";
    private static final String STATUS_UNPAID = "Unpaid";

    private JTabbedPane tabbedPane;
    private JPanel financeInfoPanel;
    private JPanel financeListPanel;

    private JTextField financeIdInfoField;
    private JComboBox<String> poIdComboBox;
    private JComboBox<String> paymentStatusInfoCombo;
    private JTextField paymentDateInfoField;
    private JTextField amountInfoField;
    private JButton addButton;

    private DefaultTableModel financeListTableModelTop = new DefaultTableModel(new Object[]{"Finance ID", "Actions"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return column == 1;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return (columnIndex == 1) ? ButtonPanel.class : super.getColumnClass(columnIndex);
        }
    };
    private JTable financeListTableTop;
    private JScrollPane financeListScrollPaneTop;

    private JTextField financeIdUpdateField;
    private JTextField poIdUpdateField;
    private JComboBox<String> approvalStatusUpdateCombo;
    private JComboBox<String> paymentStatusUpdateCombo;
    private JTextField paymentDateUpdateField;
    private JTextField amountUpdateField;
    private JTextField verifiedByUpdateField;
    private JButton updateButton;

    private List<FinanceRecord> financeRecords = new ArrayList<>();

    public finance_e() {
        this(new ArrayList<>());
    }

    public finance_e(List<FinanceRecord> financeRecords) {
        this.financeRecords = financeRecords;
        setLayout(new BorderLayout());

        tabbedPane = new JTabbedPane();
        Font tabTitleFont = new Font("Arial", Font.BOLD, 20);
        tabbedPane.setFont(tabTitleFont);

        financeInfoPanel = createFinanceInfoPanel();
        tabbedPane.addTab("Finance Info", financeInfoPanel);

        financeListPanel = createFinanceListPanel();
        tabbedPane.addTab("Finance List", financeListPanel);

        add(tabbedPane, BorderLayout.CENTER);

        loadFinanceData();
        populateFinanceListTableTop();
    }

    private JPanel createFinanceInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;

        JLabel financeIdLabel = new JLabel("Finance ID:");
        financeIdInfoField = new JTextField(15);
        financeIdInfoField.setEditable(false);
        financeIdInfoField.setText(generateNewFinanceId());

        JLabel poIdLabel = new JLabel("PO ID:");
        poIdComboBox = new JComboBox<>();
        loadPoIds();

        JLabel paymentStatusLabel = new JLabel("Payment Status:");
        paymentStatusInfoCombo = new JComboBox<>(new String[]{STATUS_PAID, STATUS_UNPAID});

        JLabel paymentDateLabel = new JLabel("Payment Date:");
        paymentDateInfoField = new JTextField(15);

        JLabel amountLabel = new JLabel("Amount:");
        amountInfoField = new JTextField(15);

        addButton = new JButton("Add");

        gbc.gridx = 0;
        gbc.gridy = 0;
        inputPanel.add(financeIdLabel, gbc);
        gbc.gridx = 1;
        inputPanel.add(financeIdInfoField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        inputPanel.add(poIdLabel, gbc);
        gbc.gridx = 1;
        inputPanel.add(poIdComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        inputPanel.add(paymentStatusLabel, gbc);
        gbc.gridx = 1;
        inputPanel.add(paymentStatusInfoCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        inputPanel.add(paymentDateLabel, gbc);
        gbc.gridx = 1;
        inputPanel.add(paymentDateInfoField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        inputPanel.add(amountLabel, gbc);
        gbc.gridx = 1;
        inputPanel.add(amountInfoField, gbc);

        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.EAST;
        inputPanel.add(addButton, gbc);

        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String financeId = financeIdInfoField.getText().trim();
                String poId = (String) poIdComboBox.getSelectedItem();
                String paymentStatus = (String) paymentStatusInfoCombo.getSelectedItem();
                String paymentDate = paymentDateInfoField.getText().trim();
                String amountText = amountInfoField.getText().trim();

                if (financeId.isEmpty() || poId == null || poId.isEmpty() || paymentDate.isEmpty() || amountText.isEmpty()) {
                    JOptionPane.showMessageDialog(finance_e.this, "Please fill in all fields", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                FinanceRecord newRecord = new FinanceRecord(
                        financeId,
                        poId,
                        STATUS_PENDING,
                        paymentStatus,
                        paymentDate,
                        amountText,
                        Integer.parseInt(login_c.currentUserId)
                );
                
                financeRecords.add(newRecord);
                saveFinanceData();
                populateFinanceListTableTop();
                
                financeIdInfoField.setText(generateNewFinanceId());
                poIdComboBox.setSelectedIndex(0);
                paymentStatusInfoCombo.setSelectedIndex(0);
                paymentDateInfoField.setText("");
                amountInfoField.setText("");
                
                JOptionPane.showMessageDialog(finance_e.this, 
                    "Finance record added successfully!\n" +
                    "Finance ID: " + financeId + "\n" +
                    "PO ID: " + poId + "\n" +
                    "Amount: " + amountText, 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        panel.add(inputPanel, BorderLayout.NORTH);
        return panel;
    }

    private void loadPoIds() {
        poIdComboBox.removeAllItems();
        poIdComboBox.addItem("");
        
        try (BufferedReader br = new BufferedReader(new FileReader(PO_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split("\\|");
                if (data.length > 0) {
                    String poId = data[0].trim();
                    if (!poId.isEmpty()) {
                        poIdComboBox.addItem(poId);
                    }
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error reading PO file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createFinanceListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 20, 0, 20));

        financeListTableTop = new JTable(financeListTableModelTop);
        financeListTableTop.getColumn("Actions").setCellRenderer(new ButtonRenderer());
        financeListTableTop.getColumn("Actions").setCellEditor(new ButtonEditor(financeListTableTop));
        financeListScrollPaneTop = new JScrollPane(financeListTableTop);
        financeListScrollPaneTop.setMinimumSize(new Dimension(financeListScrollPaneTop.getMinimumSize().width, 50));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(financeListScrollPaneTop, BorderLayout.CENTER);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(createFinanceListBottomPanel(), BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createFinanceListBottomPanel() {
        JPanel bottomPanel = new JPanel(new GridBagLayout());
        TitledBorder titledBorder = BorderFactory.createTitledBorder("Finance Details");
        LineBorder topBorder = new LineBorder(Color.BLACK, 2);

        bottomPanel.setBorder(new CompoundBorder(
                new EmptyBorder(0, 0, 10, 0),
                new CompoundBorder(topBorder, new EmptyBorder(5, 0, 5, 0))
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;

        JLabel financeIdLabel = new JLabel("Finance ID:");
        financeIdUpdateField = new JTextField(15);
        financeIdUpdateField.setEditable(false);

        JLabel poIdLabelUpdate = new JLabel("PO ID:");
        poIdUpdateField = new JTextField(15);
        poIdUpdateField.setEditable(false);

        JLabel approvalStatusLabel = new JLabel("Approval Status:");
        approvalStatusUpdateCombo = new JComboBox<>(new String[]{STATUS_PENDING, STATUS_VERIFIED, STATUS_NOT_VERIFIED});

        JLabel paymentStatusLabel = new JLabel("Payment Status:");
        paymentStatusUpdateCombo = new JComboBox<>(new String[]{STATUS_PAID, STATUS_UNPAID});

        JLabel paymentDateLabel = new JLabel("Payment Date:");
        paymentDateUpdateField = new JTextField(15);
        paymentDateUpdateField.setEditable(false);

        JLabel amountLabel = new JLabel("Amount:");
        amountUpdateField = new JTextField(15);
        amountUpdateField.setEditable(false);

        JLabel verifiedByLabel = new JLabel("Verified By:");
        verifiedByUpdateField = new JTextField(15);
        verifiedByUpdateField.setEditable(false);

        updateButton = new JButton("Update");

        gbc.gridx = 0;
        gbc.gridy = 0;
        bottomPanel.add(financeIdLabel, gbc);
        gbc.gridx = 1;
        bottomPanel.add(financeIdUpdateField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        bottomPanel.add(poIdLabelUpdate, gbc);
        gbc.gridx = 1;
        bottomPanel.add(poIdUpdateField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        bottomPanel.add(approvalStatusLabel, gbc);
        gbc.gridx = 1;
        bottomPanel.add(approvalStatusUpdateCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        bottomPanel.add(paymentStatusLabel, gbc);
        gbc.gridx = 1;
        bottomPanel.add(paymentStatusUpdateCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        bottomPanel.add(paymentDateLabel, gbc);
        gbc.gridx = 1;
        bottomPanel.add(paymentDateUpdateField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        bottomPanel.add(amountLabel, gbc);
        gbc.gridx = 1;
        bottomPanel.add(amountUpdateField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 6;
        bottomPanel.add(verifiedByLabel, gbc);
        gbc.gridx = 1;
        bottomPanel.add(verifiedByUpdateField, gbc);

        gbc.gridx = 1;
        gbc.gridy = 7;
        gbc.anchor = GridBagConstraints.EAST;
        bottomPanel.add(updateButton, gbc);

        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String financeId = financeIdUpdateField.getText();
                boolean found = false;
                for (FinanceRecord record : financeRecords) {
                    if (record.getFinanceId().equals(financeId)) {
                        found = true;
                        int confirm = JOptionPane.showConfirmDialog(
                                finance_e.this,
                                "Are you sure you want to update Finance ID: " + record.getFinanceId() + "?",
                                "Confirm Update",
                                JOptionPane.YES_NO_OPTION);
                        if (confirm == JOptionPane.YES_OPTION) {
                            record.setApprovalStatus((String) approvalStatusUpdateCombo.getSelectedItem());
                            record.setPaymentStatus((String) paymentStatusUpdateCombo.getSelectedItem());
                            record.setVerifiedBy(Integer.parseInt(login_c.currentUserId));
                            
                            JOptionPane.showMessageDialog(finance_e.this, 
                                "Finance updated successfully! Verified By: " + login_c.currentUserId, 
                                "Success", JOptionPane.INFORMATION_MESSAGE);
                            clearUpdateFields();
                            populateFinanceListTableTop();
                            saveFinanceData();
                        } else {
                            JOptionPane.showMessageDialog(finance_e.this, 
                                "Update cancelled.", 
                                "Cancelled", JOptionPane.INFORMATION_MESSAGE);
                        }
                        break;
                    }
                }
                clearUpdateFields();
                if (!found) {
                    JOptionPane.showMessageDialog(finance_e.this, 
                        "Could not find finance record.", 
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        return bottomPanel;
    }

    public void populateFinanceListTableTop() {
        financeListTableModelTop.setRowCount(0);
        for (FinanceRecord record : financeRecords) {
            financeListTableModelTop.addRow(new Object[]{
                record.getFinanceId(),
                new ButtonPanel(record)
            });
        }
    }

    private void loadFinanceData() {
        financeRecords.clear();
        try (BufferedReader br = new BufferedReader(new FileReader(FINANCE_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split("\\|");
                if (data.length == 7) {
                    String financeId = data[0].trim();
                    String poId = data[1].trim();
                    String approvalStatus = data[2].trim();
                    String paymentStatus = data[3].trim();
                    String paymentDate = data[4].trim();
                    String amount = data[5].trim();
                    int verifiedBy = Integer.parseInt(data[6].trim());

                    FinanceRecord record = new FinanceRecord(
                            financeId,
                            poId,
                            approvalStatus,
                            paymentStatus,
                            paymentDate,
                            amount,
                            verifiedBy
                    );
                    financeRecords.add(record);
                } else {
                    System.err.println("Skipping invalid line in finance.txt: " + line + ". Expected 7 columns.");
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error reading finance file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (NumberFormatException e) {
            System.err.println("Error parsing verifiedBy in finance.txt: " + e.getMessage());
        }
    }

    public void saveFinanceData() {
        try (FileWriter writer = new FileWriter(FINANCE_FILE)) {
            for (FinanceRecord record : financeRecords) {
                writer.write(record.getFinanceId() + "|"
                        + record.getPoId() + "|"
                        + record.getApprovalStatus() + "|"
                        + record.getPaymentStatus() + "|"
                        + record.getPaymentDate() + "|"
                        + record.getAmount() + "|"
                        + record.getVerifiedBy() + "\n");
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving finance file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public String generateNewFinanceId() {
        int lastId = 8000;
        for (FinanceRecord record : financeRecords) {
            try {
                int currentId = Integer.parseInt(record.getFinanceId());
                if (currentId > lastId) {
                    lastId = currentId;
                }
            } catch (NumberFormatException e) {
                // Skip non-numeric IDs
            }
        }
        return String.valueOf(lastId + 1);
    }

    private void populateUpdateFields(FinanceRecord record) {
        financeIdUpdateField.setText(record.getFinanceId());
        poIdUpdateField.setText(record.getPoId());
        approvalStatusUpdateCombo.setSelectedItem(record.getApprovalStatus());
        paymentStatusUpdateCombo.setSelectedItem(record.getPaymentStatus());
        paymentDateUpdateField.setText(record.getPaymentDate());
        amountUpdateField.setText(record.getAmount());
        verifiedByUpdateField.setText(String.valueOf(record.getVerifiedBy()));
    }

    private void clearUpdateFields() {
        financeIdUpdateField.setText("");
        poIdUpdateField.setText("");
        approvalStatusUpdateCombo.setSelectedIndex(0);
        paymentStatusUpdateCombo.setSelectedIndex(0);
        paymentDateUpdateField.setText("");
        amountUpdateField.setText("");
        verifiedByUpdateField.setText("");
    }

    public static class FinanceRecord {
        private String financeId;
        private String poId;
        private String approvalStatus;
        private String paymentStatus;
        private String paymentDate;
        private String amount;
        private int verifiedBy;

        public FinanceRecord(String financeId, String poId, String approvalStatus, String paymentStatus, 
                           String paymentDate, String amount, int verifiedBy) {
            this.financeId = financeId;
            this.poId = poId;
            this.approvalStatus = approvalStatus;
            this.paymentStatus = paymentStatus;
            this.paymentDate = paymentDate;
            this.amount = amount;
            this.verifiedBy = verifiedBy;
        }

        public String getFinanceId() {
            return financeId;
        }

        public String getPoId() {
            return poId;
        }

        public String getApprovalStatus() {
            return approvalStatus;
        }

        public String getPaymentStatus() {
            return paymentStatus;
        }

        public String getPaymentDate() {
            return paymentDate;
        }

        public String getAmount() {
            return amount;
        }

        public int getVerifiedBy() {
            return verifiedBy;
        }

        public void setPoId(String poId) {
            this.poId = poId;
        }

        public void setApprovalStatus(String approvalStatus) {
            this.approvalStatus = approvalStatus;
        }

        public void setPaymentStatus(String paymentStatus) {
            this.paymentStatus = paymentStatus;
        }

        public void setPaymentDate(String paymentDate) {
            this.paymentDate = paymentDate;
        }

        public void setAmount(String amount) {
            this.amount = amount;
        }

        public void setVerifiedBy(int verifiedBy) {
            this.verifiedBy = verifiedBy;
        }
    }

    private class ButtonPanel extends JPanel {
        private JButton viewButton;
        private FinanceRecord record;

        public ButtonPanel(FinanceRecord record) {
            this.record = record;
            setLayout(new FlowLayout(FlowLayout.LEFT, 7, 0));
            viewButton = new JButton("View");

            Dimension buttonSize = new Dimension(90, 14);
            viewButton.setPreferredSize(buttonSize);

            Font smallerFont = new Font(viewButton.getFont().getName(), Font.PLAIN, 13);
            viewButton.setFont(smallerFont);

            add(viewButton);

            viewButton.setEnabled(true);

            viewButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    populateUpdateFields(record);
                    tabbedPane.setSelectedIndex(1);
                }
            });
        }
    }

    private class ButtonRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            FinanceRecord record = financeRecords.get(row);
            if (record != null) {
                ButtonPanel panel = new ButtonPanel(record);
                return panel;
            } else {
                return new JLabel();
            }
        }
    }

    private class ButtonEditor extends DefaultCellEditor {
        private ButtonPanel panel;

        public ButtonEditor(JTable table) {
            super(new JTextField());
            setClickCountToStart(1);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            FinanceRecord record = financeRecords.get(row);
            if (record != null) {
                panel = new ButtonPanel(record);
                return panel;
            } else {
                return new JPanel();
            }
        }

        @Override
        public Object getCellEditorValue() {
            return null;
        }

        @Override
        public boolean isCellEditable(java.util.EventObject e) {
            return true;
        }

        @Override
        public boolean shouldSelectCell(java.util.EventObject anEvent) {
            return false;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Finance Management");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.getContentPane().add(new finance_e());
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}