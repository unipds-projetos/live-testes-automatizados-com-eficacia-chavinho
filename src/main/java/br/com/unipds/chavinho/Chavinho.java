package br.com.unipds.chavinho;

import br.com.unipds.chavinho.model.CSVConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class Chavinho {
	private final CSVConfig csvConfig;
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

	public Chavinho(CSVConfig csvConfig) {
		this.csvConfig = csvConfig;
	}

	public <T> List<T> writeToObject(Class<T> clazz) {

		try {
			final var linhas = leLinhas();
			if (linhas.isEmpty()) {
				return new ArrayList<>();
			}

			final var inicio = csvConfig.isTemCabecalho() ? 1 : 0;
			if (!linhas.get(inicio).contains(csvConfig.getSeparador())) {
				throw new CSVConversionException("Separador não encontrado no caminho. " + csvConfig.getNomeArquivo());
			}

			final var resultado = new ArrayList<T>();
			final var constructor = clazz.getDeclaredConstructors()[0];


			for (int i = inicio; i < linhas.size(); i++) {
				final var valores = linhas.get(i).split(csvConfig.getSeparador());
				final var args = converteValores(valores, constructor.getParameterTypes());
				resultado.add((T) constructor.newInstance(args));
			}
			return resultado;
		} catch (Exception e) {
			throw new CSVConversionException("Erro ao processar arquivo CSV", e);
		}
	}

	private List<String> leLinhas() throws IOException {
		if (csvConfig.getNomeArquivo() != null && !csvConfig.getNomeArquivo().isEmpty()) {
			return Files.readAllLines(Paths.get(csvConfig.getNomeArquivo()));
		}

		if (csvConfig.getConteudo() != null && !csvConfig.getConteudo().isEmpty()) {
			return csvConfig.getConteudo().lines().toList();
		}

		return List.of();
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
