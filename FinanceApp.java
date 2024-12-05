import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.File;
import java.io.IOException;

/**
 * The FinanceApp class serves as the main controller for a personal finance management application.
 * It initializes and manages the user interface and data components, handling user data loading,
 * persistence, and the graphical user interface (GUI) updates. It includes features for tracking expenses,
 * managing budgets, generating reports, and importing/exporting transactions.
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
     * Constructor initializes the application with the given user.
     * @param user The current user of the application.
     */
    public FinanceApp(User user) {
        this.currentUser = user;
        this.expenseManager = new ExpenseManager();
        this.dataPersistenceManager = new DataPersistenceManager();
        
        initializeApplication();
    }

    /**
     * Initializes the application by loading user data, creating the main frame, setting up auto-save, 
     * and updating the GUI.
     */
    private void initializeApplication() {
        loadUserData(); // Load saved data
        createFinanceAppFrame(); // Create the main application frame and components
        setupAutoSave(); // Set up auto-save functionality
        setupUpdateCallback(); // Set up GUI update callback
    }

    /**
     * Loads user data from persistence.
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
     * Sets up the application's auto-save feature using a shutdown hook.
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
     * Sets up a callback to update GUI components upon data changes.
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
     * Creates and configures the main application frame and its components.
     */
    private void createFinanceAppFrame() {
        frame = new JFrame("Personal Finance Assistant - " + currentUser.getUsername());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 800);
        frame.setMinimumSize(new Dimension(1000, 600));
        
        tabbedPane = new JTabbedPane(); // Initialize tabbedPane
        initializePanels(); // Create and initialize all panels
        frame.setJMenuBar(createMenuBar()); // Add menu bar
        frame.add(tabbedPane); // Add tabbed pane to frame
        
        frame.setLocationRelativeTo(null); // Center frame on screen
        frame.setVisible(true); // Display the frame
    }

    /**
     * Initializes panels within the application.
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
     * Loads an icon from a given file name.
     * @param filename The name of the icon file.
     * @return ImageIcon that has been loaded or an empty icon in case of failure.
     */
    private ImageIcon loadIcon(String filename) {
        try {
            return new ImageIcon(filename);
        } catch (Exception e) {
            System.err.println("Could not load icon: " + filename);
            return new ImageIcon(); // Return empty icon if loading fails
        }
    }

    /**
     * Creates the application's menu bar with file and import/export menus.
     * @return JMenuBar the fully constructed menu bar.
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
     * Adds items to the file menu.
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
     * Adds items to the import/export menu.
     * @param importExportMenu The menu to which items are added.
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
     * Updates all GUI components.
     */
    private void updateAllComponents() {
        if (dashboardPanel != null) {
            dashboardPanel.updateFinancialSummary();
        }
        if (budgetManagerPanel != null) {
            budgetManagerPanel.updateAllProgressBars();
        }
    }

    /**
     * Saves the current user data.
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
     * Exports transactions to a file selected by the user.
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
     * Handles user logout, saves data, and closes the application.
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
     * Opens the settings panel for user settings modification.
     */
    private void openSettings() {
        
        SettingsPanel settingsPanel = new SettingsPanel(currentUser, new SettingsPanel.Callback() {
            public void onSaveChanges(String oldPassword, String newPassword) {
                try {
                    if (!newPassword.isEmpty() && !oldPassword.isEmpty()) {
                        if (currentUser.checkPassword(oldPassword)) {
                            String oldUserName = currentUser.getUsername();
                            dataPersistenceManager.deleteUserPassword(currentUser);
                            currentUser = new User(oldUserName, newPassword);
                            if (Files.exists(Paths.get("data/users.txt"))) {
                                List<String> lines = new ArrayList<>();
                                lines = Files.readAllLines(Paths.get("data/users.txt"));
                                lines.add(currentUser.getUsername() + ":" + currentUser.getPasswordHash() + ":" + currentUser.getSalt());
                                Files.write(Paths.get("data/users.txt"), lines);
                                System.out.println("Saved user data for: " + currentUser.getUsername());
                            }
                        } else {
                            JOptionPane.showMessageDialog(null, "The old password is incorrect. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "Both the old password and new password fields must be filled out.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    
                    saveCurrentData();
                    JOptionPane.showMessageDialog(frame, "Settings updated successfully.");
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(frame, 
                        "Error updating settings: " + e.getMessage(), 
                        "Update Error", 
                        JOptionPane.ERROR_MESSAGE);
                }
                switchToMainPanel();
            }

            @Override
            public void onCancel() {
                switchToMainPanel();
            }

            @Override
            public void onDeleteAccount() {
                dataPersistenceManager.deleteUserPassword(currentUser);
                dataPersistenceManager.deleteUserData(currentUser);
                logout();
            }
        });

        frame.getContentPane().removeAll();
        frame.getContentPane().add(settingsPanel);
        frame.revalidate();
        frame.repaint();
    }

    /**
     * Switches the main frame content back to the main panel after settings or account modifications.
     */
    private void switchToMainPanel() {
        frame.getContentPane().removeAll();
        tabbedPane = new JTabbedPane();
        initializePanels();
        frame.getContentPane().add(tabbedPane);
        frame.revalidate();
        frame.repaint();
    }
}
