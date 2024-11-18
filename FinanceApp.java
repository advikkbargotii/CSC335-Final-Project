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
        frame.setSize(800, 600);
        frame.setLayout(new BorderLayout());

        JLabel welcomeLabel = new JLabel("Welcome, " + currentUser.getUsername() + "!");
        welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        frame.add(welcomeLabel, BorderLayout.NORTH);

        // Expense Tracker Panel
        ExpenseManager expenseManager = new ExpenseManager();
        ExpenseTrackerPanel expenseTrackerPanel = new ExpenseTrackerPanel(expenseManager);
        frame.add(expenseTrackerPanel, BorderLayout.CENTER);

        frame.setVisible(true);
    }

}
