package com.example.trabalhointerdiciplinar.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import com.example.trabalhointerdiciplinar.R;
import com.example.trabalhointerdiciplinar.model.Produto;

/** Carrega a foto do produto a partir do nome de drawable guardado em {@code urlImagem}. */
public final class ProdutoImagens {
    private ProdutoImagens() {
    }

    /** Fotos demonstrativas oferecidas no cadastro de produto: {chave do drawable, rótulo}. */
    public static final String[][] OPCOES = {
            {"paella_hero", "Paella mista"},
            {"img_paella_valenciana", "Paella valenciana"},
            {"img_paella_frutos_do_mar", "Paella de frutos do mar"},
            {"img_combo_mista", "Combo paella mista"},
            {"img_combo_frutos_do_mar", "Combo frutos do mar"},
            {"img_bebida_suco", "Suco"},
            {"img_bebida_agua", "Água"},
    };

    /** Posição da chave em {@link #OPCOES}; 0 quando ausente. */
    public static int indiceDaChave(String chave) {
        for (int i = 0; i < OPCOES.length; i++) {
            if (OPCOES[i][0].equals(chave)) return i;
        }
        return 0;
    }

    /**
     * Versão leve: decodifica a imagem já reduzida (potência de 2) para que o menor lado fique
     * com pelo menos {@code ladoMinimoPx}. Evita carregar a foto inteira para miniaturas e cards.
     */
    public static void carregarReduzida(ImageView view, Produto produto, int ladoMinimoPx) {
        Context context = view.getContext();
        String nome = produto.getUrlImagem();
        int resId = 0;
        if (nome != null && !nome.isEmpty()) {
            resId = context.getResources().getIdentifier(nome, "drawable", context.getPackageName());
        }
        if (resId == 0) {
            view.setImageResource(R.drawable.bg_image_placeholder);
            return;
        }
        BitmapFactory.Options opcoes = new BitmapFactory.Options();
        opcoes.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(context.getResources(), resId, opcoes);
        int amostra = 1;
        while (opcoes.outWidth > 0
                && opcoes.outWidth / (amostra * 2) >= ladoMinimoPx
                && opcoes.outHeight / (amostra * 2) >= ladoMinimoPx) {
            amostra *= 2;
        }
        opcoes.inJustDecodeBounds = false;
        opcoes.inSampleSize = amostra;
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resId, opcoes);
        if (bitmap != null) {
            view.setImageBitmap(bitmap);
        } else {
            view.setImageResource(resId);
        }
    }

    public static void carregar(ImageView view, Produto produto) {
        Context context = view.getContext();
        String nome = produto.getUrlImagem();
        int resId = 0;
        if (nome != null && !nome.isEmpty()) {
            resId = context.getResources().getIdentifier(nome, "drawable", context.getPackageName());
        }
        view.setImageResource(resId != 0 ? resId : R.drawable.bg_image_placeholder);
    }
}
