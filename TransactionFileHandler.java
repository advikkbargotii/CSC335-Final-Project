import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles the importing and exporting of transaction files for an ExpenseManager.
 * This class allows transactions to be loaded from and saved to files, managing data integrity and formatting.
 */
public class TransactionFileHandler {
    private ExpenseManager expenseManager;
    
    /**
     * Constructs a TransactionFileHandler with a reference to an ExpenseManager to manage expenses.
     * @param expenseManager The ExpenseManager that will handle the expenses extracted or saved.
     */
    public TransactionFileHandler(ExpenseManager expenseManager) {
        this.expenseManager = expenseManager;
    }
    
    /**
     * Imports transactions from a specified file into the ExpenseManager.
     * This method reads a file line by line, parsing and validating each transaction before adding it to the manager.
     * 
     * @param file The file from which transactions are to be imported.
     * @throws IOException If an I/O error occurs reading from the file.
     * @throws TransactionImportException If any error occurs during the import process, including data validation errors.
     */
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
                    
                    LocalDate date = LocalDate.parse(parts[0].trim());
                    String category = parts[1].trim();
                    if (!ExpenseManager.predefinedCategories.contains(category)) {
                        errorLines.add("Line " + lineNumber + ": Invalid category - " + category);
                        continue;
                    }
                    
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
    
    /**
     * Exports all transactions managed by the ExpenseManager to a specified file.
     * Each transaction is written in a CSV format with the date, category, amount, and description.
     * 
     * @param file The file to which transactions are to be exported.
     * @throws IOException If an I/O error occurs writing to the file.
     */
    public void exportTransactions(File file) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (Expense expense : expenseManager.getAllExpenses()) {
                String line = String.format("%s,%s,%.2f,%s\n",
                    expense.getDate(),
                    expense.getCategory(),
                    expense.getAmount(),
                    expense.getDescription().replace(",", ";")  // Handle commas in descriptions
                );
                writer.write(line);
            }
        }
    }
    
    /**
     * Exception class for handling errors during the transaction import process.
     * This class encapsulates information about partial successes where some transactions might still be processed correctly.
     */
    public static class TransactionImportException extends Exception {
        private final boolean partialSuccess;
        
        /**
         * Constructs a TransactionImportException with a message and a success flag.
         * @param message Detailed message about the errors encountered during import.
         * @param partialSuccess True if some transactions were successfully imported despite errors.
         */
        public TransactionImportException(String message, boolean partialSuccess) {
            super(message);
            this.partialSuccess = partialSuccess;
        }
        
        /**
         * Indicates whether any transactions were successfully imported.
         * @return True if some transactions were successfully imported, false otherwise.
         */
        public boolean isPartialSuccess() {
            return partialSuccess;
        }
    }
}
