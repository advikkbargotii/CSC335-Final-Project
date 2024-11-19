import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class User {
    private String username;
    private String passwordHash;

    // Constructor for creating new user
    public User(String username, String password) {
        this.username = username;
        this.passwordHash = hashPassword(password);
    }

    // Constructor for loading existing user
    public User(String username, String passwordHash, boolean isLoading) {
        this.username = username;
        this.passwordHash = passwordHash;
    }

    public String getUsername() {
        return username;
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public boolean checkPassword(String password) {
        return hashPassword(password).equals(this.passwordHash);
    }

    @Override
    public String toString() {
        return "User: " + username;
    }
}
