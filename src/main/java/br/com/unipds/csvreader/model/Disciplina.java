package br.com.unipds.csvreader.model;

import br.com.unipds.csvreader.core.CsvRecordReader;

import java.util.List;

public record Disciplina(int numero, String nome) {

    public static List<Disciplina> lerCsv(String caminhoArquivo) {
        CsvRecordReader reader = CsvRecordReader.builder().withHeader(true).build();
        return reader.read(caminhoArquivo, Disciplina.class);
    }
}