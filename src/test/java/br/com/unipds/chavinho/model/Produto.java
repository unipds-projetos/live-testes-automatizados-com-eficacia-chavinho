package br.com.unipds.chavinho.model;

public class Produto {
    private int id;
    private String nome;
    private double preco;


    public int getId() {
        return id;
    }
    
    public String getNome() {
        return nome;
    }
    
    public double getPreco() {
        return preco;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Produto produto = (Produto) o;
        return id == produto.id &&
               Double.compare(produto.preco, preco) == 0 &&
               nome.equals(produto.nome);
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + nome.hashCode();
        result = 31 * result + Double.hashCode(preco);
        return result;
    }
}
