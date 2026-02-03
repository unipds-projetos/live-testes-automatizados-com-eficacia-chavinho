package br.com.unipds.csvreader.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class CsvParser {

    ///
    /// ANÁLISE MANUAL (PARSER) COM MÁQUINA DE ESTADOS
    ///
    public List<String> parseLine(String line, char separator) {
        List<String> result = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '\"') {
                inQuotes = !inQuotes;
            } else if (c == separator && !inQuotes) {
                result.add(cleanQuotes(currentField.toString()));
                currentField.setLength(0);
            } else {
                currentField.append(c);
            }
        }
        result.add(cleanQuotes(currentField.toString()));
        return result;
    }

    private String cleanQuotes(String value) {
        if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }

    public char detectSeparator(Path path) {
        try (Stream<String> lines = Files.lines(path)) {
            return detectSeparatorInLine(lines.findFirst().orElse(""));
        } catch (IOException e) {
            return ',';
        }
    }

    public char detectSeparatorInLine(String line) {
        if (line == null || line.isEmpty()) return ',';
        int commas = line.replaceAll("[^,]", "").length();
        int semis = line.replaceAll("[^;]", "").length();
        return semis >= commas ? ';' : ',';
    }
}