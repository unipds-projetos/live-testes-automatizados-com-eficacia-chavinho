package br.com.unipds.chavinho2;

public class Chavinho2 {

  public final String separador;
  public final boolean temCabecalho;

  private Chavinho2(Builder builder) {
    this.separador = builder.separador;
    this.temCabecalho = builder.temCabecalho;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private String separador = ",";
    private boolean temCabecalho = false;

    public Builder separador(String separador) {
      this.separador = separador;
      return this;
    }

    public Builder temCabecalho(boolean temCabecalho) {
      this.temCabecalho = temCabecalho;
      return this;
    }

    public Chavinho2 build() {
      return new Chavinho2(this);
    }

  }

}
