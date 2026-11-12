package dating.ui;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Проверка консольного интерфейса приложения знакомств.
 */
@Timeout(10)
class ConsoleUITest {

    /**
     * Запускает интерфейс с заранее заданным вводом
     * и возвращает весь текст, выведенный в консоль.
     */
    private String runConsole(String userInput, String... args) {
        ByteArrayOutputStream result = new ByteArrayOutputStream();

        try (Scanner scanner = new Scanner(userInput);
             PrintStream output = new PrintStream(
                     result, true, StandardCharsets.UTF_8
             )) {

            ConsoleUI ui = new ConsoleUI(scanner, output);
            ui.run(args);
        }

        return result.toString(StandardCharsets.UTF_8);
    }

    @Test
    void menuShowsUsers() {
        String result = runConsole("1\n0\n");

        assertTrue(result.contains("Список пользователей"));
        assertTrue(result.contains("#1 | Анна"));
        assertTrue(result.contains("#2 | Борис"));
        assertTrue(result.contains("#8 | Илья"));
    }

    @Test
    void canAddInterestAndViewProfile() {
        String result = runConsole(
                "12\n2\nлошади\n2\n2\n0\n"
        );

        assertTrue(result.contains("Интерес добавлен."));
        assertTrue(result.contains("#2 | Борис"));
        assertTrue(result.contains("лошади"));
    }

    @Test
    void canFindUsersByCommonInterests() {
        String result = runConsole(
                "3\n2\n19\n120\n\n0\n"
        );

        assertTrue(result.contains("Подбор по интересам"));
        assertTrue(result.contains("#3 | Вера"));
        assertTrue(result.contains("#7 | Жанна"));
        assertTrue(result.contains("Общих интересов: 2"));
    }

    @Test
    void invalidNumberDoesNotCrashMenu() {
        String result = runConsole(
                "2\nнечисло\n1\n0\n"
        );

        assertTrue(result.contains("Некорректное число"));
        assertTrue(result.contains("#1 | Анна"));
    }

    @Test
    void canRegisterUserAndFilterByCity() {
        String result = runConsole(
                "11\n9\nЛена\n24\nТула\n"
                        + "4\n18\n30\nТула\n0\n"
        );

        assertTrue(result.contains("Создан профиль #9"));
        assertTrue(result.contains("#9 | Лена | 24 | Тула"));
    }

    @Test
    void simulationWithSameSeedIsReproducible() {
        String first = runConsole("", "--simulate", "25");
        String second = runConsole("", "--simulate", "25");

        assertEquals(first, second);

        assertTrue(first.contains("seed=25"));
        assertTrue(first.contains("Итого: профилей 8"));
        assertTrue(first.contains("фото 12"));
        assertTrue(first.contains("знакомств 11"));
        assertTrue(first.contains("лайков 23"));
        assertTrue(first.contains("Демонстрация завершена."));
    }
}
