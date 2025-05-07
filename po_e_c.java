import java.io.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

public class po_e_c {

    private static final String PO_FILE = "TXT/po.txt";

    // 读取所有采购订单数据
    public List<String[]> loadPurchaseOrders() {
        List<String[]> fullPoData = new ArrayList<>();
        File file = new File(PO_FILE);

        if (!file.exists()) {
            try {
                File parentDir = file.getParentFile();
                if (parentDir != null && !parentDir.exists()) {
                    parentDir.mkdirs();
                }
                file.createNewFile();
            } catch (IOException | SecurityException ioe) {
                JOptionPane.showMessageDialog(null, "Error creating PO file: " + ioe.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
            return fullPoData; // 返回空列表
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty())
                    continue;
                String[] data = line.split("\\|");
                if (data.length >= 10) {
                    fullPoData.add(data);
                } else {
                    System.err.println("Skipping malformed line #" + lineNumber + " in " + PO_FILE
                            + " (expected 10 fields, got " + data.length + "): " + line);
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error loading purchase orders: " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
        return fullPoData;
    }

    // 添加采购订单到文件 (Order By is now automatically set by po_e)
    public boolean addPurchaseOrder(String prID, String itemID, String supplierID, String quantityStr, String orderDate,
            String orderBy, String receivedBy, String approvedBy) {
        // 数据验证
        if (prID.isEmpty() || itemID.isEmpty() || supplierID.isEmpty() || quantityStr.isEmpty() || orderDate.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Please fill all required fields!", "Input Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(quantityStr);
            if (quantity <= 0)
                throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Quantity must be a positive number.", "Input Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (!orderDate.matches("\\d{2}-\\d{2}-\\d{4}")) {
            JOptionPane.showMessageDialog(null, "Order Date must be in DD-MM-YYYY format.", "Input Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        int poID = generatePOID();
        if (poID < 0) {
            JOptionPane.showMessageDialog(null, "Could not generate PO ID.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        String formattedPOID = String.format("%04d", poID);

        String newPO = String.join("|", formattedPOID, prID, itemID, supplierID, String.valueOf(quantity), orderDate,
                orderBy, receivedBy, approvedBy, "Pending");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(PO_FILE, true))) {
            writer.write(newPO);
            writer.newLine();
            JOptionPane.showMessageDialog(null, "Purchase Order (PO ID: " + formattedPOID + ") added successfully!",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            return true;
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error saving purchase order: " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return false;
        }
    }

    // 从文件中删除采购订单
    public boolean deletePurchaseOrder(String poIDToDelete) {
        File inputFile = new File(PO_FILE);
        File tempFile = null;
        try {
            tempFile = File.createTempFile("temp_po_", ".txt", inputFile.getParentFile());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Could not create temporary file: " + e.getMessage(), "File Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        boolean deleted = false;
        boolean found = false;

        if (!inputFile.exists()) {
            JOptionPane.showMessageDialog(null, "PO file not found.", "Error", JOptionPane.ERROR_MESSAGE);
            if (tempFile.exists())
                tempFile.delete();
            return false;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
                BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty())
                    continue;
                String[] data = line.split("\\|");
                if (data.length > 0 && data[0].equals(poIDToDelete)) {
                    found = true;
                } else {
                    writer.write(line);
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error processing PO file for deletion: " + e.getMessage(), "File Error",
                    JOptionPane.ERROR_MESSAGE);
            if (tempFile.exists())
                tempFile.delete();
            return false;
        }

        if (!found) {
            JOptionPane.showMessageDialog(null, "Could not find PO ID " + poIDToDelete + " in the file.", "Not Found",
                    JOptionPane.WARNING_MESSAGE);
            if (tempFile.exists())
                tempFile.delete();
            return false;
        }

        try {
            if (!inputFile.delete()) {
                System.gc();
                Thread.sleep(100);
                if (!inputFile.delete())
                    throw new IOException("Could not delete original file: " + inputFile.getAbsolutePath());
            }
            if (!tempFile.renameTo(inputFile)) {
                try (InputStream in = new FileInputStream(tempFile);
                        OutputStream out = new FileOutputStream(inputFile)) {
                    byte[] buf = new byte[8192];
                    int len;
                    while ((len = in.read(buf)) > 0)
                        out.write(buf, 0, len);
                } finally {
                    tempFile.delete();
                }
            }
            return true;
        } catch (IOException | SecurityException | InterruptedException e) {
            JOptionPane.showMessageDialog(null, "Error updating PO file: " + e.getMessage(), "File Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    // 生成唯一的 PO ID
    private int generatePOID() {
        int maxID = 0;
        File file = new File(PO_FILE);
        if (!file.exists())
            return 1;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty())
                    continue;
                String[] data = line.split("\\|");
                if (data.length > 0 && !data[0].trim().isEmpty()) {
                    try {
                        int currentID = Integer.parseInt(data[0].trim());
                        if (currentID > maxID)
                            maxID = currentID;
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error reading PO file for ID generation: " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return -1;
        }
        return maxID + 1;
    }

    // 更新采购订单
    public boolean updatePurchaseOrder(String poIDToUpdate, String[] updatedData) {
        File inputFile = new File(PO_FILE);
        File tempFile = null;
        try {
            tempFile = File.createTempFile("temp_po_update_", ".txt", inputFile.getParentFile());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Could not create temporary file for update: " + e.getMessage(),
                    "File Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        boolean updated = false;
        boolean found = false;

        if (!inputFile.exists()) {
            JOptionPane.showMessageDialog(null, "PO file not found.", "Error", JOptionPane.ERROR_MESSAGE);
            if (tempFile.exists())
                tempFile.delete();
            return false;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
                BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty())
                    continue;
                String[] data = line.split("\\|");
                if (data.length > 0 && data[0].equals(poIDToUpdate)) {
                    found = true;
                    if (updatedData.length == 10) {
                        writer.write(String.join("|", updatedData));
                        writer.newLine();
                        updated = true;
                    } else {
                        // Handle error: updatedData has incorrect number of fields
                        JOptionPane.showMessageDialog(null,
                                "Error: Incorrect data format for updating PO ID " + poIDToUpdate + ".", "Update Error",
                                JOptionPane.ERROR_MESSAGE);
                        writer.write(line); // Write the original line to avoid data loss
                        writer.newLine();
                    }
                } else {
                    writer.write(line);
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error processing PO file for update: " + e.getMessage(), "File Error",
                    JOptionPane.ERROR_MESSAGE);
            if (tempFile.exists())
                tempFile.delete();
            return false;
        }

        if (!found) {
            JOptionPane.showMessageDialog(null, "Could not find PO ID " + poIDToUpdate + " to update.", "Not Found",
                    JOptionPane.WARNING_MESSAGE);
            if (tempFile.exists())
                tempFile.delete();
            return false;
        }

        if (updated) {
            try {
                if (!inputFile.delete()) {
                    System.gc();
                    Thread.sleep(100);
                    if (!inputFile.delete())
                        throw new IOException("Could not delete original file for update: "
                                + inputFile.getAbsolutePath());
                }
                if (!tempFile.renameTo(inputFile)) {
                    try (InputStream in = new FileInputStream(tempFile);
                            OutputStream out = new FileOutputStream(inputFile)) {
                        byte[] buf = new byte[8192];
                        int len;
                        while ((len = in.read(buf)) > 0)
                            out.write(buf, 0, len);
                    } finally {
                        tempFile.delete();
                    }
                }
                return true;
            } catch (IOException | SecurityException | InterruptedException e) {
                JOptionPane.showMessageDialog(null, "Error finalizing PO update: " + e.getMessage(), "File Error",
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } else {
            if (tempFile.exists())
                tempFile.delete(); // Clean up temp file if no update happened
            return false;
        }
    }
}