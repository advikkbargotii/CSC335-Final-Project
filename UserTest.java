import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

// Primary Author: Harshit Jain
/**
 * Unit tests for the User class.
 */
public class UserTest {

    /**
     * Tests that the User constructor initializes the username, generates a salt,
     * and hashes the password correctly.
     */
    @Test
    public void testUserConstructor_NewUser() {
        String username = "testUser";
        String password = "securePassword123";
        User user = new User(username, password);

        assertEquals(username, user.getUsername(), "Username should be initialized correctly.");
        assertNotNull(user.getSalt(), "Salt should be generated for the user.");
        assertFalse(user.getSalt().isEmpty(), "Salt should not be empty.");
        assertNotNull(user.getPasswordHash(), "Password hash should be generated.");
    }

    /**
     * Tests that the constructor for loading existing users correctly initializes
     * all fields.
     */
    @Test
    public void testUserConstructor_LoadExistingUser() {
        String username = "existingUser";
        String passwordHash = "hashedPassword123";
        String salt = "randomSalt";
        User user = new User(username, passwordHash, salt);

        assertEquals(username, user.getUsername(), "Username should be initialized correctly.");
        assertEquals(passwordHash, user.getPasswordHash(), "Password hash should be initialized correctly.");
        assertEquals(salt, user.getSalt(), "Salt should be initialized correctly.");
    }

    /**
     * Tests the checkPassword method for matching and non-matching passwords.
     */
    @Test
    public void testCheckPassword() {
        String username = "testUser";
        String password = "securePassword123";
        User user = new User(username, password);

        assertTrue(user.checkPassword(password), "checkPassword should return true for correct password.");
        assertFalse(user.checkPassword("wrongPassword"), "checkPassword should return false for incorrect password.");
    }

    /**
     * Tests the getSalt method to ensure it returns the correct salt.
     */
    @Test
    public void testGetSalt() {
        String username = "testUser";
        String password = "securePassword123";
        User user = new User(username, password);
        String salt = user.getSalt();

        assertNotNull(salt, "Salt should not be null.");
        assertFalse(salt.isEmpty(), "Salt should not be empty.");
    }

    /**
     * Tests the toString method for correct formatting.
     */
    @Test
    public void testToStringMethod() {
        String username = "testUser";
        User user = new User(username, "securePassword123");
        String result = user.toString();

        assertEquals("User: testUser", result, "The toString output should match the expected format.");
    }

    /**
     * Tests that creating a User with a null username throws an exception.
     */
    @Test
    public void testNullUsername() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> new User(null, "password123"));
        assertEquals("Username cannot be empty.", exception.getMessage(), "Exception message should match.");
    }

    /**
     * Tests that creating a User with a null password throws an exception.
     */
    @Test
    public void testNullPassword() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> new User("testUser", null));
        assertEquals("Password cannot be empty.", exception.getMessage(), "Exception message should match.");
    }

    /**
     * Tests the hashing algorithm used for password hashing.
     */
    @Test
    public void testPasswordHashingConsistency() {
        String username = "testUser";
        String password = "securePassword123";
        User user = new User(username, password);
        String hash1 = user.getPasswordHash();
        String hash2 = user.getPasswordHash();

        assertEquals(hash1, hash2, "Password hashes should be consistent for the same instance.");
    }
}
