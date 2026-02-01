# Desafio Chavinho2
## Processamento eficiente de CSV com Testes Automatizados

Este projeto faz parte do desafio prático apresentado na live  
**“Testes Automatizados com Eficiência”**, cujo objetivo foi evoluir a
implementação original do *Chavinho* aplicando boas práticas de design,
testes automatizados e eficiência no uso de memória.

---

## 🎯 Objetivos do Desafio

Os principais TODOs propostos foram:

- Remover a implementação original do **Chavinho**
- Criar uma nova versão (**Chavinho2**) utilizando **Builder Pattern**
- Garantir leitura de CSV utilizando **`record`**
- Manter compatibilidade com os testes existentes
- Processar arquivos grandes (≈ 311 MB)
- Garantir execução com JVM limitada a **128 MB de heap**
- Validar tudo através de **testes automatizados (TDD)**

---

## 🧱 Arquitetura da Solução

### Chavinho2

Classe imutável criada via **Builder Pattern**, responsável apenas por
configurações de leitura do CSV.

#### Responsabilidades

- Definir o separador do arquivo
- Indicar se o CSV possui cabeçalho

#### Exemplo de uso

```
Chavinho2 chavinho2 = Chavinho2.builder()
    .separador(",")
    .temCabecalho(true)
    .build();
```

---

**Chavinho2Service**

Classe responsável pela leitura e processamento dos arquivos CSV.

**Métodos disponíveis**

```leCsvDeArquivo```

```
public <T> List<T> leCsvDeArquivo(
    String nomeArquivo,
    Class<T> classe,
    Chavinho2 chavinho2
)
```
**Características**

- Lê todo o arquivo em memória
- Retorna uma lista de record
- Indicado para arquivos pequenos ou médios
- Mantido por compatibilidade e simplicidade

processaCsvDeArquivo

public <T> void processaCsvDeArquivo(
    String nomeArquivo,
    Class<T> classe,
    Chavinho2 chavinho2,
    Consumer<T> consumer
)

**Características**

- Processamento **linha a linha**
- Não acumula dados em memória
- Utiliza ```Files.lines()``` (stream lazy)
- Ideal para arquivos grandes
- Permite execução com heap reduzido
- Linhas mal formatadas são ignoradas para evitar falhas em massa

---

**🧪 Testes Automatizados**

Os testes cobrem os seguintes cenários:

- Leitura de CSV com e sem cabeçalho
- Diferentes separadores de campo
- Uso obrigatório de ```record```
- Arquivo inexistente
- Arquivo inválido
- Processamento de arquivo grande sem estouro de memória

---

**Teste principal do desafio**

```java
@Test
@DisplayName("Deve processar arquivo grande sem estourar memória")
void deveProcessarArquivoGrandeSemEstourarMemoria() {
    Assertions.assertDoesNotThrow(() ->
        chavinho2Service.processaCsvDeArquivo(
            csv,
            Product.class,
            chavinho2,
            product -> contador.incrementAndGet()
        )
    );
}

```

**O que este teste valida**

- O arquivo completo é percorrido
- Nenhuma exceção de memória é lançada
- O processamento ocorre de forma streaming

    O **teste não valida quantidade de linhas**, e sim o
    **comportamento do sistema sob restrição de memória**.

---

🧠 **Uso de Memória**

Para validação do desafio, os testes foram executados com a seguinte
configuração de JVM:

```
-Xmx128m
```

O método ```processaCsvDeArquivo``` foi desenhado especificamente para operar
dentro desse limite, processando o CSV de forma sequencial e sem retenção
de dados no heap.

---

**⚠ Decisões Técnicas**

- Linhas mal formatadas são ignoradas durante o processamento streaming
- Não há logging por linha inválida para evitar impacto de performance
- A leitura completa do arquivo foi mantida apenas para cenários menores
- O foco do desafio foi robustez e eficiência, não validação semântica
    completa do CSV