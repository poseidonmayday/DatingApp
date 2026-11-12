package dating.model;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class UserProfileTest {
    @Test void shouldCreateProfileAndCleanText() {
        UserProfile user = new UserProfile(1, "  Анна   Мария ", 25, "  Нижний   Новгород ");
        assertEquals(1, user.getId());
        assertEquals("Анна Мария", user.getName());
        assertEquals(25, user.getAge());
        assertEquals("Нижний Новгород", user.getCity());
        assertTrue(user.getInterests().isEmpty());
        assertTrue(user.getSocialContacts().isEmpty());
    }

    @Test void shouldAcceptAgeBoundariesAndRejectInvalidData() {
        assertEquals(18, new UserProfile(1, "А", 18, "Москва").getAge());
        assertEquals(120, new UserProfile(2, "Б", 120, "Казань").getAge());
        assertThrows(IllegalArgumentException.class, () -> new UserProfile(0, "А", 25, "Москва"));
        assertThrows(IllegalArgumentException.class, () -> new UserProfile(-1, "А", 25, "Москва"));
        assertThrows(IllegalArgumentException.class, () -> new UserProfile(1, "А", 17, "Москва"));
        assertThrows(IllegalArgumentException.class, () -> new UserProfile(1, "А", 121, "Москва"));
        assertThrows(IllegalArgumentException.class, () -> new UserProfile(1, " ", 25, "Москва"));
        assertThrows(IllegalArgumentException.class, () -> new UserProfile(1, null, 25, "Москва"));
        assertThrows(IllegalArgumentException.class, () -> new UserProfile(1, "А", 25, " "));
        assertThrows(IllegalArgumentException.class, () -> new UserProfile(1, "А", 25, null));
    }

    @Test void shouldNormalizeInterestsAndIgnoreDuplicates() {
        DatingService service = new DatingService();
        UserProfile user = service.registerUser(1, "Анна", 25, "Москва");
        assertTrue(service.addInterest(1, " СПОРТ "));
        assertTrue(service.addInterest(1, "Книги"));
        int events = service.getEventLog().size();
        assertFalse(service.addInterest(1, "спорт"));
        assertEquals(List.of("книги", "спорт"), List.copyOf(user.getInterests()));
        assertEquals(events, service.getEventLog().size());
        assertThrows(IllegalArgumentException.class, () -> service.addInterest(1, " "));
        assertThrows(IllegalArgumentException.class, () -> service.addInterest(1, null));
        assertEquals(2, user.getInterests().size());
    }

    @Test void shouldNormalizeNetworkNamesAndReplaceContacts() {
        DatingService service = new DatingService();
        UserProfile user = service.registerUser(1, "Анна", 25, "Москва");
        assertTrue(service.setSocialContact(1, " VK ", " Anna_25 "));
        assertFalse(service.setSocialContact(1, "vk", "Anna_25"));
        assertTrue(service.setSocialContact(1, "Vk", "NewAnna"));
        assertEquals(Map.of("vk", "NewAnna"), user.getSocialContacts());
        int events = service.getEventLog().size();
        assertThrows(IllegalArgumentException.class, () -> service.setSocialContact(1, "vk", " "));
        assertThrows(IllegalArgumentException.class, () -> service.setSocialContact(1, " ", "anna"));
        assertEquals(Map.of("vk", "NewAnna"), user.getSocialContacts());
        assertEquals(events, service.getEventLog().size());
    }

    @Test void shouldProtectInterestAndContactSnapshots() {
        DatingService service = new DatingService();
        UserProfile user = service.registerUser(1, "Анна", 25, "Москва");
        service.addInterest(1, "кино");
        service.setSocialContact(1, "VK", "anna");
        Set<String> interests = user.getInterests();
        Map<String, String> contacts = user.getSocialContacts();
        assertThrows(UnsupportedOperationException.class, () -> interests.add("спорт"));
        assertThrows(UnsupportedOperationException.class, () -> contacts.put("vk", "other"));
        service.addInterest(1, "спорт");
        service.setSocialContact(1, "VK", "new");
        assertEquals(Set.of("кино"), interests);
        assertEquals("anna", contacts.get("vk"));
    }
}
