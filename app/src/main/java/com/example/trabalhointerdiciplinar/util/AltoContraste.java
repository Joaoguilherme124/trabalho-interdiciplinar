package com.example.trabalhointerdiciplinar.util;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.trabalhointerdiciplinar.R;

import com.google.android.material.card.MaterialCardView;

/**
 * Alto contraste: troca os cinzas e transparências de texto por grafite sólido e reforça as bordas
 * dos cartões. Mantém as cores da marca; para desfazer, a tela é recriada.
 */
public final class AltoContraste {
    private AltoContraste() {
    }

    /** Aplica agora e a cada novo layout (listas e diálogos criam views depois). */
    public static void observar(View raiz) {
        aplicar(raiz);
        raiz.getViewTreeObserver().addOnGlobalLayoutListener(() -> aplicar(raiz));
    }

    public static void aplicar(View v) {
        int grafite = ContextCompat.getColor(v.getContext(), R.color.brand_grafite);
        int mudo = ContextCompat.getColor(v.getContext(), R.color.brand_grafite_muted);
        int vinho = ContextCompat.getColor(v.getContext(), R.color.brand_vinho);
        aplicar(v, grafite, mudo, vinho);
    }

    private static void aplicar(View v, int grafite, int mudo, int vinho) {
        if (v instanceof TextView) {
            TextView t = (TextView) v;
            int cor = t.getCurrentTextColor();
            if (cor == mudo || cor == 0xB326282D) t.setTextColor(grafite);
            int dica = t.getCurrentHintTextColor();
            if (dica == mudo) t.setHintTextColor(grafite);
        }
        if (v instanceof MaterialCardView) {
            MaterialCardView c = (MaterialCardView) v;
            if (c.getStrokeWidth() > 0 && c.getStrokeColor() != 0xFFF5B82E) {
                c.setStrokeColor(vinho);
            }
        }
        if (v instanceof ViewGroup) {
            ViewGroup g = (ViewGroup) v;
            for (int i = 0; i < g.getChildCount(); i++) aplicar(g.getChildAt(i), grafite, mudo, vinho);
        }
    }
}
