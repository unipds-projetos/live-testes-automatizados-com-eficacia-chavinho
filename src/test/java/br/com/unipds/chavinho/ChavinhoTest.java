package br.com.unipds.chavinho;

import br.com.unipds.chavinho.model.CSVConfig;
import br.com.unipds.chavinho.model.Disciplina;
import br.com.unipds.chavinho.model.ItemCardapio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class ChavinhoTest {

	@Nested
	@DisplayName("Cenário: String CSV serializado para Object")
	class CenarioStringCsv {
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
			assertEquals("Introdução ao Java", disciplinas.getFirst().nome());
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

		@Test
		@DisplayName("Deve lançar exceção quando não tem fonte de dados")
		void deveLancarExcecaoQuandoNaoTemFonteDeDados() {
			CSVConfig config = new CSVConfig.Builder()
					.separador(";")
					.temCabecalho(true)
					.build();

			Chavinho chavinho = new Chavinho(config);

			assertThrows(CSVConversionException.class, () ->
				chavinho.processarCsvEmLotes(Disciplina.class, 10, _ -> {})
			);
		}

	}

	@Nested
	@DisplayName("Cenário: Processamento em lotes")
	class CenarioProcessamentoLotes {

		@Test
		@DisplayName("Deve processar CSV em lotes com string")
		void deveProcessarCsvEmLotesComString() {
			String csv = """
					1;Refresco do Chaves;Suco de limão que parece de tamarindo e tem gosto de groselha.;2.99;BEBIDAS;false;;false
					2;Sanduíche de Presunto do Chaves;Sanduíche de presunto simples mas feito com muito amor.;3.50;PRATOS_PRINCIPAIS;true;2.99;false
					3;Torta de Frango da Dona Florinda;Torta de frango com recheio cremoso e massa crocante.;12.99;PRATOS_PRINCIPAIS;true;10.99;false
					""";

			CSVConfig config = new CSVConfig.Builder()
					.conteudo(csv)
					.separador(";")
					.build();

			Chavinho chavinho = new Chavinho(config);
			List<ItemCardapio> itemCardapios = new ArrayList<>();

			chavinho.processarCsvEmLotes(ItemCardapio.class, 2, itemCardapios::addAll);

			assertEquals(3, itemCardapios.size());
			assertEquals("Refresco do Chaves", itemCardapios.getFirst().nome());
			assertEquals("PRATOS_PRINCIPAIS", itemCardapios.get(1).categoria());
			assertTrue(itemCardapios.get(2).emPromocao());
		}

		@Test
		@DisplayName("Deve processar CSV em lotes com arquivo")
		void deveProcessarCsvEmLotesComArquivo() {
			CSVConfig config = new CSVConfig.Builder()
					.nome("src/test/resources/unipds-disciplinas.csv")
					.separador(",")
					.temCabecalho(true)
					.build();

			Chavinho chavinho = new Chavinho(config);
			List<Disciplina> disciplinas = new ArrayList<>();

			chavinho.processarCsvEmLotes(Disciplina.class, 10, disciplinas::addAll);

			assertInstanceOf(Disciplina.class, disciplinas.getFirst());
			assertEquals(11, disciplinas.size());
			assertEquals("Introdução ao Java", disciplinas.getFirst().nome());

		}

		@Test
		@DisplayName("Deve processar cada lote separadamente")
		void deveProcessarCadaLoteSeparadamente() {
			String csv = """
					numero;nome
					1;Java
					2;Python
					3;JavaScript
					""";

			CSVConfig config = new CSVConfig.Builder()
					.conteudo(csv)
					.separador(";")
					.temCabecalho(true)
					.build();

			Chavinho chavinho = new Chavinho(config);
			AtomicInteger contadorDeLotes = new AtomicInteger(0);

			chavinho.processarCsvEmLotes(Disciplina.class, 1, lote -> {
				contadorDeLotes.incrementAndGet();
				assertEquals(1, lote.size());
			});

			assertEquals(3, contadorDeLotes.get());
		}


	}
}