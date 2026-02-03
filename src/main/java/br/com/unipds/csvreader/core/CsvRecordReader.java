package br.com.unipds.csvreader.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

public class CsvRecordReader {

    private final boolean hasHeader;
    private final Character separator;

    private final CsvParser parser = new CsvParser();
    private final CsvMapper mapper = new CsvMapper();

    private CsvRecordReader(boolean hasHeader, Character separator) {
        this.hasHeader = hasHeader;
        this.separator = separator;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private boolean hasHeader = true;
        private Character separator = null;

        public Builder withHeader(boolean hasHeader) {
            this.hasHeader = hasHeader;
            return this;
        }

        public Builder withSeparator(char separator) {
            this.separator = separator;
            return this;
        }

        public CsvRecordReader build() {
            return new CsvRecordReader(hasHeader, separator);
        }
    }

    public <T> List<T> read(String fileName, Class<T> recordClass) {
        char effectiveSeparator = resolveSeparator(fileName);
        try (Stream<T> stream = buildStream(fileName, effectiveSeparator, recordClass)) {
            return stream.toList();
        }
    }

    public <T> void process(String fileName, Class<T> recordClass, Consumer<T> processor) {
        char effectiveSeparator = resolveSeparator(fileName);
        try (Stream<T> stream = buildStream(fileName, effectiveSeparator, recordClass)) {
            stream.forEach(processor);
        }
    }

    public <T> List<T> readString(String csvContent, Class<T> recordClass) {
        if (csvContent == null || csvContent.isBlank()) return List.of();

        char effectiveSeparator = (this.separator != null) ?
                this.separator : parser.detectSeparatorInLine(csvContent.lines().findFirst().orElse(""));

        Function<List<String>, T> recordMapper = mapper.createMapper(recordClass);

        Stream<String> lines = csvContent.lines();
        if (this.hasHeader) lines = lines.skip(1);

        return lines
                .filter(line -> !line.trim().isEmpty())
                .map(line -> parser.parseLine(line, effectiveSeparator))
                .map(recordMapper)
                .toList();
    }

    private char resolveSeparator(String fileName) {
        return (this.separator != null) ? this.separator : parser.detectSeparator(Paths.get(fileName));
    }

    private <T> Stream<T> buildStream(String fileName, char separator, Class<T> recordClass) {
        Path path = Paths.get(fileName);

        Function<List<String>, T> recordMapper = mapper.createMapper(recordClass);

        try {
            Stream<String> fileLines = Files.lines(path);
            if (this.hasHeader) {
                fileLines = fileLines.skip(1);
            }

            return fileLines
                    .filter(line -> !line.trim().isEmpty())
                    .map(line -> parser.parseLine(line, separator)) // Parser O(n)
                    .map(recordMapper);

        } catch (IOException e) {
            throw new CsvParsingException("Erro de IO: " + e.getMessage(), e);
        }
    }
}