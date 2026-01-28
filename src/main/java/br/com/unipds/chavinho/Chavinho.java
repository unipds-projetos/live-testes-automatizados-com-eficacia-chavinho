package br.com.unipds.chavinho;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class Chavinho {

  public List<Disciplina> leCsv(String csv) {
    return leCsv(csv, false);
  }

  public List<Disciplina> leCsv(String csv, boolean temCabecalho) {
    if (csv == null || csv.isEmpty()) {
      return new ArrayList<>();
    }
    return trataCsv(csv.lines(), temCabecalho);
  }

  public List<Disciplina> leCsvDeArquivo(String nomeArquivo, boolean temCabecalho) {

    Path path = Paths.get(nomeArquivo);
    try (Stream<String> linhas = Files.lines(path)) {
      return trataCsv(linhas, temCabecalho);
    } catch (Exception ex) {
      throw new ChavinhoException(ex);
    }
  }

  private List<Disciplina> trataCsv(Stream<String> linhas, boolean temCabecalho) {
    return linhas.skip(temCabecalho ? 1 : 0)
        .map(linha -> {
          String[] pedacos = linha.split(",");
          return new Disciplina(Integer.parseInt(pedacos[0]), pedacos[1]);
        })
        .toList();
  }

}

