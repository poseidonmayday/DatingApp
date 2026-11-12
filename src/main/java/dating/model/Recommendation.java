package dating.model;

import java.util.HashSet;
import java.util.List;

/** Кандидат и основания рекомендации: интересы либо общие знакомые. */
public final class Recommendation {
    private final UserProfile user;
    private final List<String> reasons;

    /**
     * Создаёт результат с положительным числом совпадений.
     * @param user рекомендуемый профиль
     * @param reasons непустой список уникальных непустых оснований
     * @throws IllegalArgumentException если профиль или основания некорректны
     */
    public Recommendation(UserProfile user, List<String> reasons) {
        if (user == null || reasons == null || reasons.isEmpty()) {
            throw new IllegalArgumentException("Нужны профиль и хотя бы одно основание рекомендации.");
        }
        for (String reason : reasons) {
            if (reason == null || reason.isBlank()) { throw new IllegalArgumentException("Основание не должно быть пустым."); }
        }
        if (new HashSet<>(reasons).size() != reasons.size()) {
            throw new IllegalArgumentException("Основания рекомендации не должны повторяться.");
        }
        this.user = user;
        this.reasons = List.copyOf(reasons);
    }

    /**
     * Возвращает кандидата.
     * @return профиль пользователя
     */
    public UserProfile getUser() { return user; }

    /**
     * Возвращает число оснований для ранжирования.
     * @return количество совпавших интересов либо общих знакомых
     */
    public int getScore() { return reasons.size(); }

    /**
     * Возвращает объяснение результата.
     * @return неизменяемый список совпавших интересов либо ID и имён знакомых
     */
    public List<String> getReasons() { return reasons; }
}
