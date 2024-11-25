import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import javax.swing.border.EmptyBorder;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class DashboardPanel extends JPanel {
    private ExpenseManager expenseManager;
    private User currentUser;
    private JPanel mainContentPanel;
    private CardLayout cardLayout;
    private JLabel totalBudgetLabel;
    private JLabel totalExpensesLabel;
    private JLabel remainingBudgetLabel;

    public DashboardPanel(ExpenseManager expenseManager, User currentUser) {
        this.expenseManager = expenseManager;
        this.currentUser = currentUser;
        
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        
        createNavigationPanel();
        createHeaderPanel();
        createMainContent();
        
        updateFinancialSummary();
    }

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

    private JLabel createSummaryCard(String title, String initialValue) {
        JLabel label = new JLabel(String.format("<html><div style='text-align: center;'>" +
            "<span style='font-size: 14px; color: #666;'>%s</span><br>" +
            "<span style='font-size: 20px; color: #000;'>$%s</span></div></html>", 
            title, initialValue));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            new EmptyBorder(10, 20, 10, 20)
        ));
        label.setOpaque(true);
        label.setBackground(Color.WHITE);
        return label;
    }

    private void createNavigationPanel() {
        JPanel navigationPanel = new JPanel(new GridLayout(6, 1, 0, 5));
        navigationPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        navigationPanel.setPreferredSize(new Dimension(200, 0));
        navigationPanel.setBackground(new Color(245, 245, 245));

        addNavButton(navigationPanel, "Dashboard", "overview", e -> showCard("overview"));
        addNavButton(navigationPanel, "Expenses", "expenses", e -> showCard("expenses"));
        addNavButton(navigationPanel, "Budget", "budget", e -> showCard("budget"));
        addNavButton(navigationPanel, "Reports", "reports", e -> showCard("reports"));
        
        // Add Import/Export and Settings buttons without functionality
        JButton importExportButton = addNavButton(navigationPanel, "Import/Export", "import_export", e -> {
            JOptionPane.showMessageDialog(this, 
                "Import/Export functionality coming soon!", 
                "Feature Not Available", 
                JOptionPane.INFORMATION_MESSAGE);
        });
        
        JButton settingsButton = addNavButton(navigationPanel, "Settings", "settings", e -> {
            JOptionPane.showMessageDialog(this, 
                "Settings functionality coming soon!", 
                "Feature Not Available", 
                JOptionPane.INFORMATION_MESSAGE);
        });

        add(navigationPanel, BorderLayout.WEST);
    }

    private JButton addNavButton(JPanel panel, String text, String command, ActionListener listener) {
        JButton button = new JButton(text);
        button.setActionCommand(command);
        button.addActionListener(listener);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFont(new Font("Arial", Font.PLAIN, 14));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setBackground(new Color(245, 245, 245));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(230, 230, 230));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(245, 245, 245));
            }
        });
        
        panel.add(button);
        return button;
    }

    private void createMainContent() {
        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);

        mainContentPanel.add(createOverviewPanel(), "overview");
        mainContentPanel.add(new ExpenseTrackerPanel(expenseManager), "expenses");
        mainContentPanel.add(new BudgetManagerPanel(expenseManager.getBudgetManager()), "budget");
        mainContentPanel.add(new ReportManagerPanel(new ReportManager(expenseManager)), "reports");

        add(mainContentPanel, BorderLayout.CENTER);
    }

    private JPanel createOverviewPanel() {
        JPanel overviewPanel = new JPanel(new BorderLayout(10, 10));
        overviewPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Recent transactions panel
        JPanel recentTransactionsPanel = new JPanel(new BorderLayout());
        recentTransactionsPanel.setBorder(BorderFactory.createTitledBorder("Recent Transactions"));
        
        String[] columnNames = {"Date", "Category", "Amount", "Description"};
        Object[][] data = new Object[5][4]; // Show last 5 transactions
        JTable recentTransactionsTable = new JTable(data, columnNames);
        JScrollPane scrollPane = new JScrollPane(recentTransactionsTable);
        recentTransactionsPanel.add(scrollPane);

        // Quick actions panel
        JPanel quickActionsPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        quickActionsPanel.setBorder(new EmptyBorder(10, 0, 10, 0));

        addQuickActionButton(quickActionsPanel, "Add Expense", e -> showCard("expenses"));
        addQuickActionButton(quickActionsPanel, "Set Budget", e -> showCard("budget"));
        addQuickActionButton(quickActionsPanel, "View Reports", e -> showCard("reports"));

        overviewPanel.add(quickActionsPanel, BorderLayout.NORTH);
        overviewPanel.add(recentTransactionsPanel, BorderLayout.CENTER);

        return overviewPanel;
    }

    private void addQuickActionButton(JPanel panel, String text, ActionListener listener) {
        JButton button = new JButton(text);
        button.addActionListener(listener);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setFocusPainted(false);
        panel.add(button);
    }

    private void showCard(String cardName) {
        cardLayout.show(mainContentPanel, cardName);
    }

    public void updateFinancialSummary() {
        double totalBudget = expenseManager.getBudgetManager().getAllBudgets().values().stream()
                .mapToDouble(Double::doubleValue)
                .sum();
        double totalExpenses = expenseManager.getAllExpenses().stream()
                .mapToDouble(Expense::getAmount)
                .sum();
        double remainingBudget = totalBudget - totalExpenses;

        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US);

        totalBudgetLabel.setText(createSummaryHTML("Total Budget", currencyFormatter.format(totalBudget)));
        totalExpensesLabel.setText(createSummaryHTML("Total Expenses", currencyFormatter.format(totalExpenses)));
        remainingBudgetLabel.setText(createSummaryHTML("Remaining Budget", currencyFormatter.format(remainingBudget)));
    }

    private String createSummaryHTML(String title, String value) {
        return String.format("<html><div style='text-align: center;'>" +
            "<span style='font-size: 14px; color: #666;'>%s</span><br>" +
            "<span style='font-size: 20px; color: #000;'>%s</span></div></html>", 
            title, value);
    }
}
