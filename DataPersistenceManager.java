import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
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
        String userDataPath = Paths.get(DATA_DIR, getUserDataFileName(user.getUsername())).toString();
        System.out.println("Attempting to save data for user: " + user.getUsername());
        System.out.println("Saving to path: " + userDataPath);
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(userDataPath))) {
            // Write budget data
            writer.write("[BUDGETS]\n");
            Map<String, Double> budgets = expenseManager.getBudgetManager().getAllBudgets();
            System.out.println("Saving budgets: " + budgets.size() + " entries");
            for (Map.Entry<String, Double> entry : budgets.entrySet()) {
                String line = String.format("%s,%.2f\n", entry.getKey(), entry.getValue());
                writer.write(line);
                System.out.println("Wrote budget: " + line.trim());
            }
            
            // Write expenses data
            writer.write("[EXPENSES]\n");
            List<Expense> expenses = expenseManager.getAllExpenses();
            System.out.println("Saving expenses: " + expenses.size() + " entries");
            for (Expense expense : expenses) {
                String line = String.format("%s,%s,%.2f,%s\n",
                    expense.getDate(),
                    expense.getCategory(),
                    expense.getAmount(),
                    expense.getDescription().replace(",", ";"));
                writer.write(line);
                System.out.println("Wrote expense: " + line.trim());
            }
            System.out.println("Data save completed successfully");
            
        } catch (IOException e) {
            System.err.println("Error saving user data: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error saving user data: " + e.getMessage(), e);
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
                    if (parts.length == 2) {
                        String category = parts[0];
                        double amount = Double.parseDouble(parts[1]);
                        expenseManager.getBudgetManager().setBudget(category, amount);
                        budgetCount++;
                        System.out.println("Loaded budget: " + category + " = " + amount);
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
}