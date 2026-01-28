package br.com.unipds.chavinho;

import javax.swing.*;

public class ChavinhoException extends RuntimeException {
  public ChavinhoException(Throwable throwable) {
    super(throwable);
  }

  public ChavinhoException(String message) {
    super(message);
  }
}
