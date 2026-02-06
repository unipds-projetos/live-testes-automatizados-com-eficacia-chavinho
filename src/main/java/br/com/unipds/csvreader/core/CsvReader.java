package br.com.unipds.csvreader.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class CsvReader {

    private final boolean hasHeader;
    private final Character separator;
    private final CsvParser parser = new CsvParser();
    private final CsvMapper mapper = new CsvMapper();

    private CsvReader(boolean hasHeader, Character separator) {
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

        public CsvReader build() {
            return new CsvReader(hasHeader, separator);
        }
    }

    public List<Map<String, String>> read(String fileName) {
        List<Map<String, String>> result = new ArrayList<>();
        process(fileName, result::add);
        return result;
    }

    public <T> List<T> read(String fileName, CsvRowMapper<T> mapper) {
        List<Map<String, String>> dadosBrutos = read(fileName);
        List<T> listaTipada = new ArrayList<>();

        for (Map<String, String> linha : dadosBrutos) {
            T objeto = mapper.mapRow(linha);
            listaTipada.add(objeto);
        }
        return listaTipada;
    }

    public List<Map<String, String>> readString(String csvContent) {
        if (csvContent == null || csvContent.isBlank()) return List.of();

        char effectiveSeparator = (this.separator != null) ?
                this.separator : parser.detectSeparatorInLine(csvContent.lines().findFirst().orElse(""));

        List<Map<String, String>> result = new ArrayList<>();
        runPipeline(csvContent.lines(), effectiveSeparator, result::add);
        return result;
    }

    public void process(String fileName, Consumer<Map<String, String>> processor) {
        char effectiveSeparator = resolveSeparator(fileName);
        Path path = Paths.get(fileName);

        try (Stream<String> lines = Files.lines(path)) {
            runPipeline(lines, effectiveSeparator, processor);
        } catch (IOException e) {
            throw new CsvParsingException("Erro de IO ao processar arquivo: " + e.getMessage(), e);
        }
    }

    private void runPipeline(Stream<String> linesStream, char separator, Consumer<Map<String, String>> processor) {
        Iterator<String> iterator = linesStream.iterator();
        if (!iterator.hasNext()) return;

        List<String> headers = new ArrayList<>();

        if (this.hasHeader) {
            String headerLine = iterator.next();
            headers = sanitizeHeaders(parser.parseLine(headerLine, separator));
        }

        while (iterator.hasNext()) {
            String line = iterator.next();
            if (line.trim().isEmpty()) continue;

            List<String> values = parser.parseLine(line, separator);

            Map<String, String> rowMap = mapper.mapToMap(values, headers, this.hasHeader);

            processor.accept(rowMap);
        }
    }

    private List<String> sanitizeHeaders(List<String> rawHeaders) {
        List<String> cleaned = new ArrayList<>();
        for (String header : rawHeaders) {
            if (header == null) {
                cleaned.add(null);
                continue;
            }
            String s = header;
            s = s.replace("\uFEFF", "");
            s = s.replace("\"", "");
            s = s.trim();
            s = s.toLowerCase();
            cleaned.add(s);
        }
        return cleaned;
    }

    private char resolveSeparator(String fileName) {
        return (this.separator != null) ? this.separator : parser.detectSeparator(Paths.get(fileName));
    }
}