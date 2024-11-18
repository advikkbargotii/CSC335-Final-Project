import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class Main {
    private static ArrayList<User> users = new ArrayList<>();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::createMainFrame);
    }


    private static void createMainFrame() {
        JFrame frame = new JFrame("Personal Finance Assistant");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLayout(new GridLayout(3, 1));

        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Create Account");
        JButton exitButton = new JButton("Exit");

        frame.add(loginButton);
        frame.add(registerButton);
        frame.add(exitButton);

        loginButton.addActionListener(e -> showLoginDialog(frame));
        registerButton.addActionListener(e -> showRegistrationDialog(frame));
        exitButton.addActionListener(e -> System.exit(0));

        frame.setVisible(true);
    }

    private static void showLoginDialog(JFrame parentFrame) {
        JDialog loginDialog = new JDialog(parentFrame, "Login", true);
        loginDialog.setSize(300, 200);
        loginDialog.setLayout(new GridLayout(3, 2));

        JLabel userLabel = new JLabel("Username:");
        JTextField userField = new JTextField();
        JLabel passLabel = new JLabel("Password:");
        JPasswordField passField = new JPasswordField();

        JButton loginButton = new JButton("Login");
        JButton cancelButton = new JButton("Cancel");

        loginDialog.add(userLabel);
        loginDialog.add(userField);
        loginDialog.add(passLabel);
        loginDialog.add(passField);
        loginDialog.add(loginButton);
        loginDialog.add(cancelButton);

        loginButton.addActionListener(e -> {
            String username = userField.getText();
            String password = new String(passField.getPassword());

            if (authenticateUser(username, password)) {
                JOptionPane.showMessageDialog(loginDialog, "Login successful! Welcome, " + username + "!");
                loginDialog.dispose();
                
                // Find the user and launch FinanceApp
                for (User user : users) {
                    if (user.getUsername().equals(username)) {
                        new FinanceApp(user);
                        break;
                    }
                }
            } else {
                JOptionPane.showMessageDialog(loginDialog, "Login failed. Invalid credentials.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> loginDialog.dispose());

        loginDialog.setVisible(true);
    }

    private static void showRegistrationDialog(JFrame parentFrame) {
        JDialog registrationDialog = new JDialog(parentFrame, "Register", true);
        registrationDialog.setSize(300, 250);
        registrationDialog.setLayout(new GridLayout(4, 2));

        JLabel userLabel = new JLabel("Username:");
        JTextField userField = new JTextField();
        JLabel passLabel = new JLabel("Password:");
        JPasswordField passField = new JPasswordField();
        JLabel feedbackLabel = new JLabel("Password must be 8+ characters with at least 1 number.");
        feedbackLabel.setForeground(Color.RED);

        JButton registerButton = new JButton("Register");
        JButton cancelButton = new JButton("Cancel");

        registrationDialog.add(userLabel);
        registrationDialog.add(userField);
        registrationDialog.add(passLabel);
        registrationDialog.add(passField);
        registrationDialog.add(new JLabel()); 
        registrationDialog.add(feedbackLabel);
        registrationDialog.add(registerButton);
        registrationDialog.add(cancelButton);

        registerButton.addActionListener(e -> {
            String username = userField.getText();
            String password = new String(passField.getPassword());

            if (isPasswordValid(password)) {
                // Ensure the username is unique
                if (isUsernameAvailable(username)) {
                    User newUser = new User(username, password);
                    users.add(newUser);
                    JOptionPane.showMessageDialog(registrationDialog, "Registration successful!");
                    registrationDialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(registrationDialog, "Username already taken. Please choose another.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                feedbackLabel.setText("Invalid password! Ensure 8+ characters and at least 1 number.");
            }
        });

        cancelButton.addActionListener(e -> registrationDialog.dispose());

        registrationDialog.setVisible(true);
    }

    // Helper method to check password validity
    private static boolean isPasswordValid(String password) {
        if (password.length() < 8) {
            return false;
        }
        boolean hasNumber = password.chars().anyMatch(Character::isDigit);
        return hasNumber;
    }

    // Helper method to check if username is available
    private static boolean isUsernameAvailable(String username) {
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                return false;
            }
        }
        return true;
    }

    private static boolean authenticateUser(String username, String password) {
        for (User user : users) {
            if (user.getUsername().equals(username) && user.checkPassword(password)) {
                return true;
            }
        }
        return false;
    }
}
