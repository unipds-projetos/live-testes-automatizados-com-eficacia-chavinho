package br.com.unipds.csvreader.core;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.RecordComponent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

public class CsvRecordReader {

    private static final Map<Class<?>, Function<String, Object>> CONVERTERS = Map.of(
            int.class, Integer::parseInt,
            long.class, Long::parseLong,
            boolean.class, Boolean::parseBoolean,
            String.class, s -> s,
            double.class, CsvRecordReader::parseDoubleSafe
    );

    /*
     *   Lê arquivo do disco e retorna Lista
    */
    public <T> List<T> read(String fileName, boolean hasHeader, Class<T> recordClass) {
        String separator = detectSeparator(Paths.get(fileName));
        try (Stream<T> stream = streamFromFile(fileName, separator, hasHeader, recordClass)) {
            return stream.toList();
        }
    }

    /*
     *   Lê String da memória e retorna Lista
     */
    public <T> List<T> readString(String csvContent, boolean hasHeader, Class<T> recordClass) {
        if (csvContent == null || csvContent.isBlank()) {
            return List.of();
        }

        String separator = detectSeparatorInLine(csvContent.lines().findFirst().orElse(""));

        try {
            Stream<String> lines = csvContent.lines();
            if (hasHeader) {
                lines = lines.skip(1);
            }

            return mapToRecordStream(lines, separator, recordClass).toList();
        } catch (Exception e) {
            throw new CsvParsingException("Erro ao processar String CSV: " + e.getMessage(), e);
        }
    }

    /*
    *   Processa arquivo grande (Memória Constante)
    */
    public <T> void process(String fileName, boolean hasHeader, Class<T> recordClass, Consumer<T> processor) {
        String separator = detectSeparator(Paths.get(fileName));
        try (Stream<T> stream = streamFromFile(fileName, separator, hasHeader, recordClass)) {
            stream.forEach(processor);
        }
    }

    private <T> Stream<T> streamFromFile(String fileName, String separator, boolean hasHeader, Class<T> recordClass) {
        Path path = Paths.get(fileName);
        try {
            Stream<String> fileLines = Files.lines(path);
            if (hasHeader) {
                fileLines = fileLines.skip(1);
            }
            return mapToRecordStream(fileLines, separator, recordClass);
        } catch (IOException e) {
            throw new CsvParsingException("Erro de IO ao ler arquivo: " + e.getMessage(), e);
        }
    }

    /*
    *   Transforma linhas de texto em Objetos
    */
    private <T> Stream<T> mapToRecordStream(Stream<String> lines, String separator, Class<T> recordClass) {
        try {
            RecordComponent[] components = recordClass.getRecordComponents();
            if (components == null) throw new CsvParsingException("A classe não é um Record: " + recordClass.getName());

            Class<?>[] types = Arrays.stream(components).map(RecordComponent::getType).toArray(Class<?>[]::new);
            Constructor<T> constructor = recordClass.getDeclaredConstructor(types);

            return lines
                    .filter(line -> !line.trim().isEmpty())
                    .map(line -> mapLine(line, separator, types, constructor));

        } catch (ReflectiveOperationException e) {
            throw new CsvParsingException("Erro ao preparar Reflection: " + e.getMessage(), e);
        }
    }

    /*
     *  Utiliza o separador, mas apenas se ele for seguido por um número par de aspas"
     *  Garante que sejam ignorados separadores que estão dentro de um texto com aspas.
     */
    private <T> T mapLine(String line, String separator, Class<?>[] types, Constructor<T> constructor) {
        try {

            String regex = separator + "(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";

            String[] cols = line.split(regex, -1);

            Object[] args = new Object[types.length];
            for (int i = 0; i < types.length; i++) {
                String value = (i < cols.length) ? cols[i] : null;

                if (value != null && value.startsWith("\"") && value.endsWith("\"")) {
                    value = value.substring(1, value.length() - 1);
                }

                args[i] = convert(types[i], value);
            }
            return constructor.newInstance(args);
        } catch (Exception e) {
            throw new CsvParsingException("Erro na linha: " + line, e);
        }
    }

    private String detectSeparator(Path path) {
        try (Stream<String> lines = Files.lines(path)) {
            return detectSeparatorInLine(lines.findFirst().orElse(""));
        } catch (IOException e) {
            return ",";
        }
    }

    private String detectSeparatorInLine(String line) {
        if (line.isEmpty()) return ",";
        int commas = line.replaceAll("[^,]", "").length();
        int semis = line.replaceAll("[^;]", "").length();
        return semis >= commas ? ";" : ",";
    }

    private Object convert(Class<?> type, String value) {
        Function<String, Object> converter = CONVERTERS.get(type);
        if (converter == null) throw new CsvParsingException("Tipo não suportado: " + type.getName());

        if (value == null) {
            if (type == int.class) return 0;
            if (type == long.class) return 0L;
            if (type == double.class) return 0.0;
            if (type == boolean.class) return false;
            return null;
        }

        return converter.apply(value);
    }

    private static Double parseDoubleSafe(String value) {
        return (value != null && !value.isEmpty()) ? Double.parseDouble(value) : 0.0;
    }
}