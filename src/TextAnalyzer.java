import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TextAnalyzer {
    // Блокирующие очереди для каждого потока-анализатора
    private static final BlockingQueue<String> queueA = new ArrayBlockingQueue<>(100);
    private static final BlockingQueue<String> queueB = new ArrayBlockingQueue<>(100);
    private static final BlockingQueue<String> queueC = new ArrayBlockingQueue<>(100);

    // Параметры
    private static final int TEXT_COUNT = 10_000;
    private static final int TEXT_LENGTH = 100_000;
    private static final Random random = new Random();

    public static void main(String[] args) {
        // Создаем пул потоков
        ExecutorService executor = Executors.newFixedThreadPool(4);

        // Запускаем поток-генератор
        executor.submit(() -> generateTexts());

        // Запускаем потоки-анализаторы для каждого символа
        executor.submit(() -> analyzeQueue(queueA, 'a'));
        executor.submit(() -> analyzeQueue(queueB, 'b'));
        executor.submit(() -> analyzeQueue(queueC, 'c'));

        // Завершаем работу пула после выполнения задач
        executor.shutdown();
    }

    // Генерация текстов и помещение в очереди
    private static void generateTexts() {
        try {
            for (int i = 0; i < TEXT_COUNT; i++) {
                String text = generateRandomText();
                // Помещаем одну и ту же строку во все очереди
                queueA.put(text);
                queueB.put(text);
                queueC.put(text);
            }
            // Добавляем сигнал завершения (null) в очереди
            queueA.put("");
            queueB.put("");
            queueC.put("");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // Генерация случайного текста из символов 'a', 'b', 'c'
    private static String generateRandomText() {
        StringBuilder sb = new StringBuilder(TEXT_LENGTH);
        for (int i = 0; i < TEXT_LENGTH; i++) {
            sb.append((char) ('a' + random.nextInt(3)));
        }
        return sb.toString();
    }

    // Анализ очереди для конкретного символа
    private static void analyzeQueue(BlockingQueue<String> queue, char targetChar) {
        try {
            String maxText = null;
            long maxCount = -1;

            while (true) {
                String text = queue.take(); // Берем строку из очереди
                if (text.isEmpty()) break; // Сигнал завершения

                long count = text.chars().filter(ch -> ch == targetChar).count();
                if (count > maxCount) {
                    maxCount = count;
                    maxText = text;
                }
            }

            // Вывод результатов
            System.out.printf("Максимальное количество '%c': %d%n", targetChar, maxCount);
            System.out.printf("Текст с максимумом '%c': %s... (первые 50 символов)%n",
                    targetChar, maxText.substring(0, Math.min(50, maxText.length())));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}