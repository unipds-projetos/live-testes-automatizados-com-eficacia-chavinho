package br.com.unipds.csvreader.core;

import java.lang.reflect.Constructor;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class CsvMapper {

    private static final Map<Class<?>, Function<String, Object>> CONVERTERS = Map.of(
            int.class, Integer::parseInt,
            long.class, Long::parseLong,
            boolean.class, Boolean::parseBoolean,
            String.class, s -> s,
            double.class, CsvMapper::parseDoubleSafe
    );

    public <T> Function<List<String>, T> createMapper(Class<T> targetClass) {
        try {
            RecordComponent[] components = targetClass.getRecordComponents();
            if (components == null) {
                Constructor<T> constructor = targetClass.getDeclaredConstructor();
                throw new CsvParsingException("Para alta performance, utilize Java Records.");
            }

            Class<?>[] types = Arrays.stream(components)
                    .map(RecordComponent::getType)
                    .toArray(Class<?>[]::new);

            Constructor<T> constructor = targetClass.getDeclaredConstructor(types);

            return (columns) -> {
                try {
                    Object[] args = convertArgs(columns, types);
                    return constructor.newInstance(args);
                } catch (Exception e) {
                    throw new RuntimeException("Erro ao instanciar linha: " + e.getMessage(), e);
                }
            };

        } catch (Exception e) {
            throw new CsvParsingException("Erro ao preparar mapper para a classe: " + targetClass.getName(), e);
        }
    }

    private Object[] convertArgs(List<String> columns, Class<?>[] types) {
        Object[] args = new Object[types.length];
        for (int i = 0; i < types.length; i++) {
            String value = (i < columns.size()) ? columns.get(i) : null;
            args[i] = convert(types[i], value);
        }
        return args;
    }

    private Object convert(Class<?> type, String value) {
        Function<String, Object> converter = CONVERTERS.get(type);
        if (converter == null) throw new CsvParsingException("Tipo não suportado: " + type.getName());

        if (value == null || value.trim().isEmpty()) {
            if (type == int.class) return 0;
            if (type == long.class) return 0L;
            if (type == double.class) return 0.0;
            if (type == boolean.class) return false;
            return null;
        }
        return converter.apply(value);
    }

    private static Double parseDoubleSafe(String value) {
        return (value != null && !value.isEmpty()) ? Double.parseDouble(value) : 0.0;
    }
}