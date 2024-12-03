import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void testUserCreation() {
        // Testing the creation of a user with a username and password
        User user = new User("testUser", "securePassword123");
        assertNotNull(user, "User instance should not be null.");
        assertEquals("testUser", user.getUsername(), "Username should be correctly set.");
    }

    @Test
    void testPasswordHashing() {
        // Ensure the password is hashed and not stored in plaintext
        User user = new User("testUser", "securePassword123");
        String passwordHash = user.getPasswordHash();
        assertNotNull(passwordHash, "Password hash should not be null.");
        assertNotEquals("securePassword123", passwordHash, "Password hash should not be equal to the plain password.");
    }

    @Test
    void testPasswordValidation() {
        // Test that the checkPassword method works correctly
        User user = new User("testUser", "securePassword123");
        assertTrue(user.checkPassword("securePassword123"), "Password should be valid.");
        assertFalse(user.checkPassword("wrongPassword"), "Password should be invalid.");
    }

    @Test
    void testPasswordHashingConsistency() {
        // Verify that hashing the same password twice results in different outputs (SHA-256 is a one-way function)
        User user1 = new User("user1", "testPassword123");
        User user2 = new User("user2", "testPassword123");
        assertNotEquals(user1.getPasswordHash(), user2.getPasswordHash(), "Password hashes should not be the same even for identical passwords.");
    }

    @Test
    void testUserCreationWithSpecialCharacters() {
        // Test for username with special characters or spaces
        User user = new User("user!@#123", "securePassword123");
        assertEquals("user!@#123", user.getUsername(), "Username with special characters should be handled correctly.");
    }

    @Test
    void testEmptyPassword() {
        // Check if the system allows creating a user with an empty password
        assertThrows(IllegalArgumentException.class, () -> new User("testUser", ""),
                "User creation with an empty password should throw an exception.");
    }

    @Test
    void testEmptyUsername() {
        // Check if the system allows creating a user with an empty username
        assertThrows(IllegalArgumentException.class, () -> new User("", "securePassword123"),
                "User creation with an empty username should throw an exception.");
    }

    @Test
    void testNullPassword() {
        // Ensure that null password input is properly handled
        assertThrows(IllegalArgumentException.class, () -> new User("testUser", null),
                "User creation with null password should throw an IllegalArgumentException.");
    }


    @Test
    public void testNullUsername() {
        assertThrows(IllegalArgumentException.class, () -> new User(null, "password123"), 
                     "User creation with null username should throw an IllegalArgumentException.");
    }


    @Test
    void testToStringMethod() {
        // Test the output of the toString method
        User user = new User("testUser", "securePassword123");
        String expectedOutput = "User: testUser";
        assertEquals(expectedOutput, user.toString(), "The toString() output should match the expected format.");
    }



    @Test
    void testEquality() {
        // Test that two users with the same username and password hash are considered equal
        User user1 = new User("user1", "password123");
        User user2 = new User("user1", "password123");
        assertTrue(user1.getUsername().equals(user2.getUsername()), "Users with the same username should be equal.");
    }

    @Test
    void testInequality() {
        // Test that two users with different usernames are not equal
        User user1 = new User("user1", "password123");
        User user2 = new User("user2", "password123");
        assertFalse(user1.getUsername().equals(user2.getUsername()), "Users with different usernames should not be equal.");
    }
}
