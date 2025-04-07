import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

public class finance_r extends JPanel {

    private static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 25);
    private static final Font CONTENT_FONT = new Font("Arial", Font.PLAIN, 18);
    private TreeMap<Date, String> monthYearData = new TreeMap<>(new Comparator<Date>() {
        SimpleDateFormat monthYearFormat = new SimpleDateFormat("yyyyMM");

        @Override
        public int compare(Date date1, Date date2) {
            return monthYearFormat.format(date2).compareTo(monthYearFormat.format(date1));
        }
    });

    public finance_r() {
        setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("Financial Reports", SwingConstants.LEFT);
        titleLabel.setFont(TITLE_FONT);
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.add(titleLabel, BorderLayout.WEST);
        add(titleLabel, BorderLayout.NORTH);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        readFinanceAndSalesData();

        SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMMM-yyyy");
        Map<String, String> combinedData = new LinkedHashMap<>();

        for (Map.Entry<Date, String> entry : monthYearData.entrySet()) {
            String monthYear = monthYearFormat.format(entry.getKey());
            combinedData.put(monthYear, combinedData.getOrDefault(monthYear, "") + entry.getValue());
        }

        for (Map.Entry<String, String> entry : combinedData.entrySet()) {
            JPanel rowPanel = new JPanel(new GridLayout(1, 2));
            rowPanel.setBorder(new LineBorder(Color.LIGHT_GRAY, 1));

            JLabel monthYearLabel = new JLabel(entry.getKey());
            monthYearLabel.setFont(CONTENT_FONT);
            JButton monthYearButton = new JButton("View");
            monthYearButton.setFont(CONTENT_FONT);

            rowPanel.add(monthYearLabel);
            rowPanel.add(monthYearButton);

            mainPanel.add(rowPanel);

            monthYearButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    FinanceReport financeReportPanel = new FinanceReport(entry.getKey());
                    JDialog dialog = new JDialog();
                    dialog.add(financeReportPanel);
                    dialog.setSize(1600, 1000);
                    dialog.setLocationRelativeTo(finance_r.this);
                    dialog.setVisible(true);
                }
            });
        }

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(new EmptyBorder(0, 0, 20, 0));

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);
    }

    private void readFinanceAndSalesData() {
        SimpleDateFormat inputDateFormat = new SimpleDateFormat("dd-MM-yyyy");
        String financeFilePath = "TXT/finance.txt";
        String salesFilePath = "TXT/sales_data.txt";

        if (new File(financeFilePath).exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(financeFilePath))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split("\\|");
                    if (parts.length >= 5) {
                        try {
                            Date date = inputDateFormat.parse(parts[4]);
                            monthYearData.put(date, monthYearData.getOrDefault(date, "") + "Finance: " + line + "\n");
                        } catch (ParseException e) {
                            System.err.println("Error parsing date in finance.txt: " + e.getMessage());
                        }
                    }
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error reading finance.txt: " + e.getMessage());
            }
        } else {
            JOptionPane.showMessageDialog(this, "finance.txt not found.");
        }

        if (new File(salesFilePath).exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(salesFilePath))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split("\\|");
                    if (parts.length >= 3) {
                        try {
                            Date date = inputDateFormat.parse(parts[2]);
                            monthYearData.put(date, monthYearData.getOrDefault(date, "") + "Sales: " + line + "\n");
                        } catch (ParseException e) {
                            System.err.println("Error parsing date in sales_data.txt: " + e.getMessage());
                        }
                    }
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error reading sales_data.txt: " + e.getMessage());
            }
        } else {
            JOptionPane.showMessageDialog(this, "sales_data.txt not found.");
        }
    }

    static class FinanceReport extends JPanel {

        private DefaultTableModel tableModel;
        private JTable reportTable;
        private double overallTotal = 0.0;
        private String selectedMonthYear;

        public FinanceReport(String monthYear) {
            this.selectedMonthYear = monthYear;
            setLayout(new GridBagLayout());
            setBorder(new EmptyBorder(20, 20, 20, 20));

            JLabel titleLabel = new JLabel("Financial Report - " + monthYear, SwingConstants.LEFT);
            titleLabel.setFont(TITLE_FONT);
            JPanel titlePanel = new JPanel(new BorderLayout());
            titlePanel.add(titleLabel, BorderLayout.NORTH);

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weightx = 1.0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            add(titleLabel, gbc);

            tableModel = new DefaultTableModel();
            reportTable = new JTable(tableModel);
            tableModel.addColumn("Date");
            tableModel.addColumn("Item Purchase");
            tableModel.addColumn("Item Sales");
            tableModel.addColumn("Unit Purchase/Sold");
            tableModel.addColumn("Cost Per Unit");
            tableModel.addColumn("Total");

            TableColumn column;
            column = reportTable.getColumnModel().getColumn(0);
            column.setPreferredWidth(150);
            column = reportTable.getColumnModel().getColumn(1);
            column.setPreferredWidth(150);
            column = reportTable.getColumnModel().getColumn(2);
            column.setPreferredWidth(150);
            column = reportTable.getColumnModel().getColumn(3);
            column.setPreferredWidth(180);
            column = reportTable.getColumnModel().getColumn(4);
            column.setPreferredWidth(150);
            column = reportTable.getColumnModel().getColumn(5);
            column.setPreferredWidth(150);

            reportTable.setFont(CONTENT_FONT);
            reportTable.getTableHeader().setFont(CONTENT_FONT);

            reportTable.setRowHeight(30);

            JScrollPane scrollPane = new JScrollPane(reportTable);
            scrollPane.setPreferredSize(new Dimension(800, 400));

            gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 1;
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;
            gbc.fill = GridBagConstraints.BOTH;
            add(scrollPane, gbc);

            JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JLabel totalLabel = new JLabel("Total: ");
            totalLabel.setFont(CONTENT_FONT);
            JLabel totalValueLabel = new JLabel("0.00");
            totalValueLabel.setFont(CONTENT_FONT);
            totalPanel.add(totalLabel);
            totalPanel.add(totalValueLabel);

            gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 2;
            gbc.weightx = 1.0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            add(totalPanel, gbc);

            processFinanceData();
            totalValueLabel.setText(String.format("%.2f", overallTotal));
        }

        private void processFinanceData() {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
            SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMMM-yyyy");
            SimpleDateFormat monthYearCompareFormat = new SimpleDateFormat("yyyyMM");
            Map<String, Double> itemPrices = readItemPrices();

            try (BufferedReader br = new BufferedReader(new FileReader("TXT/finance.txt"))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split("\\|");
                    if (parts.length >= 7) {
                        String itemId = parts[0];
                        String dateStr = parts[4];
                        int quantity = Integer.parseInt(parts[6]);

                        try {
                            Date date = dateFormat.parse(dateStr);
                            if (monthYearCompareFormat.format(date).equals(monthYearCompareFormat.format(monthYearFormat.parse(selectedMonthYear)))) {
                                double purchaseTotal = findPurchaseTotal(itemId, quantity, itemPrices);
                                double salesTotal = findSalesTotal(itemId, quantity, itemPrices);

                                tableModel.addRow(new Object[]{dateStr, itemId, "", "", itemPrices.get(itemId), salesTotal - purchaseTotal});

                                overallTotal += salesTotal - purchaseTotal;
                            }
                        } catch (ParseException e) {
                            System.err.println("Error parsing date: " + e.getMessage());
                        }
                    }
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error reading finance.txt: " + e.getMessage());
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid number format in finance.txt.");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "An unexpected error occurred.");
            }
        }

        private Map<String, Double> readItemPrices() {
            Map<String, Double> itemPrices = new HashMap<>();
            String itemsFilePath = "TXT/sales_data.txt";
            if (new File(itemsFilePath).exists()) {
                try (BufferedReader br = new BufferedReader(new FileReader(itemsFilePath))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        String[] parts = line.split("\\|");
                        if (parts.length >= 2) {
                            itemPrices.put(parts[0], Double.parseDouble(parts[1]));
                        }
                    }
                } catch (IOException | NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Error reading sales_data.txt: " + e.getMessage());
                }
            } else {
                JOptionPane.showMessageDialog(this, "sales_data.txt not found.");
            }
            return itemPrices;
        }

        private double findPurchaseTotal(String itemId, int quantity, Map<String, Double> itemPrices) {
            double total = 0.0;
            String poFilePath = "TXT/po.txt";
            if (new File(poFilePath).exists()) {
                try (BufferedReader br = new BufferedReader(new FileReader(poFilePath))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        String[] parts = line.split("\\|");
                        if (parts.length >= 3 && parts[0].equals(itemId)) {
                            total = quantity * itemPrices.get(itemId);
                            break;
                        }
                    }
                } catch (IOException | NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Error reading po.txt: " + e.getMessage());
                }
            } else {
                JOptionPane.showMessageDialog(this, "po.txt not found.");
            }
            return total;
        }

        private double findSalesTotal(String itemId, int quantity, Map<String, Double> itemPrices) {
            double total = 0.0;
            String salesFilePath = "TXT/sales_data.txt";
            if (new File(salesFilePath).exists()) {
                try (BufferedReader br = new BufferedReader(new FileReader(salesFilePath))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        String[] parts = line.split("\\|");
                        if (parts.length >= 3 && parts[0].equals(itemId)) {
                            total = quantity * itemPrices.get(itemId);
                            break;
                        }
                    }
                } catch (IOException | NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Error reading sales_data.txt: " + e.getMessage());
                }
            } else {
                JOptionPane.showMessageDialog(this, "sales_data.txt not found.");
            }
            return total;
        }
    }
}
