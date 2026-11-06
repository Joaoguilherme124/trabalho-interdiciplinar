package com.example.trabalhointerdiciplinar.util;

import android.view.View;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/** Ajustes de janela (edge-to-edge) compartilhados pelas telas de entrada. */
public final class JanelaInsets {
    private JanelaInsets() {
    }

    /**
     * Para telas com faixa colorida no topo: a faixa continua sob a barra de status, mas ganha
     * o espaço dela como preenchimento; a raiz sobe com o teclado e com a barra de navegação.
     */
    /**
     * Login: o topo ganha o espaço da barra de status (o grafismo nasce logo abaixo dela); o
     * conteúdo sobe junto com o teclado e os grafismos da base somem enquanto ele está aberto.
     * Sem teclado, a base sangra até a borda inferior da tela.
     */
    public static void aplicarNoLogin(View rolagem, View topo, View baseDecorativa) {
        final int topoOriginal = topo.getPaddingTop();
        ViewCompat.setOnApplyWindowInsetsListener(rolagem, (v, insets) -> {
            Insets barras = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets teclado = insets.getInsets(WindowInsetsCompat.Type.ime());
            boolean tecladoAberto = insets.isVisible(WindowInsetsCompat.Type.ime());
            topo.setPadding(topo.getPaddingLeft(), topoOriginal + barras.top,
                    topo.getPaddingRight(), topo.getPaddingBottom());
            v.setPadding(barras.left, 0, barras.right, tecladoAberto ? teclado.bottom : 0);
            baseDecorativa.setVisibility(tecladoAberto ? View.GONE : View.VISIBLE);
            return insets;
        });
    }

    public static void aplicarComCabecalho(View raiz, View cabecalho) {
        final int topoOriginal = cabecalho.getPaddingTop();
        ViewCompat.setOnApplyWindowInsetsListener(raiz, (v, insets) -> {
            Insets barras = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets teclado = insets.getInsets(WindowInsetsCompat.Type.ime());
            cabecalho.setPadding(cabecalho.getPaddingLeft(), topoOriginal + barras.top,
                    cabecalho.getPaddingRight(), cabecalho.getPaddingBottom());
            v.setPadding(barras.left, 0, barras.right, Math.max(barras.bottom, teclado.bottom));
            return insets;
        });
    }
}
