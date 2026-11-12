package dating.model;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/** Неориентированный граф: каждому ID соответствует множество знакомых. */
public final class SocialGraph {
    private final Map<Integer, Set<Integer>> connections = new TreeMap<>();

    /** Создаёт пустой граф. */
    public SocialGraph() { }

    /**
     * Добавляет вершину без связей.
     * @param userId положительный ID
     * @throws IllegalArgumentException если ID неположителен или уже добавлен
     */
    public void addUser(int userId) {
        if (userId <= 0 || connections.containsKey(userId)) {
            throw new IllegalArgumentException("ID должен быть положительным и отсутствовать в графе.");
        }
        connections.put(userId, new TreeSet<>());
    }

    /**
     * Создаёт двустороннее знакомство между существующими вершинами.
     * @param firstId первый пользователь
     * @param secondId второй пользователь
     * @return true для новой связи, false для уже существующей
     * @throws IllegalArgumentException если вершина неизвестна или указано знакомство с собой
     */
    public boolean connect(int firstId, int secondId) {
        requireUser(firstId);
        requireUser(secondId);

        if (firstId == secondId) {
            throw new IllegalArgumentException("Нельзя добавить знакомство с самим собой.");
        }

        if (connections.get(firstId).contains(secondId)) {
            return false;
        }

        connections.get(firstId).add(secondId);
        connections.get(secondId).add(firstId);
        return true;
    }

    /**
     * Проверяет знакомство.
     * @param firstId первый пользователь
     * @param secondId второй пользователь
     * @return существует ли связь; для одной и той же вершины — false
     * @throws IllegalArgumentException если вершина неизвестна
     */
    public boolean areConnected(int firstId, int secondId) {
        requireUser(firstId);
        requireUser(secondId);
        return connections.get(firstId).contains(secondId);
    }

    /**
     * Возвращает знакомых пользователя.
     * @param userId существующая вершина
     * @return неизменяемый снимок ID в порядке возрастания
     * @throws IllegalArgumentException если пользователь неизвестен
     */
    public Set<Integer> getFriends(int userId) {
        requireUser(userId);
        return Collections.unmodifiableSet(
                new TreeSet<>(connections.get(userId))
        );
    }

    /**
     * Находит пересечение множеств знакомых.
     * @param firstId первый пользователь
     * @param secondId второй пользователь
     * @return общие знакомые по возрастанию ID, без самих сравниваемых людей
     * @throws IllegalArgumentException если вершина неизвестна
     */
    public Set<Integer> getMutualFriends(int firstId, int secondId) {
        requireUser(firstId);
        requireUser(secondId);

        Set<Integer> mutual = new TreeSet<>(connections.get(firstId));
        mutual.retainAll(connections.get(secondId));
        mutual.remove(firstId);
        mutual.remove(secondId);

        return Collections.unmodifiableSet(mutual);
    }

    private void requireUser(int id) {
        if (!connections.containsKey(id)) {
            throw new IllegalArgumentException(
                    "Нет пользователя с ID " + id + " в графе."
            );
        }
    }
}
