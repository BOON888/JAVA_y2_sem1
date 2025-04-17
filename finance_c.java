import java.io.*;
import java.util.ArrayList;
import java.util.List;

// Data model representing a finance record
public class finance_c {

    private String finance_id;
    private String po_id;
    private String approval_status;
    private String payment_status;
    private String payment_date;
    private Double amount;
    private String verified_by;

    public finance_c(String finance_id, String po_id, String approval_status, String payment_status, 
                   String payment_date, Double amount, String verified_by) {
        this.finance_id = finance_id;
        this.po_id = po_id;
        this.approval_status = approval_status;
        this.payment_status = payment_status;
        this.payment_date = payment_date;
        this.amount = amount;
        this.verified_by = verified_by;
    }

    // Getters and setters
    public String getFinance_id() {
        return finance_id;
    }

    public void setFinance_id(String finance_id) {
        this.finance_id = finance_id;
    }

    public String getPo_id() {
        return po_id;
    }

    public void setPo_id(String po_id) {
        this.po_id = po_id;
    }

    public String getApproval_status() {
        return approval_status;
    }

    public void setApproval_status(String approval_status) {
        this.approval_status = approval_status;
    }

    public String getPayment_status() {
        return payment_status;
    }

    public void setPayment_status(String payment_status) {
        this.payment_status = payment_status;
    }

    public String getPayment_date() {
        return payment_date;
    }

    public void setPayment_date(String payment_date) {
        this.payment_date = payment_date;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getVerified_by() {
        return verified_by;
    }

    public void setVerified_by(String verified_by) {
        this.verified_by = verified_by;
    }

    // Convert to a formatted string for file storage
    public String toFileString() {
        return finance_id + "|" + po_id + "|" + approval_status + "|" + payment_status + "|" + 
               payment_date + "|" + amount + "|" + verified_by;
    }
}

// Controller class that handles finance record CRUD operations
class FinanceController {

    private static final String FINANCE_FILE = "TXT/finance.txt";

    // Load finance records from the file
    public static List<finance_c> loadFinanceRecords() {
        List<finance_c> records = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(FINANCE_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length == 7) {
                    try {
                        Double amount = Double.parseDouble(parts[5]);
                        finance_c record = new finance_c(parts[0], parts[1], parts[2], parts[3], 
                                                   parts[4], amount, parts[6]);
                        records.add(record);
                    } catch (NumberFormatException e) {
                        System.out.println("Error parsing amount for record: " + line);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading finance file: " + e.getMessage());
        }
        return records;
    }

    // Add a new finance record to the file
    public static void addFinanceRecord(finance_c record) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FINANCE_FILE, true))) {
            bw.write(record.toFileString());
            bw.newLine();
        } catch (IOException e) {
            System.out.println("Error writing finance file: " + e.getMessage());
        }
    }

    // Update an existing finance record in the file
    public static void updateFinanceRecord(finance_c updatedRecord) {
        File inputFile = new File(FINANCE_FILE);
        StringBuilder updatedContent = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length > 0 && parts[0].equals(updatedRecord.getFinance_id())) {
                    updatedContent.append(updatedRecord.toFileString()).append("\n");
                } else {
                    updatedContent.append(line).append("\n");
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading finance file: " + e.getMessage());
            return;
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(inputFile))) {
            bw.write(updatedContent.toString().trim());
        } catch (IOException e) {
            System.out.println("Error updating finance file: " + e.getMessage());
        }
    }

    // Delete a finance record from the file by finance_id
    public static void deleteFinanceRecord(String financeId) {
        File inputFile = new File(FINANCE_FILE);
        StringBuilder updatedContent = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.startsWith(financeId + "|")) {
                    updatedContent.append(line).append("\n");
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading finance file: " + e.getMessage());
            return;
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(inputFile))) {
            bw.write(updatedContent.toString().trim());
        } catch (IOException e) {
            System.out.println("Error updating finance file: " + e.getMessage());
        }
    }

    // Generate the next finance_id (for adding a new record)
    public static String generateNextFinanceId() {
        int lastId = 1000;
        List<finance_c> records = loadFinanceRecords();
        for (finance_c r : records) {
            try {
                int id = Integer.parseInt(r.getFinance_id());
                if (id > lastId) {
                    lastId = id;
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid finance_id format: " + r.getFinance_id());
            }
        }
        return String.valueOf(lastId + 1);
    }
}