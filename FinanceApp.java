import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class FinanceApp {
    private User currentUser;
    private ExpenseManager expenseManager;
    private JFrame frame;
    private Map<String, JProgressBar> progressBars; //stores progress bars for each category

    public FinanceApp(User user) {
        this.currentUser = user;
        this.expenseManager = new ExpenseManager();
        this.progressBars = new HashMap<>();
        
        // Set up callback to update progress bars
        expenseManager.setGuiUpdateCallback(() -> {
            for (String category : ExpenseManager.predefinedCategories) {
                updateProgress(category);
            }
        });
        
        createFinanceAppFrame();
        addBudgetManagementFeatures();
        createReportFeatures();
    }
    
    private void createReportFeatures() {
 
        JButton reportButton = new JButton("View Monthly Report");
        reportButton.addActionListener(e -> showReportManagerPanel());
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(reportButton);
        
        frame.add(buttonPanel, BorderLayout.NORTH);
    }


    private void showReportManagerPanel() {

        JFrame reportFrame = new JFrame("Monthly Report");
        reportFrame.setSize(800, 600);
        reportFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);


        ReportManager reportManager = new ReportManager(expenseManager);
        ReportManagerPanel reportManagerPanel = new ReportManagerPanel(reportManager);
        reportFrame.add(reportManagerPanel);

 
        reportFrame.setVisible(true);
    }



    private void createFinanceAppFrame() {
        frame = new JFrame("Personal Finance Assistant - Dashboard");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(new BorderLayout());

        JLabel welcomeLabel = new JLabel("Welcome, " + currentUser.getUsername() + "!");
        welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        frame.add(welcomeLabel, BorderLayout.NORTH);

        BudgetManagerPanel budgetManagerPanel = new BudgetManagerPanel(expenseManager.getBudgetManager());
        ExpenseTrackerPanel expenseTrackerPanel = new ExpenseTrackerPanel(expenseManager);
        
        frame.add(expenseTrackerPanel, BorderLayout.CENTER);
        frame.add(budgetManagerPanel, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    private void addBudgetManagementFeatures() {
        JPanel budgetPanel = new JPanel(new GridLayout(0, 4)); // Adjust grid layout for progress bars

        for (String category : ExpenseManager.predefinedCategories) {
            JLabel label = new JLabel("Budget for " + category + ":");
            JTextField budgetField = new JTextField();
            JButton setButton = new JButton("Set");
            JProgressBar progressBar = new JProgressBar(0, 100);
            progressBar.setStringPainted(true);
            progressBars.put(category, progressBar); // Store the progress bar for dynamic updates

            setButton.addActionListener(e -> {
                try {
                    double amount = Double.parseDouble(budgetField.getText());
                    expenseManager.getBudgetManager().setBudget(category, amount);
                    updateProgress(category); // Update progress bar upon setting the budget
                    JOptionPane.showMessageDialog(frame, "Budget set for " + category);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame, "Please enter a valid number.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            budgetPanel.add(label);
            budgetPanel.add(budgetField);
            budgetPanel.add(setButton);
            budgetPanel.add(progressBar); // Add progress bar to the panel
        }

        frame.add(budgetPanel, BorderLayout.SOUTH);
        frame.revalidate();
    }

    public void updateProgress(String category) {
        double totalExpenses = expenseManager.getBudgetManager().calculateTotalExpensesByCategory(category);
        double budget = expenseManager.getBudgetManager().getBudget(category);
        int progressValue = (int) ((totalExpenses / budget) * 100);
        JProgressBar progressBar = progressBars.get(category);
        if (progressBar != null) {
            progressBar.setValue(progressValue);
            progressBar.setString(progressValue + "%");
            if (progressValue >= 80) {
                progressBar.setForeground(Color.RED);
                JOptionPane.showMessageDialog(frame, "Warning: Budget exceeded 80% for " + category, "Budget Alert", JOptionPane.WARNING_MESSAGE);
            } else {
                progressBar.setForeground(Color.GREEN);
            }
        }
    }
}
