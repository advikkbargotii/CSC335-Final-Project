import javax.swing.*;
import java.awt.*;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Provides a GUI panel for managing budgets per category for each month.
 * This panel allows users to view, set, and update budgets with visual feedback.
 */
public class BudgetManagerPanel extends JPanel {
    private BudgetManager budgetManager;
    private Map<String, JProgressBar> progressBars;
    private Map<String, JTextField> budgetFields;
    private Map<String, Boolean> warningShown;
    private JComboBox<String> monthSelector;
    private YearMonth selectedMonth;
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("MMMM yyyy");

    /**
     * Constructs a panel for budget management interfacing with a BudgetManager.
     * @param budgetManager The BudgetManager to interact with for budget data.
     */
    public BudgetManagerPanel(BudgetManager budgetManager) {
        this.budgetManager = budgetManager;
        this.progressBars = new HashMap<>();
        this.budgetFields = new HashMap<>();
        this.warningShown = new HashMap<>();
        this.selectedMonth = YearMonth.now();
        
        for (String category : ExpenseManager.predefinedCategories) {
            warningShown.put(category, false);
        }
        
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        createMonthSelector();
        initializeBudgetPanel();
    }

    /**
     * Creates a selector for choosing the month for budget operations.
     */
    private void createMonthSelector() {
        JPanel selectorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        monthSelector = new JComboBox<>();
        updateMonthSelector();
        
        monthSelector.addActionListener(e -> {
            String selected = (String) monthSelector.getSelectedItem();
            if (selected != null) {
                selectedMonth = YearMonth.parse(selected, MONTH_FORMATTER);
                updateAllProgressBars();
            }
        });

        JButton refreshButton = createStyledButton("Refresh Months");
        refreshButton.addActionListener(e -> updateMonthSelector());

        selectorPanel.add(new JLabel("Select Month: "));
        selectorPanel.add(monthSelector);
        selectorPanel.add(refreshButton);

        add(selectorPanel, BorderLayout.NORTH);
    }

    /**
     * Updates the month selector combo box with available months from the BudgetManager.
     */
    private void updateMonthSelector() {
        String currentSelection = monthSelector.getSelectedItem() != null ? 
            monthSelector.getSelectedItem().toString() : null;
           
        monthSelector.removeAllItems();
        
        ArrayList<YearMonth> months = budgetManager.getAvailableMonths();
        Collections.sort(months);
        
        for (YearMonth month : months) {
            monthSelector.addItem(month.format(MONTH_FORMATTER));
        }
        
        if (currentSelection != null) {
            monthSelector.setSelectedItem(currentSelection);
        } else {
            monthSelector.setSelectedItem(selectedMonth.format(MONTH_FORMATTER));
        }
    }

    /**
     * Initializes the panel for budget management.
     */
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

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Creates a panel for managing budget of a specific category.
     * @param category The category for which to manage the budget.
     * @return The panel for managing budget for the category.
     */
    private JPanel createCategoryPanel(String category) {
        JPanel categoryPanel = new JPanel(new BorderLayout(10, 5));
        categoryPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 230, 230)),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JLabel categoryLabel = new JLabel(category);
        categoryLabel.setFont(new Font("Arial", Font.BOLD, 16));
        
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

        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setPreferredSize(new Dimension(progressBar.getPreferredSize().width, 25));
        progressBar.setFont(new Font("Arial", Font.PLAIN, 12));
        progressBars.put(category, progressBar);

        JPanel progressInfoPanel = new JPanel(new BorderLayout(5, 0));
        progressInfoPanel.add(progressBar, BorderLayout.CENTER);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(categoryLabel, BorderLayout.WEST);
        topPanel.add(inputPanel, BorderLayout.EAST);

        categoryPanel.add(topPanel, BorderLayout.NORTH);
        categoryPanel.add(progressInfoPanel, BorderLayout.CENTER);

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
                
                budgetManager.setBudget(category, amount, selectedMonth);
                updateProgressBar(category);
                budgetField.setText("");
                
                JOptionPane.showMessageDialog(this,
                    String.format("Budget for %s set to $%.2f for %s", 
                        category, amount, selectedMonth.format(MONTH_FORMATTER)),
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

        updateProgressBar(category);
        return categoryPanel;
    }

    /**
     * Creates a styled button with specific font and color settings.
     * @param text The text to display on the button.
     * @return The styled button.
     */
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

    /**
     * Updates the progress bar for a specific category based on total expenses and budget.
     * @param category The category for which the progress bar should be updated.
     */
    public void updateProgressBar(String category) {
        double totalExpenses = budgetManager.calculateTotalExpensesByCategory(category, selectedMonth);
        double budget = budgetManager.getBudget(category, selectedMonth);
        
        JProgressBar progressBar = progressBars.get(category);
        if (progressBar != null) {
            if (budget <= 0) {
                progressBar.setValue(0);
                progressBar.setString("No budget set");
                progressBar.setForeground(Color.GRAY);
                warningShown.put(category, false);
            } else {
                int percentage = (int) ((totalExpenses / budget) * 100);
                percentage = Math.min(percentage, 100);
                
                progressBar.setValue(percentage);
                progressBar.setString(String.format("%d%% ($%.2f / $%.2f)", 
                    percentage, totalExpenses, budget));

                if (percentage >= 90) {
                    progressBar.setForeground(new Color(183, 28, 28));
                } else if (percentage >= 75) {
                    progressBar.setForeground(new Color(230, 81, 0));
                } else if (percentage >= 50) {
                    progressBar.setForeground(new Color(255, 152, 0));
                } else {
                    progressBar.setForeground(new Color(67, 160, 71));
                }

                checkAndShowWarning(category, percentage, totalExpenses, budget);
            }
        }
    }

    /**
     * Checks if a warning should be shown for budget over-utilization and displays it if necessary.
     * @param category The budget category.
     * @param percentage The utilization percentage.
     * @param totalExpenses Total expenses incurred for the category.
     * @param budget The set budget for the category.
     */
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
            warningShown.put(category, false);
        }
    }

    /**
     * Updates all progress bars based on the current budget and expenses.
     */
    public void updateAllProgressBars() {
        for (String category : ExpenseManager.predefinedCategories) {
            updateProgressBar(category);
        }
    }

    /**
     * Resets the warning flags for all categories.
     */
    public void resetWarningFlags() {
        for (String category : ExpenseManager.predefinedCategories) {
            warningShown.put(category, false);
        }
    }
}
