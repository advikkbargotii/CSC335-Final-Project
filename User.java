import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
 
// Primary Author: Harshit Jain
/**
 * The User class represents a user in the system.
 * It manages the user's credentials including their username, hashed password, and unique salt.
 */
public class User {
    private final String username;
    private final String passwordHash;
    private final String salt;

    /**
     * Constructor for creating a new user.
     * This constructor generates a unique salt and hashes the password with the salt.
     *
     * @param username the username for the user; must not be null or empty
     * @param password the plain-text password for the user; must not be null or empty
     * @throws IllegalArgumentException if the username or password is null or empty
     */
    public User(String username, String password) {
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty.");
        }
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty.");
        }
        this.username = username;
        this.salt = generateSalt(); 
        this.passwordHash = hashPassword(password, this.salt); // Hash the password with the salt
    }

    /**
     * Constructor for loading existing users.
     *
     * @param username     the username of the user
     * @param passwordHash the hashed password of the user
     * @param salt         the unique salt used for hashing the password
     */
    public User(String username, String passwordHash, String salt) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.salt = salt;
    }

    /**
     * Generates a unique salt using a secure random generator.
     *
     * @return the generated salt as a Base64 encoded string
     */
    private String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] saltBytes = new byte[16];
        random.nextBytes(saltBytes);
        return Base64.getEncoder().encodeToString(saltBytes);
    }

    /**
     * Hashes the given password using the provided salt and SHA-256 algorithm.
     *
     * @param password the plain-text password to hash
     * @param salt     the salt to use for hashing
     * @return the hashed password as a Base64 encoded string
     * @throws IllegalStateException if the SHA-256 algorithm is not available
     */
    private String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt.getBytes());
            byte[] hashBytes = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Error hashing password", e);
        }
    }

    /**
     * Checks if the provided plain-text password matches the stored hashed password.
     *
     * @param password the plain-text password to verify
     * @return true if the password matches, false otherwise
     */
    public boolean checkPassword(String password) {
        return this.passwordHash.equals(hashPassword(password, this.salt));
    }

    /**
     * Gets the username of the user.
     *
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Gets the hashed password of the user.
     *
     * @return the hashed password
     */
    public String getPasswordHash() {
        return passwordHash;
    }

    /**
     * Gets the salt used for hashing the password.
     *
     * @return the salt
     */
    public String getSalt() {
        return salt;
    }

    /**
     * Provides a string representation of the user.
     *
     * @return the string representation in the format "User: {username}"
     */
    @Override
    public String toString() {
        return "User: " + username;
    }
}
