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

        for (String category : spendingData.keySet()) {
            g.setColor(categoryColors.getOrDefault(category, Color.GRAY));
            g.fillRect(x, legendY, 20, 20);
            g.setColor(Color.BLACK);
            g.drawString(category, x + 30, legendY + 15);
            legendY += 30;
        }
    }
    
    private void refreshReport() {

        String report = reportManager.generateMonthlySummaryReport();


        reportTextArea.setText(report);
        reportTextArea.setCaretPosition(0);


        pieChartPanel.repaint();
    }

}
