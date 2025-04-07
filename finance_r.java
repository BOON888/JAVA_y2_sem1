import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class finance_r extends JPanel {

    private static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 25);
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
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS)); // Use BoxLayout

        readFinanceAndSalesData();

        SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMMM-yyyy");
        Map<String, String> combinedData = new LinkedHashMap<>();

        for (Map.Entry<Date, String> entry : monthYearData.entrySet()) {
            String monthYear = monthYearFormat.format(entry.getKey());
            combinedData.put(monthYear, combinedData.getOrDefault(monthYear, "") + entry.getValue());
        }

        for (Map.Entry<String, String> entry : combinedData.entrySet()) {
            JPanel rowPanel = new JPanel(new GridLayout(1, 2)); // Panel for each row
            rowPanel.setBorder(new LineBorder(Color.LIGHT_GRAY, 1)); // Add border

            JLabel monthYearLabel = new JLabel(entry.getKey());
            JButton monthYearButton = new JButton("View");

            rowPanel.add(monthYearLabel);
            rowPanel.add(monthYearButton);

            mainPanel.add(rowPanel); // Add row panel to main panel

            monthYearButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JTextArea textArea = new JTextArea(entry.getKey() + " Report:\n" + entry.getValue());
                    textArea.setEditable(false);
                    JScrollPane scrollPane = new JScrollPane(textArea);
                    JDialog dialog = new JDialog();
                    dialog.add(scrollPane);
                    dialog.setSize(1600, 1000);
                    dialog.setLocationRelativeTo(finance_r.this);
                    dialog.setVisible(true);
                }
            });
        }

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(new EmptyBorder(0, 0, 20, 0));

        JScrollPane scrollPane = new JScrollPane(mainPanel); // Scroll main panel
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);
    }

    private void readFinanceAndSalesData() {
        SimpleDateFormat inputDateFormat = new SimpleDateFormat("dd-MM-yyyy");

        // Read finance data
        try (BufferedReader br = new BufferedReader(new FileReader("TXT/finance.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 5) {
                    try {
                        Date date = inputDateFormat.parse(parts[4]);
                        monthYearData.put(date, monthYearData.getOrDefault(date, "") + "Finance: " + line + "\n");
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error reading finance.txt: " + e.getMessage());
        }

        // Read sales data
        try (BufferedReader br = new BufferedReader(new FileReader("TXT/sales_data.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 3) {
                    try {
                        Date date = inputDateFormat.parse(parts[2]);
                        monthYearData.put(date, monthYearData.getOrDefault(date, "") + "Sales: " + line + "\n");
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error reading sales_data.txt: " + e.getMessage());
        }
    }
}