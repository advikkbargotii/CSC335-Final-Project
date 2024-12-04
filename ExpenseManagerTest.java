import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public class ExpenseManagerTest {
    
    private ExpenseManager expenseManager;
    private Expense testExpense;
    private boolean callbackTriggered;
    
    @BeforeEach
    void setUp() {
        expenseManager = new ExpenseManager();
        testExpense = new Expense(
            LocalDate.of(2024, 1, 1),
            "Food",
            50.00,
            "Test expense"
        );
        callbackTriggered = false;
        expenseManager.setGuiUpdateCallback(() -> callbackTriggered = true);
    }
    
    @Test
    void testAddExpense() {
        expenseManager.addExpense(testExpense);
        
        List<Expense> expenses = expenseManager.getAllExpenses();
        assertEquals(1, expenses.size());
        assertEquals(testExpense, expenses.get(0));
        assertTrue(callbackTriggered);
    }
    
    @Test
    void testEditExpense() {
        expenseManager.addExpense(testExpense);
        
        Expense updatedExpense = new Expense(
            LocalDate.of(2024, 1, 2),
            "Transportation",
            75.50,
            "Updated expense"
        );
        
        expenseManager.editExpense(0, updatedExpense);
        
        List<Expense> expenses = expenseManager.getAllExpenses();
        assertEquals(1, expenses.size());
        assertEquals(updatedExpense, expenses.get(0));
        assertTrue(callbackTriggered);
    }
    
    @Test
    void testEditExpenseInvalidIndex() {
        assertThrows(IndexOutOfBoundsException.class, 
            () -> expenseManager.editExpense(0, testExpense));
    }
    
    @Test
    void testDeleteExpense() {
        expenseManager.addExpense(testExpense);
        callbackTriggered = false;
        
        expenseManager.deleteExpense(0);
        
        assertTrue(expenseManager.getAllExpenses().isEmpty());
        assertTrue(callbackTriggered);
    }
    
    @Test
    void testDeleteExpenseInvalidIndex() {
        assertThrows(IndexOutOfBoundsException.class, 
            () -> expenseManager.deleteExpense(0));
    }
    
    @Test
    void testFilterByCategory() {
        Expense expense1 = new Expense(LocalDate.now(), "Food", 50.00, "Food 1");
        Expense expense2 = new Expense(LocalDate.now(), "Transportation", 30.00, "Transport 1");
        Expense expense3 = new Expense(LocalDate.now(), "Food", 40.00, "Food 2");
        
        expenseManager.addExpense(expense1);
        expenseManager.addExpense(expense2);
        expenseManager.addExpense(expense3);
        
        List<Expense> foodExpenses = expenseManager.filterByCategory("Food");
        assertEquals(2, foodExpenses.size());
        assertTrue(foodExpenses.contains(expense1));
        assertTrue(foodExpenses.contains(expense3));
    }
    
    @Test
    void testFilterByDateRange() {
        LocalDate date1 = LocalDate.of(2024, 1, 1);
        LocalDate date2 = LocalDate.of(2024, 1, 15);
        LocalDate date3 = LocalDate.of(2024, 1, 31);
        
        Expense expense1 = new Expense(date1, "Food", 50.00, "Expense 1");
        Expense expense2 = new Expense(date2, "Food", 30.00, "Expense 2");
        Expense expense3 = new Expense(date3, "Food", 40.00, "Expense 3");
        
        expenseManager.addExpense(expense1);
        expenseManager.addExpense(expense2);
        expenseManager.addExpense(expense3);
        
        List<Expense> filteredExpenses = expenseManager.filterByDateRange(date1, date2);
        assertEquals(2, filteredExpenses.size());
        assertTrue(filteredExpenses.contains(expense1));
        assertTrue(filteredExpenses.contains(expense2));
    }
    
    @Test
    void testGetPredefinedCategories() {
        List<String> categories = ExpenseManager.getPredefinedCategories();
        assertEquals(5, categories.size());
        assertTrue(categories.contains("Food"));
        assertTrue(categories.contains("Transportation"));
        assertTrue(categories.contains("Entertainment"));
        assertTrue(categories.contains("Utilities"));
        assertTrue(categories.contains("Miscellaneous"));
    }
    
    @Test
    void testGetExpensesForMonth() {
        LocalDate date1 = LocalDate.of(2024, 1, 15);
        LocalDate date2 = LocalDate.of(2024, 2, 15);
        
        Expense expense1 = new Expense(date1, "Food", 50.00, "January Expense");
        Expense expense2 = new Expense(date2, "Food", 30.00, "February Expense");
        
        expenseManager.addExpense(expense1);
        expenseManager.addExpense(expense2);
        
        List<Expense> januaryExpenses = expenseManager.getExpensesForMonth(YearMonth.of(2024, 1));
        assertEquals(1, januaryExpenses.size());
        assertEquals(expense1, januaryExpenses.get(0));
    }
    
    @Test
    void testCalculateMonthlyExpensesByCategory() {
        LocalDate date = LocalDate.of(2024, 1, 15);
        YearMonth yearMonth = YearMonth.of(2024, 1);
        
        expenseManager.addExpense(new Expense(date, "Food", 50.00, "Food 1"));
        expenseManager.addExpense(new Expense(date, "Food", 30.00, "Food 2"));
        expenseManager.addExpense(new Expense(date, "Transportation", 20.00, "Transport"));
        
        double foodTotal = expenseManager.calculateMonthlyExpensesByCategory("Food", yearMonth);
        assertEquals(80.00, foodTotal);
    }
    
    @Test
    void testGetMonthlyTotalsByCategory() {
        LocalDate date = LocalDate.of(2024, 1, 15);
        YearMonth yearMonth = YearMonth.of(2024, 1);
        
        expenseManager.addExpense(new Expense(date, "Food", 50.00, "Food 1"));
        expenseManager.addExpense(new Expense(date, "Food", 30.00, "Food 2"));
        expenseManager.addExpense(new Expense(date, "Transportation", 20.00, "Transport"));
        
        Map<String, Double> monthlyTotals = expenseManager.getMonthlyTotalsByCategory(yearMonth);
        
        assertEquals(80.00, monthlyTotals.get("Food"));
        assertEquals(20.00, monthlyTotals.get("Transportation"));
        assertEquals(0.00, monthlyTotals.get("Entertainment"));
        assertEquals(0.00, monthlyTotals.get("Utilities"));
        assertEquals(0.00, monthlyTotals.get("Miscellaneous"));
    }
    
    @Test
    void testGuiUpdateCallback() {
        Runnable callback = () -> callbackTriggered = true;
        expenseManager.setGuiUpdateCallback(callback);
        assertEquals(callback, expenseManager.getGuiUpdateCallback());
        
        expenseManager.triggerUpdate();
        assertTrue(callbackTriggered);
    }
}