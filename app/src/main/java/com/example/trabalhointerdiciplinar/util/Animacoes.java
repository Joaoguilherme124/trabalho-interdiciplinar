package com.example.trabalhointerdiciplinar.util;

import android.animation.ValueAnimator;
import android.view.View;
import android.view.ViewGroup;

/** Animações discretas compartilhadas pelas telas. */
public final class Animacoes {
    private static final long DURACAO_MS = 280L;
    private static final long ATRASO_ENTRE_ITENS_MS = 50L;

    private static volatile boolean reduzidas;

    private Animacoes() {
    }

    /** Preferência do app "Reduzir animações". */
    public static void definirReduzidas(boolean ativo) {
        reduzidas = ativo;
    }

    /** Animações decorativas permitidas: sistema E preferência do app. */
    public static boolean ativas() {
        return !reduzidas && ValueAnimator.areAnimatorsEnabled();
    }

    /**
     * Entrada leve em cascata (fade + pequeno deslocamento) dos filhos diretos do container.
     * Não anima quando o usuário desativou animações no sistema.
     */
    public static void entradaEmCascata(ViewGroup container) {
        if (!ativas()) {
            return;
        }
        float deslocamento = 12 * container.getResources().getDisplayMetrics().density;
        for (int i = 0; i < container.getChildCount(); i++) {
            View filho = container.getChildAt(i);
            filho.setAlpha(0f);
            filho.setTranslationY(deslocamento);
            filho.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setStartDelay(i * ATRASO_ENTRE_ITENS_MS)
                    .setDuration(DURACAO_MS)
                    .start();
        }
    }
}
