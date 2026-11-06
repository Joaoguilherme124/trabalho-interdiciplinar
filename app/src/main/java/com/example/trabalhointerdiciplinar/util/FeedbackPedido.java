package com.example.trabalhointerdiciplinar.util;

import android.view.View;

import androidx.core.content.ContextCompat;

import com.example.trabalhointerdiciplinar.R;
import com.example.trabalhointerdiciplinar.data.CarrinhoManager;

import com.google.android.material.snackbar.Snackbar;

/** "Adicionado ao pedido!" com Desfazer, no visual da marca. */
public final class FeedbackPedido {
    private FeedbackPedido() {
    }

    /**
     * @param ancora       view acima da qual a mensagem aparece (barra de pedido), ou nulo
     * @param alteracaoId  alteração do carrinho que o Desfazer reverte
     * @param aoDesfazer   executado depois de desfeito, para atualizar a tela
     */
    public static void adicionado(View raiz, View ancora, long alteracaoId, Runnable aoDesfazer) {
        Snackbar bar = Snackbar.make(raiz, R.string.adicionado_ao_pedido, 5000);
        bar.setAction(R.string.desfazer, v -> {
            if (CarrinhoManager.getInstance().desfazer(alteracaoId) && aoDesfazer != null) {
                aoDesfazer.run();
            }
        });
        int vinho = ContextCompat.getColor(raiz.getContext(), R.color.brand_vinho);
        int creme = ContextCompat.getColor(raiz.getContext(), R.color.brand_creme);
        int acafrao = ContextCompat.getColor(raiz.getContext(), R.color.brand_acafrao);
        bar.setBackgroundTint(vinho);
        bar.setTextColor(creme);
        bar.setActionTextColor(acafrao);
        if (ancora != null && ancora.getVisibility() == View.VISIBLE) bar.setAnchorView(ancora);
        if (!Animacoes.ativas()) bar.setAnimationMode(Snackbar.ANIMATION_MODE_FADE);
        bar.show();
    }
}
