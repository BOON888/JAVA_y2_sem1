
import java.io.*;

public class login_c {

    private String selectedRole;
    private String enteredPassword;

    public login_c(String selectedRole, String enteredPassword) {
        this.selectedRole = selectedRole;
        this.enteredPassword = enteredPassword;
    }

    public boolean authenticate() {
        File file = new File("TXT/users.txt");

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");

                if (parts.length == 6) {
                    String role = parts[3];
                    String password = parts[2];

                    if (role.equalsIgnoreCase(selectedRole) && password.equals(enteredPassword)) {
                        return true; // Found a matching user
                    }
                }
            }

        } catch (IOException e) {
            System.out.println("Error reading users.txt: " + e.getMessage());
        }

        return false; // No match found
    }
}
