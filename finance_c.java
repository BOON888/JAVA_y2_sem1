import java.util.ArrayList;
import java.util.List;

public class finance_c {
    public static final String STATUS_PENDING = "Pending";
    public static final String STATUS_VERIFIED = "Verified";
    public static final String STATUS_NOT_VERIFIED = "Not Verified";
    public static final String STATUS_PAID = "Paid";
    
    private List<FinanceRecord> financeRecords = new ArrayList<>();
    
    public static class FinanceRecord {
        private String financeId;
        private String poId;
        private String approvalStatus;
        private String paymentStatus;
        private String paymentDate;
        private String amount;
        private int verifiedBy;
        
        public FinanceRecord(String financeId, String poId, String approvalStatus, 
                           String paymentStatus, String paymentDate, String amount, int verifiedBy) {
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
        
        public void setApprovalStatus(String status) { this.approvalStatus = status; }
        public void setPaymentStatus(String status) { this.paymentStatus = status; }
        public void setVerifiedBy(int userId) { this.verifiedBy = userId; }
    }
    
    public String generateNewFinanceId() {
        return "FIN" + System.currentTimeMillis();
    }
    
    public List<String> loadPoIds() {
        // In a real application, this would load from a database
        List<String> poIds = new ArrayList<>();
        poIds.add("PO1001");
        poIds.add("PO1002");
        poIds.add("PO1003");
        return poIds;
    }
    
    public void addFinanceRecord(FinanceRecord record) {
        financeRecords.add(record);
    }
    
    public void updateFinanceRecord(FinanceRecord record) {
        // In a real application, this would update the record in the database
        for (int i = 0; i < financeRecords.size(); i++) {
            if (financeRecords.get(i).getFinanceId().equals(record.getFinanceId())) {
                financeRecords.set(i, record);
                break;
            }
        }
    }
    
    public List<FinanceRecord> getFinanceRecords() {
        return financeRecords;
    }
}