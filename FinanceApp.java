import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class FinanceApp {
    private User currentUser;
    private ExpenseManager expenseManager;
    private DataPersistenceManager DataPersistenceManager;
    private JFrame frame;
    private Map<String, JProgressBar> progressBars;
    private DashboardPanel dashboardPanel;
    private ExpenseTrackerPanel expenseTrackerPanel;
    private BudgetManagerPanel budgetManagerPanel;

    public FinanceApp(User user) {
        this.currentUser = user;
        this.expenseManager = new ExpenseManager();
        this.DataPersistenceManager = new DataPersistenceManager();
        this.progressBars = new HashMap<>();
        
        // Load saved data
        try {
            DataPersistenceManager.loadUserData(currentUser, expenseManager);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                "Error loading user data: " + e.getMessage(),
                "Data Load Error",
                JOptionPane.ERROR_MESSAGE);
        }
        
        // Set up auto-save
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                DataPersistenceManager.saveUserData(currentUser, expenseManager);
            } catch (Exception e) {
                System.err.println("Error saving data during shutdown: " + e.getMessage());
            }
        }));
        
        // Set up GUI update callback
        expenseManager.setGuiUpdateCallback(() -> {
            SwingUtilities.invokeLater(() -> {
                updateAllProgressBars();
                if (dashboardPanel != null) {
                    dashboardPanel.updateFinancialSummary();
                }
                // Save data after updates
                try {
                    DataPersistenceManager.saveUserData(currentUser, expenseManager);
                } catch (Exception e) {
                    System.err.println("Error saving data: " + e.getMessage());
                }
            });
        });
        
        createFinanceAppFrame();
    }

    private void createFinanceAppFrame() {
        frame = new JFrame("Personal Finance Assistant - " + currentUser.getUsername());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 800);
        frame.setMinimumSize(new Dimension(1000, 600));

        // Create main tabbed pane
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // Create and add dashboard panel
        dashboardPanel = new DashboardPanel(expenseManager, currentUser);
        tabbedPane.addTab("Dashboard", new ImageIcon(), dashboardPanel, "View your financial overview");
        
        // Create and add expense tracker panel
        expenseTrackerPanel = new ExpenseTrackerPanel(expenseManager);
        tabbedPane.addTab("Expenses", new ImageIcon(), expenseTrackerPanel, "Manage your expenses");
        
        // Create budget panel with progress bars
        JPanel budgetPanel = createBudgetProgressPanel();
        tabbedPane.addTab("Budgets", new ImageIcon(), budgetPanel, "Manage your budgets");
        
        // Add menu bar
        frame.setJMenuBar(createMenuBar());
        
        // Add tabbed pane to frame
        frame.add(tabbedPane);
        
        // Center frame on screen
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        // File menu
        JMenu fileMenu = new JMenu("File");
        
        JMenuItem saveItem = new JMenuItem("Save Data");
        saveItem.addActionListener(e -> saveCurrentData());
        
        JMenuItem backupItem = new JMenuItem("Create Backup");
        backupItem.addActionListener(e -> createDataBackup());
        
        JMenuItem exportItem = new JMenuItem("Export Data");
        exportItem.addActionListener(e -> exportData());
        
        JMenuItem logoutItem = new JMenuItem("Logout");
        logoutItem.addActionListener(e -> logout());
        
        fileMenu.add(saveItem);
        fileMenu.add(backupItem);
        fileMenu.add(exportItem);
        fileMenu.addSeparator();
        fileMenu.add(logoutItem);
        
        // Reports menu
        JMenu reportsMenu = new JMenu("Reports");
        
        JMenuItem monthlyReportItem = new JMenuItem("Monthly Report");
        monthlyReportItem.addActionListener(e -> showReportManagerPanel());
        
        reportsMenu.add(monthlyReportItem);
        
        menuBar.add(fileMenu);
        menuBar.add(reportsMenu);
        
        return menuBar;
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
                    if (amount < 0) {
                        throw new IllegalArgumentException("Budget cannot be negative");
                    }
                    expenseManager.getBudgetManager().setBudget(category, amount);
                    budgetField.setText("");
                    JOptionPane.showMessageDialog(frame, 
                        String.format("Budget set for %s: $%.2f", category, amount),
                        "Budget Updated",
                        JOptionPane.INFORMATION_MESSAGE);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame,
                        "Please enter a valid number",
                        "Invalid Input",
                        JOptionPane.ERROR_MESSAGE);
                } catch (IllegalArgumentException ex) {
                    JOptionPane.showMessageDialog(frame,
                        ex.getMessage(),
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

    private void updateProgress(String category) {
        double totalExpenses = expenseManager.getBudgetManager().calculateTotalExpensesByCategory(category);
        double budget = expenseManager.getBudgetManager().getBudget(category);
        
        if (budget <= 0) {
            return;
        }
        
        int progressValue = (int) ((totalExpenses / budget) * 100);
        JProgressBar progressBar = progressBars.get(category);
        
        if (progressBar != null) {
            progressBar.setValue(progressValue);
            progressBar.setString(String.format("%d%% ($%.2f / $%.2f)", progressValue, totalExpenses, budget));

            if (progressValue >= 90) {
                progressBar.setForeground(new Color(183, 28, 28));
            } else if (progressValue >= 80) {
                progressBar.setForeground(new Color(230, 81, 0));
            } else if (progressValue >= 60) {
                progressBar.setForeground(new Color(255, 152, 0));
            } else {
                progressBar.setForeground(new Color(67, 160, 71));
            }

            // Show warning at 80%
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

    private void updateAllProgressBars() {
        for (String category : ExpenseManager.predefinedCategories) {
            updateProgress(category);
        }
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

    public void saveCurrentData() {
        try {
            DataPersistenceManager.saveUserData(currentUser, expenseManager);
            JOptionPane.showMessageDialog(frame,
                "Data saved successfully!",
                "Save Success",
                JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame,
                "Error saving data: " + e.getMessage(),
                "Save Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    public void createDataBackup() {
        try {
            DataPersistenceManager.backupUserData(currentUser);
            JOptionPane.showMessageDialog(frame,
                "Backup created successfully!",
                "Backup Success",
                JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame,
                "Error creating backup: " + e.getMessage(),
                "Backup Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportData() {
        // Implement export functionality
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Data");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        
        if (fileChooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
            try {
                // Save to selected file
                DataPersistenceManager.saveUserData(currentUser, expenseManager);
                JOptionPane.showMessageDialog(frame,
                    "Data exported successfully!",
                    "Export Success",
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(frame,
                    "Error exporting data: " + e.getMessage(),
                    "Export Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void logout() {
        try {
            DataPersistenceManager.saveUserData(currentUser, expenseManager);
            frame.dispose();
            Main.showMainWindow(); // Updated method call
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame,
                "Error during logout: " + e.getMessage(),
                "Logout Error",
                JOptionPane.ERROR_MESSAGE);
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
