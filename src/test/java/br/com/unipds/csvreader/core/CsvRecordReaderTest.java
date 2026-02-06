package br.com.unipds.csvreader.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

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
        Assertions.assertEquals("1", resultado.get(0).get("id"));
        Assertions.assertEquals("admin", resultado.get(0).get("user_name"));
    }

    @Test
    @DisplayName("Unit: Deve ler String CSV sem cabeçalho (usando índices)")
    void deveLerStringSemHeader() {
        String csv = "100;Banana;Fruta";
        CsvReader leitorSemHeader = CsvReader.builder().withHeader(false).build();

        List<Map<String, String>> resultado = leitorSemHeader.readString(csv);

        Assertions.assertEquals(1, resultado.size());
        Assertions.assertEquals("100", resultado.get(0).get("0"));
    }

    @Test
    @DisplayName("Unit: Deve retornar lista vazia se input for nulo ou branco")
    void deveTratarInputVazio() {
        Assertions.assertTrue(csvReader.readString(null).isEmpty());
        Assertions.assertTrue(csvReader.readString("").isEmpty());
    }

    /// ==================================================================
    ///     TESTES DE INTEGRAÇÃO (Arquivos físicos)
    /// ==================================================================

    @Test
    @DisplayName("Integration: Deve ler arquivo separado por VÍRGULA (ex: Disciplinas)")
    void deveLerArquivoSeparadoPorVirgula() {
        String arquivo = "src/test/resources/unipds-disciplinas.csv";
        List<Map<String, String>> linhas = csvReader.read(arquivo);

        Assertions.assertFalse(linhas.isEmpty(), "Arquivo não deveria estar vazio");

        Map<String, String> primeiraLinha = linhas.get(0);

        String valor = primeiraLinha.get("nome disciplina");
        if (valor == null) valor = primeiraLinha.get("nome");

        Assertions.assertNotNull(valor, "Coluna de nome não encontrada. Chaves: " + primeiraLinha.keySet());
        Assertions.assertEquals("Introdução ao Java", valor.trim());
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
        double preco = Double.parseDouble(precoStr);
        double precoEsperado = 25.9;

        Assertions.assertEquals("Tacos de Carnitas", nome);
        Assertions.assertNotNull(precoStr, "Coluna de preço não encontrada");
        Assertions.assertEquals(precoEsperado, preco, 0.001, "O preço deve bater exatamente com o CSV");
    }

    @Test
    @DisplayName("Integration: Deve processar arquivo GIGANTE via Stream (ex: Products)")
    void deveProcessarArquivoGigante() {
        String arquivo = "src/test/resources/products-2000000.csv";

        AtomicInteger contador = new AtomicInteger(0);

        csvReader.process(arquivo, produto -> {
            int linhaAtual = contador.incrementAndGet();

            if (linhaAtual == 1) {
                String nomeEsperado = "Pro Charger Tablet Brush Go 360";

                Assertions.assertEquals(nomeEsperado, produto.get("name"), "Nome incorreto na linha 1");
                Assertions.assertNotNull(produto.get("price"), "Preço não deveria ser nulo");
            }
        });

        int totalEsperado = 2000000;
        Assertions.assertEquals(totalEsperado, contador.get(), "O número total de linhas processadas está incorreto");
    }

    @Test
    @DisplayName("Idiomatic: Deve converter para Objeto Tipado usando Interface (Strategy)")
    void deveLerComStrategyPattern() {
        String arquivo = "src/test/resources/itens-cardapio.csv";

        CsvReader leitorSemHeader = CsvReader.builder()
                .withHeader(false)
                .build();

        CsvRowMapper<ProdutoDTO> mapper = new CsvRowMapper<ProdutoDTO>() {
            @Override
            public ProdutoDTO mapRow(Map<String, String> row) {
                String nome = row.get("1");
                String precoStr = row.get("3");
                return new ProdutoDTO(nome, Double.parseDouble(precoStr));
            }
        };

        List<ProdutoDTO> produtos = leitorSemHeader.read(arquivo, mapper);
        Assertions.assertFalse(produtos.isEmpty());
        ProdutoDTO ultimoProduto = produtos.get(produtos.size() - 1);

        Assertions.assertEquals("Tacos de Carnitas", ultimoProduto.nome);
        Assertions.assertEquals(25.9, ultimoProduto.preco, 0.001);
    }

    @Test
    @DisplayName("Parser: Deve respeitar separador dentro de aspas (Não deve quebrar a string)")
    void deveRespeitarSeparadorDentroDeAspas() {
        String csv = """
                id;nome;preco
                1;"Botas; de Couro";150.00
                """;

        CsvReader leitor = CsvReader.builder()
                .withSeparator(';')
                .withHeader(true)
                .build();

        List<Map<String, String>> resultado = leitor.readString(csv);

        Assertions.assertEquals(1, resultado.size());
        Assertions.assertEquals("Botas; de Couro", resultado.get(0).get("nome"));
        Assertions.assertEquals("150.00", resultado.get(0).get("preco"));
    }

    @Test
    @DisplayName("Parser: Deve ler colunas vazias corretamente")
    void deveLerColunasVazias() {

        String csv = "1;Mouse;;true";

        CsvReader leitor = CsvReader.builder()
                .withHeader(false)
                .withSeparator(';')
                .build();

        List<Map<String, String>> resultado = leitor.readString(csv);

        Map<String, String> linha = resultado.get(0);

        Assertions.assertEquals("1", linha.get("0"));
        Assertions.assertEquals("Mouse", linha.get("1"));
        Assertions.assertEquals("", linha.get("2"), "A coluna vazia deveria ser uma String vazia");
        Assertions.assertEquals("true", linha.get("3"));
    }

    @Test
    @DisplayName("Error Handling: Deve lançar exceção amigável se arquivo não existir")
    void deveLancarErroArquivoInexistente() {
        String arquivoFalso = "caminho/que/nao/existe.csv";

        CsvParsingException exception = Assertions.assertThrows(CsvParsingException.class, () -> {
            csvReader.read(arquivoFalso);
        });

        Assertions.assertTrue(exception.getMessage().contains("Erro de IO"),
                "A mensagem de erro deveria mencionar problema de IO");
    }

    /// ==================================================================
    ///     NOVO TESTE: STRATEGY PATTERN
    /// ==================================================================
    /// Classe auxiliar DTO (Data Transfer Object) apenas para teste
    /// ==================================================================

    static class ProdutoDTO {
        String nome;
        double preco;

        public ProdutoDTO(String nome, double preco) {
            this.nome = nome;
            this.preco = preco;
        }
    }
}