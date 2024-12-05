import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.time.YearMonth;
import java.time.LocalDate;
import java.util.*;

class BudgetManagerTest {
    private BudgetManager budgetManager;
    private ExpenseManager expenseManager;
    private YearMonth currentMonth;

    @BeforeEach
    void setUp() {
        expenseManager = new ExpenseManager();
        budgetManager = new BudgetManager(expenseManager);
        currentMonth = YearMonth.now();
    }

    @Test
    void testInitialDefaultBudgets() {
        Map<String, Double> budgets = budgetManager.getAllBudgets(currentMonth);
        assertEquals(1000.0, budgets.get("Food"));
        assertEquals(1000.0, budgets.get("Transportation"));
        assertEquals(1000.0, budgets.get("Entertainment"));
        assertEquals(1000.0, budgets.get("Utilities"));
        assertEquals(1000.0, budgets.get("Miscellaneous"));
    }

    @Test
    void testSetAndGetBudget() {
        budgetManager.setBudget("Food", 500.0, currentMonth);
        assertEquals(500.0, budgetManager.getBudget("Food", currentMonth));
    }

    @Test
    void testGetBudgetForNonexistentCategory() {
        assertEquals(0.0, budgetManager.getBudget("NonexistentCategory", currentMonth));
    }

    @Test
    void testCalculateTotalExpensesByCategory() {
        LocalDate today = LocalDate.now();
        expenseManager.addExpense(new Expense(today, "Food", 100.0, "Food 1"));
        expenseManager.addExpense(new Expense(today, "Food", 200.0, "Food 2"));
        
        assertEquals(300.0, budgetManager.calculateTotalExpensesByCategory("Food", currentMonth));
    }

    @Test
    void testCalculateBudgetUtilization() {
        budgetManager.setBudget("Food", 1000.0, currentMonth);
        expenseManager.addExpense(new Expense(LocalDate.now(), "Food", 500.0, "Food expense"));

        Map<String, Double> utilization = budgetManager.calculateBudgetUtilization(currentMonth);
        assertEquals(50.0, utilization.get("Food"));
    }

    @Test
    void testBudgetUtilizationWithZeroBudget() {
        budgetManager.setBudget("Food", 0.0, currentMonth);
        expenseManager.addExpense(new Expense(LocalDate.now(), "Food", 100.0, "Food expense"));
        
        Map<String, Double> utilization = budgetManager.calculateBudgetUtilization(currentMonth);
        assertEquals(0.0, utilization.get("Food"));
    }

    @Test
    void testUpdateCallback() {
        boolean[] callbackExecuted = {false};
        budgetManager.setUpdateCallback(() -> callbackExecuted[0] = true);
        budgetManager.setBudget("Food", 500.0, currentMonth);
        assertTrue(callbackExecuted[0]);
    }

    @Test
    void testGetAllBudgets() {
        budgetManager.setBudget("Food", 500.0, currentMonth);
        budgetManager.setBudget("Utilities", 300.0, currentMonth);
        
        Map<String, Double> allBudgets = budgetManager.getAllBudgets(currentMonth);
        assertEquals(500.0, allBudgets.get("Food"));
        assertEquals(300.0, allBudgets.get("Utilities"));
    }

    @Test
    void testGetAvailableMonths() {
        YearMonth nextMonth = currentMonth.plusMonths(1);
        budgetManager.setBudget("Food", 500.0, nextMonth);
        expenseManager.addExpense(new Expense(LocalDate.now(), "Food", 100.0, "Current month expense"));
        expenseManager.addExpense(new Expense(LocalDate.now().plusMonths(1), "Food", 200.0, "Next month expense"));
        
        ArrayList<YearMonth> months = budgetManager.getAvailableMonths();
        assertTrue(months.contains(currentMonth));
        assertTrue(months.contains(nextMonth));
        assertEquals(2, months.size());
    }

    
    @Test
    void testExpensesAcrossMultipleMonths() {
        YearMonth nextMonth = currentMonth.plusMonths(1);
        expenseManager.addExpense(new Expense(LocalDate.now(), "Food", 100.0, "Current month"));
        expenseManager.addExpense(new Expense(LocalDate.now().plusMonths(1), "Food", 200.0, "Next month"));
        
        assertEquals(100.0, budgetManager.calculateTotalExpensesByCategory("Food", currentMonth));
        assertEquals(200.0, budgetManager.calculateTotalExpensesByCategory("Food", nextMonth));
    }
    
    @Test
    void testBudgetUpdate() {
        budgetManager.setBudget("Food", 200.0, currentMonth);
        budgetManager.setBudget("Food", 300.0, currentMonth);
        assertEquals(300.0, budgetManager.getBudget("Food", currentMonth));
    }

    @Test
    void testMultipleCallbackExecutions() {
        int[] callbackCount = {0};
        budgetManager.setUpdateCallback(() -> callbackCount[0]++);
        budgetManager.setBudget("Food", 200.0, currentMonth);
        budgetManager.setBudget("Transportation", 300.0, currentMonth);
        assertEquals(2, callbackCount[0]);
    }

    @Test
    void testGetBudgetAfterCategoryDeletion() {
        budgetManager.setBudget("Food", 200.0, currentMonth);
        budgetManager.setBudget("Food", 0.0, currentMonth);
        assertEquals(0.0, budgetManager.getBudget("Food", currentMonth));
    }


    @Test
    void testUtilizationWithNoExpenses() {
        budgetManager.setBudget("Utilities", 500.0, currentMonth);
        assertEquals(0.0, budgetManager.calculateBudgetUtilization(currentMonth).get("Utilities"));
    }

    @Test
    void testGetAllBudgetsWithNoBudgetsSet() {
        assertTrue(budgetManager.getAllBudgets(currentMonth.plusMonths(2)).isEmpty());
    }

    @Test
    void testAvailableMonthsFromExpensesOnly() {
        expenseManager.addExpense(new Expense(LocalDate.now().minusMonths(1), "Food", 100.0, "Previous month"));
        assertTrue(budgetManager.getAvailableMonths().contains(currentMonth.minusMonths(1)));
    }

}
