import javax.swing.*;
import java.awt.*;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

public class ReportManagerPanel extends JPanel {
    private ReportManager reportManager;
    private JTextArea reportTextArea;
    private JButton refreshButton;
    private JPanel pieChartPanel;
    private NumberFormat currencyFormatter;

    public ReportManagerPanel(ReportManager reportManager) {
        this.reportManager = reportManager;
        this.currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US);
        initializePanel();
    }

    private void initializePanel() {
        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Create top panel for title and refresh button
        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);

        // Create main content panel
        JSplitPane splitPane = createMainContentPanel();
        add(splitPane, BorderLayout.CENTER);

        refreshReport();
    }

    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        JLabel titleLabel = new JLabel("Financial Report");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));

        refreshButton = createStyledButton("Refresh Report", new Color(63, 81, 181));
        refreshButton.addActionListener(e -> refreshReport());

        topPanel.add(titleLabel, BorderLayout.WEST);
        topPanel.add(refreshButton, BorderLayout.EAST);

        return topPanel;
    }

    private JSplitPane createMainContentPanel() {
        // Create the left panel with report text
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
            ),
            "Detailed Report"
        ));

        reportTextArea = new JTextArea();
        reportTextArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        reportTextArea.setEditable(false);
        reportTextArea.setMargin(new Insets(10, 10, 10, 10));
        reportTextArea.setBackground(new Color(252, 252, 252));

        JScrollPane scrollPane = new JScrollPane(reportTextArea);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        leftPanel.add(scrollPane);

        // Create the right panel with pie chart and summary
        JPanel rightPanel = createRightPanel();

        // Create split pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setResizeWeight(0.6);
        splitPane.setDividerLocation(0.6);

        return splitPane;
    }

    private JPanel createRightPanel() {
        JPanel rightPanel = new JPanel(new BorderLayout(0, 20));
        rightPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                "Visual Summary"
            ),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // Create pie chart panel
        pieChartPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawPieChart(g);
            }
        };
        pieChartPanel.setPreferredSize(new Dimension(400, 400));

        // Create summary panel
        JPanel summaryPanel = createSummaryPanel();

        rightPanel.add(pieChartPanel, BorderLayout.CENTER);
        rightPanel.add(summaryPanel, BorderLayout.SOUTH);

        return rightPanel;
    }

    private JPanel createSummaryPanel() {
        JPanel summaryPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        summaryPanel.setBorder(BorderFactory.createTitledBorder("Summary"));

        double totalBudget = reportManager.getTotalBudget();
        double totalExpenses = reportManager.getTotalExpenses();
        double remainingBudget = totalBudget - totalExpenses;

        addSummaryRow(summaryPanel, "Total Budget:", totalBudget);
        addSummaryRow(summaryPanel, "Total Expenses:", totalExpenses);
        addSummaryRow(summaryPanel, "Remaining Budget:", remainingBudget);

        return summaryPanel;
    }

    private void addSummaryRow(JPanel panel, String label, double amount) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        JLabel labelComponent = new JLabel(label);
        JLabel valueComponent = new JLabel(currencyFormatter.format(amount));
        valueComponent.setHorizontalAlignment(SwingConstants.RIGHT);
        
        // Set color based on the type of value
        if (label.contains("Remaining")) {
            valueComponent.setForeground(amount >= 0 ? new Color(76, 175, 80) : new Color(211, 47, 47));
        }

        row.add(labelComponent, BorderLayout.WEST);
        row.add(valueComponent, BorderLayout.EAST);
        panel.add(row);
    }

    private void refreshReport() {
        // Update the report text with formatted content
        StringBuilder formattedReport = new StringBuilder();
        formattedReport.append("FINANCIAL REPORT\n");
        formattedReport.append("================\n\n");

        Map<String, Double> categorySpending = reportManager.getCategoryWiseSpending();
        double totalBudget = reportManager.getTotalBudget();
        double totalExpenses = reportManager.getTotalExpenses();

        // Add category-wise breakdown
        formattedReport.append("Category Breakdown:\n");
        formattedReport.append("------------------\n");
        for (Map.Entry<String, Double> entry : categorySpending.entrySet()) {
            double categoryBudget = reportManager.budgetManager.getBudget(entry.getKey());
            formattedReport.append(String.format("%-15s:\n", entry.getKey()));
            formattedReport.append(String.format("  Budget:    %s\n", currencyFormatter.format(categoryBudget)));
            formattedReport.append(String.format("  Spent:     %s\n", currencyFormatter.format(entry.getValue())));
            formattedReport.append(String.format("  Remaining: %s\n\n", 
                currencyFormatter.format(categoryBudget - entry.getValue())));
        }

        // Add overall summary
        formattedReport.append("\nOverall Summary:\n");
        formattedReport.append("--------------\n");
        formattedReport.append(String.format("Total Budget:     %s\n", currencyFormatter.format(totalBudget)));
        formattedReport.append(String.format("Total Expenses:   %s\n", currencyFormatter.format(totalExpenses)));
        formattedReport.append(String.format("Remaining Budget: %s\n", currencyFormatter.format(totalBudget - totalExpenses)));

        reportTextArea.setText(formattedReport.toString());
        reportTextArea.setCaretPosition(0);

        // Refresh the visual components
        pieChartPanel.repaint();
        revalidate();
        repaint();
    }

    private JButton createStyledButton(String text, Color baseColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 12));
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

    private void drawPieChart(Graphics g) {
        Map<String, Double> spendingData = reportManager.getCategoryWiseSpending();

        if (spendingData.isEmpty() || spendingData.values().stream().allMatch(spending -> spending == 0)) {
            drawEmptyPieChart(g);
            return;
        }

        Map<String, Color> categoryColors = Map.of(
            "Food", new Color(76, 175, 80),         // Green
            "Transportation", new Color(33, 150, 243), // Blue
            "Entertainment", new Color(156, 39, 176),  // Purple
            "Utilities", new Color(255, 152, 0),      // Orange
            "Miscellaneous", new Color(158, 158, 158)  // Grey
        );

        double totalSpending = spendingData.values().stream().mapToDouble(Double::doubleValue).sum();
        
        // Calculate pie chart dimensions
        int padding = 40;
        int size = Math.min(pieChartPanel.getWidth() - 200, pieChartPanel.getHeight() - 100); // Reduced size to accommodate legend
        size = Math.min(size, 300); // Maximum size limit
        int x = padding;
        int y = (pieChartPanel.getHeight() - size) / 2;

        // Draw pie chart
        int startAngle = 0;
        for (Map.Entry<String, Double> entry : spendingData.entrySet()) {
            if (entry.getValue() > 0) {
                int arcAngle = (int) Math.round((entry.getValue() / totalSpending) * 360);
                g.setColor(categoryColors.getOrDefault(entry.getKey(), Color.GRAY));
                g.fillArc(x, y, size, size, startAngle, arcAngle);
                startAngle += arcAngle;
            }
        }

        // Draw legend
        int legendX = x + size + 20;
        int legendY = y;
        int boxSize = 15;
        int spacing = 5;
        int lineHeight = 25;

        g.setFont(new Font("Arial", Font.PLAIN, 12));
        FontMetrics fm = g.getFontMetrics();

        for (Map.Entry<String, Double> entry : spendingData.entrySet()) {
            if (entry.getValue() > 0) {
                // Draw color box
                g.setColor(categoryColors.getOrDefault(entry.getKey(), Color.GRAY));
                g.fillRect(legendX, legendY, boxSize, boxSize);

                // Draw text
                g.setColor(Color.BLACK);
                String legendText = String.format("%s: %s (%.1f%%)", 
                    entry.getKey(),
                    currencyFormatter.format(entry.getValue()),
                    (entry.getValue() / totalSpending) * 100);

                // Check if legend text would exceed panel width
                if (legendX + boxSize + spacing + fm.stringWidth(legendText) > pieChartPanel.getWidth()) {
                    // Start a new column
                    legendX = x + size + 20;
                    legendY += lineHeight * 2;
                }

                g.drawString(legendText, legendX + boxSize + spacing, legendY + boxSize);
                legendY += lineHeight;
            }
        }
    }

    private void drawEmptyPieChart(Graphics g) {
        int padding = 40;
        int size = Math.min(pieChartPanel.getWidth(), pieChartPanel.getHeight()) - (2 * padding);
        int x = (pieChartPanel.getWidth() - size) / 2;
        int y = (pieChartPanel.getHeight() - size) / 2;

        g.setColor(new Color(200, 200, 200));
        g.fillArc(x, y, size, size, 0, 360);

        g.setColor(Color.DARK_GRAY);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        String message = "No spending data available";
        FontMetrics fm = g.getFontMetrics();
        int messageWidth = fm.stringWidth(message);
        g.drawString(message, 
            (pieChartPanel.getWidth() - messageWidth) / 2,
            pieChartPanel.getHeight() / 2);
    }
}
