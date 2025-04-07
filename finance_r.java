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

public class finance_r extends JPanel { // Changed to JPanel

    private static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 25); // Font for the title

    private TreeMap<Date, String> monthYearData = new TreeMap<>(Collections.reverseOrder()); // Use TreeMap for sorting

    public finance_r() { // Changed to JPanel constructor
    
        setLayout(new BorderLayout()); // Set layout for JPanel
        setBorder(new EmptyBorder(0, 0, 20, 0));

        // Add a title label
        JLabel titleLabel = new JLabel("Finance Report",SwingConstants.LEFT);
        titleLabel.setFont(TITLE_FONT);
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.add(titleLabel, BorderLayout.WEST);
        add(titleLabel, BorderLayout.NORTH);


        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(0, 2)); // Dynamic rows, 2 columns

        // Read finance data and populate monthYearData
        readFinanceData();

        SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMMM-yyyy");

        // Combine data for the same month and year
        Map<String, String> combinedData = new LinkedHashMap<>();
        for (Map.Entry<Date, String> entry : monthYearData.entrySet()) {
            String monthYear = monthYearFormat.format(entry.getKey());
            combinedData.put(monthYear, combinedData.getOrDefault(monthYear, "") + entry.getValue());
        }

        for (Map.Entry<String, String> entry : combinedData.entrySet()) {
            JLabel monthYearLabel = new JLabel(entry.getKey());
            JButton monthYearButton = new JButton("View");

            mainPanel.add(monthYearLabel);
            mainPanel.add(monthYearButton);

            monthYearButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JOptionPane.showMessageDialog(finance_r.this, entry.getKey() + " Report:\n" + entry.getValue());
                }
            });
        }

        JScrollPane scrollPane = new JScrollPane(mainPanel); // Add scroll pane
        add(scrollPane, BorderLayout.CENTER); // Add scroll pane to center
    }

    private void readFinanceData() {
        try (BufferedReader br = new BufferedReader(new FileReader("TXT/finance.txt"))) {
            String line;
            SimpleDateFormat inputDateFormat = new SimpleDateFormat("dd-MM-yyyy");

            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 5) {
                    try {
                        Date date = inputDateFormat.parse(parts[4]);
                        monthYearData.put(date, monthYearData.getOrDefault(date, "") + line + "\n");
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error reading finance.txt: " + e.getMessage());
        }
    }
}