package com.example.trabalhointerdiciplinar.util;

import java.text.Normalizer;
import java.util.Locale;

/** Utilitários de texto para busca: sem diferença de caixa nem de acentos. */
public final class Texto {
    private Texto() {
    }

    /** "Camarão" -> "camarao". Nulo vira texto vazio. */
    public static String normalizar(String texto) {
        if (texto == null) return "";
        String base = Normalizer.normalize(texto.trim().toLowerCase(Locale.ROOT), Normalizer.Form.NFD);
        return base.replaceAll("\\p{M}+", "");
    }
}
