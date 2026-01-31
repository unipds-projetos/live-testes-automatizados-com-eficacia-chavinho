package br.com.unipds.csvreader.core;

import br.com.unipds.csvreader.model.Disciplina;
import br.com.unipds.csvreader.model.ItemCardapio;
import br.com.unipds.csvreader.model.Product;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

class CsvReaderTest {

    private CsvRecordReader csvRecordReader;

    @BeforeEach
    void setup() {
        csvRecordReader = new CsvRecordReader();
    }

    // ==================================================================================
    // 1. TESTES DE UNIDADE
    // ==================================================================================

    @Test
    @DisplayName("Deve ser resiliente e retornar lista vazia quando input (String) é nulo")
    void deveRetornarListaVaziaComInputNulo() {
        List<Disciplina> lista = csvRecordReader.readString(null, true, Disciplina.class);

        Assertions.assertNotNull(lista);
        Assertions.assertTrue(lista.isEmpty());
    }

    @Test
    @DisplayName("Deve converter String CSV em memória para objetos corretamente")
    void deveLerStringParaDisciplina() {
        String csv = """
                id,nome
                1,Java Avançado
                """;

        List<Disciplina> lista = csvRecordReader.readString(csv, true, Disciplina.class);

        Assertions.assertEquals(1, lista.size());
        Assertions.assertEquals("Java Avançado", lista.getFirst().nome());
    }

    @Test
    @DisplayName("Deve suportar Generics e ler outros tipos (ItemCardapio)")
    void deveLerStringParaItemCardapio() {
        String csv = "100;Tacos Pastor";

        List<ItemCardapio> lista = csvRecordReader.readString(csv, false, ItemCardapio.class);

        Assertions.assertEquals(1, lista.size());
        Assertions.assertEquals("Tacos Pastor", lista.getFirst().nome());
    }

    // ==================================================================================
    // 2. TESTES DE INTEGRAÇÃO
    // ==================================================================================

    @Test
    @DisplayName("Disciplina: Deve lançar exceção se o arquivo não existe (via método estático)")
    void deveLancarExcecaoSeArquivoNaoExiste() {
        Assertions.assertThrows(CsvParsingException.class, () -> {
            Disciplina.lerCsv("caminho/falso/nao/existe.csv");
        });
    }

    @Test
    @DisplayName("Disciplina: Deve ler arquivo real do disco corretamente")
    void deveLerArquivoDeDisciplinas() {
        List<Disciplina> lista = Disciplina.lerCsv("src/test/resources/unipds-disciplinas.csv");

        Assertions.assertFalse(lista.isEmpty());
        Assertions.assertEquals(11, lista.size());
        Assertions.assertEquals("Introdução ao Java", lista.getFirst().nome().trim());
    }

    @Test
    @DisplayName("ItemCardapio: Deve ler arquivo real do disco corretamente")
    void deveLerArquivoDeCardapio() {
        List<ItemCardapio> lista = ItemCardapio.lerCsv("src/test/resources/itens-cardapio.csv");

        Assertions.assertFalse(lista.isEmpty());
        Assertions.assertEquals(6, lista.size());

        ItemCardapio item = lista.getLast();
        Assertions.assertEquals("Tacos de Carnitas", item.nome());
        Assertions.assertTrue(item.preco() > 0);
    }

    @Test
    void deveProcessarArquivoGiganteSemEstourarMemoria() {
        Product.processarCsv("src/test/resources/products-2000000.csv", produto -> {
            Assertions.assertNotNull(produto.name());
        });
    }
}