package com.example.trabalhointerdiciplinar.util;

import android.content.res.ColorStateList;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.trabalhointerdiciplinar.R;
import com.example.trabalhointerdiciplinar.model.StatusPedido;

/** Selo colorido de status de pedido (texto em português; o enum interno não muda). */
public final class SeloStatus {
    private SeloStatus() {
    }

    public static void aplicar(TextView selo, StatusPedido status) {
        int fundo;
        int texto = R.color.white;
        switch (status) {
            case PENDENTE:
                fundo = R.color.brand_acafrao;
                texto = R.color.brand_grafite;
                break;
            case SEPARACAO:
            case ENVIADO:
                fundo = R.color.brand_mediterraneo;
                break;
            case ENTREGUE:
                fundo = R.color.brand_vinho;
                break;
            default:
                fundo = R.color.brand_grafite_muted;
                break;
        }
        selo.setText(PedidoApresentacao.rotuloStatus(status));
        selo.setBackgroundTintList(ColorStateList.valueOf(
                ContextCompat.getColor(selo.getContext(), fundo)));
        selo.setTextColor(ContextCompat.getColor(selo.getContext(), texto));
    }
}
