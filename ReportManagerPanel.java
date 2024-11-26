import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class ReportManagerPanel extends JPanel {
    private ReportManager reportManager;
    private JTextArea reportTextArea;  
    private JButton refreshButton;     
    private JPanel pieChartPanel;     

    public ReportManagerPanel(ReportManager reportManager) {
        this.reportManager = reportManager;
        initializePanel();
    }

    private void initializePanel() {
        setLayout(new BorderLayout());

        reportTextArea = new JTextArea();
        reportTextArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        reportTextArea.setEditable(false);
        reportTextArea.setLineWrap(true);
        reportTextArea.setWrapStyleWord(true);

        reportTextArea.setBorder(BorderFactory.createCompoundBorder(
            reportTextArea.getBorder(),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JScrollPane scrollPane = new JScrollPane(reportTextArea);
        add(scrollPane, BorderLayout.CENTER);

        refreshButton = new JButton("Refresh Report");
        refreshButton.addActionListener(e -> refreshReport());
        add(refreshButton, BorderLayout.SOUTH);

        pieChartPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawPieChart(g);
            }
        };

        pieChartPanel.setPreferredSize(new Dimension(400, 400));
        add(pieChartPanel, BorderLayout.EAST);

        refreshReport();
    }


    private void drawPieChart(Graphics g) {
        Map<String, Double> spendingData = reportManager.getCategoryWiseSpending();

        // Check if there is no data
        if (spendingData.isEmpty() || spendingData.values().stream().allMatch(spending -> spending == 0)) {
            int x = 10, y = 10, width = 300, height = 300;

            g.setColor(Color.LIGHT_GRAY);
            g.drawOval(x, y, width, height);
            g.setFont(new Font("Arial", Font.BOLD, 16));
            g.setColor(Color.BLACK);
            String message = "No data available";
            FontMetrics fm = g.getFontMetrics();
            int textWidth = fm.stringWidth(message);
            int textHeight = fm.getAscent();
            g.drawString(message, x + (width - textWidth) / 2, y + (height + textHeight) / 2);

            return;
        }

        Map<String, Color> categoryColors = Map.of(
            "Food", Color.GREEN,
            "Transportation", Color.BLUE,
            "Entertainment", Color.MAGENTA,
            "Utilities", Color.ORANGE,
            "Miscellaneous", Color.CYAN
        );

        double totalSpending = spendingData.values().stream().mapToDouble(Double::doubleValue).sum();

        int x = 10, y = 10, width = 300, height = 300;
        int startAngle = 0;

        for (Map.Entry<String, Double> entry : spendingData.entrySet()) {
            String category = entry.getKey();
            double spending = entry.getValue();

            int arcAngle = (int) Math.round((spending / totalSpending) * 360);
            g.setColor(categoryColors.getOrDefault(category, Color.GRAY));
            g.fillArc(x, y, width, height, startAngle, arcAngle);
            startAngle += arcAngle;
        }

        drawLegend(g, categoryColors, spendingData, x + width + 20, y);
    }


    private void drawLegend(Graphics g, Map<String, Color> categoryColors, Map<String, Double> spendingData, int x, int y) {
        int legendY = y;
        int padding = 10;


        int maxTextWidth = spendingData.keySet().stream()
            .mapToInt(category -> g.getFontMetrics().stringWidth(category))
            .max()
            .orElse(0);

        int rectWidth = 20;
        int rectHeight = 20;
        int spacing = 10;

        for (String category : spendingData.keySet()) {
            g.setColor(categoryColors.getOrDefault(category, Color.GRAY));
            g.fillRect(x, legendY, rectWidth, rectHeight);
            g.setColor(Color.BLACK);
            g.drawString(category, x + rectWidth + padding, legendY + rectHeight - 5);
            legendY += rectHeight + spacing;
        }


        int totalWidth = x + rectWidth + padding + maxTextWidth + padding;
        pieChartPanel.setPreferredSize(new Dimension(Math.max(totalWidth, 400), 400));
    }

    
    private void refreshReport() {

        String report = reportManager.generateMonthlySummaryReport();


        reportTextArea.setText(report);
        reportTextArea.setCaretPosition(0);


        pieChartPanel.repaint();
    }

}
