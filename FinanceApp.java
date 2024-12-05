import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * The main class for the Finance Application, managing the user interface and interactions
 * with the ExpenseManager and DataPersistenceManager for data operations.
 */
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

    /**
     * Constructs a FinanceApp instance associated with a specific user.
     * @param user The user for whom the finance application is initialized.
     */
    public FinanceApp(User user) {
        this.currentUser = user;
        this.expenseManager = new ExpenseManager();
        this.dataPersistenceManager = new DataPersistenceManager();
        
        initializeApplication();
    }

    /**
     * Initializes the application by setting up the main frame, loading user data,
     * and establishing necessary background operations like auto-save.
     */
    private void initializeApplication() {
        loadUserData();
        createFinanceAppFrame();
        setupAutoSave();
        setupUpdateCallback();
    }

    /**
     * Loads user data from a persistent storage managed by DataPersistenceManager.
     */
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

    /**
     * Sets up the auto-save functionality using a shutdown hook to save data when the application closes.
     */
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

    /**
     * Sets up a callback for GUI updates when the expense data changes.
     */
    private void setupUpdateCallback() {
        expenseManager.setGuiUpdateCallback(() -> {
            SwingUtilities.invokeLater(() -> {
                updateAllComponents();
                saveCurrentData();
            });
        });
    }

    /**
     * Creates the main application frame and configures basic window settings.
     */
    private void createFinanceAppFrame() {
        frame = new JFrame("Personal Finance Assistant - " + currentUser.getUsername());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 800);
        frame.setMinimumSize(new Dimension(1000, 600));

        tabbedPane = new JTabbedPane();
        initializePanels();
        frame.setJMenuBar(createMenuBar());
        frame.add(tabbedPane);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    /**
     * Initializes panels for each major functionality in the app such as dashboard, expense tracking, etc.
     */
    private void initializePanels() {
        dashboardPanel = new DashboardPanel(expenseManager, currentUser, tabbedPane);
        expenseTrackerPanel = new ExpenseTrackerPanel(expenseManager);
        budgetManagerPanel = new BudgetManagerPanel(expenseManager.getBudgetManager());
        reportManagerPanel = new ReportManagerPanel(new ReportManager(expenseManager));

        tabbedPane.addTab("Dashboard", loadIcon("dashboard.png"), dashboardPanel, "View your financial overview");
        tabbedPane.addTab("Expenses", loadIcon("expense.png"), expenseTrackerPanel, "Manage your expenses");
        tabbedPane.addTab("Budget", loadIcon("budget.jpeg"), budgetManagerPanel, "Manage your budgets");
        tabbedPane.addTab("Reports", loadIcon("report.png"), reportManagerPanel, "View financial reports");
    }

    /**
     * Loads an icon for a given filename.
     * @param filename The file name of the icon.
     * @return ImageIcon if successful, or an empty icon if the file is not found.
     */
    private ImageIcon loadIcon(String filename) {
        try {
            return new ImageIcon(filename);
        } catch (Exception e) {
            System.err.println("Could not load icon: " + filename);
            return new ImageIcon();
        }
    }

    /**
     * Creates the menu bar with file and import/export options.
     * @return JMenuBar The configured menu bar for the application.
     */
    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        addMenuItems(fileMenu);
        JMenu importExportMenu = new JMenu("Import/Export");
        addImportExportItems(importExportMenu);
        menuBar.add(fileMenu);
        menuBar.add(importExportMenu);
        return menuBar;
    }

    /**
     * Adds menu items to the file menu.
     * @param fileMenu The file menu to which items are added.
     */
    private void addMenuItems(JMenu fileMenu) {
        JMenuItem saveItem = new JMenuItem("Save Data");
        saveItem.addActionListener(e -> saveCurrentData());
        JMenuItem backupItem = new JMenuItem("Create Backup");
        backupItem.addActionListener(e -> createDataBackup());
        JMenuItem settingsItem = new JMenuItem("User Settings");
        settingsItem.addActionListener(e -> openSettings());
        JMenuItem logoutItem = new JMenuItem("Logout");
        logoutItem.addActionListener(e -> logout());
        fileMenu.add(saveItem);
        fileMenu.add(backupItem);
        fileMenu.addSeparator();
        fileMenu.add(settingsItem);
        fileMenu.add(logoutItem);
    }

    /**
     * Adds import and export options to the specified menu.
     * @param importExportMenu The menu to which import/export items are added.
     */
    private void addImportExportItems(JMenu importExportMenu) {
        JMenuItem importItem = new JMenuItem("Import Transactions");
        importItem.addActionListener(e -> importTransactions());
        JMenuItem exportItem = new JMenuItem("Export Transactions");
        exportItem.addActionListener(e -> exportTransactions());
        importExportMenu.add(importItem);
        importExportMenu.add(exportItem);
    }

    /**
     * Updates all components of the application related to financial data display.
     */
    private void updateAllComponents() {
        dashboardPanel.updateFinancialSummary();
        budgetManagerPanel.updateAllProgressBars();
    }

    /**
     * Saves the current data using the DataPersistenceManager.
     */
    private void saveCurrentData() {
        try {
            dataPersistenceManager.saveUserData(currentUser, expenseManager);
        } catch (Exception e) {
            System.err.println("Error saving data: " + e.getMessage());
        }
    }

    /**
     * Creates a backup of the user data.
     */
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

    /**
     * Imports transactions from a file selected by the user.
     */
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

    /**
     * Exports transactions to a file chosen by the user.
     */
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

    /**
     * Logs out the current user, saves data, and disposes of the application frame.
     */
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
    
    /**
     * Opens the user settings interface for managing user preferences and information.
     */
    private void openSettings() {
        // Implementation details would be added here for handling user settings.
    }
}
