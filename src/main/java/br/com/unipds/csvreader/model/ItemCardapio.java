package br.com.unipds.csvreader.model;

import br.com.unipds.csvreader.core.CsvRecordReader;

import java.util.List;

public record ItemCardapio(
        long id,
        String nome,
        String descricao,
        double preco,
        String categoria,
        boolean emPromocao,
        double precoComDesconto,
        boolean impostoIsento) {

    public static List<ItemCardapio> lerCsv(String caminhoArquivo) {
        return new CsvRecordReader().read(caminhoArquivo, true, ItemCardapio.class);
    }
}