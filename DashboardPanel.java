import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import javax.swing.border.EmptyBorder;
import java.text.NumberFormat;
import java.time.YearMonth;
import java.util.List;
import java.util.Locale;
import java.util.Map;

// Primary Author: Harshit
/**
 * A panel that displays the main dashboard of the Personal Finance Assistant.
 * This includes a summary of financial information and quick access buttons to main features.
 */
public class DashboardPanel extends JPanel {
    private ExpenseManager expenseManager;
    private User currentUser;
    private JPanel totalBudgetLabel;
    private JPanel totalExpensesLabel;
    private JPanel remainingBudgetLabel;
    private JTabbedPane parentTabbedPane;

    /**
     * Constructs a new DashboardPanel with the specified components.
     *
     * @param expenseManager The manager handling expense operations
     * @param currentUser The currently logged-in user
     * @param parentTabbedPane The main application's tabbed pane for navigation
     */
    public DashboardPanel(ExpenseManager expenseManager, User currentUser, JTabbedPane parentTabbedPane) {
        this.expenseManager = expenseManager;
        this.currentUser = currentUser;
        this.parentTabbedPane = parentTabbedPane;
        
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        
        createHeaderPanel();
        createMainContent();
        
        updateFinancialSummary();
    }

    /**
     * Creates the header panel containing welcome message and financial summary cards.
     */
    private void createHeaderPanel() {
        // Create the main header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY),
            new EmptyBorder(10, 0, 10, 0)
        ));

        // Add a welcome label with the username
        JLabel welcomeLabel = new JLabel("Welcome, " + currentUser.getUsername());
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        welcomeLabel.setHorizontalAlignment(SwingConstants.LEFT);
        headerPanel.add(welcomeLabel, BorderLayout.WEST);

        // Create the summary panel for financial information
        JPanel summaryPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        summaryPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        summaryPanel.setBackground(Color.WHITE);

        // Add the financial summary cards
        totalBudgetLabel = createSummaryCardPanel("Total Budget", "0.00");
        totalExpensesLabel = createSummaryCardPanel("Total Expenses", "0.00");
        remainingBudgetLabel = createSummaryCardPanel("Remaining Budget", "0.00");

        summaryPanel.add(totalBudgetLabel);
        summaryPanel.add(totalExpensesLabel);
        summaryPanel.add(remainingBudgetLabel);

        // Add the summary panel to the header panel
        headerPanel.add(summaryPanel, BorderLayout.CENTER);

        // Add the header panel to the main layout
        add(headerPanel, BorderLayout.NORTH);
    }

    /**
     * Creates a financial summary card with the given title and initial value.
     *
     * @param title        The title of the financial summary
     * @param initialValue The initial value to display
     * @return A panel containing the styled financial summary
     */
    private JPanel createSummaryCardPanel(String title, String initialValue) {
        JPanel cardPanel = new JPanel(new BorderLayout());
        cardPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            new EmptyBorder(10, 20, 10, 20)
        ));
        cardPanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        titleLabel.setForeground(new Color(102, 102, 102));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel valueLabel = new JLabel("$" + initialValue);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 20));
        valueLabel.setForeground(Color.BLACK);
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);

        cardPanel.add(titleLabel, BorderLayout.NORTH);
        cardPanel.add(valueLabel, BorderLayout.CENTER);

        return cardPanel;
    }



    /**
     * Creates the main content area containing quick action buttons and recent transactions.
     */
    private void createMainContent() {
        JPanel overviewPanel = new JPanel(new BorderLayout(10, 10));
        overviewPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Quick actions panel with styled buttons
        JPanel quickActionsPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        quickActionsPanel.setBorder(new EmptyBorder(10, 0, 10, 0));

        // Create styled quick action buttons
        JButton expenseButton = createStyledButton("Manage Expenses", new Color(63, 81, 181));
        JButton budgetButton = createStyledButton("Set Budget", new Color(76, 175, 80));
        JButton reportButton = createStyledButton("View Reports", new Color(255, 152, 0));

        // Add action listeners to switch tabs
        expenseButton.addActionListener(e -> parentTabbedPane.setSelectedIndex(1)); // Expenses tab
        budgetButton.addActionListener(e -> parentTabbedPane.setSelectedIndex(2));  // Budget tab
        reportButton.addActionListener(e -> parentTabbedPane.setSelectedIndex(3));  // Reports tab

        quickActionsPanel.add(expenseButton);
        quickActionsPanel.add(budgetButton);
        quickActionsPanel.add(reportButton);

        // Recent transactions panel
        JPanel recentTransactionsPanel = new JPanel(new BorderLayout());
        recentTransactionsPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
            ),
            "Recent Transactions"
        ));
        
        String[] columnNames = {"Date", "Category", "Amount", "Description"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);
        JTable recentTransactionsTable = new JTable(tableModel);
        
        // Add the 5 most recent transactions
        List<Expense> allExpenses = expenseManager.getAllExpenses();
        int startIndex = Math.max(0, allExpenses.size() - 5);
        for (int i = startIndex; i < allExpenses.size(); i++) {
            Expense expense = allExpenses.get(i);
            tableModel.addRow(new Object[]{
                expense.getDate(),
                expense.getCategory(),
                String.format("$%.2f", expense.getAmount()),
                expense.getDescription()
            });
        }

        JScrollPane scrollPane = new JScrollPane(recentTransactionsTable);
        recentTransactionsPanel.add(scrollPane);

        overviewPanel.add(quickActionsPanel, BorderLayout.NORTH);
        overviewPanel.add(recentTransactionsPanel, BorderLayout.CENTER);

        add(overviewPanel, BorderLayout.CENTER);
    }

    /**
     * Creates a styled button with hover effects.
     *
     * @param text The button text
     * @param baseColor The base color of the button
     * @return A styled JButton instance
     */
    private JButton createStyledButton(String text, Color baseColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(baseColor);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(baseColor.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(baseColor);
            }
        });
        
        return button;
    }

    /**
     * Updates the financial summary display with current budget and expense information.
     * This method should be called whenever the underlying data changes.
     */
    /**
     * Updates the financial summary display with current budget and expense information.
     * This method should be called whenever the underlying data changes.
     */
    public void updateFinancialSummary() {
        YearMonth currentMonth = YearMonth.now();

        // Calculate total budget
        Map<String, Double> allBudgets = expenseManager.getBudgetManager().getAllBudgets(currentMonth);
        double totalBudget = 0.0;
        for (Double budget : allBudgets.values()) {
            totalBudget += budget;
        }

        // Calculate total expenses
        List<Expense> allExpenses = expenseManager.getAllExpenses();
        double totalExpenses = 0.0;
        for (Expense expense : allExpenses) {
            if (YearMonth.from(expense.getDate()).equals(currentMonth)) {
                totalExpenses += expense.getAmount();
            }
        }

        // Calculate remaining budget
        double remainingBudget = totalBudget - totalExpenses;

        // Format values as currency
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US);

        // Update summary panels
        updateSummaryCardPanel(totalBudgetLabel, "Total Budget", currencyFormatter.format(totalBudget));
        updateSummaryCardPanel(totalExpensesLabel, "Total Expenses", currencyFormatter.format(totalExpenses));
        updateSummaryCardPanel(remainingBudgetLabel, "Remaining Budget", currencyFormatter.format(remainingBudget));

        revalidate();
        repaint();
    }


    /**
     * Updates the content of a summary card panel with the given title and value.
     *
     * @param panel The JPanel representing the summary card to update.
     * @param title The title for the summary card.
     * @param value The value for the summary card.
     */
    private void updateSummaryCardPanel(JPanel panel, String title, String value) {
        JLabel titleLabel = (JLabel) panel.getComponent(0); // Get the title label
        JLabel valueLabel = (JLabel) panel.getComponent(1); // Get the value label

        titleLabel.setText(title);
        valueLabel.setText(value);
    }


}
