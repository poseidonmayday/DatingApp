package dating.model;

/** Запись журнала с последовательным номером, без зависимости от системного времени. */
public final class AppEvent {
    private final long number;
    private final String message;

    /**
     * Создаёт событие.
     * @param number положительный порядковый номер
     * @param message непустое описание
     * @throws IllegalArgumentException если номер или описание некорректны
     */
    public AppEvent(long number, String message) {
        if (number <= 0) { throw new IllegalArgumentException("Номер события должен быть положительным."); }
        this.number = number;
        this.message = UserProfile.requireText(message, "Событие");
    }

    /**
     * Возвращает порядок события.
     * @return номер, начиная с единицы
     */
    public long getNumber() { return number; }

    /**
     * Возвращает описание.
     * @return сообщение о действии
     */
    public String getMessage() { return message; }

    /**
     * Формирует запись для отображения.
     * @return номер и описание события
     */
    @Override
    public String toString() { return "[" + number + "] " + message; }
}
