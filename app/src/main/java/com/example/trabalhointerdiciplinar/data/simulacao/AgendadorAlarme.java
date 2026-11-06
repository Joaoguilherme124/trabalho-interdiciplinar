package com.example.trabalhointerdiciplinar.data.simulacao;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Agenda a próxima checagem do pedido com AlarmManager (alarme inexato, sem permissão especial):
 * um alarme por pedido, substituído a cada etapa, sem tarefas periódicas e sem manter nada
 * ativo. O sistema pode atrasar o disparo (Doze); por isso a fonte de verdade é o estado
 * persistido, reconciliado de novo sempre que o aplicativo volta a ser aberto.
 */
public final class AgendadorAlarme implements PedidoSimulador.Agendador {
    private static final long TOLERANCIA_MS = 3_000L;

    private final Context context;
    private final AlarmManager alarmes;
    /** Alvo já agendado por pedido: evita regravar o mesmo alarme a cada reconciliação. */
    private final Map<String, Long> alvos = new HashMap<>();
    private final Set<String> jaCancelados = new HashSet<>();

    public AgendadorAlarme(Context context) {
        this.context = context.getApplicationContext();
        this.alarmes = this.context.getSystemService(AlarmManager.class);
    }

    private PendingIntent intencao(String pedidoId) {
        Intent i = new Intent(context, ReceptorEtapa.class)
                .setAction("paella.ETAPA_PEDIDO")
                .putExtra(ReceptorEtapa.EXTRA_PEDIDO_ID, pedidoId);
        // O mesmo requestCode por pedido faz o novo alarme substituir o anterior.
        return PendingIntent.getBroadcast(context, pedidoId.hashCode(), i,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    @Override
    public synchronized void agendar(String pedidoId, long atrasoMs) {
        long alvo = System.currentTimeMillis() + Math.max(0, atrasoMs);
        Long anterior = alvos.get(pedidoId);
        if (anterior != null && Math.abs(anterior - alvo) < TOLERANCIA_MS) return;
        alvos.put(pedidoId, alvo);
        jaCancelados.remove(pedidoId);
        alarmes.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alvo, intencao(pedidoId));
    }

    @Override
    public synchronized void cancelar(String pedidoId) {
        boolean tinha = alvos.remove(pedidoId) != null;
        if (!tinha && !jaCancelados.add(pedidoId)) return;
        alarmes.cancel(intencao(pedidoId));
    }
}
