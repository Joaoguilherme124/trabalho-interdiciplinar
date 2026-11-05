package com.example.trabalhointerdiciplinar.model;

public class ItemPedido {
    private String idProduto;
    private String titulo;
    private int quantidade;
    private double precoUnitario;

    public ItemPedido() {
    }

    public ItemPedido(String idProduto, String titulo, int quantidade, double precoUnitario) {
        this.idProduto = idProduto;
        this.titulo = titulo;
        this.quantidade = quantidade;
        this.precoUnitario = precoUnitario;
    }

    public String getIdProduto() {
        return idProduto;
    }

    public void setIdProduto(String idProduto) {
        this.idProduto = idProduto;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

    public double getPrecoUnitario() {
        return precoUnitario;
    }

    public void setPrecoUnitario(double precoUnitario) {
        this.precoUnitario = precoUnitario;
    }

    public double getSubtotal() {
        return quantidade * precoUnitario;
    }
}
