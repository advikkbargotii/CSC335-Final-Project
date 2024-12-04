import javax.swing.*;
import java.awt.*;

public class SettingsPanel extends JPanel {

    public interface Callback {
        void onSaveChanges(String oldPassword, String newPassword);
        void onCancel();
        void onDeleteAccount();
    }

    private final Callback callback;
    private final JTextField usernameField;
    private final JPasswordField newPasswordField, oldPasswordField;
    private final JLabel strengthLabel; // Label for showing password strength

    public SettingsPanel(User currentUser, Callback callback) {
        this.callback = callback;

        // Set layout
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Add title
        JLabel titleLabel = new JLabel("Settings", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        add(titleLabel, BorderLayout.NORTH);

        // Main form panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Username field (read-only for now)
        JLabel usernameLabel = new JLabel("Username:");
        usernameField = new JTextField(currentUser.getUsername());
        usernameField.setEnabled(false);
        usernameField.setBackground(Color.LIGHT_GRAY);
        usernameField.setDisabledTextColor(Color.DARK_GRAY);
        usernameField.setFont(new Font("Arial", Font.PLAIN, 16));

        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(usernameLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(usernameField, gbc);

        // Old Password field
        JLabel oldPasswordLabel = new JLabel("Old Password:");
        oldPasswordField = new JPasswordField();
        oldPasswordField.setFont(new Font("Arial", Font.PLAIN, 16));

        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(oldPasswordLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(oldPasswordField, gbc);


        JLabel newPasswordLabel = new JLabel("New Password:");
        newPasswordField = new JPasswordField();
        newPasswordField.setFont(new Font("Arial", Font.PLAIN, 16));
        newPasswordField.getDocument().addDocumentListener(new PasswordStrengthListener(newPasswordField));

        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(newPasswordLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(newPasswordField, gbc);


        JLabel strengthLabelTitle = new JLabel("Password Strength:");
        strengthLabel = new JLabel("Very Weak");
        strengthLabel.setFont(new Font("Arial", Font.BOLD, 14));
        strengthLabel.setForeground(Color.RED);

        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(strengthLabelTitle, gbc);
        gbc.gridx = 1;
        formPanel.add(strengthLabel, gbc);

        add(formPanel, BorderLayout.CENTER);

        // Action buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 20, 20));
        buttonPanel.setBackground(Color.WHITE);

        JButton saveButton = new JButton("Save");
        saveButton.setFont(new Font("Arial", Font.BOLD, 16));
        saveButton.setBackground(new Color(34, 139, 34));
        saveButton.setForeground(Color.WHITE);
        saveButton.addActionListener(e -> handleSave());

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setFont(new Font("Arial", Font.BOLD, 16));
        cancelButton.setBackground(new Color(220, 53, 69));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.addActionListener(e -> callback.onCancel());

        JButton deleteAccountButton = new JButton("Delete Account");
        deleteAccountButton.setFont(new Font("Arial", Font.BOLD, 16));
        deleteAccountButton.setBackground(Color.RED);
        deleteAccountButton.setForeground(Color.WHITE);
        deleteAccountButton.addActionListener(e -> callback.onDeleteAccount());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        buttonPanel.add(deleteAccountButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private static void updatePasswordStrengthIndicator(JLabel strengthLabel, String password) {
        int strength = calculatePasswordStrength(password);
        String[] strengthTexts = {
        		"Very Weak  ", 
        		"Weak       ", 
        		"Moderate   ", 
        		"Strong     ", 
        		"Very Strong", 
        		"Excellent  "
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

    private void handleSave() {
        String newPassword = new String(newPasswordField.getPassword());
        String oldPassword = new String(oldPasswordField.getPassword());

        if (calculatePasswordStrength(newPassword) < 3) {
            JOptionPane.showMessageDialog(
                this,
                "The new password is not strong enough. Please create a stronger password.",
                "Weak Password",
                JOptionPane.WARNING_MESSAGE
            );
        } else {
            // Apply settings and notify callback
            callback.onSaveChanges(oldPassword, newPassword);
        }
    }

    // Listener to dynamically update password strength
    private class PasswordStrengthListener implements javax.swing.event.DocumentListener {
        private final JPasswordField passwordField;

        public PasswordStrengthListener(JPasswordField passwordField) {
            this.passwordField = passwordField;
        }

        @Override
        public void insertUpdate(javax.swing.event.DocumentEvent e) {
            updateStrength();
        }

        @Override
        public void removeUpdate(javax.swing.event.DocumentEvent e) {
            updateStrength();
        }

        @Override
        public void changedUpdate(javax.swing.event.DocumentEvent e) {
            updateStrength();
        }

        private void updateStrength() {
            String password = new String(passwordField.getPassword());
            updatePasswordStrengthIndicator(strengthLabel, password);
        }
    }
}

