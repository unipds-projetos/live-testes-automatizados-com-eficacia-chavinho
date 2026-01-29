package br.com.unipds.chavinho;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

public class ChavinhoTest {

    private Chavinho2 chavinho;

    @BeforeEach
    void setup() {
        chavinho = new Chavinho2();
    }

    @Test
    void deveRetornarUmaListaVaziaQuandoMeuInputForNulo() {
        // arrange
        String csv = null;

        // act
        List<Disciplina> lista = chavinho.leCsv(csv);

        // assert
        Assertions.assertNotNull(lista);
        Assertions.assertTrue(lista.isEmpty());
    }

    @Test
    void deveRetornarUmaListaVaziaQuandoMeuInputForVazio() {
        // arrange
        String csv = "";

        // act
        List<Disciplina> lista = chavinho.leCsv(csv);

        // assert
        Assertions.assertNotNull(lista);
        Assertions.assertTrue(lista.isEmpty());
    }

    @Test
    void dadoQueEuPasseUmaDisciplinaEuQueroAListBonitinha() {
        String csv = """
                0,Introdução ao Java
                """;

        List<Disciplina> lista = chavinho.leCsv(csv);

        Assertions.assertNotNull(lista);
        Assertions.assertEquals(1, lista.size());

        Disciplina disciplina = lista.get(0);

        Assertions.assertEquals(0, disciplina.numero());
        Assertions.assertEquals("Introdução ao Java", disciplina.nome());
    }

    @Test
    @DisplayName("Teste que valida a lista obtida a partir do CSV pegando a primeira e a ultima disciplina")
    void dadoQueEuPasseUmaListaDeDisciplinasEuQueroAListaCompletaBonitinha() {
        String csv = """
                número disciplina,nome disciplina
                0,Introdução ao Java
                1,Fundamentos do Java
                2,Desenvolvimento de Aplicações Back-End com Spring Boot e Quarkus + IA Corporativa com Java e Langchain4j
                3,Fundamentos de Front-End com React
                4,Arquitetura de Sistemas
                5,Software Design e System Design
                6,Concorrência e Multithreading em Java
                7,Infraestrutura e Cloud Computing com Docker Kubernetes e AWS
                8,Bancos de Dados Relacionais e NoSQL
                9,Testes Automatizados e Qualidade de Código
                10,Como Atrair as Melhores Vagas do Mercado
                """;

        List<Disciplina> lista = chavinho.leCsv(csv, true);

        Assertions.assertNotNull(lista);
        Assertions.assertEquals(11, lista.size());

        Disciplina disciplina = lista.getFirst();

        Assertions.assertEquals(0, disciplina.numero());
        Assertions.assertEquals("Introdução ao Java", disciplina.nome());

        disciplina = lista.getLast();

        Assertions.assertEquals(10, disciplina.numero());
        Assertions.assertEquals("Como Atrair as Melhores Vagas do Mercado", disciplina.nome());
    }

    @Test
    void deveLancarExcecaoSeOArquivoNaoExiste() {
        Assertions.assertThrows(ChavinhoException.class, () -> {
            chavinho.leCsvDeArquivo("arquivo/que/nao/existe", false);
        });
    }

    @Test
    void deveLerListaDeUmArquivoCsv() {
        Chavinho2 chavinho = new Chavinho2();

        List<Disciplina> lista = chavinho.leCsvDeArquivo("src/test/resources/unipds-disciplinas.csv",
                ",", true, Disciplina.class);

        Assertions.assertNotNull(lista);
        Assertions.assertEquals(11, lista.size());

        Disciplina disciplina = lista.getFirst();

        Assertions.assertEquals(0, disciplina.numero());
        Assertions.assertEquals("Introdução ao Java", disciplina.nome());

        disciplina = lista.getLast();

        Assertions.assertEquals(10, disciplina.numero());
        Assertions.assertEquals("Como Atrair as Melhores Vagas do Mercado", disciplina.nome());
    }

    @Test
    void deveLerListaDeUmOutroArquivoCsvCompletamenteDiferente() {
        Chavinho2 chavinho = new Chavinho2();
        List<ItemCardapio> lista =
                chavinho.leCsvDeArquivo("src/test/resources/itens-cardapio.csv",
                        ";", false, ItemCardapio.class);
        Assertions.assertNotNull(lista);
        Assertions.assertEquals(7, lista.size());

        ItemCardapio item = lista.getFirst();
        Assertions.assertEquals(1, item.id());
        Assertions.assertEquals("Refresco do Chaves", item.nome());

        item = lista.getLast();
        Assertions.assertEquals(7, item.id());
        Assertions.assertEquals("Tacos de Carnitas", item.nome());
    }

    @Test
    void deveLerListaDeUmOutroArquivoComClasseNormal() {
        Chavinho2 chavinho = new Chavinho2();
        List<Produto> lista =
                chavinho.leCsvDeArquivo("src/test/resources/products-2000000.csv",
                        ",", true, Produto.class);
        Assertions.assertNotNull(lista);
        Assertions.assertEquals(2000000, lista.size());

        Produto item = lista.getFirst();
        Assertions.assertEquals(1, item.getIndex());
        Assertions.assertEquals("Pro Charger Tablet Brush Go 360", item.getName());

        item = lista.getLast();
        Assertions.assertEquals(2000000, item.getIndex());
        Assertions.assertEquals("Smart Thermostat Lite", item.getName());
    }
}