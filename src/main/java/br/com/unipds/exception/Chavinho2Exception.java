package br.com.unipds.exception;

public class Chavinho2Exception extends RuntimeException {
  public Chavinho2Exception(Throwable throwable) {
    super(throwable);
  }

  public Chavinho2Exception(String message) {
    super(message);
  }
}
