package dating.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ProfileFilterTest {
    @Test void shouldIncludeBothAgeBoundaries() {
        ProfileFilter filter = new ProfileFilter(20, 30, "");
        assertTrue(filter.matches(new UserProfile(1, "А", 20, "Москва")));
        assertTrue(filter.matches(new UserProfile(2, "Б", 30, "Казань")));
        assertFalse(filter.matches(new UserProfile(3, "В", 19, "Москва")));
        assertFalse(filter.matches(new UserProfile(4, "Г", 31, "Москва")));
        assertEquals(20, filter.getMinAge());
        assertEquals(30, filter.getMaxAge());
    }

    @Test void shouldCompareCitiesWithoutCaseOrExtraSpaces() {
        ProfileFilter filter = new ProfileFilter(18, 120, " нижний   НОВГОРОД ");
        assertTrue(filter.matches(new UserProfile(1, "А", 25, "Нижний Новгород")));
        assertFalse(filter.matches(new UserProfile(2, "Б", 25, "Москва")));
        assertEquals("нижний новгород", filter.getCity());
    }

    @Test void shouldTreatEmptyOrMissingCityAsAnyCity() {
        UserProfile user = new UserProfile(1, "А", 120, "Москва");
        assertTrue(ProfileFilter.all().matches(user));
        assertTrue(new ProfileFilter(18, 120, null).matches(user));
        assertTrue(new ProfileFilter(18, 120, " ").matches(user));
    }

    @Test void shouldRejectIncorrectRangeAndMissingProfile() {
        assertThrows(IllegalArgumentException.class, () -> new ProfileFilter(17, 30, ""));
        assertThrows(IllegalArgumentException.class, () -> new ProfileFilter(18, 121, ""));
        assertThrows(IllegalArgumentException.class, () -> new ProfileFilter(30, 20, ""));
        assertThrows(IllegalArgumentException.class, () -> ProfileFilter.all().matches(null));
    }
}
