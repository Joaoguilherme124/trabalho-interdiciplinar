package com.example.trabalhointerdiciplinar.data.cozinha;

import com.example.trabalhointerdiciplinar.model.Pedido;
import com.example.trabalhointerdiciplinar.model.StatusPedido;

import java.util.List;

/** Indicadores da Central da Cozinha, sempre derivados dos pedidos reais da loja. */
public final class IndicadoresCozinha {
    public final int novos;
    public final int emPreparo;
    public final int prontos;
    public final int finalizados;

    private IndicadoresCozinha(int novos, int emPreparo, int prontos, int finalizados) {
        this.novos = novos;
        this.emPreparo = emPreparo;
        this.prontos = prontos;
        this.finalizados = finalizados;
    }

    public static IndicadoresCozinha calcular(List<Pedido> pedidos) {
        int n = 0, e = 0, p = 0, f = 0;
        if (pedidos != null) {
            for (Pedido pedido : pedidos) {
                StatusPedido s = pedido.getStatus();
                if (s == StatusPedido.PENDENTE) n++;
                else if (s == StatusPedido.SEPARACAO) e++;
                else if (s == StatusPedido.ENVIADO) p++;
                else if (s == StatusPedido.ENTREGUE) f++;
            }
        }
        return new IndicadoresCozinha(n, e, p, f);
    }

    public int emAndamento() {
        return novos + emPreparo + prontos;
    }
}
