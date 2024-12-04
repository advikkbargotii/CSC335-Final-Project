import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class TransactionFileHandler {
    private ExpenseManager expenseManager;
    
    public TransactionFileHandler(ExpenseManager expenseManager) {
        this.expenseManager = expenseManager;
    }
    
    public void importTransactions(File file) throws IOException, TransactionImportException {
        List<String> errorLines = new ArrayList<>();
        int lineNumber = 0;
        int successfulImports = 0;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                try {
                    if (line.trim().isEmpty()) continue;
                    
                    String[] parts = line.split(",");
                    if (parts.length != 4) {
                        errorLines.add("Line " + lineNumber + ": Invalid number of fields");
                        continue;
                    }
                    
                    // Parse date
                    LocalDate date = LocalDate.parse(parts[0].trim());
                    
                    // Validate category
                    String category = parts[1].trim();
                    if (!ExpenseManager.predefinedCategories.contains(category)) {
                        errorLines.add("Line " + lineNumber + ": Invalid category - " + category);
                        continue;
                    }
                    
                    // Parse amount
                    double amount;
                    try {
                        amount = Double.parseDouble(parts[2].trim());
                        if (amount <= 0) {
                            throw new NumberFormatException("Amount must be positive");
                        }
                    } catch (NumberFormatException e) {
                        errorLines.add("Line " + lineNumber + ": Invalid amount");
                        continue;
                    }
                    
                    String description = parts[3].trim();
                    
                    // Create and add expense
                    Expense expense = new Expense(date, category, amount, description);
                    expenseManager.addExpense(expense);
                    successfulImports++;
                    
                } catch (DateTimeParseException e) {
                    errorLines.add("Line " + lineNumber + ": Invalid date format");
                } catch (Exception e) {
                    errorLines.add("Line " + lineNumber + ": " + e.getMessage());
                }
            }
        }
        
        // Generate import summary
        StringBuilder summary = new StringBuilder();
        summary.append(String.format("Successfully imported %d transactions\n", successfulImports));
        
        if (!errorLines.isEmpty()) {
            summary.append("\nErrors encountered:\n");
            errorLines.forEach(error -> summary.append(error).append("\n"));
        }
        
        if (expenseManager.getGuiUpdateCallback() != null) {
            expenseManager.getGuiUpdateCallback().run();
        }
        
        throw new TransactionImportException(summary.toString(), successfulImports > 0);
    }
    
    public void exportTransactions(File file) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (Expense expense : expenseManager.getAllExpenses()) {
                String line = String.format("%s,%s,%.2f,%s\n",
                    expense.getDate(),
                    expense.getCategory(),
                    expense.getAmount(),
                    expense.getDescription().replace(",", ";")  // Replace commas in description
                );
                writer.write(line);
            }
        }
    }
    
    public static class TransactionImportException extends Exception {
        private final boolean partialSuccess;
        
        public TransactionImportException(String message, boolean partialSuccess) {
            super(message);
            this.partialSuccess = partialSuccess;
        }
        
        public boolean isPartialSuccess() {
            return partialSuccess;
        }
    }
}
