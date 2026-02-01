package br.com.unipds.chavinho2;

import br.com.unipds.chavinho2.record.Product;
import br.com.unipds.exception.Chavinho2Exception;
import br.com.unipds.chavinho2.record.Disciplina;
import br.com.unipds.chavinho2.model.ClasseNaoRecord;
import br.com.unipds.chavinho2.record.ItemCardapio;
import br.com.unipds.service.Chavinho2Service;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class Chavinho2Test {

    private final Chavinho2Service chavinho2Service = new Chavinho2Service();

    @Test
    @DisplayName("Deve ler CSV sem cabeçalho usando record e separador ';'")
    void deveLerCsvSemCabecalhoComRecordESeparadorPontoEVirgula() {
        String csv = "src/test/resources/itens-cardapio.csv";

        Chavinho2 chavinho2 = Chavinho2.builder()
                .separador(";")
                .temCabecalho(false)
                .build();
        List<ItemCardapio> lista = chavinho2Service.leCsvDeArquivo(csv, ItemCardapio.class, chavinho2);
        ItemCardapio ultimo = lista.getLast();

        Assertions.assertNotNull(lista);
        Assertions.assertEquals(7, lista.size());
        Assertions.assertEquals(1L, lista.getFirst().id());
        Assertions.assertEquals("Tacos de Carnitas", ultimo.nome());
        Assertions.assertTrue(lista.getFirst().getClass().isRecord());
    }

    @Test
    @DisplayName("Deve ignorar a primeira linha quando CSV possui cabeçalho")
    void deveLerCsvComCabecalhoIgnorandoPrimeiraLinha() {
        String csv = "src/test/resources/unipds-disciplinas.csv";

        Chavinho2 chavinho2 = Chavinho2.builder()
                .separador(",")
                .temCabecalho(true)
                .build();
        List<Disciplina> lista = chavinho2Service.leCsvDeArquivo(csv, Disciplina.class, chavinho2);

        Assertions.assertNotNull(lista);
        Assertions.assertEquals(11, lista.size());

        Disciplina primeiro = lista.getFirst();
        Assertions.assertEquals(0, primeiro.numero());

        Disciplina ultimo = lista.getLast();
        Assertions.assertEquals("Como Atrair as Melhores Vagas do Mercado ", ultimo.nome());
        Assertions.assertTrue(ultimo.getClass().isRecord());
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar ler arquivo inválido")
    void deveLancarExcecaoAoLerArquivoInvalido() {
        String csv = "src/test/resources/itens-cardapio-invalido.csv";

        Chavinho2 chavinho2 = Chavinho2.builder()
                .separador(";")
                .temCabecalho(false)
                .build();

        Chavinho2Exception ex = Assertions.assertThrows(Chavinho2Exception.class, () ->
                chavinho2Service.leCsvDeArquivo(csv, ItemCardapio.class, chavinho2));

        Assertions.assertInstanceOf(NumberFormatException.class, ex.getCause());
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar ler arquivo inexistente")
    void deveLancarExcecaoAoLerArquivoInexistente() {
        String csv = "src/test/resources/arquivo-inexistente.csv";

        Chavinho2 chavinho2 = Chavinho2.builder()
                .separador(";")
                .temCabecalho(false)
                .build();

        Chavinho2Exception ex = Assertions.assertThrows(Chavinho2Exception.class, () ->
                chavinho2Service.leCsvDeArquivo(csv, ItemCardapio.class, chavinho2));

        Assertions.assertInstanceOf(NoSuchFileException.class, ex.getCause());
    }

    @Test
    @DisplayName("Deve lançar exceção quando a classe não for um record")
    void deveLancarExcecaoQuandoClasseNaoForRecord() {
        String csv = "src/test/resources/itens-cardapio.csv";

        Chavinho2 chavinho2 = Chavinho2.builder()
                .separador(";")
                .temCabecalho(false)
                .build();

        Chavinho2Exception ex = Assertions.assertThrows(Chavinho2Exception.class, () ->
                chavinho2Service.leCsvDeArquivo(csv, ClasseNaoRecord.class, chavinho2));

        Assertions.assertEquals(
                "A classe informada deve ser um record.",
                ex.getMessage()
        );
    }

    @Test
    @DisplayName("Deve lançar exceção quando input for nulo")
    void deveLancarExcecaoQuandoNaoEncontrarOArquivo() {
        Chavinho2 chavinho2 = Chavinho2.builder()
                .separador(";")
                .temCabecalho(false)
                .build();

        Assertions.assertThrows(NullPointerException.class, () -> {
            chavinho2Service.leCsvDeArquivo(null, ItemCardapio.class, chavinho2);
        });
    }

    @Test
    @DisplayName("Deve lançar exceção quando input for vazio")
    void deveLancarExcessaoQuandoInputForVazio() {
        Chavinho2 chavinho2 = Chavinho2.builder()
                .separador(";")
                .temCabecalho(false)
                .build();
        Chavinho2Exception ex = Assertions.assertThrows(Chavinho2Exception.class, () -> {
            chavinho2Service.leCsvDeArquivo("", ItemCardapio.class, chavinho2);
        });

        Assertions.assertInstanceOf(UncheckedIOException.class, ex.getCause());
    }

    @Test
    @DisplayName("Deve processar arquivo grande sem estourar memória")
    void deveProcessarArquivoGrandeSemEstourarMemoria() {
        String csv = "src/test/resources/products-2000000.csv";

        Chavinho2 chavinho2 = Chavinho2.builder()
                .separador(",")
                .temCabecalho(true)
                .build();

        Chavinho2Service chavinho2Service = new Chavinho2Service();

        Assertions.assertDoesNotThrow(() ->
                chavinho2Service.processaCsvDeArquivo(
                        csv,
                        Product.class,
                        chavinho2,
                        product -> {}
                )
        );
    }

}
