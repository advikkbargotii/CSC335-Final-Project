import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.util.*;
import java.util.Base64;
import java.util.List;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Main {
    private static final String USER_DATA_FILE = "data/users.txt";
    private static ArrayList<User> users = new ArrayList<>();
    private static JFrame mainFrame;

    public static void main(String[] args) {
        loadUserData();
        SwingUtilities.invokeLater(() -> {
            mainFrame = new JFrame("Personal Finance Assistant");
            createMainFrame();
        });
    }

    private static JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setBackground(new Color(63, 81, 181));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(200, 60));
        button.setOpaque(true);
        
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(new Color(48, 63, 159));
            }
            public void mouseExited(MouseEvent evt) {
                button.setBackground(new Color(63, 81, 181));
            }
        }); 

        return button;
    }

    private static void loadUserData() {
        try {
            Files.createDirectories(Paths.get("data"));

            if (!Files.exists(Paths.get(USER_DATA_FILE))) {
                Files.createFile(Paths.get(USER_DATA_FILE));
            }

            List<String> lines = Files.readAllLines(Paths.get(USER_DATA_FILE));
            users.clear();

            for (String line : lines) {
                String[] parts = line.split(":");
                if (parts.length == 3) { // Ensure username, hash, and salt are present
                    users.add(new User(parts[0], parts[1], parts[2])); // Pass salt to User constructor
                }
            }
            System.out.println("Loaded " + users.size() + " users from file");
        } catch (IOException e) {
            System.err.println("Error loading user data: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private static void saveUserData(User user) {
        try {
            Files.createDirectories(Paths.get("data"));
            boolean userExists = false;
            List<String> lines = new ArrayList<>();

            if (Files.exists(Paths.get(USER_DATA_FILE))) {
                lines = Files.readAllLines(Paths.get(USER_DATA_FILE));
                for (int i = 0; i < lines.size(); i++) {
                    String[] parts = lines.get(i).split(":");
                    if (parts[0].equals(user.getUsername())) {
                        lines.set(i, user.getUsername() + ":" + user.getPasswordHash() + ":" + user.getSalt());
                        userExists = true;
                        break;
                    }
                }
            }

            if (!userExists) {
                lines.add(user.getUsername() + ":" + user.getPasswordHash() + ":" + user.getSalt());
            }

            Files.write(Paths.get(USER_DATA_FILE), lines);
            System.out.println("Saved user data for: " + user.getUsername());

        } catch (IOException e) {
            System.err.println("Error saving user data: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private static void createMainFrame() {
        mainFrame = new JFrame("Personal Finance Assistant");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(500, 400);
        mainFrame.setLayout(new GridBagLayout());
        mainFrame.getContentPane().setBackground(new Color(240, 240, 240));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);

        JLabel titleLabel = new JLabel("Personal Finance Assistant", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(33, 33, 33));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        mainFrame.add(titleLabel, gbc);

        JButton loginButton = createStyledButton("Login");
        JButton registerButton = createStyledButton("Create Account");
        JButton exitButton = createStyledButton("Exit");

        loginButton.addActionListener(e -> showLoginDialog(mainFrame));
        registerButton.addActionListener(e -> showRegistrationDialog(mainFrame));
        exitButton.addActionListener(e -> System.exit(0));

        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.gridx = 0;
        mainFrame.add(loginButton, gbc);

        gbc.gridx = 1;
        mainFrame.add(registerButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        mainFrame.add(exitButton, gbc);

        mainFrame.setLocationRelativeTo(null);
        mainFrame.setVisible(true);
    }

    private static void showRegistrationDialog(JFrame parentFrame) {
        JDialog registrationDialog = new JDialog(parentFrame, "Register", true);
        registrationDialog.setSize(400, 300);
        registrationDialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel userLabel = new JLabel("Username:");
        JTextField userField = new JTextField(20);
        JLabel passLabel = new JLabel("Password:");
        JPasswordField passField = new JPasswordField(20);
        JLabel strengthLabel = new JLabel("Password Strength: ");
        strengthLabel.setForeground(Color.DARK_GRAY);

        JButton registerButton = new JButton("Register");
        JButton cancelButton = new JButton("Cancel");

        gbc.gridx = 0;
        gbc.gridy = 0;
        registrationDialog.add(userLabel, gbc);
        
        gbc.gridx = 1;
        registrationDialog.add(userField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        registrationDialog.add(passLabel, gbc);
        
        gbc.gridx = 1;
        registrationDialog.add(passField, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 2;
        registrationDialog.add(strengthLabel, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        registrationDialog.add(registerButton, gbc);
        
        gbc.gridx = 1;
        registrationDialog.add(cancelButton, gbc);

        passField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { update(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { update(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { update(); }
            
            public void update() {
                updatePasswordStrengthIndicator(strengthLabel, new String(passField.getPassword()));
            }
        });

        registerButton.addActionListener(e -> {
            String username = userField.getText();
            String password = new String(passField.getPassword());

            if (isPasswordValid(password)) {
                if (isUsernameAvailable(username)) {
                    try {
                        User newUser = new User(username, password);
                        users.add(newUser);
                        saveUserData(newUser);
                        JOptionPane.showMessageDialog(registrationDialog, "Registration successful!");
                        registrationDialog.dispose();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(registrationDialog,
                            "Registration failed: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(registrationDialog,
                        "Username already taken. Please choose another.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(registrationDialog,
                    "Invalid password! Must be 8+ characters with at least 1 number.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> registrationDialog.dispose());

        registrationDialog.setLocationRelativeTo(parentFrame);
        registrationDialog.setVisible(true);
    }

    private static void showLoginDialog(JFrame parentFrame) {
        JDialog loginDialog = new JDialog(parentFrame, "Login", true);
        loginDialog.setLayout(new GridBagLayout());
        loginDialog.setSize(350, 250);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("Login", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        loginDialog.add(titleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.gridx = 0;
        loginDialog.add(new JLabel("Username:"), gbc);
        
        gbc.gridx = 1;
        JTextField userField = new JTextField(15);
        loginDialog.add(userField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        loginDialog.add(new JLabel("Password:"), gbc);
        
        gbc.gridx = 1;
        JPasswordField passField = new JPasswordField(15);
        loginDialog.add(passField, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton loginButton = new JButton("Login");
        JButton cancelButton = new JButton("Cancel");
        
        buttonPanel.add(loginButton);
        buttonPanel.add(cancelButton);
        
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        loginDialog.add(buttonPanel, gbc);

        loginButton.addActionListener(e -> {
            String username = userField.getText();
            String password = new String(passField.getPassword());

            try {
                if (authenticateUser(username, password)) {
                    User currentUser = findUser(username);
                    JOptionPane.showMessageDialog(loginDialog, 
                        "Login successful! Welcome, " + username + "!");
                    loginDialog.dispose();
                    mainFrame.setVisible(false);
                    new FinanceApp(currentUser);
                } else {
                    JOptionPane.showMessageDialog(loginDialog, 
                        "Login failed. Invalid credentials.", 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(loginDialog, 
                    "Login error: " + ex.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> loginDialog.dispose());

        loginDialog.setLocationRelativeTo(parentFrame);
        loginDialog.setVisible(true);
    }

    private static void updatePasswordStrengthIndicator(JLabel strengthLabel, String password) {
        int strength = calculatePasswordStrength(password);
        String[] strengthTexts = {
            "Very Weak", "Weak", "Moderate", "Strong", "Very Strong", "Excellent"
        };
        Color[] strengthColors = {
            Color.RED, Color.ORANGE, Color.YELLOW, Color.GREEN.darker(), Color.GREEN, Color.BLUE
        };
        
        strengthLabel.setText(strengthTexts[strength]);
        strengthLabel.setForeground(strengthColors[strength]);
    }

    private static int calculatePasswordStrength(String password) {
        int strength = 0;
        
        if (password.length() >= 12) strength += 2;
        else if (password.length() >= 8) strength += 1;
        
        if (password.matches(".*[A-Z].*")) strength += 1;
        if (password.matches(".*[a-z].*")) strength += 1;
        if (password.matches(".*\\d.*")) strength += 1;
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) strength += 1;
        
        return Math.min(strength, 5);
    }

    private static boolean isPasswordValid(String password) {
        return password.length() >= 8 && password.matches(".*\\d.*");
    }

    private static boolean isUsernameAvailable(String username) {
        return users.stream().noneMatch(user -> user.getUsername().equals(username));
    }

    private static User findUser(String username) {
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                return user;
            }
        }
        return null;
    }

    private static boolean authenticateUser(String username, String password) {
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                return user.checkPassword(password); // Hashes password with the stored salt for verification
            }
        }
        return false;
    }


    private static String hashPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(password.getBytes());
        return Base64.getEncoder().encodeToString(hash);
    }


    // If you need to access createMainFrame from outside, rename it to something like:
    public static void showMainWindow() {
        SwingUtilities.invokeLater(() -> {
            mainFrame = new JFrame("Personal Finance Assistant");
            createMainFrame();
        });
    }
}
