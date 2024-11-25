import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class FinanceApp {
    private User currentUser;
    private ExpenseManager expenseManager;
    private JFrame frame;
    private Map<String, JProgressBar> progressBars;
    private DashboardPanel dashboardPanel;

    public FinanceApp(User user) {
        this.currentUser = user;
        this.expenseManager = new ExpenseManager();
        this.progressBars = new HashMap<>();
        
        // Set up callback to update both progress bars and dashboard
        expenseManager.setGuiUpdateCallback(() -> {
            SwingUtilities.invokeLater(() -> {
                updateAllProgressBars();
                if (dashboardPanel != null) {
                    dashboardPanel.updateFinancialSummary();
                }
            });
        });
        
        createFinanceAppFrame();
    }

    private JPanel createBudgetProgressPanel() {
        JPanel budgetPanel = new JPanel(new GridLayout(0, 4, 10, 10));
        budgetPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Budget Progress"),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        for (String category : ExpenseManager.predefinedCategories) {
            JPanel categoryPanel = new JPanel(new BorderLayout(5, 5));
            categoryPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            
            JLabel label = new JLabel(category);
            label.setFont(new Font("Arial", Font.BOLD, 12));
            
            JPanel inputPanel = new JPanel(new BorderLayout(5, 0));
            JTextField budgetField = new JTextField(8);
            JButton setButton = createStyledButton("Set");
            
            inputPanel.add(budgetField, BorderLayout.CENTER);
            inputPanel.add(setButton, BorderLayout.EAST);
            
            JProgressBar progressBar = new JProgressBar(0, 100);
            progressBar.setStringPainted(true);
            progressBar.setPreferredSize(new Dimension(progressBar.getPreferredSize().width, 20));
            progressBars.put(category, progressBar);

            setButton.addActionListener(e -> {
                try {
                    double amount = Double.parseDouble(budgetField.getText());
                    expenseManager.getBudgetManager().setBudget(category, amount);
                    // Trigger the update callback
                    if (expenseManager.getGuiUpdateCallback() != null) {
                        expenseManager.getGuiUpdateCallback().run();
                    }
                    JOptionPane.showMessageDialog(frame, 
                        String.format("Budget set for %s: $%.2f", category, amount),
                        "Budget Updated",
                        JOptionPane.INFORMATION_MESSAGE);
                    budgetField.setText("");
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame,
                        "Please enter a valid number.",
                        "Invalid Input",
                        JOptionPane.ERROR_MESSAGE);
                }
            });

            categoryPanel.add(label, BorderLayout.NORTH);
            categoryPanel.add(inputPanel, BorderLayout.CENTER);
            categoryPanel.add(progressBar, BorderLayout.SOUTH);
            
            budgetPanel.add(categoryPanel);
        }

        return budgetPanel;
    }

    private void createFinanceAppFrame() {
        frame = new JFrame("Personal Finance Assistant");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 800);
        frame.setMinimumSize(new Dimension(1000, 600));

        // Create main dashboard panel
        dashboardPanel = new DashboardPanel(expenseManager, currentUser);
        
        // Create budget panel with progress bars
        JPanel budgetProgressPanel = createBudgetProgressPanel();
        
        // Add budget progress panel to the bottom of the dashboard
        dashboardPanel.add(budgetProgressPanel, BorderLayout.SOUTH);
        
        frame.add(dashboardPanel);
        
        // Center the frame on screen
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBackground(new Color(63, 81, 181));
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(48, 63, 159));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(63, 81, 181));
            }
        });
        
        return button;
    }

    private void updateAllProgressBars() {
        for (String category : ExpenseManager.predefinedCategories) {
            updateProgress(category);
        }
    }

    public void updateProgress(String category) {
        double totalExpenses = expenseManager.getBudgetManager().calculateTotalExpensesByCategory(category);
        double budget = expenseManager.getBudgetManager().getBudget(category);
        
        if (budget <= 0) {
            return; // Skip update if no budget is set
        }
        
        int progressValue = (int) ((totalExpenses / budget) * 100);
        JProgressBar progressBar = progressBars.get(category);
        
        if (progressBar != null) {
            progressBar.setValue(progressValue);
            progressBar.setString(String.format("%d%% ($%.2f / $%.2f)", progressValue, totalExpenses, budget));

            // Update color based on progress
            if (progressValue >= 90) {
                progressBar.setForeground(new Color(183, 28, 28)); // Dark Red
            } else if (progressValue >= 80) {
                progressBar.setForeground(new Color(230, 81, 0)); // Orange Red
            } else if (progressValue >= 60) {
                progressBar.setForeground(new Color(255, 152, 0)); // Orange
            } else {
                progressBar.setForeground(new Color(67, 160, 71)); // Green
            }

            // Show warning if threshold reached
            if (progressValue >= 80 && progressValue < 90) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(frame,
                        String.format("Warning: Budget for %s has reached %d%%\nExpenses: $%.2f\nBudget: $%.2f",
                            category, progressValue, totalExpenses, budget),
                        "Budget Alert",
                        JOptionPane.WARNING_MESSAGE);
                });
            }
        }
    }

    public void showReportManagerPanel() {
        JDialog reportDialog = new JDialog(frame, "Monthly Report", true);
        reportDialog.setSize(900, 700);
        reportDialog.setLocationRelativeTo(frame);

        ReportManager reportManager = new ReportManager(expenseManager);
        ReportManagerPanel reportManagerPanel = new ReportManagerPanel(reportManager);
        reportDialog.add(reportManagerPanel);

        reportDialog.setVisible(true);
    }
}
