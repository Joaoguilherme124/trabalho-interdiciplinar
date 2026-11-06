package com.example.trabalhointerdiciplinar.data.simulacao;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** Disparado pelo alarme na virada de etapa (mesmo com o app fechado): só reconcilia o estado. */
public final class ReceptorEtapa extends BroadcastReceiver {
    public static final String EXTRA_PEDIDO_ID = "pedido_id";

    @Override
    public void onReceive(Context context, Intent intent) {
        Simulador.obter(context).reconciliar();
    }
}
