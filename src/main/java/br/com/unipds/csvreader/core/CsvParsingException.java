package br.com.unipds.csvreader.core;

public class CsvParsingException extends RuntimeException {

  public CsvParsingException(String message) {
    super(message);
  }

  public CsvParsingException(String message, Throwable cause) {
    super(message, cause);
  }
}