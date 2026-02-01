package br.com.unipds.chavinho2.model;

public class ClasseNaoRecord {

    long id;
    String nome;
    String descricao;
    double preco;
    String categoria;
    boolean emPromocao;
    double precoComDesconto;
    boolean impostoIsento;

    public ClasseNaoRecord(long id, String nome, String descricao, double preco, String categoria,
                        boolean emPromocao, double precoComDesconto, boolean impostoIsento) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.preco = preco;
        this.categoria = categoria;
        this.emPromocao = emPromocao;
        this.precoComDesconto = precoComDesconto;
        this.impostoIsento = impostoIsento;
    }

    public long id() { return id; }
    public String nome() { return nome; }
    public String descricao() { return descricao; }
    public double preco() { return preco; }
    public String categoria() { return categoria; }
    public boolean emPromocao() { return emPromocao; }
    public double precoComDesconto() { return precoComDesconto; }
    public boolean impostoIsento() { return impostoIsento; }

}
