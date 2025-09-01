package com.javarush.martynov;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

class CaesarCipherApp {
    private final String ALPHABET = "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯабвгдеёжзийклмнопрстуфхцчшщъыьэюя .,!?";

    void start() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("=*=*= Caesar Cipher Application =*=*=");

        while (true) {
            System.out.println("\nВыберите режим работы:");
            System.out.println("1 - Шифрование файла");
            System.out.println("2 - Расшифровка файла по ключу");
            System.out.println("3 - Расшифровка методом \"Brute Force\"");
            System.out.println("4 - Расшифровка методом \"Brute Force\" со статистическим анализом");
            System.out.println("0 - Выход");

            System.out.print("Ваш выбор: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    encryptMode(scanner);
                    break;
                case "2":
                    decryptMode(scanner);
                    break;
                case "3":
                    bruteForceMode(scanner);
                    break;
                case "4":
                    statisticalAnalysisMode(scanner);
                    break;
                case "0":
                    System.out.println("Выход из программы.");
                    scanner.close();
                    return;
                default:
                    System.out.println("Некорректный выбор. Попробуйте снова.");
            }
        }
    }

    /**
     * Шифрование файла.
     *
     * @getInputFile запрашиваем у пользователя путь к файлу
     * @getOutputFile сохраняем зашифрованный файл по указанному пути от пользователя
     */

    private void encryptMode(Scanner scanner) {
        System.out.println("\n=== Шифрование файла ===");
        File inputFile = getInputFile(scanner);
        if (inputFile == null) return;

        int key = getKey(scanner);
        if (key == -1) return;

        File outputFile = getOutputFile(scanner);

        try {
            processFile(inputFile, outputFile, key, true);
            System.out.println("Файл успешно зашифрован и сохранён в: " + outputFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Ошибка при обработке файлов: " + e.getMessage());
        }
    }

    /**
     * Расшифровка файла по ключу.
     *
     * @param scanner пользователь выбирает ключ
     * @getInputFile запрашиваем у пользователя путь к файлу
     * @getOutputFile сохраняем зашифрованный файл по указанному пути от пользователя
     */

    private void decryptMode(Scanner scanner) {
        System.out.println("\n=== Расшифровка файла по ключу ===");
        File inputFile = getInputFile(scanner);
        if (inputFile == null) return;

        int key = getKey(scanner);
        if (key == -1) return;

        File outputFile = getOutputFile(scanner);

        try {
            processFile(inputFile, outputFile, key, false);
            System.out.println("Файл успешно расшифрован и сохранён в: " + outputFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Ошибка при обработке файлов: " + e.getMessage());
        }
    }

    /**
     * Выполняет расшифровку файла методом brute force (полного перебора ключей).
     * Метод считывает содержимое файла, перебирает все возможные ключи шифра Цезаря
     * и выводит на экран фрагменты расшифрованного текста, чтобы пользователь
     * мог визуально определить правильный ключ. После этого запрашивает
     * у пользователя выбор ключа для сохранения полного файла.
     *
     * @param scanner для получения ввода от пользователя.
     *                Используется для запроса пути к файлу и выбора ключа.
     */

    private void bruteForceMode(Scanner scanner) {
        System.out.println("\n=== Расшифровка методом brute force ===");
        File inputFile = getInputFile(scanner);
        if (inputFile == null) return;

        try {
            // Читаем весь файл в память (можно оптимизировать для очень больших файлов)
            String content = Files.readString(inputFile.toPath(), StandardCharsets.UTF_8);

            System.out.println("Пример расшифровки для всех ключей:");

            for (int key = 1; key < ALPHABET.length(); key++) {
                String decoded = caesarCipher(content, key, false);
                System.out.println("\nКлюч: " + key);
                System.out.println("-----");
                // Выводим первые 300 символов для примера
                System.out.println(decoded.length() > 300 ? decoded.substring(0, 300) + "..." : decoded);
            }

            System.out.println("\nВведите ключ для сохранения расшифрованного файла (0 для отмены):");
            int chosenKey;
            do {
                System.out.print("Ключ: ");
                String input = scanner.nextLine();
                try {
                    chosenKey = Integer.parseInt(input);
                } catch (NumberFormatException e) {
                    System.out.println("Введите корректное число.");
                    chosenKey = -1;
                }
            } while (chosenKey < 0 || chosenKey >= ALPHABET.length());

            if (chosenKey == 0) {
                System.out.println("Операция отменена.");
                return;
            }

            File outputFile = getOutputFile(scanner);
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(outputFile),
                    StandardCharsets.UTF_8))) {
                writer.write(caesarCipher(content, chosenKey, false));
                System.out.println("Файл успешно расшифрован и сохранён: " + outputFile.getAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("Ошибка при чтении файла: " + e.getMessage());
        }
    }

    /**
     * Запрашивает у пользователя путь к исходному файлу и возвращает объект File.
     * Метод выполняет проверку: если файл по указанному пути не существует
     * или является не файлом (например, это каталог), выводит сообщение об ошибке
     * и возвращает null. В противном случае возвращает валидный объект файла.
     *
     * @param scanner для считывания ввода пользователя из консоли.
     * @return File, если файл существует и является файлом,
     * иначе возвращает null.
     */

    private File getInputFile(Scanner scanner) {
        System.out.print("Введите путь к исходному файлу: ");
        String path = scanner.nextLine();
        File file = new File(path);
        if (!file.exists() || !file.isFile()) {
            System.out.println("Файл не существует или не является файлом!");
            return null;
        }
        return file;
    }

    /**
     * Запрашивает у пользователя путь для сохранения результата и возвращает
     * объект File.
     * Метод проверяет, что файл по указанному пути уже существует и является файлом.
     * Если это условие не выполняется, выводит сообщение об ошибке и возвращает null.
     * Примечание: Эта реализация не позволяет сохранить результат в новый файл,
     * так как требует, чтобы файл уже существовал на диске.
     *
     * @param scanner для считывания ввода пользователя.
     * @return File если файл уже существует и является файлом,
     * иначе возвращает null.
     */

    private File getOutputFile(Scanner scanner) {
        System.out.print("Введите путь для сохранения результата: ");
        String path = scanner.nextLine();
        File file = new File(path);
        if (!file.exists() || !file.isFile()) {
            System.out.println("Файл не существует или не является файлом!");
            return null;
        }
        return file;
    }

    /**
     * Запрашивает у пользователя целочисленный ключ для шифрования или расшифровки
     * и выполняет его валидацию.
     * Метод в цикле запрашивает ввод, пока пользователь не введет корректное
     * число в заданном диапазоне. Обрабатывает ошибки ввода (например, если
     * пользователь ввел не число) и сообщает о них.
     *
     * @param scanner используемый для считывания ввода пользователя из консоли.
     * @return Валидный целочисленный ключ в диапазоне от 1 до длины алфавита - 1.
     */

    private int getKey(Scanner scanner) {
        int maxKey = ALPHABET.length() - 1;
        int key = -1;
        do {
            System.out.print("Введите ключ (целое число от 1 до " + maxKey + "): ");
            String input = scanner.nextLine();
            try {
                key = Integer.parseInt(input);
                if (key < 1 || key > maxKey) {
                    System.out.println("Ключ вне допустимого диапазона.");
                    key = -1;
                }
            } catch (NumberFormatException e) {
                System.out.println("Введите корректное число.");
            }
        } while (key == -1);
        return key;
    }

    /**
     * Обрабатывает файл, выполняя шифрование или расшифровку методом шифра Цезаря.
     * Метод читает исходный файл построчно, применяет к каждой строке шифр
     * с заданным ключом и записывает результат в выходной файл.
     * Для надежной работы с текстовыми данными, метод использует
     * кодировку StandardCharsets.UTF_8.
     *
     * @param inputFile  Объект представляющий исходный файл.
     *                   Должен быть валидным, существующим файлом.
     * @param outputFile Объект представляющий выходной файл.
     *                   В него будет записан результат.
     * @param key        Целочисленный ключ шифрования.
     * @param encrypt    Логическое значение: true, если нужно выполнить шифрование;
     *                   false, если требуется расшифровка.
     * @throws IOException Если произошла ошибка ввода-вывода при чтении или записи файлов.
     */

    private void processFile(File inputFile, File outputFile, int key, boolean encrypt) throws IOException {
        // Буфер для чтения
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), StandardCharsets.UTF_8));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                String transformed = caesarCipher(line, key, encrypt);
                writer.write(transformed);
                writer.newLine();
            }
        }
    }

    /**
     * Функция шифрования или расшифровки строки с помощью шифра Цезаря.
     *
     * @param text    Входной текст
     * @param key     Ключ сдвига
     * @param encrypt true - шифрование, false - расшифровка
     * @return преобразованный текст
     */
    private String caesarCipher(String text, int key, boolean encrypt) {
        StringBuilder result = new StringBuilder();
        int n = ALPHABET.length();
        int shift = encrypt ? key : n - key;

        for (char ch : text.toCharArray()) {
            int idx = ALPHABET.indexOf(ch);
            if (idx == -1) {
                // Если символ не найден в алфавите, оставляем его без изменений
                result.append(ch);
            } else {
                int newIndex = (idx + shift) % n;
                result.append(ALPHABET.charAt(newIndex));
            }
        }
        return result.toString();
    }

    /**
     * Выполняет автоматическую расшифровку файла методом статистического анализа.
     * Метод анализирует частоту встречаемости символов в зашифрованном тексте
     * и находит наиболее вероятные ключи шифрования. Он выводит на консоль
     * примеры расшифрованного текста для каждого из предложенных ключей,
     * после чего позволяет пользователю выбрать один из них для сохранения
     * полного файла.
     *
     * @param scanner используется для получения ввода от пользователя (путь к файлу, выбор ключа).
     */

    private void statisticalAnalysisMode(Scanner scanner) {
        System.out.println("\n=== Автоматическая расшифровка методом статистического анализа ===");
        File inputFile = getInputFile(scanner);
        if (inputFile == null) return;

        try {
            String content = Files.readString(inputFile.toPath(), StandardCharsets.UTF_8);
            List<Integer> candidates = statisticalAttack(content);

            System.out.println("Найденные вероятные ключи: " + candidates);
            System.out.println("Первые варианты расшифровки:");

            for (int key : candidates) {
                System.out.println("\nКлюч: " + key);
                System.out.println("------------------");
                String decoded = caesarCipher(content, key, false);
                System.out.println(decoded.length() > 300 ? decoded.substring(0, 300) + "..." : decoded);
            }

            System.out.print("Введите ключ для сохранения результата или 0 для отмены: ");
            int chosenKey = -1;
            while (true) {
                String input = scanner.nextLine();
                try {
                    chosenKey = Integer.parseInt(input);
                    if (chosenKey == 0) {
                        System.out.println("Операция отменена.");
                        return;
                    }
                    if (!candidates.contains(chosenKey)) {
                        System.out.println("Введите ключ из предложенного списка или 0:");
                    } else {
                        break;
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Введите корректное число.");
                }
            }

            File outputFile = getOutputFile(scanner);
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8))) {
                writer.write(caesarCipher(content, chosenKey, false));
                System.out.println("Файл успешно сохранён: " + outputFile.getAbsolutePath());
            }

        } catch (IOException e) {
            System.err.println("Ошибка при чтении файла: " + e.getMessage());
        }
    }

    /**
     * Карта, содержащая статистическую частотность букв русского алфавита.
     * Ключ — это буква (символ), а значение — её приблизительная частота
     * встречаемости в процентах.
     */

    private final Map<Character, Double> RUSSIAN_FREQ = createRussianFreqMap();

    private Map<Character, Double> createRussianFreqMap() {
        Map<Character, Double> freq = new HashMap<>();
        freq.put('О', 10.97);
        freq.put('Е', 8.45);
        freq.put('А', 8.01);
        freq.put('И', 7.35);
        freq.put('Н', 6.70);
        freq.put('Т', 6.26);
        freq.put('С', 5.47);
        freq.put('Р', 4.73);
        freq.put('В', 4.54);
        freq.put('Л', 4.40);
        freq.put('К', 3.49);
        freq.put('М', 3.21);
        freq.put('Д', 3.01);
        freq.put('П', 2.81);
        freq.put('У', 2.62);
        freq.put('Я', 2.01);
        freq.put('Ы', 1.95);
        freq.put('Ь', 1.74);
        freq.put('Г', 1.70);
        freq.put('З', 1.65);
        freq.put('Б', 1.59);
        freq.put('Ч', 1.44);
        freq.put('Й', 1.21);
        freq.put('Х', 0.96);
        freq.put('Ж', 0.94);
        freq.put('Ш', 0.73);
        freq.put('Ц', 0.48);
        freq.put('Ю', 0.47);
        freq.put('Э', 0.36);
        freq.put('Ф', 0.25);
        freq.put('Щ', 0.25);
        freq.put('Ъ', 0.04);
        freq.put('Ё', 0.04);

        return freq;
    }

    /**
     * Метод анализа частот букв текста.
     * Подсчитывает процентное соотношение букв А-Я.
     */
    private Map<Character, Double> calculateFrequency(String text) {
        Map<Character, Integer> counts = new HashMap<>();
        int total = 0;
        for (char c = 'А'; c <= 'Я'; c++) counts.put(c, 0);

        for (char ch : text.toUpperCase().toCharArray()) {
            if (counts.containsKey(ch)) {
                counts.put(ch, counts.get(ch) + 1);
                total++;
            }
        }

        Map<Character, Double> freq = new HashMap<>();
        if (total == 0) {
            for (char c = 'А'; c <= 'Я'; c++) freq.put(c, 0.0);
            return freq;
        }
        for (char c = 'А'; c <= 'Я'; c++) {
            freq.put(c, counts.get(c) * 100.0 / total);
        }
        return freq;
    }

    /**
     * Расчёт суммы квадратов разностей частот эталонных и текста.
     */
    private double frequencyScore(Map<Character, Double> freqText, Map<Character, Double> freqRussian) {
        double score = 0.0;
        for (char c = 'А'; c <= 'Я'; c++) {
            double diff = freqText.getOrDefault(c, 0.0) - freqRussian.getOrDefault(c, 0.0);
            score += diff * diff;
        }
        return score;
    }

    /**
     * Автоматическая расшифровка методом статистического анализа без ключа.
     * Возвращает список наиболее вероятных ключей.
     */
    private List<Integer> statisticalAttack(String ciphertext) {
        int n = ALPHABET.length();
        double bestScore = Double.MAX_VALUE;
        List<Integer> bestKeys = new ArrayList<>();

        for (int key = 1; key < n; key++) {
            String decoded = caesarCipher(ciphertext, key, false);
            Map<Character, Double> freq = calculateFrequency(decoded);
            double score = frequencyScore(freq, RUSSIAN_FREQ);

            if (score < bestScore) {
                bestScore = score;
                bestKeys.clear();
                bestKeys.add(key);
            } else if (Math.abs(score - bestScore) < 1e-6) {
                bestKeys.add(key);
            }
        }
        return bestKeys;
    }
}

