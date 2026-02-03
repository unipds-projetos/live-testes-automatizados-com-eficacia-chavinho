package br.com.unipds.csvreader.core;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CsvMapper {

    /// -------------------------------------------------------------
    /// Transforma uma lista bruta de valores em um Mapa estruturado.
    /// -------------------------------------------------------------
    public Map<String, String> mapToMap(List<String> values, List<String> headers, boolean hasHeader) {
        Map<String, String> rowMap = new LinkedHashMap<>();

        if (hasHeader && !headers.isEmpty()) {
            for (int i = 0; i < headers.size(); i++) {
                String key = headers.get(i).trim();
                String value = (i < values.size()) ? values.get(i) : null;
                rowMap.put(key, value);
            }
        } else {
            for (int i = 0; i < values.size(); i++) {
                rowMap.put(String.valueOf(i), values.get(i));
            }
        }
        return rowMap;
    }
}