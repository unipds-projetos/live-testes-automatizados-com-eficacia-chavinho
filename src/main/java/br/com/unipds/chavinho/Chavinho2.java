package br.com.unipds.chavinho;

import java.awt.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public class Chavinho2 {

    //region Definindo o mapeamento dos tipos
    public static final Map<Class<?>, Function<String, Object>> CONVERSOR = new HashMap<>();

    static {

        Function<String, String> limpar = s -> (s == null) ? "" : s.replace("\"", "").trim();

        CONVERSOR.put(int.class, s -> {
            String tmp = limpar.apply(s);
            return tmp.isEmpty() ? 0 : Integer.parseInt(tmp.trim());
        });
        CONVERSOR.put(Integer.class, s -> {
            String tmp = limpar.apply(s);
            return tmp.isEmpty() ? null : Integer.parseInt(tmp.trim());
        });

        CONVERSOR.put(long.class, s -> {
            String tmp = limpar.apply(s);
            return tmp.isEmpty() ? 0 : Long.parseLong(tmp.trim());
        });
        CONVERSOR.put(Long.class, s -> {
            String tmp = limpar.apply(s);
            return tmp.isEmpty() ? null : Long.parseLong(tmp.trim());
        });

        CONVERSOR.put(double.class, s -> {
            String tmp = limpar.apply(s);
            return tmp.isEmpty() ? 0 : Double.parseDouble(tmp.trim());
        });
        CONVERSOR.put(Double.class, s -> {
            String tmp = limpar.apply(s);
            return tmp.isEmpty() ? null : Double.parseDouble(tmp.trim());
        });

        CONVERSOR.put(boolean.class, s -> {
            String v = limpar.apply(s);
            return !v.isEmpty() && Boolean.parseBoolean(v);
        });
        CONVERSOR.put(Boolean.class, s -> {
            String v = limpar.apply(s);
            return v.isEmpty() ? null : Boolean.parseBoolean(v);
        });

        CONVERSOR.put(String.class, s -> s == null ? "" : s.replace("\"", "").trim());
    }

    //endregion

    public List<Disciplina> leCsv(String csv) {
        return leCsv(csv, false);
    }

    //region Metodos legados
    public List<Disciplina> leCsv(String csv, boolean temCabecalho) {
        if (csv == null || csv.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return trataCsv(csv.lines(), ",", temCabecalho, Disciplina.class);
        } catch (Exception e) {
            throw new ChavinhoException("Erro ao processar csv");
        }

    }

    public List<Disciplina> leCsvDeArquivo(String nomeArquivo, boolean temCabecalho) {

        Path path = Paths.get(nomeArquivo);
        try (Stream<String> linhas = Files.lines(path)) {
            return trataCsv(linhas, ",", temCabecalho, Disciplina.class);
        } catch (Exception ex) {
            throw new ChavinhoException(ex);
        }
    }

    public <T> List<T> leCsvDeArquivo(String nomeArquivo, String separador, boolean temCabecalho, Class<T> classe) {
        Path path = Paths.get(nomeArquivo);
        try (Stream<String> linhas = Files.lines(path)) {
            return trataCsv(linhas, separador, temCabecalho, classe);
        } catch (Exception ex) {
            throw new ChavinhoException(ex);
        }
    }

    //endregion

    private <T> List<T> trataCsv(Stream<String> linhas, String separador, boolean temCabecalho, Class<
            T> classe) throws Exception {
        final Constructor<T> construtor;
        final Class<?>[] componentes;

        //Extraindo a estrutura da classe e construtor do loop das linhas
        BaseClasse<T> base = GetBaseClasse(classe);

        return linhas.skip(temCabecalho ? 1 : 0)
                .map(linha -> {

                    try {
                        //tratamento para extrair o campo da linha
                        //para evitar erro no split da linha
                        String[] pedacos = linha.split(separador + "(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);

                        //Lendo os atributos da classe
                        //e convertendo os valores para instancia da classe
                        Object[] valoresConvertidos = new Object[base.componentes().length];
                        for (int i = 0; i < base.componentes().length; i++) {
                            valoresConvertidos[i] = CONVERSOR.get(base.componentes()[i]).apply(pedacos[i]);
                        }
                        //Verificando se é um record
                        //ou classe normal
                        if (classe.isRecord()) {
                            return base.construtor().newInstance(valoresConvertidos);
                        } else {
                            T instancia = base.construtor().newInstance();
                            for (int i = 0; i < base.campos().length; i++) {
                                Field f = base.campos()[i];
                                f.setAccessible(true);
                                f.set(instancia, valoresConvertidos[i]);
                            }
                            return instancia;
                        }

                    } catch (InvocationTargetException | InstantiationException |
                             IllegalAccessException e) {
                        throw new ChavinhoException(e);
                    }
                })
                .toList();
    }

    //Metodo para extrair os atributos da classe
    //para evitar a extração durante a execução do loop
    private <T> BaseClasse<T> GetBaseClasse(Class<T> classe) throws Exception {
        if (classe.isRecord()) {
            RecordComponent[] recordComponents = classe.getRecordComponents();
            Class<?>[] componentes = Arrays.stream(recordComponents)
                    .map(RecordComponent::getType)
                    .toArray(Class<?>[]::new);

            Constructor<T> construtor = classe.getDeclaredConstructor(componentes);
            return new BaseClasse<>(construtor, componentes, null);
        } else {
            Field[] atributos = classe.getDeclaredFields();
            Class<?>[] componentes = Arrays.stream(atributos)
                    .map(Field::getType)
                    .toArray(Class<?>[]::new);
            Constructor<T> construtor = classe.getDeclaredConstructor();
            return new BaseClasse<>(construtor, componentes, atributos);
        }
    }

    //Record para manter a estrutura da classe
    private record BaseClasse<T>(
            Constructor<T> construtor,
            Class<?>[] componentes,
            Field[] campos
    ) {
    }
}
