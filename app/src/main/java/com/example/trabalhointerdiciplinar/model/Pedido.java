package com.example.trabalhointerdiciplinar.model;

import java.util.ArrayList;
import java.util.List;

public class Pedido {
    private String id;
    private String idComprador;
    private String idLoja;
    private String nomeLoja;
    private List<ItemPedido> listaItens = new ArrayList<>();
    private double valorTotal;
    private FormaPagamento formaPagamento;
    private StatusPedido status;
    private String data;
    private String enderecoEntrega;

    public Pedido() {
    }

    public Pedido(String id, String idComprador, String idLoja, String nomeLoja,
                  List<ItemPedido> listaItens, double valorTotal,
                  FormaPagamento formaPagamento, StatusPedido status,
                  String data, String enderecoEntrega) {
        this.id = id;
        this.idComprador = idComprador;
        this.idLoja = idLoja;
        this.nomeLoja = nomeLoja;
        this.listaItens = listaItens != null ? listaItens : new ArrayList<>();
        this.valorTotal = valorTotal;
        this.formaPagamento = formaPagamento;
        this.status = status;
        this.data = data;
        this.enderecoEntrega = enderecoEntrega;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdComprador() {
        return idComprador;
    }

    public void setIdComprador(String idComprador) {
        this.idComprador = idComprador;
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

    public List<ItemPedido> getListaItens() {
        return listaItens;
    }

    public void setListaItens(List<ItemPedido> listaItens) {
        this.listaItens = listaItens;
    }

    public double getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(double valorTotal) {
        this.valorTotal = valorTotal;
    }

    public FormaPagamento getFormaPagamento() {
        return formaPagamento;
    }

    public void setFormaPagamento(FormaPagamento formaPagamento) {
        this.formaPagamento = formaPagamento;
    }

    public StatusPedido getStatus() {
        return status;
    }

    public void setStatus(StatusPedido status) {
        this.status = status;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getEnderecoEntrega() {
        return enderecoEntrega;
    }

    public void setEnderecoEntrega(String enderecoEntrega) {
        this.enderecoEntrega = enderecoEntrega;
    }
}
