import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class BudgetManagerPanel extends JPanel {
    private BudgetManager budgetManager;
    private Map<String, JProgressBar> progressBars;

    public BudgetManagerPanel(BudgetManager budgetManager) {
        this.budgetManager = budgetManager;
        setLayout(new GridLayout(0, 2));
        this.progressBars = new HashMap<>();
        initializeProgressBars();
    }

    private void initializeProgressBars() {
        for (String category : ExpenseManager.predefinedCategories) {
            JProgressBar progressBar = new JProgressBar(0, 100);
            progressBar.setStringPainted(true);
            progressBars.put(category, progressBar);

            add(new JLabel(category + " Budget:"));
            updateProgressBar(category); // Initial update on creation
        }
    }

    public void updateProgressBar(String category) {
        double totalExpenses = budgetManager.calculateTotalExpensesByCategory(category);
        double budget = budgetManager.getBudget(category);
        int progress = (int) ((totalExpenses / budget) * 100);
        JProgressBar progressBar = progressBars.get(category);
        progressBar.setValue(progress);

        
        if (progress >= 80 ) {
            progressBar.setForeground(Color.RED);
        } else {
            progressBar.setForeground(Color.GREEN);
        }
    }

    //method to update all progress bars
    public void updateAllProgressBars() {
        for (String category : ExpenseManager.predefinedCategories) {
            updateProgressBar(category);
        }
    }
}
