package br.com.unipds.chavinho;

import br.com.unipds.chavinho.model.CSVConfig;
import br.com.unipds.chavinho.model.Disciplina;
import br.com.unipds.chavinho.model.ItemCardapio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ChavinhoTest {

	@Nested
	@DisplayName("Cenário: String CSV serializado para Object")
	class CenarioStringCsv {

		@Test
		@DisplayName("Deve retornar lista vazia se input vazio ou nulo")
		void deveRetornarListaVaziaQuandoInputForNuloOuVazio() {
			final var config = new CSVConfig.Builder()
					.conteudo(null)
					.build();

			final var chavinho = new Chavinho(config);
			final var itemCardapios = chavinho.writeToObject(ItemCardapio.class);

			assertNotNull(itemCardapios);
			assertTrue(itemCardapios.isEmpty());
		}

		@Test
		@DisplayName("Deve retornar lista com uma disciplina quando input for de uma disciplina")
		void deveRetornarListaDeDisciplinaQuandoInputForDeUmaDisciplina() {
			final var csv = """
					0,Introdução ao Java
					""";

			final var config = new CSVConfig.Builder()
					.conteudo(csv)
					.separador(",")
					.build();

			final var chavinho = new Chavinho(config);
			final var disciplinas = chavinho.writeToObject(Disciplina.class);

			assertInstanceOf(Disciplina.class, disciplinas.getFirst());
			assertEquals(1, disciplinas.size());
			assertEquals("Introdução ao Java", disciplinas.getFirst().nome());
		}
	}

	@Nested
	@DisplayName("Cenário: Csv serializado para Item Cardapio")
	class CenarioItemCardapio {
		@Test
		@DisplayName("Deve converter CSV para lista de itens do cardápio")
		void deveConverterCsvParaListaDeItensCardapio() {
			final var config = new CSVConfig.Builder()
					.nome("src/test/resources/itens-cardapio.csv")
					.separador(";")
					.build();

			final var chavinho = new Chavinho(config);
			final var itens = chavinho.writeToObject(ItemCardapio.class);

			assertInstanceOf(ItemCardapio.class, itens.getFirst());
			assertEquals(7, itens.size());
			assertEquals("Refresco do Chaves", itens.getFirst().nome());
			assertEquals(2.99, itens.getFirst().preco());
			assertFalse(itens.getFirst().emPromocao());
		}

		@Test
		@DisplayName("Deve converter todos os campos corretamente")
		void deveConverterTodosOsCamposCorretamente() {
			final var config = new CSVConfig.Builder()
					.nome("src/test/resources/itens-cardapio.csv")
					.separador(";")
					.build();

			final var chavinho = new Chavinho(config);
			final var itens = chavinho.writeToObject(ItemCardapio.class);

			final var churros = itens.get(5);
			assertEquals(6L, churros.id());
			assertEquals("Churros do Chaves", churros.nome());
			assertEquals(4.99, churros.preco());
			assertEquals("SOBREMESAS", churros.categoria());
			assertTrue(churros.emPromocao());
			assertEquals(3.99, churros.precoComDesconto());
			assertTrue(churros.impostoIsento());
		}

		@Test
		@DisplayName("Deve converter double vazio para 0.0")
		void deveConverterDoubleVazioParaZero() {
			final var config = new CSVConfig.Builder()
					.nome("src/test/resources/itens-cardapio.csv")
					.separador(";")
					.build();

			final var chavinho = new Chavinho(config);
			final var itens = chavinho.writeToObject(ItemCardapio.class);

			final var refresco = itens.getFirst();
			assertEquals(0.0, refresco.precoComDesconto());
		}
	}

	@Nested
	@DisplayName("Cenário: Csv serializado para Disciplina")
	class CenarioUnipdsDisciplina {

		@Test
		@DisplayName("Deve converter CSV para lista de disciplinas")
		void deveConverterCsvParaListaDeDisciplinas() {
			final var config = new CSVConfig.Builder()
					.nome("src/test/resources/unipds-disciplinas.csv")
					.separador(",")
					.temCabecalho(true)
					.build();

			final var chavinho = new Chavinho(config);
			final var disciplinas = chavinho.writeToObject(Disciplina.class);

			assertInstanceOf(Disciplina.class, disciplinas.getFirst());
			assertEquals(11, disciplinas.size());
			assertEquals("Introdução ao Java", disciplinas.getFirst().nome().trim());
		}

		@Test
		@DisplayName("Deve converter todos os campos corretamente")
		void deveConverterTodosOsCamposCorretamente() {
			final var config = new CSVConfig.Builder()
					.nome("src/test/resources/unipds-disciplinas.csv")
					.separador(",")
					.temCabecalho(true)
					.build();

			final var chavinho = new Chavinho(config);
			final var disciplinas = chavinho.writeToObject(Disciplina.class);

			final var introducaoAoJava = disciplinas.getFirst();
			assertEquals(0, introducaoAoJava.numero());
			assertEquals("Introdução ao Java", introducaoAoJava.nome());
		}
	}

	@Nested
	@DisplayName("Cenário: Csv com erro de conversão")
	class CenarioCSVConversionException {
		@Test
		@DisplayName("Deve lançar exceção quando separador não encontrado")
		void deveLancarExcecaoQuandoSeparadorNaoEncontrado() {
			final var config = new CSVConfig.Builder()
					.nome("src/test/resources/itens-cardapio.csv")
					.separador("|")
					.build();

			final var chavinho = new Chavinho(config);

			assertThrows(CSVConversionException.class, () -> chavinho.writeToObject(ItemCardapio.class));
		}

		@Test
		@DisplayName("Deve lançar exceção quando arquivo não existe")
		void deveLancarExcecaoQuandoArquivoNaoExiste() {
			final var config = new CSVConfig.Builder()
					.nome("arquivo-inexistente.csv")
					.separador(";")
					.build();

			final var chavinho = new Chavinho(config);

			assertThrows(CSVConversionException.class, () -> chavinho.writeToObject(ItemCardapio.class));
		}
	}
}