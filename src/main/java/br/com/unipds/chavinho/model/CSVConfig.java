package br.com.unipds.chavinho.model;

public final class CSVConfig {
	private final String conteudo;
	private final String nomeArquivo;
	private final String separador;
	private final boolean temCabecalho;

	public CSVConfig(Builder builder) {
		conteudo = builder.conteudo;
		nomeArquivo = builder.nome;
		separador = builder.separador;
		temCabecalho = builder.temCabecalho;
	}

	public String getConteudo() {
		return conteudo;
	}

	public String getNomeArquivo() {
		return nomeArquivo;
	}

	public String getSeparador() {
		return separador;
	}

	public boolean isTemCabecalho() {
		return temCabecalho;
	}

	public static class Builder {
		private String conteudo;
		private String nome;
		private String separador;
		private boolean temCabecalho = false;

		public Builder conteudo(String conteudo) {
			this.conteudo = conteudo;
			return this;
		}

		public Builder nome(String nome) {
			this.nome = nome;
			return this;
		}

		public Builder separador(String separador) {
			this.separador = separador;
			return this;
		}

		public Builder temCabecalho(boolean temCabecalho) {
			this.temCabecalho = temCabecalho;
			return this;
		}

		public CSVConfig build() {
			return new CSVConfig(this);
		}
	}
}
