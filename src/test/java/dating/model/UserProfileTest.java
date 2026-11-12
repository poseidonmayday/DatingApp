
package dating.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserProfileTest {

    @Test
    void shouldCreateProfileAndCleanText() {
        UserProfile user = new UserProfile(
                1, "  Иван   Петров  ", 20, "  Воронеж  "
        );

        assertEquals(1, user.getId());
        assertEquals("Иван Петров", user.getName());
        assertEquals(20, user.getAge());
        assertEquals("Воронеж", user.getCity());

        assertTrue(user.getInterests().isEmpty());
        assertTrue(user.getSocialContacts().isEmpty());
    }

    @Test
    void shouldAcceptAgeBoundariesAndRejectInvalidData() {
        UserProfile young = new UserProfile(1, "Иван", 18, "Москва");
        UserProfile old = new UserProfile(2, "Пётр", 120, "Воронеж");

        assertEquals(18, young.getAge());
        assertEquals(120, old.getAge());

        assertThrows(IllegalArgumentException.class,
                () -> new UserProfile(0, "Иван", 20, "Москва"));

        assertThrows(IllegalArgumentException.class,
                () -> new UserProfile(1, "Иван", 17, "Москва"));

        assertThrows(IllegalArgumentException.class,
                () -> new UserProfile(1, "Иван", 121, "Москва"));

        assertThrows(IllegalArgumentException.class,
                () -> new UserProfile(1, "", 20, "Москва"));

        assertThrows(IllegalArgumentException.class,
                () -> new UserProfile(1, "Иван", 20, " "));
    }
}
