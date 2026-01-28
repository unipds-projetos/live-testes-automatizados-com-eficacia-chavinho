package br.com.unipds.chavinho;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class Chavinho2 {
  public <T> List<T> leCsvDeArquivo(String nomeArquivo, String separador, boolean temCabecalho, Class<T> classe) {
    Path path = Paths.get(nomeArquivo);
    try (Stream<String> linhas = Files.lines(path)) {
      return trataCsv(linhas, separador, temCabecalho, classe);
    } catch (Exception ex) {
      throw new ChavinhoException(ex);
    }
  }

  private <T> List<T> trataCsv(Stream<String> linhas, String separador, boolean temCabecalho, Class<T> classe) {
    return linhas.skip(temCabecalho ? 1 : 0)
        .map(linha -> {

          try {

            String[] pedacos = linha.split(separador);

            RecordComponent[] recordComponents = classe.getRecordComponents();

            Class<?>[] recordComponentTypes = Arrays.stream(recordComponents)
                .map(RecordComponent::getType)
                .toArray(Class<?>[]::new);

            Object[] valoresConvertidos = new Object[recordComponentTypes.length];
            for (int i = 0; i < recordComponentTypes.length; i++) {
              Class<?> type = recordComponentTypes[i];
              String valor = pedacos[i];
              converteValor(type, valor, valoresConvertidos, i);
            }

            Constructor<T> constructor = classe.getDeclaredConstructor(recordComponentTypes);
            return constructor.newInstance(valoresConvertidos);

          } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                   IllegalAccessException e) {
            throw new ChavinhoException(e);
          }
        })
        .toList();
  }

  private static void converteValor(Class<?> type, String valor, Object[] valoresConvertidos, int i) {
    if (type.isAssignableFrom(int.class)) {
      int valorConvertido = Integer.parseInt(valor);
      valoresConvertidos[i] = valorConvertido;
    } else if (type.isAssignableFrom(long.class)) {
      long valorConvertido = Long.parseLong(valor);
      valoresConvertidos[i] = valorConvertido;
    } else if (type.isAssignableFrom(double.class)) {
      double valorConvertido = 0.0;
      if (valor != null && !valor.isEmpty()) {
        valorConvertido = Double.parseDouble(valor);
      }
      valoresConvertidos[i] = valorConvertido;
    } else if (type.isAssignableFrom(boolean.class)) {
      boolean valorConvertido = Boolean.parseBoolean(valor);
      valoresConvertidos[i] = valorConvertido;
    } else if (type.isAssignableFrom(String.class)) {
      valoresConvertidos[i] = valor;
    } else {
      throw new ChavinhoException("Tipo não suportado na conversao de valor do CSV.");
    }
  }
}
