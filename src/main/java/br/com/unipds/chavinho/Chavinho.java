package br.com.unipds.chavinho;

import br.com.unipds.chavinho.model.CSVConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class Chavinho {
	private static final Map<Class<?>, Function<String, Object>> CONVERSORES = Map.ofEntries(
			Map.entry(int.class, Integer::parseInt),
			Map.entry(Integer.class, Integer::parseInt),
			Map.entry(double.class, s -> s.isEmpty() ? 0.0 : Double.parseDouble(s)),
			Map.entry(Double.class, s -> s.isEmpty() ? 0.0 : Double.parseDouble(s)),
			Map.entry(float.class, Float::parseFloat),
			Map.entry(Float.class, Float::parseFloat),
			Map.entry(long.class, Long::parseLong),
			Map.entry(Long.class, Long::parseLong),
			Map.entry(short.class, Short::parseShort),
			Map.entry(Short.class, Short::parseShort),
			Map.entry(byte.class, Byte::parseByte),
			Map.entry(Byte.class, Byte::parseByte),
			Map.entry(boolean.class, Boolean::parseBoolean),
			Map.entry(Boolean.class, Boolean::parseBoolean),
			Map.entry(char.class, s -> s.charAt(0)),
			Map.entry(Character.class, s -> s.charAt(0))
	);
	private final CSVConfig csvConfig;

	public Chavinho(CSVConfig csvConfig) {
		this.csvConfig = csvConfig;
	}

	public <T> List<T> writeToObject(Class<T> clazz) {
		List<T> resultado = new ArrayList<>();
		processarCsvEmLotes(clazz, 50, resultado::addAll);
		return resultado;
	}

	public <T> void processarCsvEmLotes(Class<T> clazz, int tamanhoDoBatch, Consumer<List<T>> processadorDeLote) {
		try (BufferedReader reader = obterReader()) {

			if (csvConfig.isTemCabecalho()) {
				final var _ = reader.readLine();
			}

			List<T> lote = new ArrayList<>(tamanhoDoBatch);
			String linha;

			while ((linha = reader.readLine()) != null) {
				if (!linha.contains(csvConfig.getSeparador())) {
					throw new CSVConversionException("Separador não encontrado na linha: " + linha);
				}

				lote.add(converterLinha(linha, clazz));

				if (lote.size() >= tamanhoDoBatch) {
					processadorDeLote.accept(lote);
					lote.clear();
				}
			}

			if (!lote.isEmpty()) {
				processadorDeLote.accept(lote);
			}
		} catch (CSVConversionException e) {
			throw e;
		} catch (Exception e) {
			throw new CSVConversionException("Erro ao processar CSV em lotes", e);
		}
	}

	private BufferedReader obterReader() throws IOException {
		if (csvConfig.getNomeArquivo() != null && !csvConfig.getNomeArquivo().isEmpty()) {
			return Files.newBufferedReader(Paths.get(csvConfig.getNomeArquivo()));
		}

		if (csvConfig.getConteudo() != null && !csvConfig.getConteudo().isEmpty()) {
			return new BufferedReader(new StringReader(csvConfig.getConteudo()));
		}

		throw new CSVConversionException("Nenhuma fonte de dados configurada");
	}

	private <T> T converterLinha(String linha, Class<T> clazz) {
		try {
			final var valores = linha.split(csvConfig.getSeparador());
			if (clazz.isRecord()) {
				return converterParaRecord(valores, clazz);
			}

			return converterParaClasse(valores, clazz);
		} catch (Exception e) {
			throw new CSVConversionException("Erro ao converter linha: " + linha, e);
		}

	}

	private <T> T converterParaClasse(String[] valores, Class<T> clazz) throws Exception {
		T objeto = clazz.getDeclaredConstructor().newInstance();
		final var fields = clazz.getDeclaredFields();

		for (int i = 0; i < Math.min(valores.length, fields.length); i++) {
			fields[i].setAccessible(true);
			fields[i].set(objeto, converte(valores[i], fields[i].getType()));
		}

		return objeto;
	}

	private <T> T converterParaRecord(String[] valores, Class<T> clazz) throws Exception {
		final var constructor = clazz.getDeclaredConstructors()[0];
		final var args = converteValores(valores, constructor.getParameterTypes());
		return (T) constructor.newInstance(args);
	}

	private Object[] converteValores(String[] valores, Class<?>[] tipos) {
		final var args = new Object[tipos.length];
		for (int i = 0; i < tipos.length; i++) {
			args[i] = converte(valores[i], tipos[i]);
		}
		return args;
	}

	private Object converte(String valor, Class<?> tipo) {
		valor = valor.trim();

		return CONVERSORES.getOrDefault(tipo, s -> s)
				.apply(valor);
	}
}
