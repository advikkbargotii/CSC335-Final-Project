import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

public class DataPersistenceManager {
    private static final String DATA_DIR = "data";
    
    private static String getUserDataFileName(String username) {
        return username + "_data.txt";
    }

    public DataPersistenceManager() {
        initializeDataDirectory();
    }

    private void initializeDataDirectory() {
        try {
            Path dirPath = Paths.get(DATA_DIR);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
                System.out.println("Created data directory at: " + dirPath.toAbsolutePath());
            } else {
                System.out.println("Data directory exists at: " + dirPath.toAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("Error creating data directory: " + e.getMessage());
            throw new RuntimeException("Could not create data directory", e);
        }
    }

    public void saveUserData(User user, ExpenseManager expenseManager) {
    	   String userDataPath = DATA_DIR + "/" + getUserDataFileName(user.getUsername());

    	   try (BufferedWriter writer = new BufferedWriter(new FileWriter(userDataPath))) {
    	       // Write budget data
    	       writer.write("[BUDGETS]\n");
    	       ArrayList<YearMonth> months = expenseManager.getBudgetManager().getAvailableMonths();

    	       for (YearMonth month : months) {
    	           Map<String, Double> budgets = expenseManager.getBudgetManager().getAllBudgets(month);
    	           for (Map.Entry<String, Double> entry : budgets.entrySet()) {
    	               String line = month.toString() + "," + entry.getKey() + "," + 
    	                   String.format("%.2f", entry.getValue()) + "\n";
    	               writer.write(line);
    	           }
    	       }

    	       // Write expenses data 
    	       writer.write("[EXPENSES]\n");
    	       List<Expense> expenses = expenseManager.getAllExpenses();
    	       
    	       for (Expense expense : expenses) {
    	           String line = expense.getDate() + "," +
    	               expense.getCategory() + "," +
    	               String.format("%.2f", expense.getAmount()) + "," +
    	               expense.getDescription().replace(",", ";") + "\n";
    	           writer.write(line);
    	       }

    	   } catch (IOException e) {
    	       throw new RuntimeException("Error saving user data: " + e.getMessage());
    	   }
    	}

    public void loadUserData(User user, ExpenseManager expenseManager) {
        String userDataPath = Paths.get(DATA_DIR, getUserDataFileName(user.getUsername())).toString();
        System.out.println("Attempting to load data for user: " + user.getUsername());
        System.out.println("Loading from path: " + userDataPath);
        
        if (!Files.exists(Paths.get(userDataPath))) {
            System.out.println("No existing data file found for user");
            return;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(userDataPath))) {
            String line;
            String section = "";
            int budgetCount = 0;
            int expenseCount = 0;
            
            while ((line = reader.readLine()) != null) {
                if (line.equals("[BUDGETS]")) {
                    section = "BUDGETS";
                    System.out.println("Reading budgets section");
                    continue;
                } else if (line.equals("[EXPENSES]")) {
                    section = "EXPENSES";
                    System.out.println("Reading expenses section");
                    continue;
                }
                
                if (section.equals("BUDGETS")) {
                    String[] parts = line.split(",");
                    if (parts.length == 3) {
                        YearMonth month = YearMonth.parse(parts[0]);
                        String category = parts[1];
                        double amount = Double.parseDouble(parts[2]);
                        expenseManager.getBudgetManager().setBudget(category, amount, month);
                        budgetCount++;
                        System.out.println("Loaded budget: " + category + " = " + amount + " for " + month);
                    }
                } else if (section.equals("EXPENSES")) {
                    String[] parts = line.split(",", 4);
                    if (parts.length == 4) {
                        Expense expense = new Expense(
                            LocalDate.parse(parts[0]),
                            parts[1],
                            Double.parseDouble(parts[2]),
                            parts[3].replace(";", ",")
                        );
                        expenseManager.addExpense(expense);
                        expenseCount++;
                        System.out.println("Loaded expense: " + expense);
                    }
                }
            }
            
            System.out.println("Data load completed successfully");
            System.out.println("Loaded " + budgetCount + " budgets and " + expenseCount + " expenses");
            
        } catch (IOException e) {
            System.err.println("Error loading user data: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error loading user data: " + e.getMessage(), e);
        }
    }

    public void backupUserData(User user) {
        String userDataPath = Paths.get(DATA_DIR, getUserDataFileName(user.getUsername())).toString();
        String backupPath = userDataPath + ".backup";
        System.out.println("Creating backup from " + userDataPath + " to " + backupPath);
        
        try {
            Files.copy(Paths.get(userDataPath), Paths.get(backupPath), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Backup created successfully");
        } catch (IOException e) {
            System.err.println("Error creating backup: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error creating backup: " + e.getMessage(), e);
        }
    }

    public void deleteUserData(User user) {
        String userDataPath = Paths.get(DATA_DIR, getUserDataFileName(user.getUsername())).toString();
        System.out.println("Attempting to delete user data at: " + userDataPath);
        
        try {
            boolean deleted = Files.deleteIfExists(Paths.get(userDataPath));
            System.out.println("Delete operation result: " + (deleted ? "File deleted" : "File not found"));
        } catch (IOException e) {
            System.err.println("Error deleting user data: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error deleting user data: " + e.getMessage(), e);
        }
    }
    
    public void deleteUserPassword(User user) {

        String usersFilePath = Paths.get(DATA_DIR, "users.txt").toString();
        System.out.println("Attempting to delete user data at: " + usersFilePath);

        try {
            File usersFile = new File(usersFilePath);
            if (usersFile.exists()) {

                List<String> lines = Files.readAllLines(usersFile.toPath());
                
                // Filter out the line matching the username:password hash
                String userLineToRemove = user.getUsername() + ":" + user.getPasswordHash() + ":" + user.getSalt();
                List<String> updatedLines = lines.stream()
                                                 .filter(line -> !line.trim().equals(userLineToRemove))
                                                 .toList();

                // Write the updated lines back to the users file
                Files.write(usersFile.toPath(), updatedLines, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
                System.out.println("User entry removed from users file.");
            } else {
                System.out.println("Users file not found. Skipping removal of user entry.");
            }
        } catch (IOException e) {
            System.err.println("Error deleting user data: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error deleting user data: " + e.getMessage(), e);
        }
    }
}
