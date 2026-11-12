package dating.model;

/** Неизменяемое описание фотографии; сам файл не загружается. */
public final class Photo {
    private final int id;
    private final int ownerId;
    private final String path;

    /**
     * Создаёт описание фотографии.
     * @param id положительный ID фотографии
     * @param ownerId положительный ID владельца
     * @param path непустой строковый путь
     * @throws IllegalArgumentException если данные некорректны
     */
    public Photo(int id, int ownerId, String path) {
        if (id <= 0 || ownerId <= 0) {
            throw new IllegalArgumentException(
                    "ID фотографии и владельца должны быть положительными."
            );
        }

        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException(
                    "Путь к фото не должен быть пустым."
            );
        }

        this.id = id;
        this.ownerId = ownerId;
        this.path = path.strip();
    }

    /** Возвращает ID фотографии. */
    public int getId() {
        return id;
    }

    /** Возвращает ID владельца. */
    public int getOwnerId() {
        return ownerId;
    }

    /** Возвращает путь к фотографии. */
    public String getPath() {
        return path;
    }
}
