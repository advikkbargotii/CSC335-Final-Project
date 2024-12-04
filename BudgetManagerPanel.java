import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class BudgetManagerPanel extends JPanel {
    private BudgetManager budgetManager;
    private Map<String, JProgressBar> progressBars;
    private Map<String, JTextField> budgetFields;
    private Map<String, Boolean> warningShown;

    public BudgetManagerPanel(BudgetManager budgetManager) {
        this.budgetManager = budgetManager;
        this.progressBars = new HashMap<>();
        this.budgetFields = new HashMap<>();
        this.warningShown = new HashMap<>();
        
        // Initialize warning flags for all categories
        for (String category : ExpenseManager.predefinedCategories) {
            warningShown.put(category, false);
        }
        
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        initializeBudgetPanel();
    }

    private void initializeBudgetPanel() {
        JPanel mainPanel = new JPanel(new GridLayout(0, 1, 0, 20));
        mainPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
            ),
            "Budget Management"
        ));

        for (String category : ExpenseManager.predefinedCategories) {
            mainPanel.add(createCategoryPanel(category));
        }

        // Add mainPanel to a ScrollPane
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createCategoryPanel(String category) {
        JPanel categoryPanel = new JPanel(new BorderLayout(10, 5));
        categoryPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 230, 230)),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // Category label
        JLabel categoryLabel = new JLabel(category);
        categoryLabel.setFont(new Font("Arial", Font.BOLD, 16));
        
        // Input panel (budget field and set button)
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        
        JLabel dollarSign = new JLabel("$");
        dollarSign.setFont(new Font("Arial", Font.PLAIN, 14));
        
        JTextField budgetField = new JTextField(10);
        budgetField.setFont(new Font("Arial", Font.PLAIN, 14));
        budgetFields.put(category, budgetField);
        
        JButton setButton = createStyledButton("Set Budget");
        
        inputPanel.add(dollarSign);
        inputPanel.add(budgetField);
        inputPanel.add(setButton);

        // Progress bar
        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setPreferredSize(new Dimension(progressBar.getPreferredSize().width, 25));
        progressBar.setFont(new Font("Arial", Font.PLAIN, 12));
        progressBars.put(category, progressBar);

        // Progress info panel
        JPanel progressInfoPanel = new JPanel(new BorderLayout(5, 0));
        progressInfoPanel.add(progressBar, BorderLayout.CENTER);

        // Add components to category panel
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(categoryLabel, BorderLayout.WEST);
        topPanel.add(inputPanel, BorderLayout.EAST);

        categoryPanel.add(topPanel, BorderLayout.NORTH);
        categoryPanel.add(progressInfoPanel, BorderLayout.CENTER);

        // Set button action
        setButton.addActionListener(e -> {
            try {
                String input = budgetField.getText().trim();
                if (input.isEmpty()) {
                    throw new IllegalArgumentException("Please enter a budget amount");
                }
                
                double amount = Double.parseDouble(input);
                if (amount < 0) {
                    throw new IllegalArgumentException("Budget cannot be negative");
                }
                
                budgetManager.setBudget(category, amount);
                updateProgressBar(category);
                budgetField.setText("");
                
                JOptionPane.showMessageDialog(this,
                    String.format("Budget for %s set to $%.2f", category, amount),
                    "Budget Updated",
                    JOptionPane.INFORMATION_MESSAGE);
                    
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                    "Please enter a valid number",
                    "Invalid Input",
                    JOptionPane.ERROR_MESSAGE);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    "Invalid Input",
                    JOptionPane.ERROR_MESSAGE);
            }
        });

        // Initialize progress bar
        updateProgressBar(category);

        return categoryPanel;
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setBackground(new Color(63, 81, 181));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
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

    public void updateProgressBar(String category) {
        double totalExpenses = budgetManager.calculateTotalExpensesByCategory(category);
        double budget = budgetManager.getBudget(category);
        
        JProgressBar progressBar = progressBars.get(category);
        if (progressBar != null) {
            if (budget <= 0) {
                progressBar.setValue(0);
                progressBar.setString("No budget set");
                progressBar.setForeground(Color.GRAY);
                warningShown.put(category, false); // Reset warning flag when budget is 0
            } else {
                int percentage = (int) ((totalExpenses / budget) * 100);
                percentage = Math.min(percentage, 100); // Cap at 100%
                
                progressBar.setValue(percentage);
                progressBar.setString(String.format("%d%% ($%.2f / $%.2f)", 
                    percentage, totalExpenses, budget));

                // Color coding based on usage
                if (percentage >= 90) {
                    progressBar.setForeground(new Color(183, 28, 28)); // Dark Red
                } else if (percentage >= 75) {
                    progressBar.setForeground(new Color(230, 81, 0)); // Dark Orange
                } else if (percentage >= 50) {
                    progressBar.setForeground(new Color(255, 152, 0)); // Orange
                } else {
                    progressBar.setForeground(new Color(67, 160, 71)); // Green
                }

                // Check for threshold and show warning if needed
                checkAndShowWarning(category, percentage, totalExpenses, budget);
            }
        }
    }

    private void checkAndShowWarning(String category, int percentage, double totalExpenses, double budget) {
        if (percentage >= 80 && !warningShown.get(category)) {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(
                    this,
                    String.format("Warning: Budget utilization for %s has reached %d%%\n" +
                        "Current Expenses: $%.2f\n" +
                        "Budget Limit: $%.2f\n" +
                        "Remaining Budget: $%.2f",
                        category, percentage, totalExpenses, budget, (budget - totalExpenses)),
                    "Budget Warning",
                    JOptionPane.WARNING_MESSAGE
                );
                warningShown.put(category, true);
            });
        } else if (percentage < 80) {
            // Reset warning flag when utilization drops below threshold
            warningShown.put(category, false);
        }
    }

    public void updateAllProgressBars() {
        for (String category : ExpenseManager.predefinedCategories) {
            updateProgressBar(category);
        }
    }

    public void resetWarningFlags() {
        for (String category : ExpenseManager.predefinedCategories) {
            warningShown.put(category, false);
        }
    }
}
