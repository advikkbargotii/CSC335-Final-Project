import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class ExpenseTrackerPanel extends JPanel {
    private ExpenseManager expenseManager;
    private DefaultTableModel tableModel;
    private JTable expenseTable;

    public ExpenseTrackerPanel(ExpenseManager expenseManager) {
        this.expenseManager = expenseManager;
        setLayout(new BorderLayout());

        // Table for displaying expenses
        tableModel = new DefaultTableModel(new String[]{"Date", "Category", "Amount", "Description"}, 0);
        expenseTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(expenseTable);
        add(scrollPane, BorderLayout.CENTER);

        // Form for adding expenses
        JPanel formPanel = new JPanel(new GridLayout(6, 2));

        JTextField dateField = new JTextField();
        JComboBox<String> categoryBox = new JComboBox<>(expenseManager.getPredefinedCategories().toArray(new String[0]));
        JTextField amountField = new JTextField();
        JTextField descriptionField = new JTextField();

        JButton addButton = new JButton("Add Expense");
        JButton deleteButton = new JButton("Delete Selected Expense");

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

        add(formPanel, BorderLayout.SOUTH);

        // Action listener for adding expenses
        addButton.addActionListener(e -> {
            try {
                LocalDate date = LocalDate.parse(dateField.getText());
                String category = (String) categoryBox.getSelectedItem();
                double amount = Double.parseDouble(amountField.getText());
                String description = descriptionField.getText();

                Expense expense = new Expense(date, category, amount, description);
                expenseManager.addExpense(expense);

                // Update table
                tableModel.addRow(new Object[]{date, category, amount, description});

                // Clear input fields
                dateField.setText("");
                categoryBox.setSelectedIndex(0);
                amountField.setText("");
                descriptionField.setText("");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid input. Please check your data.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Action listener for deleting expenses
        deleteButton.addActionListener(e -> {
            int selectedRow = expenseTable.getSelectedRow();
            if (selectedRow != -1) {
                // Remove from the expense manager
                expenseManager.deleteExpense(selectedRow);

                // Remove from the table
                tableModel.removeRow(selectedRow);
            } else {
                JOptionPane.showMessageDialog(this, "No expense selected.", "Error", JOptionPane.WARNING_MESSAGE);
            }
        });

        // Filter panel with GridBagLayout for better control
        JPanel filterPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        JTextField startDateField = new JTextField(10); // Fixed size for start date
        JTextField endDateField = new JTextField(10);  // Fixed size for end date
        JList<String> categoryList = new JList<>(expenseManager.getPredefinedCategories().toArray(new String[0]));
        categoryList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane categoryScrollPane = new JScrollPane(categoryList);
        JButton filterButton = new JButton("Filter");
        JButton resetButton = new JButton("Reset");

        gbc.gridx = 0;
        gbc.gridy = 0;
        filterPanel.add(new JLabel("Start Date (YYYY-MM-DD):"), gbc);

        gbc.gridx = 1;
        filterPanel.add(startDateField, gbc);

        gbc.gridx = 2;
        filterPanel.add(new JLabel("End Date (YYYY-MM-DD):"), gbc);

        gbc.gridx = 3;
        filterPanel.add(endDateField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
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

        // Action listener for filtering expenses
        filterButton.addActionListener(e -> {
            try {
                LocalDate startDate = startDateField.getText().isEmpty() ? null : LocalDate.parse(startDateField.getText());
                LocalDate endDate = endDateField.getText().isEmpty() ? null : LocalDate.parse(endDateField.getText());
                List<String> selectedCategories = categoryList.getSelectedValuesList();

                List<Expense> filteredExpenses = expenseManager.getAllExpenses();

                // Filter by date range
                if (startDate != null && endDate != null) {
                    filteredExpenses = expenseManager.filterByDateRange(startDate, endDate);
                }

                // Further filter by categories
                if (!selectedCategories.isEmpty()) {
                    filteredExpenses = filteredExpenses.stream()
                            .filter(expense -> selectedCategories.contains(expense.getCategory()))
                            .collect(Collectors.toList());
                }

                // Update table
                tableModel.setRowCount(0); // Clear table
                for (Expense expense : filteredExpenses) {
                    tableModel.addRow(new Object[]{
                            expense.getDate(), expense.getCategory(), expense.getAmount(), expense.getDescription()
                    });
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid filter inputs. Please check your data.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Action listener for resetting filters
        resetButton.addActionListener(e -> {
            startDateField.setText("");
            endDateField.setText("");
            categoryList.clearSelection();

            // Reload all expenses
            tableModel.setRowCount(0);
            for (Expense expense : expenseManager.getAllExpenses()) {
                tableModel.addRow(new Object[]{
                        expense.getDate(), expense.getCategory(), expense.getAmount(), expense.getDescription()
                });
            }
        });
    }
}
