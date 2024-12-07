import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import javax.swing.border.EmptyBorder;
import java.text.NumberFormat;
import java.time.YearMonth;
import java.util.List;
import java.util.Locale;

// Primary Author: Harshit
/**
 * A panel that displays the main dashboard of the Personal Finance Assistant.
 * This includes a summary of financial information and quick access buttons to main features.
 */
public class DashboardPanel extends JPanel {
    private ExpenseManager expenseManager;
    private User currentUser;
    private JLabel totalBudgetLabel;
    private JLabel totalExpensesLabel;
    private JLabel remainingBudgetLabel;
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
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY),
            new EmptyBorder(0, 0, 10, 0)
        ));

        JLabel welcomeLabel = new JLabel("Welcome, " + currentUser.getUsername());
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        headerPanel.add(welcomeLabel, BorderLayout.WEST);

        JPanel summaryPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        summaryPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        totalBudgetLabel = createSummaryCard("Total Budget", "0.00");
        totalExpensesLabel = createSummaryCard("Total Expenses", "0.00");
        remainingBudgetLabel = createSummaryCard("Remaining Budget", "0.00");

        summaryPanel.add(totalBudgetLabel);
        summaryPanel.add(totalExpensesLabel);
        summaryPanel.add(remainingBudgetLabel);

        headerPanel.add(summaryPanel, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);
    }

    /**
     * Creates a styled summary card for displaying financial information.
     *
     * @param title The title of the summary card
     * @param initialValue The initial value to display
     * @return A styled JLabel containing the summary information
     */
    private JLabel createSummaryCard(String title, String initialValue) {
        JLabel label = new JLabel(String.format("<html><div style='text-align: center;'>" +
            "<span style='font-size: 14px; color: #666;'>%s</span><br>" +
            "<span style='font-size: 20px; color: #000;'>$%s</span></div></html>", 
            title, initialValue));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            new EmptyBorder(20, 30, 20, 30)
        ));
        label.setOpaque(true);
        label.setBackground(Color.WHITE);
        return label;
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
    public void updateFinancialSummary() {
        YearMonth currentMonth = YearMonth.now();
        double totalBudget = expenseManager.getBudgetManager().getAllBudgets(currentMonth).values().stream()
                .mapToDouble(Double::doubleValue)
                .sum();
        double totalExpenses = expenseManager.getAllExpenses().stream()
                .filter(expense -> YearMonth.from(expense.getDate()).equals(currentMonth))
                .mapToDouble(Expense::getAmount)
                .sum();
        double remainingBudget = totalBudget - totalExpenses;

        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US);

        updateSummaryLabel(totalBudgetLabel, "Total Budget", currencyFormatter.format(totalBudget));
        updateSummaryLabel(totalExpensesLabel, "Total Expenses", currencyFormatter.format(totalExpenses));
        updateSummaryLabel(remainingBudgetLabel, "Remaining Budget", currencyFormatter.format(remainingBudget));

        revalidate();
        repaint();
    }

    /**
     * Updates the content of a summary label with the given title and value.
     *
     * @param label The JLabel to update.
     * @param title The title for the summary.
     * @param value The value for the summary.
     */
    private void updateSummaryLabel(JLabel label, String title, String value) {
        label.setText(title + ": " + value);
        label.setFont(new Font("Arial", Font.BOLD, 22));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                new EmptyBorder(10, 10, 10, 10)
        ));
        label.setOpaque(true);
        label.setBackground(Color.WHITE);
    }

}
