package com.example.trabalhointerdiciplinar.util;

import java.util.Locale;

/** Formatações do Perfil. */
public final class PerfilApresentacao {
    private PerfilApresentacao() {
    }

    /** Iniciais do avatar: primeira letra do primeiro e do último nome ("João Silva" → "JS"). */
    public static String iniciais(String nome) {
        if (nome == null || nome.trim().isEmpty()) return "?";
        String[] partes = nome.trim().split("\\s+");
        String primeira = primeiraLetra(partes[0]);
        if (partes.length == 1) return primeira;
        return primeira + primeiraLetra(partes[partes.length - 1]);
    }

    /** Primeiro nome, para saudações. */
    public static String primeiroNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) return "";
        return nome.trim().split("\\s+")[0];
    }

    private static String primeiraLetra(String palavra) {
        return palavra.substring(0, palavra.offsetByCodePoints(0, 1)).toUpperCase(Locale.ROOT);
    }
}
