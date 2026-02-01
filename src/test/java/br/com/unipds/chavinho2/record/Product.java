package br.com.unipds.chavinho2.record;

public record Product(
        int index,
        String name,
        String description,
        String brand,
        String category,
        int price,
        String currency,
        int stock,
        long ean,
        String color,
        String size,
        String availability,
        int internalId
) {
}
