package dating.model;

/** Лайк определяется парой «пользователь — фотография». */
public final class Like {
    private final int userId;
    private final int photoId;

    /**
     * Создаёт лайк.
     * @param userId ID пользователя
     * @param photoId ID фотографии
     */
    public Like(int userId, int photoId) {
        if (userId <= 0 || photoId <= 0) {
            throw new IllegalArgumentException(
                    "ID лайка должны быть положительными."
            );
        }

        this.userId = userId;
        this.photoId = photoId;
    }

    /** Возвращает ID пользователя. */
    public int getUserId() {
        return userId;
    }

    /** Возвращает ID фотографии. */
    public int getPhotoId() {
        return photoId;
    }

    /** Сравнивает лайки по пользователю и фотографии. */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof Like like)) {
            return false;
        }

        return userId == like.userId && photoId == like.photoId;
    }

    /** Возвращает хеш-код лайка. */
    @Override
    public int hashCode() {
        return 31 * userId + photoId;
    }
}
