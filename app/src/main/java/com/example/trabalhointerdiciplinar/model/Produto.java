package com.example.trabalhointerdiciplinar.model;

public class Produto {
    private String id;
    private String idLoja;
    private String nomeLoja;
    private String titulo;
    private String descricao;
    private double preco;
    private int estoque;
    private String urlImagem;
    private float avaliacao;
    private String categoria;
    private String ingredientes = "";
    private String porcao = "";
    /** Tempo estimado de preparo em minutos (0 = não informado). */
    private int tempoEstimadoMin;
    /** Produto ativo no cardápio. Desativar não apaga: apenas deixa de ser publicado. */
    private boolean disponivel = true;

    public Produto() {
    }

    public Produto(String id, String idLoja, String nomeLoja, String titulo, String descricao,
                   double preco, int estoque, String urlImagem, float avaliacao, String categoria) {
        this.id = id;
        this.idLoja = idLoja;
        this.nomeLoja = nomeLoja;
        this.titulo = titulo;
        this.descricao = descricao;
        this.preco = preco;
        this.estoque = estoque;
        this.urlImagem = urlImagem;
        this.avaliacao = avaliacao;
        this.categoria = categoria;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdLoja() {
        return idLoja;
    }

    public void setIdLoja(String idLoja) {
        this.idLoja = idLoja;
    }

    public String getNomeLoja() {
        return nomeLoja;
    }

    public void setNomeLoja(String nomeLoja) {
        this.nomeLoja = nomeLoja;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public double getPreco() {
        return preco;
    }

    public void setPreco(double preco) {
        this.preco = preco;
    }

    public int getEstoque() {
        return estoque;
    }

    public void setEstoque(int estoque) {
        this.estoque = estoque;
    }

    public String getUrlImagem() {
        return urlImagem;
    }

    public void setUrlImagem(String urlImagem) {
        this.urlImagem = urlImagem;
    }

    public float getAvaliacao() {
        return avaliacao;
    }

    public void setAvaliacao(float avaliacao) {
        this.avaliacao = avaliacao;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getIngredientes() {
        return ingredientes == null ? "" : ingredientes;
    }

    public void setIngredientes(String ingredientes) {
        this.ingredientes = ingredientes;
    }

    public String getPorcao() {
        return porcao == null ? "" : porcao;
    }

    public void setPorcao(String porcao) {
        this.porcao = porcao;
    }

    public int getTempoEstimadoMin() {
        return tempoEstimadoMin;
    }

    public void setTempoEstimadoMin(int tempoEstimadoMin) {
        this.tempoEstimadoMin = Math.max(0, tempoEstimadoMin);
    }

    public boolean isDisponivel() {
        return disponivel;
    }

    public void setDisponivel(boolean disponivel) {
        this.disponivel = disponivel;
    }
}
