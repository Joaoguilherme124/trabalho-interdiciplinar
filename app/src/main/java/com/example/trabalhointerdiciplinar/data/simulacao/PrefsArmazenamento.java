package com.example.trabalhointerdiciplinar.data.simulacao;

import android.content.Context;
import android.content.SharedPreferences;

/** {@link Armazenamento} sobre SharedPreferences privadas do aplicativo. */
public final class PrefsArmazenamento implements Armazenamento {
    private static final String ARQUIVO = "paella_simulacao";
    private final SharedPreferences prefs;

    public PrefsArmazenamento(Context context) {
        this(context, ARQUIVO);
    }

    public PrefsArmazenamento(Context context, String arquivo) {
        prefs = context.getApplicationContext().getSharedPreferences(arquivo, Context.MODE_PRIVATE);
    }

    @Override
    public String ler(String chave) {
        return prefs.getString(chave, null);
    }

    @Override
    public void gravar(String chave, String valor) {
        prefs.edit().putString(chave, valor).apply();
    }

    boolean lerFlag(String chave) {
        return prefs.getBoolean(chave, false);
    }

    void gravarFlag(String chave, boolean valor) {
        prefs.edit().putBoolean(chave, valor).apply();
    }
}
