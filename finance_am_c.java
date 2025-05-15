
import java.io.*;
import java.util.*;

public class finance_am_c {

    public static class Finance {

        String financeId, poId, approvalStatus, paymentStatus, paymentDate, amount, verifiedBy;

        public Finance(String financeId, String poId, String approvalStatus, String paymentStatus,
                String paymentDate, String amount, String verifiedBy) {
            this.financeId = financeId;
            this.poId = poId;
            this.approvalStatus = approvalStatus;
            this.paymentStatus = paymentStatus;
            this.paymentDate = paymentDate;
            this.amount = amount;
            this.verifiedBy = verifiedBy;
        }

        public String[] toArray() {
            return new String[]{financeId, poId, approvalStatus, paymentStatus, paymentDate, amount, verifiedBy};
        }
    }

    // Read from file
    public static ArrayList<Finance> readFinanceFile(String fileName) {
        ArrayList<Finance> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(";");
                if (data.length == 7) {
                    Finance f = new Finance(data[0], data[1], data[2], data[3], data[4], data[5], data[6]);
                    list.add(f);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
        return list;
    }

    // Write to file
    public static void writeFinanceFile(String fileName, ArrayList<Finance> list) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName))) {
            for (Finance f : list) {
                bw.write(String.join(";", f.financeId, f.poId, f.approvalStatus, f.paymentStatus,
                        f.paymentDate, f.amount, f.verifiedBy));
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error writing file: " + e.getMessage());
        }
    }
}
