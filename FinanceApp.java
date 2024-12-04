import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.io.File;
import java.io.IOException;

public class FinanceApp {
    private User currentUser;
    private ExpenseManager expenseManager;
    private DataPersistenceManager dataPersistenceManager;
    private JFrame frame;
    private JTabbedPane tabbedPane;
    private DashboardPanel dashboardPanel;
    private ExpenseTrackerPanel expenseTrackerPanel;
    private BudgetManagerPanel budgetManagerPanel;
    private ReportManagerPanel reportManagerPanel;

    public FinanceApp(User user) {
        this.currentUser = user;
        this.expenseManager = new ExpenseManager();
        this.dataPersistenceManager = new DataPersistenceManager();
        
        initializeApplication();
    }

    private void initializeApplication() {
        // Load saved data
        loadUserData();
        
        // Create the main application frame and components
        createFinanceAppFrame();
        
        // Set up auto-save functionality
        setupAutoSave();
        
        // Set up GUI update callback
        setupUpdateCallback();
    }

    private void loadUserData() {
        try {
            dataPersistenceManager.loadUserData(currentUser, expenseManager);
            System.out.println("Successfully loaded user data for: " + currentUser.getUsername());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                "Error loading user data: " + e.getMessage(),
                "Data Load Error",
                JOptionPane.ERROR_MESSAGE);
            System.err.println("Error loading user data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupAutoSave() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                dataPersistenceManager.saveUserData(currentUser, expenseManager);
                System.out.println("Auto-save completed successfully");
            } catch (Exception e) {
                System.err.println("Error during auto-save: " + e.getMessage());
                e.printStackTrace();
            }
        }));
    }

    private void setupUpdateCallback() {
        expenseManager.setGuiUpdateCallback(() -> {
            SwingUtilities.invokeLater(() -> {
                updateAllComponents();
                saveCurrentData();
            });
        });
    }

    private void createFinanceAppFrame() {
        frame = new JFrame("Personal Finance Assistant - " + currentUser.getUsername());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 800);
        frame.setMinimumSize(new Dimension(1000, 600));

        // Initialize tabbedPane
        tabbedPane = new JTabbedPane();
        
        // Create and initialize all panels
        initializePanels();
        
        // Add menu bar
        frame.setJMenuBar(createMenuBar());
        
        // Add tabbed pane to frame
        frame.add(tabbedPane);
        
        // Center frame on screen and display
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void initializePanels() {
        // Create all panels
        dashboardPanel = new DashboardPanel(expenseManager, currentUser, tabbedPane);
        expenseTrackerPanel = new ExpenseTrackerPanel(expenseManager);
        budgetManagerPanel = new BudgetManagerPanel(expenseManager.getBudgetManager());
        reportManagerPanel = new ReportManagerPanel(new ReportManager(expenseManager));

        // Add panels to tabbed pane with icons
        tabbedPane.addTab("Dashboard", loadIcon("dashboard.png"), dashboardPanel, "View your financial overview");
        tabbedPane.addTab("Expenses", loadIcon("expense.png"), expenseTrackerPanel, "Manage your expenses");
        tabbedPane.addTab("Budget", loadIcon("budget.jpeg"), budgetManagerPanel, "Manage your budgets");
        tabbedPane.addTab("Reports", loadIcon("report.png"), reportManagerPanel, "View financial reports");
    }

    private ImageIcon loadIcon(String filename) {
        try {
            ImageIcon icon = new ImageIcon(filename);
            return icon;
        } catch (Exception e) {
            System.err.println("Could not load icon: " + filename);
            return new ImageIcon(); // Return empty icon if loading fails
        }
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        // File menu
        JMenu fileMenu = new JMenu("File");
        addMenuItems(fileMenu);
        
        // Import/Export menu
        JMenu importExportMenu = new JMenu("Import/Export");
        addImportExportItems(importExportMenu);
        
        menuBar.add(fileMenu);
        menuBar.add(importExportMenu);
        
        return menuBar;
    }

    private void addMenuItems(JMenu fileMenu) {
        JMenuItem saveItem = new JMenuItem("Save Data");
        saveItem.addActionListener(e -> saveCurrentData());
        
        JMenuItem backupItem = new JMenuItem("Create Backup");
        backupItem.addActionListener(e -> createDataBackup());
        
        JMenuItem logoutItem = new JMenuItem("Logout");
        logoutItem.addActionListener(e -> logout());
        
        fileMenu.add(saveItem);
        fileMenu.add(backupItem);
        fileMenu.addSeparator();
        fileMenu.add(logoutItem);
    }

    private void addImportExportItems(JMenu importExportMenu) {
        JMenuItem importItem = new JMenuItem("Import Transactions");
        importItem.addActionListener(e -> importTransactions());
        
        JMenuItem exportItem = new JMenuItem("Export Transactions");
        exportItem.addActionListener(e -> exportTransactions());
        
        importExportMenu.add(importItem);
        importExportMenu.add(exportItem);
    }

    private void updateAllComponents() {
        if (dashboardPanel != null) {
            dashboardPanel.updateFinancialSummary();
        }
        if (budgetManagerPanel != null) {
            budgetManagerPanel.updateAllProgressBars();
        }
    }

    private void saveCurrentData() {
        try {
            dataPersistenceManager.saveUserData(currentUser, expenseManager);
        } catch (Exception e) {
            System.err.println("Error saving data: " + e.getMessage());
        }
    }

    private void createDataBackup() {
        try {
            dataPersistenceManager.backupUserData(currentUser);
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

    private void importTransactions() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Import Transactions");
        
        if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
            try {
                TransactionFileHandler fileHandler = new TransactionFileHandler(expenseManager);
                fileHandler.importTransactions(fileChooser.getSelectedFile());
                JOptionPane.showMessageDialog(frame,
                    "Transactions imported successfully!",
                    "Import Success",
                    JOptionPane.INFORMATION_MESSAGE);
                updateAllComponents();
            } catch (TransactionFileHandler.TransactionImportException e) {
                JOptionPane.showMessageDialog(frame,
                    e.getMessage(),
                    e.isPartialSuccess() ? "Import Completed with Warnings" : "Import Failed",
                    e.isPartialSuccess() ? JOptionPane.WARNING_MESSAGE : JOptionPane.ERROR_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(frame,
                    "Error reading file: " + e.getMessage(),
                    "Import Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportTransactions() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Transactions");
        fileChooser.setSelectedFile(new File("transactions.txt"));
        
        if (fileChooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
            try {
                TransactionFileHandler fileHandler = new TransactionFileHandler(expenseManager);
                fileHandler.exportTransactions(fileChooser.getSelectedFile());
                JOptionPane.showMessageDialog(frame,
                    "Transactions exported successfully!",
                    "Export Success",
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(frame,
                    "Error writing file: " + e.getMessage(),
                    "Export Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void logout() {
        try {
            saveCurrentData();
            frame.dispose();
            Main.showMainWindow();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame,
                "Error during logout: " + e.getMessage(),
                "Logout Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
}
