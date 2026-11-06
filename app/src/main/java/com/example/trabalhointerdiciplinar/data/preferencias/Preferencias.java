package com.example.trabalhointerdiciplinar.data.preferencias;

import android.content.Context;

import com.example.trabalhointerdiciplinar.data.simulacao.PrefsArmazenamento;
import com.example.trabalhointerdiciplinar.util.Animacoes;

/** Acesso único às preferências locais (favoritos e acessibilidade). */
public final class Preferencias {
    private static Preferencias instancia;
    private final Favoritos favoritos;
    private final PrefsAcessibilidade acessibilidade;
    private final java.util.concurrent.atomic.AtomicInteger versao = new java.util.concurrent.atomic.AtomicInteger();

    private Preferencias(Context contexto) {
        PrefsArmazenamento prefs = new PrefsArmazenamento(contexto, "paella_preferencias");
        favoritos = new Favoritos(prefs);
        acessibilidade = new PrefsAcessibilidade(prefs);
        Animacoes.definirReduzidas(acessibilidade.reduzirAnimacoes());
    }

    public static synchronized Preferencias obter(Context contexto) {
        if (instancia == null) instancia = new Preferencias(contexto.getApplicationContext());
        return instancia;
    }

    /** Muda sempre que uma preferência de acessibilidade é alterada; telas abertas se recriam. */
    public int versao() {
        return versao.get();
    }

    public void acessibilidadeAlterada() {
        Animacoes.definirReduzidas(acessibilidade.reduzirAnimacoes());
        versao.incrementAndGet();
    }

    public Favoritos favoritos() {
        return favoritos;
    }

    public PrefsAcessibilidade acessibilidade() {
        return acessibilidade;
    }
}
