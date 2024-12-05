import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
Description: The ExpenseTrackerPanel class represents the GUI panel for managing expenses.
			It includes components for viewing, adding, deleting, and filtering expenses,
			as well as integrating with the ExpenseManager and BudgetManager.
*/

public class ExpenseTrackerPanel extends JPanel {
    private ExpenseManager expenseManager; // Manages expense data
    private DefaultTableModel tableModel; // Model for the expense table
    private JTable expenseTable; // Table for displaying expenses
    private BudgetManagerPanel budgetManagerPanel; // Panel for budget management 

    /**
    Constructs an ExpenseTrackerPanel with the specified ExpenseManager.
    Initializes and lays out the GUI components for managing expenses.
    @param expenseManager The ExpenseManager to manage and track expenses.
    */
    public ExpenseTrackerPanel(ExpenseManager expenseManager) {
        this.expenseManager = expenseManager;
        setLayout(new BorderLayout());

        // Table for displaying expenses
        tableModel = new DefaultTableModel(new String[]{"Date", "Category", "Amount", "Description"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; 
            }
        };
        expenseTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(expenseTable);
        
        // Form for adding expenses
        JPanel formPanel = new JPanel(new GridLayout(6, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField dateField = new JTextField();
        JComboBox<String> categoryBox = new JComboBox<>(expenseManager.getPredefinedCategories().toArray(new String[0]));
        JTextField amountField = new JTextField();
        JTextField descriptionField = new JTextField();

        JButton addButton = createStyledButton("Add Expense", new Color(76, 175, 80));
        JButton deleteButton = createStyledButton("Delete Selected", new Color(211, 47, 47));

        formPanel.add(new JLabel("Date (YYYY-MM-DD):"));
        formPanel.add(dateField);
        formPanel.add(new JLabel("Category:"));
        formPanel.add(categoryBox);
        formPanel.add(new JLabel("Amount:"));
        formPanel.add(amountField);
        formPanel.add(new JLabel("Description:"));
        formPanel.add(descriptionField);
        formPanel.add(new JLabel());
        formPanel.add(addButton);
        formPanel.add(new JLabel());
        formPanel.add(deleteButton);

        // Filter panel for filtering expenses
        JPanel filterPanel = new JPanel(new GridBagLayout());
        filterPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        JTextField startDateField = new JTextField(10);
        JTextField endDateField = new JTextField(10);
        JList<String> categoryList = new JList<>(expenseManager.getPredefinedCategories().toArray(new String[0]));
        categoryList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane categoryScrollPane = new JScrollPane(categoryList);
        categoryScrollPane.setPreferredSize(new Dimension(200, 100));

        JButton filterButton = createStyledButton("Filter", new Color(63, 81, 181));
        JButton resetButton = createStyledButton("Reset", new Color(158, 158, 158));
        JButton refreshButton = createStyledButton("Refresh", new Color(0, 150, 136));

        // Layout setup for filter panel
        gbc.gridx = 0; gbc.gridy = 0;
        filterPanel.add(new JLabel("Start Date (YYYY-MM-DD):"), gbc);

        gbc.gridx = 1;
        filterPanel.add(startDateField, gbc);

        gbc.gridx = 2;
        filterPanel.add(new JLabel("End Date (YYYY-MM-DD):"), gbc);

        gbc.gridx = 3;
        filterPanel.add(endDateField, gbc);

        gbc.gridx = 4;
        filterPanel.add(refreshButton, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        gbc.gridwidth = 4;
        filterPanel.add(new JLabel("Categories:"), gbc);

        gbc.gridy = 2;
        filterPanel.add(categoryScrollPane, gbc);

        gbc.gridy = 3;
        gbc.gridwidth = 2;
        filterPanel.add(filterButton, gbc);

        gbc.gridx = 2;
        filterPanel.add(resetButton, gbc);

        
        add(filterPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(formPanel, BorderLayout.SOUTH);

        addButton.addActionListener(e -> {
            try {
                LocalDate date = LocalDate.parse(dateField.getText());
                String category = (String) categoryBox.getSelectedItem();
                double amount = Double.parseDouble(amountField.getText());
                String description = descriptionField.getText();

                if (amount <= 0) {
                    throw new IllegalArgumentException("Amount must be positive");
                }

                Expense expense = new Expense(date, category, amount, description);
                expenseManager.addExpense(expense);

                refreshExpenseTable(); // Refresh's the table after adding

                // Clear input fields
                dateField.setText("");
                categoryBox.setSelectedIndex(0);
                amountField.setText("");
                descriptionField.setText("");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, 
                    "Invalid amount format", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, 
                    "Invalid input: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        deleteButton.addActionListener(e -> {
            int selectedRow = expenseTable.getSelectedRow();
            if (selectedRow != -1) {
                expenseManager.deleteExpense(selectedRow);
                refreshExpenseTable(); // Refresh the table after deleting
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Please select an expense to delete", "Error", JOptionPane.WARNING_MESSAGE);
            }
        });

        filterButton.addActionListener(e -> {
            try {
                LocalDate startDate = startDateField.getText().isEmpty() ? 
                    null : LocalDate.parse(startDateField.getText());
                LocalDate endDate = endDateField.getText().isEmpty() ? 
                    null : LocalDate.parse(endDateField.getText());
                List<String> selectedCategories = categoryList.getSelectedValuesList();

                List<Expense> filteredExpenses = expenseManager.getAllExpenses();

                if (startDate != null && endDate != null) {
                    filteredExpenses = expenseManager.filterByDateRange(startDate, endDate);
                }

                if (!selectedCategories.isEmpty()) {
                    filteredExpenses = filteredExpenses.stream()
                            .filter(expense -> selectedCategories.contains(expense.getCategory()))
                            .collect(Collectors.toList());
                }

                updateTableWithExpenses(filteredExpenses);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, 
                    "Invalid filter inputs: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        resetButton.addActionListener(e -> {
            startDateField.setText("");
            endDateField.setText("");
            categoryList.clearSelection();
            refreshExpenseTable();
        });

        refreshButton.addActionListener(e -> refreshExpenseTable());

        refreshExpenseTable();
    }

    /**
    Refreshes the expense table with all expenses.
    */
    private void refreshExpenseTable() {
        updateTableWithExpenses(expenseManager.getAllExpenses());
    }

    /**
    Updates the table model with the given list of expenses.
    @param expenses The list of expenses to display in the table.
    */
    private void updateTableWithExpenses(List<Expense> expenses) {
        tableModel.setRowCount(0); // Clear existing rows
        for (Expense expense : expenses) {
            tableModel.addRow(new Object[]{
                expense.getDate(),
                expense.getCategory(),
                String.format("$%.2f", expense.getAmount()),
                expense.getDescription()
            });
        }
    }

    /**
    Creates a styled JButton with the specified text and color.
    @param text The text of the button.
    @param baseColor The base color of the button.
    @return The styled JButton.
    */
    private JButton createStyledButton(String text, Color baseColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(baseColor);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        
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
}
