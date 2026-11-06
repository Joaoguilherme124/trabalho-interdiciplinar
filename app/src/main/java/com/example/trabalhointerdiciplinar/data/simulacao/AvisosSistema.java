package com.example.trabalhointerdiciplinar.data.simulacao;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.TaskStackBuilder;
import androidx.core.content.ContextCompat;

import com.example.trabalhointerdiciplinar.R;
import com.example.trabalhointerdiciplinar.model.StatusPedido;
import com.example.trabalhointerdiciplinar.ui.buyer.BuyerMainActivity;
import com.example.trabalhointerdiciplinar.ui.buyer.PedidoDetalheActivity;

/**
 * Notificações nativas do Android para o andamento do pedido (locais, sem push remoto). Sem a
 * permissão ou com as notificações desligadas, simplesmente não emite: a central interna e a
 * linha do tempo continuam funcionando.
 */
public final class AvisosSistema implements PedidoSimulador.Emissor {
    public static final String CANAL_ID = "pedidos_demo";

    private final Context context;

    public AvisosSistema(Context context) {
        this.context = context.getApplicationContext();
        criarCanal();
    }

    private void criarCanal() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;
        NotificationChannel canal = new NotificationChannel(CANAL_ID,
                context.getString(R.string.notif_canal_nome), NotificationManager.IMPORTANCE_HIGH);
        canal.setDescription(context.getString(R.string.notif_canal_desc));
        context.getSystemService(NotificationManager.class).createNotificationChannel(canal);
    }

    /** Permissão concedida (ou desnecessária, antes do Android 13) e notificações ligadas. */
    public boolean podeNotificar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        return NotificationManagerCompat.from(context).areNotificationsEnabled();
    }

    @Override
    public void emitir(AvisoPedido aviso) {
        if (!podeNotificar()) return;

        // Ao tocar: abre a jornada do pedido, com a aba Pedidos por baixo para o Voltar.
        Intent lista = new Intent(context, BuyerMainActivity.class)
                .putExtra(BuyerMainActivity.EXTRA_ABRIR_ABA, R.id.nav_pedidos);
        Intent jornada = new Intent(context, PedidoDetalheActivity.class)
                .putExtra(PedidoDetalheActivity.EXTRA_PEDIDO_ID, aviso.pedidoId);
        PendingIntent toque = TaskStackBuilder.create(context)
                .addNextIntent(lista)
                .addNextIntent(jornada)
                .getPendingIntent(aviso.pedidoId.hashCode(),
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        boolean pronto = aviso.etapa == StatusPedido.ENVIADO;
        Notification n = new NotificationCompat.Builder(context, CANAL_ID)
                .setSmallIcon(R.drawable.ic_notif_paella)
                .setColor(ContextCompat.getColor(context, R.color.brand_vinho))
                .setContentTitle(aviso.titulo)
                .setContentText(aviso.mensagem)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(aviso.mensagem))
                .setSubText(context.getString(R.string.notif_demonstracao))
                .setCategory(NotificationCompat.CATEGORY_STATUS)
                .setPriority(pronto ? NotificationCompat.PRIORITY_HIGH : NotificationCompat.PRIORITY_DEFAULT)
                .setOnlyAlertOnce(true)
                .setAutoCancel(true)
                .setContentIntent(toque)
                .build();
        // Um id por pedido: a etapa nova substitui a anterior em vez de empilhar avisos.
        NotificationManagerCompat.from(context).notify(aviso.pedidoId.hashCode(), n);
    }

    @Override
    public void cancelarDoPedido(String pedidoId) {
        NotificationManagerCompat.from(context).cancel(pedidoId.hashCode());
    }

    public void cancelarTodas() {
        NotificationManagerCompat.from(context).cancelAll();
    }
}
