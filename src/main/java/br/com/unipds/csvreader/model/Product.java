package br.com.unipds.csvreader.model;

import br.com.unipds.csvreader.core.CsvRecordReader;

import java.util.function.Consumer;

public record Product(
        long index,
        String name,
        String description,
        String brand,
        String category,
        double price,
        String currency,
        int stock,
        String ean,
        String color,
        String size,
        String availability,
        String internalId
) {

    public static void processarCsv(String caminhoArquivo, Consumer<Product> processador) {
        CsvRecordReader.builder()
                .withHeader(true)
                .build()
                .process(caminhoArquivo, Product.class, processador);
    }
}