package dating;

import dating.ui.ConsoleUI;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/** Точка входа в консольное приложение «Знакомства». */
public final class Main {

    private Main() {
    }

    /**
     * Запускает консольное приложение.
     * @param args аргументы командной строки
     */
    public static void main(String[] args) {
        ConsoleUI ui = new ConsoleUI(
                new Scanner(System.in, StandardCharsets.UTF_8),
                new PrintStream(System.out, true, StandardCharsets.UTF_8)
        );

        ui.run(args);
    }
}
