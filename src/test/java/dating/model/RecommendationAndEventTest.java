package dating.model;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class RecommendationAndEventTest {
    @Test void shouldKeepRecommendationReasonsSnapshotAndCountThem() {
        UserProfile user = new UserProfile(1, "Анна", 25, "Москва");
        List<String> reasons = new ArrayList<>(List.of("кино", "книги"));
        Recommendation result = new Recommendation(user, reasons);
        reasons.clear();
        assertSame(user, result.getUser());
        assertEquals(2, result.getScore());
        assertEquals(List.of("кино", "книги"), result.getReasons());
        assertThrows(UnsupportedOperationException.class, () -> result.getReasons().clear());
    }

    @Test void shouldRejectIncorrectRecommendationReasons() {
        UserProfile user = new UserProfile(1, "Анна", 25, "Москва");
        assertThrows(IllegalArgumentException.class, () -> new Recommendation(null, List.of("кино")));
        assertThrows(IllegalArgumentException.class, () -> new Recommendation(user, null));
        assertThrows(IllegalArgumentException.class, () -> new Recommendation(user, List.of()));
        assertThrows(IllegalArgumentException.class, () -> new Recommendation(user, List.of(" ")));
        assertThrows(IllegalArgumentException.class, () -> new Recommendation(user, Arrays.asList((String) null)));
        assertThrows(IllegalArgumentException.class, () -> new Recommendation(user, List.of("кино", "кино")));
    }

    @Test void shouldStoreAndFormatEvent() {
        AppEvent event = new AppEvent(1, "Добавлен профиль");
        assertEquals(1, event.getNumber());
        assertEquals("Добавлен профиль", event.getMessage());
        assertEquals("[1] Добавлен профиль", event.toString());
    }

    @Test void shouldRejectInvalidEvent() {
        assertThrows(IllegalArgumentException.class, () -> new AppEvent(0, "Действие"));
        assertThrows(IllegalArgumentException.class, () -> new AppEvent(1, " "));
        assertThrows(IllegalArgumentException.class, () -> new AppEvent(1, null));
    }
}
