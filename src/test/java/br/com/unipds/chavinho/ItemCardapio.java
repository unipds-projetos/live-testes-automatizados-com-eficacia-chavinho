package br.com.unipds.chavinho;

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
