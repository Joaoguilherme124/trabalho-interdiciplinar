package com.example.trabalhointerdiciplinar.model;

public class Usuario {
    private String id;
    private String nome;
    private String email;
    private String senha;
    private TipoUsuario tipo;
    private String endereco;
    private String telefone;
    /** Conta criada pelo admin: o lojista deve trocar e-mail e senha no primeiro acesso. */
    private boolean precisaTrocarCredenciais;

    public Usuario() {
    }

    public Usuario(String id, String nome, String email, String senha,
                   TipoUsuario tipo, String endereco, String telefone) {
        this(id, nome, email, senha, tipo, endereco, telefone, false);
    }

    public Usuario(String id, String nome, String email, String senha,
                   TipoUsuario tipo, String endereco, String telefone,
                   boolean precisaTrocarCredenciais) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.tipo = tipo;
        this.endereco = endereco;
        this.telefone = telefone;
        this.precisaTrocarCredenciais = precisaTrocarCredenciais;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public TipoUsuario getTipo() {
        return tipo;
    }

    public void setTipo(TipoUsuario tipo) {
        this.tipo = tipo;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public boolean isPrecisaTrocarCredenciais() {
        return precisaTrocarCredenciais;
    }

    public void setPrecisaTrocarCredenciais(boolean precisaTrocarCredenciais) {
        this.precisaTrocarCredenciais = precisaTrocarCredenciais;
    }
}
