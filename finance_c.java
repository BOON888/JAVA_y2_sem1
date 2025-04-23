import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class finance_c {
    private static final String FINANCE_FILE = "TXT/finance.txt";
    private static final String PO_FILE = "TXT/po.txt";
    public static final String STATUS_PENDING = "Pending";
    public static final String STATUS_VERIFIED = "Verified";
    public static final String STATUS_NOT_VERIFIED = "Not Verified";
    public static final String STATUS_PAID = "Paid";
    public static final String STATUS_UNPAID = "Unpaid";
    public static final int DEFAULT_VERIFIED_BY = 7001;

    private List<FinanceRecord> financeRecords = new ArrayList<>();

    public finance_c() {
        loadFinanceData();
    }

    public List<FinanceRecord> getFinanceRecords() {
        return financeRecords;
    }

    public void addFinanceRecord(FinanceRecord record) {
        financeRecords.add(record);
        saveFinanceData();
    }

    public void updateFinanceRecord(FinanceRecord record) {
        saveFinanceData();
    }

    public void loadFinanceData() {
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
            System.err.println("Error reading finance file: " + e.getMessage());
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
            System.err.println("Error saving finance file: " + e.getMessage());
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

    public List<String> loadPoIds() {
        List<String> poIds = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(PO_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split("\\|");
                if (data.length > 0) {
                    String poId = data[0].trim();
                    if (!poId.isEmpty()) {
                        poIds.add(poId);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading PO file: " + e.getMessage());
        }
        return poIds;
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

        // Getters and setters
        public String getFinanceId() { return financeId; }
        public String getPoId() { return poId; }
        public String getApprovalStatus() { return approvalStatus; }
        public String getPaymentStatus() { return paymentStatus; }
        public String getPaymentDate() { return paymentDate; }
        public String getAmount() { return amount; }
        public int getVerifiedBy() { return verifiedBy; }

        public void setPoId(String poId) { this.poId = poId; }
        public void setApprovalStatus(String approvalStatus) { this.approvalStatus = approvalStatus; }
        public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
        public void setPaymentDate(String paymentDate) { this.paymentDate = paymentDate; }
        public void setAmount(String amount) { this.amount = amount; }
        public void setVerifiedBy(int verifiedBy) { this.verifiedBy = verifiedBy; }
    }
}