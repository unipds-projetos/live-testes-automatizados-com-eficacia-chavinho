package br.com.unipds.csvreader.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

class CsvReaderTest {

    private CsvReader csvReader;

    @BeforeEach
    void setup() {
        csvReader = CsvReader.builder()
                .withHeader(true)
                .build();
    }

    /// ==================================================================
    ///     TESTES UNITÁRIOS (Strings em Memória)
    /// ==================================================================

    @Test
    @DisplayName("Unit: Deve ler String CSV simples com cabeçalho")
    void deveLerStringComHeader() {
        String csv = """
                id,user_name
                1,admin
                """;

        List<Map<String, String>> resultado = csvReader.readString(csv);

        Assertions.assertEquals(1, resultado.size());
        assertMapContains(resultado.get(0), "id", "1");
        assertMapContains(resultado.get(0), "user_name", "admin");
    }

    @Test
    @DisplayName("Unit: Deve ler String CSV sem cabeçalho (usando índices)")
    void deveLerStringSemHeader() {
        String csv = "100;Banana;Fruta";

        CsvReader leitorSemHeader = CsvReader.builder()
                .withHeader(false)
                .build();

        List<Map<String, String>> resultado = leitorSemHeader.readString(csv);

        Assertions.assertEquals(1, resultado.size());
        
        assertMapContains(resultado.get(0), "0", "100");
        assertMapContains(resultado.get(0), "1", "Banana");
        assertMapContains(resultado.get(0), "2", "Fruta");
    }

    @Test
    @DisplayName("Unit: Deve retornar lista vazia se input for nulo ou branco")
    void deveTratarInputVazio() {
        Assertions.assertTrue(csvReader.readString(null).isEmpty());
        Assertions.assertTrue(csvReader.readString("").isEmpty());
        Assertions.assertTrue(csvReader.readString("   ").isEmpty());
    }

    /// ==================================================================
    ///     TESTES DE INTEGRAÇÃO (Arquivos fisicos)
    /// ==================================================================

    @Test
    @DisplayName("Integration: Deve ler arquivo separado por VÍRGULA (ex: Disciplinas)")
    void deveLerArquivoSeparadoPorVirgula() {
        String arquivo = "src/test/resources/unipds-disciplinas.csv";

        List<Map<String, String>> linhas = csvReader.read(arquivo);

        Assertions.assertFalse(linhas.isEmpty(), "Arquivo não deveria estar vazio");
        
        Map<String, String> primeiraLinha = linhas.get(0);

        if (primeiraLinha.containsKey("nome disciplina")) {
            assertMapContains(primeiraLinha, "nome disciplina", "Introdução ao Java");
        } else {
            System.out.println("⚠️ Colunas encontradas em Disciplinas: " + primeiraLinha.keySet());
            Assertions.fail("Não encontrou a coluna 'name'. Verifique o log.");
        }
    }

    @Test
    @DisplayName("Integration: Deve ler arquivo separado por PONTO-E-VÍRGULA (Sem Header)")
    void deveLerArquivoSeparadoPorPontoVirgula() {
        String arquivo = "src/test/resources/itens-cardapio.csv";

        CsvReader leitorSemHeader = CsvReader.builder()
                .withHeader(false)
                .build();

        List<Map<String, String>> linhas = leitorSemHeader.read(arquivo);

        Assertions.assertFalse(linhas.isEmpty());

        Map<String, String> ultimaLinha = linhas.get(linhas.size() - 1);

        String nome = ultimaLinha.get("1");
        String precoStr = ultimaLinha.get("3");

        Assertions.assertEquals("Tacos de Carnitas", nome);
        Assertions.assertNotNull(precoStr, "Coluna '3' (preço) não encontrada.");
        Assertions.assertTrue(Double.parseDouble(precoStr) > 0);
    }

    @Test
    @DisplayName("Integration: Deve processar arquivo GIGANTE via Stream (ex: Products)")
    void deveProcessarArquivoGigante() {
        String arquivo = "src/test/resources/products-2000000.csv";

        java.util.concurrent.atomic.AtomicInteger contador = new java.util.concurrent.atomic.AtomicInteger(0);

        csvReader.process(arquivo, produto -> {
            contador.incrementAndGet();

            if (contador.get() == 1) {
                Assertions.assertNotNull(produto.get("name"),
                        "Erro ao ler 'name'. Colunas disponíveis: " + produto.keySet());
            }
        });

        Assertions.assertTrue(contador.get() > 0, "O processamento via Stream não leu nenhuma linha!");
    }

    /// ==================================================================
    ///     MÉTODOS AUXILIARES (Para facilitar o Debug)
    /// ==================================================================

    private void assertMapContains(Map<String, String> mapa, String chave, String valorEsperado) {
        String valorReal = mapa.get(chave);

        if (valorReal == null) {
            Assertions.fail(String.format("Chave '%s' não encontrada. Chaves disponíveis: %s", chave, mapa.keySet()));
        }

        Assertions.assertEquals(valorEsperado.trim(), valorReal.trim());
    }
}