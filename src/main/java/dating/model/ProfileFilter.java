package dating.model;

/** Неизменяемый фильтр по включительному диапазону возраста и необязательному городу. */
public final class ProfileFilter {
    private final int minAge;
    private final int maxAge;
    private final String city;

    /**
     * Создаёт фильтр.
     * @param minAge нижняя граница от 18
     * @param maxAge верхняя граница до 120, не меньше нижней
     * @param city город; null или пустая строка отключают ограничение по городу
     * @throws IllegalArgumentException если возрастные границы некорректны
     */
    public ProfileFilter(int minAge, int maxAge, String city) {
        if (minAge < 18 || maxAge > 120 || minAge > maxAge) {
            throw new IllegalArgumentException("Укажите диапазон возраста от 18 до 120, от меньшего к большему.");
        }
        this.minAge = minAge;
        this.maxAge = maxAge;
        this.city = city == null || city.isBlank() ? "" : UserProfile.normalize(city, "Город");
    }

    /**
     * Создаёт фильтр без ограничений, кроме допустимого возраста профиля.
     * @return все города и возраст 18–120
     */
    public static ProfileFilter all() { return new ProfileFilter(18, 120, ""); }

    /**
     * Возвращает нижнюю границу.
     * @return минимальный возраст включительно
     */
    public int getMinAge() { return minAge; }

    /**
     * Возвращает верхнюю границу.
     * @return максимальный возраст включительно
     */
    public int getMaxAge() { return maxAge; }

    /**
     * Возвращает ограничение по городу.
     * @return город в нижнем регистре или пустая строка
     */
    public String getCity() { return city; }

    /**
     * Проверяет профиль сразу по двум ограничениям.
     * @param profile проверяемый профиль
     * @return соответствует ли возраст и город
     * @throws IllegalArgumentException если профиль отсутствует
     */
    public boolean matches(UserProfile profile) {
        if (profile == null) { throw new IllegalArgumentException("Нужен профиль для проверки."); }
        return profile.getAge() >= minAge && profile.getAge() <= maxAge
                && (city.isEmpty() || city.equals(UserProfile.normalize(profile.getCity(), "Город")));
    }
}
