// src/Main.java
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {
    private static final String DB_FILE = "db.txt";
    private static final int ID_LENGTH = 6;
    private static final Random RANDOM = new Random();
    private static final Scanner SCANNER = new Scanner(System.in);

    private static final List<EmployeeImpl> employees = new ArrayList<>();

    public static void main(String[] args) {
        loadEmployees();
        printWelcome();

        while (true) {
            System.out.print("\n> ");
            String input = SCANNER.nextLine().trim();
            if (input.isEmpty()) continue;

            String[] parts = input.split("\\s+", 2);
            String command = parts[0].toLowerCase();

            try {
                if ("add".equals(command)) {
                    handleAdd(extractArgs(parts, 4, "add <ФИО> <Дата Рож.> <должность> <зарплата>"));
                } else if ("edit".equals(command)) {
                    handleEdit(extractArgs(parts, 5, "edit <id> <ФИО> <Дата Рож.> <должность> <зарплата>"));
                } else if ("fire".equals(command)) {
                    handleFire(extractId(parts, "fire <id>"));
                } else if ("filter".equals(command)) {
                    handleFilter(extractArg(parts, "filter (name|salary|position)"));
                } else if ("find".equals(command)) {
                    handleFind(extractArg(parts, "find <ФИО>"));
                } else if ("exit".equals(command)) {
                    System.out.println("Выход из программы.");
                    return;
                } else if ("help".equals(command)) {
                    printHelp();
                } else {
                    System.out.println("Неизвестная команда. Введите 'help'.");
                }
            } catch (Exception e) {
                System.out.println("Ошибка: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private static void printWelcome() {
        System.out.println("=== СИСТЕМА УЧЁТА СОТРУДНИКОВ ===");
        printEmployees(employees);
        printHelp();
    }

    private static void printHelp() {
        System.out.println("\nКоманды:");
        System.out.println("  add <ФИО> <Дата Рож.> <должность> <зарплата>       — добавить");
        System.out.println("  edit <id> <ФИО> <Дата Рож.> <должность> <зарплата>  — редактировать");
        System.out.println("  fire <id>                              — уволить");
        System.out.println("  filter (name|salary|position)          — сортировка");
        System.out.println("  find <ФИО>                             — поиск");
        System.out.println("  help                                   — помощь");
        System.out.println("  exit                                   — выход");
    }

    // === Загрузка и сохранение ===

    private static void loadEmployees() {
        employees.clear();
        Path path = Paths.get(DB_FILE);

        if (!Files.exists(path)) {
            try {
                Files.createFile(path);
            } catch (IOException e) {
                System.err.println("Не удалось создать db.txt: " + e.getMessage());
            }
            return;
        }

        try (Stream<String> lines = Files.lines(path)) {
            lines.filter(line -> !line.trim().isEmpty())
                    .map(Main::parseLine)
                    .filter(Objects::nonNull)
                    .forEach(employees::add);
        } catch (IOException e) {
            System.err.println("Ошибка чтения db.txt: " + e.getMessage());
        }
    }

    private static EmployeeImpl parseLine(String line) {
        String[] parts = line.split("\\|", 5);
        if (parts.length != 5) return null;
        try {
            return new EmployeeImpl(
                    parts[0].trim(),
                    parts[1].trim(),
                    parts[2].trim(),
                    parts[3].trim(),
                    Double.parseDouble(parts[4].trim())
            );
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static void saveEmployees() {
        try {
            Files.write(Paths.get(DB_FILE),
                    employees.stream()
                            .map(EmployeeImpl::toFileString)
                            .collect(Collectors.toList()),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            System.err.println("Ошибка сохранения: " + e.getMessage());
        }
    }

    // === Вывод ===

    // Утилита для создания строки-разделителя (Java 8)
    private static String buildLine() {
        char[] chars = new char[105];
        Arrays.fill(chars, '-');
        return new String(chars);
    }

    private static void printEmployees(List<EmployeeImpl> list) {
        if (list.isEmpty()) {
            System.out.println("Список сотрудников пуст.");
            return;
        }
        System.out.println("\nID     | ФИО                            | Дата рождения                  | Должность       | Зарплата");
        System.out.println(buildLine());  // <-- Исправлено
        list.forEach(emp -> System.out.println(emp.toString()));
    }

    // === Генерация ID ===

    private static String generateUniqueId() {
        Set<String> ids = employees.stream()
                .map(Employee::getId)
                .collect(Collectors.toSet());

        String id;
        do {
            id = String.format("%06d", RANDOM.nextInt(1_000_000));
        } while (ids.contains(id));
        return id;
    }

    // === Поиск ===

    private static EmployeeImpl findById(String id) {
        return employees.stream()
                .filter(e -> e.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    // === Обработчики команд ===

    private static void handleAdd(String[] args) {
        System.out.println(Arrays.toString(args));
        String fullName = args[0];
        String birthday = args[1];
        String position = args[2];
        double salary = Double.parseDouble(args[3]);

        String id = generateUniqueId();
        EmployeeImpl emp = new EmployeeImpl(id, fullName, birthday, position, salary);
        employees.add(emp);
        saveEmployees();
        System.out.println("Сотрудник добавлен: ID = " + id);
        printEmployees(employees);
    }

    private static void handleEdit(String[] args) {
        String id = args[0];
        EmployeeImpl emp = findById(id);
        if (emp == null) throw new IllegalArgumentException("Сотрудник с ID " + id + " не найден.");

        emp.setFullName(args[1]);
        emp.setBirthday(args[2]);
        emp.setPosition(args[3]);
        emp.setSalary(Double.parseDouble(args[4]));

        saveEmployees();
        System.out.println("Сотрудник " + id + " обновлён.");
        printEmployees(employees);
    }

    private static void handleFire(String id) {
        EmployeeImpl emp = findById(id);
        if (emp == null) throw new IllegalArgumentException("Сотрудник с ID " + id + " не найден.");

        employees.remove(emp);
        saveEmployees();
        System.out.println("Сотрудник " + id + " уволен.");
        printEmployees(employees);
    }

    private static void handleFilter(String criteria) {
        List<EmployeeImpl> sorted;

        String lower = criteria.toLowerCase();
        if ("name".equals(lower)) {
            sorted = employees.stream()
                    .sorted(Comparator.comparing(Employee::getFullName))
                    .collect(Collectors.toList());
        } else if ("salary".equals(lower)) {
            sorted = employees.stream()
                    .sorted(Comparator.comparingDouble(Employee::getSalary))
                    .collect(Collectors.toList());
        } else if ("position".equals(lower)) {
            sorted = employees.stream()
                    .sorted(Comparator.comparing(Employee::getPosition))
                    .collect(Collectors.toList());
        } else {
            throw new IllegalArgumentException("Фильтр: name, salary или position");
        }

        System.out.println("\nОтсортировано по " + criteria + ":");
        printEmployees(sorted);
    }

    private static void handleFind(String name) {
        Predicate<Employee> containsName = e ->
                e.getFullName().toLowerCase().contains(name.toLowerCase());

        List<EmployeeImpl> found = employees.stream()
                .filter(containsName)
                .collect(Collectors.toList());

        if (found.isEmpty()) {
            System.out.println("Сотрудники с именем '" + name + "' не найдены.");
        } else {
            System.out.println("\nНайдено по '" + name + "':");
            printEmployees(found);
        }
    }

    // === Утилиты для парсинга ===

    private static String[] extractArgs(String[] parts, int expected, String usage) {
        if (parts.length < 2) throw new IllegalArgumentException("Использование: " + usage);
        String[] args = parts[1].trim().split("\\s+", expected);
        if (args.length < expected) throw new IllegalArgumentException("Недостаточно аргументов: " + usage);
        return args;
    }

    private static String extractArg(String[] parts, String usage) {
        if (parts.length < 2) throw new IllegalArgumentException("Использование: " + usage);
        return parts[1].trim();
    }

    private static String extractId(String[] parts, String usage) {
        String id = extractArg(parts, usage);
        if (!id.matches("\\d{6}")) throw new IllegalArgumentException("ID должен быть 6 цифрами.");
        return id;
    }
}