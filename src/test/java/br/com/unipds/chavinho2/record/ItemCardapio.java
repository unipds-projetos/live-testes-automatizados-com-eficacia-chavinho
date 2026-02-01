package br.com.unipds.chavinho2.record;

public record ItemCardapio(
    long id,
    String nome,
    String descricao,
    double preco,
    String categoria,
    boolean emPromocao,
    double precoComDesconto,
    boolean impostoIsento) {
}
