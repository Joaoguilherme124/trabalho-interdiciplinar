package com.example.trabalhointerdiciplinar.model;

public class Loja {
    private String id;
    private String idDono;
    private String nome;
    private String cnpj;
    private String categoria;
    private float avaliacao;
    private String logoUrl;
    private StatusLoja status;
    private String telefone;
    private String endereco;
    private String descricao;

    public Loja() {
    }

    public Loja(String id, String idDono, String nome, String cnpj, String categoria,
                float avaliacao, String logoUrl, StatusLoja status,
                String telefone, String endereco, String descricao) {
        this.id = id;
        this.idDono = idDono;
        this.nome = nome;
        this.cnpj = cnpj;
        this.categoria = categoria;
        this.avaliacao = avaliacao;
        this.logoUrl = logoUrl;
        this.status = status;
        this.telefone = telefone;
        this.endereco = endereco;
        this.descricao = descricao;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdDono() {
        return idDono;
    }

    public void setIdDono(String idDono) {
        this.idDono = idDono;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public float getAvaliacao() {
        return avaliacao;
    }

    public void setAvaliacao(float avaliacao) {
        this.avaliacao = avaliacao;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public StatusLoja getStatus() {
        return status;
    }

    public void setStatus(StatusLoja status) {
        this.status = status;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
}
