package dating.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/** Хранит данные приложения, проверяет действия и выполняет поиск. Не занимается консолью. */
public final class DatingService {
    private final Map<Integer, UserProfile> users = new TreeMap<>();
    private final Map<Integer, Photo> photos = new TreeMap<>();
    private final Set<Like> likes = new LinkedHashSet<>();
    private final SocialGraph graph = new SocialGraph();
    private final List<AppEvent> eventLog = new ArrayList<>();

    /** Создаёт пустой сервис. */
    public DatingService() { }

    /**
     * Регистрирует человека и вершину графа.
     * @param id уникальный положительный ID
     * @param name имя
     * @param age возраст 18–120
     * @param city город
     * @return созданный профиль
     * @throws IllegalArgumentException если данные неверны или ID занят
     */
    public UserProfile registerUser(int id, String name, int age, String city) {
        if (users.containsKey(id)) { throw new IllegalArgumentException("Пользователь с таким ID уже существует."); }
        UserProfile user = new UserProfile(id, name, age, city);
        graph.addUser(id);
        users.put(id, user);
        log("Добавлен профиль #" + id + ": " + user.getName() + ", " + age + ", " + user.getCity() + ".");
        return user;
    }

    /**
     * Находит существующий профиль.
     * @param userId ID пользователя
     * @return профиль с доступом только к чтению
     * @throws IllegalArgumentException если ID неизвестен
     */
    public UserProfile getUser(int userId) {
        UserProfile user = users.get(userId);
        if (user == null) { throw new IllegalArgumentException("Нет пользователя с ID " + userId + "."); }
        return user;
    }

    /**
     * Возвращает все профили.
     * @return неизменяемый список по возрастанию ID
     */
    public List<UserProfile> getUsers() { return List.copyOf(users.values()); }

    /**
     * Добавляет нормализованный интерес без повторов.
     * @param userId ID пользователя
     * @param interest непустое название интереса
     * @return true, если интерес добавлен, false, если он уже был
     * @throws IllegalArgumentException если пользователь неизвестен или интерес пуст
     */
    public boolean addInterest(int userId, String interest) {
        UserProfile user = getUser(userId);
        boolean added = user.addInterest(interest);
        if (added) { log("Профиль #" + userId + ": добавлен интерес «" + UserProfile.normalize(interest, "Интерес") + "»."); }
        return added;
    }

    /**
     * Устанавливает контакт социальной сети; новый адрес заменяет прежний адрес этой сети.
     * @param userId ID пользователя
     * @param network непустое название сети
     * @param contact непустой адрес либо имя аккаунта
     * @return true при добавлении или изменении, false при повторе тех же данных
     * @throws IllegalArgumentException если пользователь или текстовые данные неверны
     */
    public boolean setSocialContact(int userId, String network, String contact) {
        UserProfile user = getUser(userId);
        boolean changed = user.putSocialContact(network, contact);
        if (changed) {
            log("Профиль #" + userId + ": контакт " + UserProfile.normalize(network, "Социальная сеть")
                    + " — " + UserProfile.requireText(contact, "Контакт") + ".");
        }
        return changed;
    }

    /**
     * Добавляет фотографию существующему пользователю.
     * @param photoId уникальный положительный ID фотографии во всём сервисе
     * @param ownerId ID владельца
     * @param path непустой путь, существование файла не проверяется
     * @return описание фотографии
     * @throws IllegalArgumentException если данные неверны, владелец неизвестен или ID занят
     */
    public Photo addPhoto(int photoId, int ownerId, String path) {
        getUser(ownerId);
        if (photos.containsKey(photoId)) { throw new IllegalArgumentException("Фотография с таким ID уже существует."); }
        Photo photo = new Photo(photoId, ownerId, path);
        photos.put(photoId, photo);
        log("Добавлено фото #" + photoId + " пользователя #" + ownerId + ": " + photo.getPath() + ".");
        return photo;
    }

    /**
     * Находит фотографию.
     * @param photoId ID фотографии
     * @return существующее фото
     * @throws IllegalArgumentException если фото неизвестно
     */
    public Photo getPhoto(int photoId) {
        Photo photo = photos.get(photoId);
        if (photo == null) { throw new IllegalArgumentException("Нет фотографии с ID " + photoId + "."); }
        return photo;
    }

    /**
     * Возвращает фотографии человека.
     * @param userId ID пользователя
     * @return неизменяемый список фото по ID
     * @throws IllegalArgumentException если пользователь неизвестен
     */
    public List<Photo> getPhotos(int userId) {
        getUser(userId);
        List<Photo> result = new ArrayList<>();
        for (Photo photo : photos.values()) {
            if (photo.getOwnerId() == userId) { result.add(photo); }
        }
        return List.copyOf(result);
    }

    /**
     * Ставит один лайк чужой фотографии.
     * @param userId автор лайка
     * @param photoId существующая фотография
     * @throws IllegalArgumentException если пользователь или фото неизвестны либо фото своё
     * @throws IllegalStateException если этот пользователь уже лайкнул это фото
     */
    public void likePhoto(int userId, int photoId) {
        getUser(userId);
        Photo photo = getPhoto(photoId);
        if (photo.getOwnerId() == userId) { throw new IllegalArgumentException("Собственные фотографии лайкать нельзя."); }
        Like like = new Like(userId, photoId);
        if (!likes.add(like)) { throw new IllegalStateException("Этот лайк уже существует."); }
        log("Пользователь #" + userId + " лайкнул фото #" + photoId + ".");
    }

    /**
     * Проверяет наличие лайка.
     * @param userId автор
     * @param photoId фотография
     * @return поставлен ли лайк
     * @throws IllegalArgumentException если пользователь или фото неизвестны
     */
    public boolean hasLiked(int userId, int photoId) {
        getUser(userId);
        getPhoto(photoId);
        return likes.contains(new Like(userId, photoId));
    }

    /**
     * Возвращает лайки фотографии.
     * @param photoId ID фото
     * @return неизменяемый список по ID автора; размер списка равен числу лайков
     * @throws IllegalArgumentException если фото неизвестно
     */
    public List<Like> getLikes(int photoId) {
        getPhoto(photoId);
        List<Like> result = new ArrayList<>();
        for (Like like : likes) {
            if (like.getPhotoId() == photoId) { result.add(like); }
        }
        result.sort(Comparator.comparingInt(Like::getUserId));
        return List.copyOf(result);
    }

    /**
     * Создаёт знакомство в обе стороны без дубликатов.
     * @param firstId первый человек
     * @param secondId второй человек
     * @return true для новой связи, false для существующей
     * @throws IllegalArgumentException если ID неизвестен или указано знакомство с собой
     */
    public boolean addAcquaintance(int firstId, int secondId) {
        getUser(firstId);
        getUser(secondId);
        boolean added = graph.connect(firstId, secondId);
        if (added) { log("Добавлено знакомство #" + firstId + " ↔ #" + secondId + "."); }
        return added;
    }

    /**
     * Проверяет знакомство существующих пользователей.
     * @param firstId первый человек
     * @param secondId второй человек
     * @return есть ли связь
     * @throws IllegalArgumentException если ID неизвестен
     */
    public boolean areAcquainted(int firstId, int secondId) { return graph.areConnected(firstId, secondId); }

    /**
     * Находит знакомых человека.
     * @param userId ID пользователя
     * @return неизменяемый список профилей по ID
     * @throws IllegalArgumentException если пользователь неизвестен
     */
    public List<UserProfile> getFriends(int userId) { return profilesFor(graph.getFriends(userId)); }

    /**
     * Находит общих знакомых.
     * @param firstId первый человек
     * @param secondId второй человек
     * @return неизменяемый список общих знакомых по ID
     * @throws IllegalArgumentException если ID неизвестен
     */
    public List<UserProfile> getMutualFriends(int firstId, int secondId) {
        return profilesFor(graph.getMutualFriends(firstId, secondId));
    }

    /**
     * Отбирает профили по возрасту и городу.
     * @param filter непустой объект фильтра
     * @return неизменяемый список по ID
     * @throws IllegalArgumentException если фильтр отсутствует
     */
    public List<UserProfile> filterUsers(ProfileFilter filter) {
        requireFilter(filter);
        List<UserProfile> result = new ArrayList<>();
        for (UserProfile user : users.values()) {
            if (filter.matches(user)) { result.add(user); }
        }
        return List.copyOf(result);
    }

    /**
     * Подбирает других пользователей с хотя бы одним общим интересом, включая знакомых.
     * @param userId исходный профиль
     * @param filter ограничения кандидатов
     * @return совпадения по убыванию их числа, затем по возрастанию ID
     * @throws IllegalArgumentException если ID неизвестен или фильтр отсутствует
     */
    public List<Recommendation> matchByInterests(int userId, ProfileFilter filter) {
        UserProfile source = getUser(userId);
        requireFilter(filter);
        List<Recommendation> result = new ArrayList<>();
        for (UserProfile candidate : users.values()) {
            if (candidate.getId() == userId || !filter.matches(candidate)) { continue; }
            Set<String> common = new TreeSet<>(source.getInterests());
            common.retainAll(candidate.getInterests());
            if (!common.isEmpty()) { result.add(new Recommendation(candidate, List.copyOf(common))); }
        }
        return ranked(result);
    }

    /**
     * Рекомендует незнакомых людей по общим связям; себя и прямых знакомых исключает.
     * @param userId исходный пользователь
     * @param filter ограничения кандидатов
     * @return кандидаты с хотя бы одним общим знакомым, по числу связей вниз и ID вверх
     * @throws IllegalArgumentException если ID неизвестен или фильтр отсутствует
     */
    public List<Recommendation> recommendPeople(int userId, ProfileFilter filter) {
        getUser(userId);
        requireFilter(filter);
        List<Recommendation> result = new ArrayList<>();
        for (UserProfile candidate : users.values()) {
            int candidateId = candidate.getId();
            if (candidateId == userId || graph.areConnected(userId, candidateId) || !filter.matches(candidate)) { continue; }
            List<String> reasons = new ArrayList<>();
            for (int mutualId : graph.getMutualFriends(userId, candidateId)) {
                reasons.add("#" + mutualId + " " + getUser(mutualId).getName());
            }
            if (!reasons.isEmpty()) { result.add(new Recommendation(candidate, reasons)); }
        }
        return ranked(result);
    }

    /**
     * Предоставляет журнал успешных изменений.
     * @return неизменяемый снимок событий в порядке выполнения
     */
    public List<AppEvent> getEventLog() { return List.copyOf(eventLog); }

    private List<UserProfile> profilesFor(Set<Integer> ids) {
        List<UserProfile> result = new ArrayList<>();
        for (int id : ids) { result.add(getUser(id)); }
        return List.copyOf(result);
    }

    private List<Recommendation> ranked(List<Recommendation> results) {
        results.sort(Comparator.comparingInt(Recommendation::getScore).reversed()
                .thenComparingInt(result -> result.getUser().getId()));
        return List.copyOf(results);
    }

    private void requireFilter(ProfileFilter filter) {
        if (filter == null) { throw new IllegalArgumentException("Нужен фильтр; для всех профилей используйте ProfileFilter.all()."); }
    }

    private void log(String message) { eventLog.add(new AppEvent((long) eventLog.size() + 1, message)); }
}
