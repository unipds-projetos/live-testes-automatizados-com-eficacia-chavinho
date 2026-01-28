# Chavinho

Este é o projeto Chavinho, uma biblioteca Java simples para ler arquivos CSV e convertê-los em listas de objetos. 
O nome do projeto é uma homenagem ao personagem "Chaves", e foi desenvolvido no contexto da UNIPDS.

## Funcionalidades

O projeto oferece duas classes principais para a leitura de CSV:

*   `Chavinho`: Uma classe para ler um CSV específico de `Disciplina`s.
*   `Chavinho2`: Uma classe mais genérica que usa reflection para converter as linhas do CSV em objetos de qualquer tipo de Record.

## Como Usar

### Chavinho

A classe `Chavinho` é usada para ler um CSV com ou sem cabeçalho e convertê-lo em uma `List<Disciplina>`.

```java
// Exemplo de uso do Chavinho

Chavinho chavinho = new Chavinho();

// Lendo de uma String
String csv = """
    0,Introdução ao Java
    1,Fundamentos do Java
    """;
List<Disciplina> disciplinas = chavinho.leCsv(csv);

// Lendo de um arquivo
List<Disciplina> disciplinasDeArquivo = chavinho.leCsvDeArquivo("caminho/para/disciplinas.csv", true);
```

### Chavinho2

A classe `Chavinho2` é mais flexível e pode ser usada para ler qualquer arquivo CSV e convertê-lo em uma lista de Records.

```java
// Exemplo de uso do Chavinho2 com um Record ItemCardapio

public record ItemCardapio(int id, String nome, String descricao, double preco, boolean disponivel) {
}

Chavinho2 chavinho2 = new Chavinho2();
List<ItemCardapio> cardapio = chavinho2.leCsvDeArquivo(
    "caminho/para/cardapio.csv",
    ";", // separador
    true, // tem cabeçalho
    ItemCardapio.class
);
```

## Como Construir o Projeto

Para construir o projeto, você precisa ter o Maven instalado. 

Em seguida, execute o seguinte comando na raiz do projeto:

```bash
mvn clean install
```

## Como Executar os Testes

Para executar os testes, execute o seguinte comando:

```bash
mvn test
```

## Sobre

Este projeto foi desenvolvido na live "Testes Automatizados com Eficácia" da pós Java Elite da [UNIPDS](https://www.unipds.com.br).

## TODO:

- eliminar o `Chavinho` primeira versão deixando apenas o `Chavinho2`
- usar o `Builder` pattern para definir configurações da classe `Chavinho` como se tem cabeçalho ou não e o separador
- verificar se a `Class<T>` passada para o `leCsvDeArquivo` é um `Record` ou não. Dar suporte a classes que não são records.
- melhorar o código em geral
- limitar a JVM de execução do código para 128 MB com `-Xmx 128m` e tratar o seguinte CSV de 311 MB: https://drive.google.com/uc?id=18BLAZDeH74Ll3b4GsMNY3s-YVnNmWblC&export=download 
