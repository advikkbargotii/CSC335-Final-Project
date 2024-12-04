import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

public class ExpenseTest {
    private Expense expense;
    private LocalDate defaultDate;
    
    @BeforeEach
    void setUp() {
        defaultDate = LocalDate.of(2024, 1, 1);
        expense = new Expense(defaultDate, "Food", 50.00, "Grocery shopping");
    }
    
    @Test
    void testExpenseConstructorAndGetters() {
        assertEquals(defaultDate, expense.getDate());
        assertEquals("Food", expense.getCategory());
        assertEquals(50.00, expense.getAmount());
        assertEquals("Grocery shopping", expense.getDescription());
    }
    
    @Test
    void testMultipleExpensesIndependence() {
        Expense expense1 = new Expense(defaultDate, "Food", 50.00, "Expense 1");
        Expense expense2 = new Expense(defaultDate, "Transportation", 30.00, "Expense 2");
        
        // Modify expense1
        expense1.setAmount(75.00);
        expense1.setDescription("Modified Expense 1");
        
        // Verify expense2 remains unchanged
        assertEquals(30.00, expense2.getAmount());
        assertEquals("Expense 2", expense2.getDescription());
    }
    
    @Test
    void testExpenseWithVariousValues() {
        LocalDate[] dates = {
            LocalDate.of(2024, 1, 1),
            LocalDate.of(2024, 12, 31),
            LocalDate.of(2024, 6, 15),
            LocalDate.of(2024, 2, 29)
        };
        
        String[] categories = {
            "Food",
            "Entertainment",
            "Utilities",
            "Transportation"
        };
        
        double[] amounts = {
            0.01,
            9999.99,
            50.55,
            100.00
        };
        
        String[] descriptions = {
            "Minimum amount",
            "Large amount",
            "With decimals",
            "Leap year date"
        };
        
        for (int i = 0; i < dates.length; i++) {
            Expense testExpense = new Expense(dates[i], categories[i], amounts[i], descriptions[i]);
            assertEquals(dates[i], testExpense.getDate());
            assertEquals(categories[i], testExpense.getCategory());
            assertEquals(amounts[i], testExpense.getAmount());
            assertEquals(descriptions[i], testExpense.getDescription());
        }
    }
    
    @Test
    void testSetDate() {
        LocalDate[] testDates = {
            LocalDate.of(2024, 1, 1),
            LocalDate.of(2024, 12, 31),
            LocalDate.of(2024, 2, 29),
            LocalDate.of(2025, 1, 1),
            LocalDate.now()
        };
        
        for (LocalDate date : testDates) {
            expense.setDate(date);
            assertEquals(date, expense.getDate());
        }
    }
    
    @Test
    void testSetCategory() {
        String[] categories = {
            "Food",
            "Transportation",
            "Entertainment",
            "Utilities",
            "Miscellaneous",
            "Custom Category",
            "",  // Empty category
            "   ", // Whitespace category
            "Category with special chars !@#$%"
        };
        
        for (String category : categories) {
            expense.setCategory(category);
            assertEquals(category, expense.getCategory());
        }
    }
    
    @Test
    void testSetAmount() {
        double[] amounts = {
            0.01,  // Minimum practical amount
            1.00,  // Whole dollar
            99999.99,  // Large amount
            50.555,  // More than 2 decimal places
            1234567.89,  // Very large amount
            0.001,  // Very small amount
            100000.00  // Large round number
        };
        
        for (double amount : amounts) {
            expense.setAmount(amount);
            assertEquals(amount, expense.getAmount());
        }
    }
    
    @Test
    void testSetDescription() {
        String[] descriptions = {
            "Regular description",
            "",  // Empty description
            "   ",  // Whitespace description
            "Very long description that contains multiple words and spans more than one line of text",
            "Description with numbers 123 and special characters !@#$%^&*()",
            "Description\nwith\nnewlines",
            "Description,with,commas",
            "Description;with;semicolons",
            "   Description with leading/trailing whitespace   "
        };
        
        for (String description : descriptions) {
            expense.setDescription(description);
            assertEquals(description, expense.getDescription());
        }
    }
    
    @Test
    void testToStringWithVariousFormats() {
        // Test regular expense
        Expense regular = new Expense(
            LocalDate.of(2024, 1, 1),
            "Food",
            50.00,
            "Regular expense"
        );
        assertEquals("2024-01-01 - Food - $50.0 - Regular expense", regular.toString());
        
        // Test with zero amount
        Expense zeroAmount = new Expense(
            LocalDate.of(2024, 1, 1),
            "Food",
            0.00,
            "Zero amount"
        );
        assertEquals("2024-01-01 - Food - $0.0 - Zero amount", zeroAmount.toString());
        
        // Test with large amount
        Expense largeAmount = new Expense(
            LocalDate.of(2024, 1, 1),
            "Food",
            999999.99,
            "Large amount"
        );
        assertEquals("2024-01-01 - Food - $999999.99 - Large amount", largeAmount.toString());
        
        // Test with empty description
        Expense emptyDesc = new Expense(
            LocalDate.of(2024, 1, 1),
            "Food",
            50.00,
            ""
        );
        assertEquals("2024-01-01 - Food - $50.0 - ", emptyDesc.toString());
        
        // Test with special characters
        Expense specialChars = new Expense(
            LocalDate.of(2024, 1, 1),
            "Category!@#",
            50.00,
            "Description!@#"
        );
        assertEquals("2024-01-01 - Category!@# - $50.0 - Description!@#", specialChars.toString());
    }
    
   
    
    @Test
    void testExtremeDates() {
        // Test with minimum and maximum possible dates
        LocalDate[] extremeDates = {
            LocalDate.of(1900, 1, 1),  // Very old date
            LocalDate.of(2100, 12, 31), // Future date
            LocalDate.now(),            // Current date
            LocalDate.of(2024, 2, 29)   // Leap year
        };
        
        for (LocalDate date : extremeDates) {
            Expense testExpense = new Expense(date, "Food", 50.00, "Test date: " + date);
            assertEquals(date, testExpense.getDate());
        }
    }
}