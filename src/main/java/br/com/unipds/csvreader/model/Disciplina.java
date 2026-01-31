package br.com.unipds.csvreader.model;

import br.com.unipds.csvreader.core.CsvRecordReader;

import java.util.List;

public record Disciplina(int numero, String nome) {

    public static List<Disciplina> lerCsv(String caminhoArquivo) {
        CsvRecordReader reader = new CsvRecordReader();
        return reader.read(caminhoArquivo, true, Disciplina.class);
    }
}