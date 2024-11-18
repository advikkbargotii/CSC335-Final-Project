import javax.swing.*;
import java.awt.*;

public class FinanceApp {
    private User currentUser;

    public FinanceApp(User user) {
        this.currentUser = user;
        createFinanceAppFrame();
    }

    private void createFinanceAppFrame() {
        JFrame frame = new JFrame("Personal Finance Assistant - Dashboard");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLayout(new BorderLayout());

        JLabel welcomeLabel = new JLabel("Welcome, " + currentUser.getUsername() + "!");
        welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        frame.add(welcomeLabel, BorderLayout.NORTH);

        // Placeholder for additional finance management components
        JPanel mainPanel = new JPanel();
        mainPanel.add(new JLabel("Finance tools will be displayed here."));
        frame.add(mainPanel, BorderLayout.CENTER);

        frame.setVisible(true);
    }
}
