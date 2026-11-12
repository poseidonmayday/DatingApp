package dating.model;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/** Профиль человека. */
public final class UserProfile {
    private final int id;
    private final String name;
    private final int age;
    private final String city;
    private final Set<String> interests = new TreeSet<>();
    private final Map<String, String> socialContacts = new TreeMap<>();

    /**
     * Создаёт профиль пользователя.
     * @param id идентификатор
     * @param name имя
     * @param age возраст
     * @param city город
     */
    public UserProfile(int id, String name, int age, String city) {
        if (id <= 0) {
            throw new IllegalArgumentException("ID должен быть положительным.");
        }
        if (age < 18 || age > 120) {
            throw new IllegalArgumentException("Возраст должен быть от 18 до 120.");
        }

        this.id = id;
        this.name = requireText(name, "Имя");
        this.age = age;
        this.city = requireText(city, "Город");
    }

    /** Возвращает ID пользователя. */
    public int getId() {
        return id;
    }

    /** Возвращает имя. */
    public String getName() {
        return name;
    }

    /** Возвращает возраст. */
    public int getAge() {
        return age;
    }

    /** Возвращает город. */
    public String getCity() {
        return city;
    }

    /** Возвращает интересы пользователя. */
    public Set<String> getInterests() {
        return Collections.unmodifiableSet(new TreeSet<>(interests));
    }

    /** Возвращает контакты пользователя. */
    public Map<String, String> getSocialContacts() {
        return Collections.unmodifiableMap(new TreeMap<>(socialContacts));
    }

    boolean addInterest(String interest) {
        return interests.add(normalize(interest, "Интерес"));
    }

    boolean putSocialContact(String network, String contact) {
        String key = normalize(network, "Социальная сеть");
        String value = requireText(contact, "Контакт");

        if (value.equals(socialContacts.get(key))) {
            return false;
        }

        socialContacts.put(key, value);
        return true;
    }

    static String requireText(String text, String field) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException(
                    field + ": укажите непустую строку."
            );
        }
        return text.strip().replaceAll("\\s+", " ");
    }

    static String normalize(String text, String field) {
        return requireText(text, field).toLowerCase(Locale.ROOT);
    }
}
