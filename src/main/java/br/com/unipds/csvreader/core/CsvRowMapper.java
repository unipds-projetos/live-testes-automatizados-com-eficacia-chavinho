package br.com.unipds.csvreader.core;

import java.util.Map;

public interface CsvRowMapper<T> {
    T mapRow(Map<String, String> row);
}