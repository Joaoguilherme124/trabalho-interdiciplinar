package com.example.trabalhointerdiciplinar.data.simulacao;

/** Armazenamento chave/valor mínimo (SharedPreferences no app, memória nos testes). */
public interface Armazenamento {
    String ler(String chave);

    void gravar(String chave, String valor);
}
