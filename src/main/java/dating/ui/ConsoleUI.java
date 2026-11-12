
package dating.ui;

import dating.model.AppEvent;
import dating.model.DatingService;
import dating.model.DemoSimulator;
import dating.model.Like;
import dating.model.Photo;
import dating.model.ProfileFilter;
import dating.model.Recommendation;
import dating.model.UserProfile;

import java.io.PrintStream;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

/**
 * Консольное меню приложения.
 * Все проверки данных выполняются в модели.
 */
public final class ConsoleUI {
    private static final long DEFAULT_SEED = 25;

    private final Scanner input;
    private final PrintStream output;
    private DatingService service;
    private boolean inputEnded;

    /** Создаёт меню с демонстрационными данными. */
    public ConsoleUI(Scanner input, PrintStream output) {
        this(input, output, new DemoSimulator(DEFAULT_SEED).run());
    }

    /** Создаёт меню для переданного сервиса. */
    public ConsoleUI(
            Scanner input,
            PrintStream output,
            DatingService service
    ) {
        this.input = Objects.requireNonNull(input);
        this.output = Objects.requireNonNull(output);
        this.service = Objects.requireNonNull(service);
    }

    /**
     * Запускает меню или автоматическую демонстрацию.
     * @param args параметры запуска
     */
    public void run(String[] args) {
        if (args.length == 0) {
            runMenu();
            return;
        }

        if (args.length == 1 && args[0].equals("--help")) {
            printUsage();
            return;
        }

        if (!args[0].equals("--simulate") || args.length > 2) {
            printUsage();
            return;
        }

        try {
            runDemo(
                    args.length == 2
                            ? Long.parseLong(args[1])
                            : DEFAULT_SEED
            );
        } catch (NumberFormatException exception) {
            output.println(
                    "Ошибка параметров: seed должен быть целым числом в диапазоне long."
            );
            printUsage();
        }
    }

    private void runMenu() {
        output.println(
                "Знакомства. Загружено профилей: "
                        + service.getUsers().size()
        );

        while (!inputEnded) {
            output.println("""

                    1. Список пользователей
                    2. Подробный профиль, фото и контакты
                    3. Подбор по общим интересам
                    4. Фильтр по возрасту и городу
                    5. Добавить знакомство
                    6. Лайкнуть фотографию
                    7. Общие знакомые
                    8. Вы можете знать
                    9. Журнал событий
                    10. Новая автоматическая демонстрация с seed
                    11. Создать профиль
                    12. Добавить интерес
                    13. Добавить фотографию
                    14. Указать контакт социальной сети
                    0. Выход
                    """);

            String choice = readLine();

            if (choice == null || choice.equals("0")) {
                return;
            }

            try {
                switch (choice) {
                    case "1" -> printUsers(service.getUsers());
                    case "2" -> showProfile(readInt("ID пользователя:"));
                    case "3" -> findMatches();
                    case "4" -> printUsers(service.filterUsers(readFilter()));
                    case "5" -> addAcquaintance();
                    case "6" -> addLike();
                    case "7" -> showMutualFriends();
                    case "8" -> findSuggested();
                    case "9" -> printJournal();
                    case "10" -> newDemo();
                    case "11" -> registerUser();
                    case "12" -> addInterest();
                    case "13" -> addPhoto();
                    case "14" -> setContact();
                    default -> output.println(
                            "Введите номер пункта от 0 до 14."
                    );
                }
            } catch (NumberFormatException exception) {
                output.println(
                        "Некорректное число. Действие отменено, выберите пункт меню снова."
                );
            } catch (IllegalArgumentException
                     | IllegalStateException exception) {
                if (!inputEnded) {
                    output.println("Ошибка: " + exception.getMessage());
                }
            }
        }
    }

    private void registerUser() {
        int id = readInt("Новый ID:");
        String name = readValue("Имя:");
        int age = readInt("Возраст (18–120):");
        String city = readValue("Город:");

        UserProfile user = service.registerUser(
                id, name, age, city
        );

        output.println("Создан профиль #" + user.getId());
    }

    private void addInterest() {
        int id = readInt("ID пользователя:");
        boolean added = service.addInterest(
                id, readValue("Интерес:")
        );

        output.println(
                added ? "Интерес добавлен." : "Такой интерес уже есть."
        );
    }

    private void addPhoto() {
        int photoId = readInt("Новый ID фотографии:");
        int ownerId = readInt("ID владельца:");

        service.addPhoto(
                photoId,
                ownerId,
                readValue("Строковый путь к фотографии:")
        );

        output.println("Фотография добавлена.");
    }

    private void setContact() {
        int userId = readInt("ID пользователя:");
        String network = readValue("Социальная сеть:");

        boolean changed = service.setSocialContact(
                userId,
                network,
                readValue("Адрес или имя аккаунта:")
        );

        output.println(
                changed ? "Контакт сохранён."
                        : "Этот контакт уже сохранён."
        );
    }

    private void addAcquaintance() {
        int first = readInt("ID первого пользователя:");
        int second = readInt("ID второго пользователя:");

        boolean added = service.addAcquaintance(first, second);

        output.println(
                added ? "Двустороннее знакомство добавлено."
                        : "Эти пользователи уже знакомы."
        );
    }

    private void addLike() {
        int author = readInt("ID автора лайка:");
        int photoId = readInt("ID фотографии:");

        service.likePhoto(author, photoId);

        output.println(
                "Лайк добавлен. Всего у фото: "
                        + service.getLikes(photoId).size()
        );
    }

    private void showMutualFriends() {
        int first = readInt("ID первого пользователя:");
        int second = readInt("ID второго пользователя:");

        output.println("Общие знакомые:");
        printUsers(service.getMutualFriends(first, second));
    }

    private void findMatches() {
        int id = readInt(
                "ID пользователя, по интересам которого ищем:"
        );

        ProfileFilter filter = readFilter();

        printRecommendations(
                "Подбор по интересам",
                service.matchByInterests(id, filter),
                "Общих интересов"
        );
    }

    private void findSuggested() {
        int id = readInt(
                "Для какого ID показать рекомендации:"
        );

        ProfileFilter filter = readFilter();

        printRecommendations(
                "Вы можете знать",
                service.recommendPeople(id, filter),
                "Общих знакомых"
        );
    }

    private ProfileFilter readFilter() {
        int min = readIntOrDefault(
                "Минимальный возраст (Enter = 18):", 18
        );

        int max = readIntOrDefault(
                "Максимальный возраст (Enter = 120):", 120
        );

        String city = readValue("Город (Enter = любой):");

        return new ProfileFilter(min, max, city);
    }

    private void showProfile(int userId) {
        UserProfile user = service.getUser(userId);

        printUsers(List.of(user));

        output.println("Интересы: " + user.getInterests());
        output.println("Контакты: " + user.getSocialContacts());

        output.println("Знакомые:");
        printUsers(service.getFriends(userId));

        output.println("Фотографии:");

        List<Photo> photos = service.getPhotos(userId);

        if (photos.isEmpty()) {
            output.println("Фотографий пока нет.");
        }

        for (Photo photo : photos) {
            List<Like> likes = service.getLikes(photo.getId());

            output.print(
                    "Фото #" + photo.getId()
                            + " | " + photo.getPath()
                            + " | лайков: " + likes.size()
                            + " | авторы:"
            );

            for (Like like : likes) {
                output.print(" #" + like.getUserId());
            }

            output.println();
        }
    }

    private void printUsers(List<UserProfile> users) {
        if (users.isEmpty()) {
            output.println("Пользователи не найдены.");
        }

        for (UserProfile user : users) {
            output.println(
                    "#" + user.getId()
                            + " | " + user.getName()
                            + " | " + user.getAge()
                            + " | " + user.getCity()
            );
        }
    }

    private void printRecommendations(
            String title,
            List<Recommendation> recommendations,
            String scoreLabel
    ) {
        output.println(title + ":");

        if (recommendations.isEmpty()) {
            output.println("Подходящих кандидатов нет.");
        }

        for (Recommendation recommendation : recommendations) {
            printUsers(List.of(recommendation.getUser()));

            output.println(
                    scoreLabel + ": "
                            + recommendation.getScore()
                            + " | " + recommendation.getReasons()
            );
        }
    }

    private void printJournal() {
        output.println("Журнал событий:");

        if (service.getEventLog().isEmpty()) {
            output.println("Событий пока нет.");
        }

        for (AppEvent event : service.getEventLog()) {
            output.println(event);
        }
    }

    private void newDemo() {
        output.println(
                "Будет создана новая демонстрация взамен текущих данных."
        );

        String value = readValue("Seed (Enter = 25):");

        runDemo(
                value.isEmpty()
                        ? DEFAULT_SEED
                        : Long.parseLong(value)
        );
    }

    private void runDemo(long seed) {
        service = new DemoSimulator(seed).run();

        output.println(
                "=== Автоматическая демонстрация, seed="
                        + seed + " ==="
        );

        printJournal();

        output.println(
                "\nПрофили, интересы, контакты, связи и фото:"
        );

        int photoCount = 0;
        int likeCount = 0;
        int directedConnections = 0;

        for (UserProfile user : service.getUsers()) {
            showProfile(user.getId());

            photoCount += service.getPhotos(user.getId()).size();

            directedConnections +=
                    service.getFriends(user.getId()).size();

            for (Photo photo : service.getPhotos(user.getId())) {
                likeCount +=
                        service.getLikes(photo.getId()).size();
            }
        }

        printRecommendations(
                "Подбор по интересам для #1",
                service.matchByInterests(1, ProfileFilter.all()),
                "Общих интересов"
        );

        for (UserProfile user : service.getUsers()) {
            printRecommendations(
                    "Вы можете знать — для #" + user.getId(),
                    service.recommendPeople(
                            user.getId(), ProfileFilter.all()
                    ),
                    "Общих знакомых"
            );
        }

        output.println("Фильтр: Москва, возраст 18–30:");

        printUsers(
                service.filterUsers(
                        new ProfileFilter(18, 30, "Москва")
                )
        );

        output.println(
                "Итого: профилей " + service.getUsers().size()
                        + ", фото " + photoCount
                        + ", знакомств " + directedConnections / 2
                        + ", лайков " + likeCount + "."
        );

        output.println("Демонстрация завершена.");
    }

    private int readInt(String prompt) {
        return Integer.parseInt(readValue(prompt));
    }

    private int readIntOrDefault(
            String prompt, int defaultValue
    ) {
        String line = readValue(prompt);

        return line.isEmpty()
                ? defaultValue
                : Integer.parseInt(line);
    }

    private String readValue(String prompt) {
        output.println(prompt);

        String line = readLine();

        if (line == null) {
            throw new IllegalStateException("Ввод завершён.");
        }

        return line;
    }

    private String readLine() {
        if (!input.hasNextLine()) {
            inputEnded = true;
            return null;
        }

        return input.nextLine().strip();
    }

    private void printUsage() {
        output.println(
                "Запуск: java -jar target/DatingApp-1.0-SNAPSHOT.jar "
                        + "[--simulate [seed]]"
        );

        output.println(
                "Без аргументов — меню с данными seed=25; "
                        + "--help — справка."
        );
    }
}
