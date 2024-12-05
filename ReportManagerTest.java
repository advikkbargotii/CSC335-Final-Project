import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class ReportManagerTest {

    private ExpenseManager mockExpenseManager;
    private BudgetManager mockBudgetManager;
    private ReportManager reportManager;

    @BeforeEach
    void setUp() {
      
        mockExpenseManager = new ExpenseManager();
        reportManager = new ReportManager(mockExpenseManager);


        mockExpenseManager.addExpense(new Expense(LocalDate.of(2024, 1, 5), "Food", 50, "Groceries"));
        mockExpenseManager.addExpense(new Expense(LocalDate.of(2024, 1, 10), "Transportation", 30, "Bus fare"));
        mockExpenseManager.addExpense(new Expense(LocalDate.of(2024, 1, 20), "Food", 20.00, "Dinner"));
        mockExpenseManager.addExpense(new Expense(LocalDate.of(2024, 1, 25), "Utilities", 100, "Electricity Bill"));

        mockExpenseManager.getBudgetManager().setBudget("Food", 200, YearMonth.of(2024, 1));
        mockExpenseManager.getBudgetManager().setBudget("Transportation", 100, YearMonth.of(2024, 1));
        mockExpenseManager.getBudgetManager().setBudget("Utilities", 150, YearMonth.of(2024, 1));
    }

    @Test
    void testGenerateMonthlySummaryReport() {
      
        YearMonth month = YearMonth.of(2024, 1);
        //System.out.println("SHOULD BE 01 " + month.toString());
        String report = reportManager.generateMonthlySummaryReport(month);

        assertNotNull(report);
        System.out.println(report);
        assertTrue(report.contains("Monthly Report for 2024-01"));
        assertTrue(report.contains("Food Budget: 200.0"));
        assertTrue(report.contains("Food Expenses: 70.0"));
        assertTrue(report.contains("Utilities Budget: 150.0"));
        assertTrue(report.contains("Utilities Expenses: 100.0"));
    }

    @Test
    void testGetTotalExpenses() {
      
        YearMonth month = YearMonth.of(2024, 1);
        double totalExpenses = reportManager.getTotalExpenses(month);

        assertEquals(200, totalExpenses, 0.001);
    }

    @Test
    void testGetTotalBudget() {
      
        YearMonth month = YearMonth.of(2024, 1);
        double totalBudget = reportManager.getTotalBudget(month);

        assertEquals(450, totalBudget, 0.001);
    }

    @Test
    void testGetCategoryWiseSpending() {
      
        YearMonth month = YearMonth.of(2024, 1);
        Map<String, Double> spendingByCategory = reportManager.getCategoryWiseSpending(month);

        assertEquals(70, spendingByCategory.get("Food"), 0.001);
        assertEquals(30, spendingByCategory.get("Transportation"), 0.001);
        assertEquals(100, spendingByCategory.get("Utilities"), 0.001);
        assertEquals(0, spendingByCategory.get("Entertainment"), 0.001);
        assertEquals(0, spendingByCategory.get("Miscellaneous"), 0.001);
    }

    @Test
    void testGenerateEmptyReport() {
      
        YearMonth month = YearMonth.of(2024, 2);
        String report = reportManager.generateMonthlySummaryReport(month);

        assertNotNull(report);
        assertTrue(report.contains("Monthly Report for 2024-02"));
        assertTrue(report.contains("Total Budget: 0.0 | Total Expenses: 0.0"));
    }
}
