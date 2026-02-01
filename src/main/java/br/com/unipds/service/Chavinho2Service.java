package br.com.unipds.service;

import br.com.unipds.chavinho2.Chavinho2;
import br.com.unipds.exception.Chavinho2Exception;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class Chavinho2Service {

    public <T> void processaCsvDeArquivo(
            String nomeArquivo,
            Class<T> classe,
            Chavinho2 chavinho2,
            Consumer<T> consumer
            ) {
        Path path = Paths.get(nomeArquivo);
        try (Stream<String> linhas = Files.lines(path)) {
            Stream<String> stream = linhas;
            if (chavinho2.temCabecalho) {
                stream = stream.skip(1);
            }
            stream.forEach(linha -> {
                try {
                    T objeto = converteLinha(linha, classe, chavinho2.separador);
                    consumer.accept(objeto);
                } catch (Chavinho2Exception e) {
                    // ignora linha mal formatada
                }
            });
        } catch (Exception e) {
            throw new Chavinho2Exception(e);
        }
    }

    private <T> T converteLinha(String linha, Class<T> classe, String separador) {
        if (!classe.isRecord()) {
            throw new Chavinho2Exception("A classe informada deve ser um record.");
        }

        try {
            String[] pedacos = linha.split(separador);

            RecordComponent[] recordComponents = classe.getRecordComponents();

            Class<?>[] tipos = Arrays.stream(recordComponents)
                    .map(RecordComponent::getType)
                    .toArray(Class<?>[]::new);

            Object[] valores = new Object[tipos.length];
            for (int i = 0; i < tipos.length; i++) {
                converteValor(tipos[i], pedacos[i], valores, i);
            }

            Constructor<T> constructor = classe.getDeclaredConstructor(tipos);
            return constructor.newInstance(valores);
        } catch (Exception e) {
            throw new Chavinho2Exception(e);
        }
    }


    public <T> List<T> leCsvDeArquivo(String nomeArquivo, Class<T> classe, Chavinho2 chavinho2) {
        Path path = Paths.get(nomeArquivo);
        try (Stream<String> linhas = Files.lines(path)) {
            return trataCsv(linhas, classe, chavinho2);
        } catch (Chavinho2Exception e) {
            throw e;
        } catch (Exception ex) {
            throw new Chavinho2Exception(ex);
        }
    }

    private <T> List<T> trataCsv(Stream<String> linhas, Class<T> classe, Chavinho2 chavinho2) {
        if (!classe.isRecord()) {
            throw new Chavinho2Exception("A classe informada deve ser um record.");
        }
        return linhas.skip(chavinho2.temCabecalho ? 1 : 0)
                .map(linha -> {

                    try {

                        String[] pedacos = linha.split(chavinho2.separador);

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
                        throw new Chavinho2Exception(e);
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
            throw new Chavinho2Exception("Tipo não suportado na conversao de valor do CSV.");
        }
    }

}
