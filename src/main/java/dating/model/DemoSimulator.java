package dating.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** Создаёт конечный демонстрационный сценарий, используя один Random на прогон. */
public final class DemoSimulator {
    private final long seed;

    /**
     * Запоминает зерно демонстрации.
     * @param seed любое значение long
     */
    public DemoSimulator(long seed) {
        this.seed = seed;
    }

    /**
     * Возвращает зерно.
     * @return исходный seed
     */
    public long getSeed() {
        return seed;
    }

    /**
     * Создаёт новый сервис с восемью профилями, интересами,
     * контактами, фото, связями и лайками.
     * Повторный вызов с тем же seed создаёт тот же результат.
     * @return заполненный сервис без какого-либо вывода
     */
    public DatingService run() {
        Random random = new Random(seed);
        DatingService service = new DatingService();

        createProfiles(service, random);
        createConnections(service, random);
        createLikes(service, random);

        return service;
    }

    private void createProfiles(DatingService service, Random random) {
        String[] names = {
                "Анна", "Борис", "Вера", "Глеб",
                "Дарья", "Егор", "Жанна", "Илья"
        };

        String[] cities = {
                "Москва", "Казань", "Санкт-Петербург"
        };

        List<String> availableInterests = List.of(
                "музыка", "книги", "спорт",
                "кино", "путешествия", "программирование"
        );

        int photoId = 1;

        for (int i = 0; i < names.length; i++) {
            int userId = i + 1;

            service.registerUser(
                    userId,
                    names[i],
                    18 + random.nextInt(18),
                    cities[random.nextInt(cities.length)]
            );

            List<String> interests =
                    new ArrayList<>(availableInterests);

            Collections.shuffle(interests, random);

            int count = 2 + random.nextInt(3);

            for (int j = 0; j < count; j++) {
                service.addInterest(userId, interests.get(j));
            }

            service.setSocialContact(
                    userId, "VK", "user_" + userId
            );

            service.setSocialContact(
                    userId, "Telegram", "@demo_" + userId
            );

            int photoCount = 1 + random.nextInt(2);

            for (int j = 1; j <= photoCount; j++) {
                service.addPhoto(
                        photoId++,
                        userId,
                        "photos/user_" + userId + "_" + j + ".jpg"
                );
            }
        }
    }

    private void createConnections(DatingService service, Random random) {
        int[][] baseConnections = {
                {1, 2}, {1, 3}, {2, 4}, {3, 4},
                {3, 5}, {5, 6}, {6, 7}, {7, 8}
        };

        for (int[] pair : baseConnections) {
            service.addAcquaintance(pair[0], pair[1]);
        }

        // Сохраняем для первого профиля пример
        // незнакомого человека с двумя общими знакомыми.
        for (int first = 2; first <= 8; first++) {
            for (int second = first + 1; second <= 8; second++) {
                if (random.nextInt(5) == 0) {
                    service.addAcquaintance(first, second);
                }
            }
        }
    }

    private void createLikes(DatingService service, Random random) {
        service.likePhoto(
                1,
                service.getPhotos(2).getFirst().getId()
        );

        for (UserProfile owner : service.getUsers()) {
            for (Photo photo : service.getPhotos(owner.getId())) {
                for (UserProfile author : service.getUsers()) {
                    int userId = author.getId();

                    if (userId != owner.getId()
                            && random.nextInt(4) == 0
                            && !service.hasLiked(userId, photo.getId())) {
                        service.likePhoto(userId, photo.getId());
                    }
                }
            }
        }
    }
}
