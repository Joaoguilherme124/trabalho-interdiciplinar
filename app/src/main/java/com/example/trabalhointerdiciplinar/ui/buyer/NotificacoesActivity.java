package com.example.trabalhointerdiciplinar.ui.buyer;

import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.trabalhointerdiciplinar.R;
import com.example.trabalhointerdiciplinar.data.DemoCatalog;
import com.example.trabalhointerdiciplinar.data.MockDataRepository;
import com.example.trabalhointerdiciplinar.data.SessionManager;
import com.example.trabalhointerdiciplinar.data.simulacao.AvisoPedido;
import com.example.trabalhointerdiciplinar.data.simulacao.Simulador;
import com.example.trabalhointerdiciplinar.model.Usuario;

import java.util.List;

/** Central de notificações do aplicativo: lista local do usuário, com estado lido/não lido. */
public class NotificacoesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notificacoes);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root_notificacoes), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
        findViewById(R.id.include_demo)
                .setVisibility(DemoCatalog.AMBIENTE_DEMO ? View.VISIBLE : View.GONE);
        findViewById(R.id.btn_voltar).setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        Simulador.obter(this).reconciliar();
        desenhar();
    }

    private void desenhar() {
        Usuario usuario = SessionManager.getInstance(this).getUsuario();
        LinearLayout lista = findViewById(R.id.lista_avisos);
        lista.removeAllViews();
        findViewById(R.id.aviso_sistema).setVisibility(
                Simulador.avisos(this).podeNotificar() ? View.GONE : View.VISIBLE);
        List<AvisoPedido> avisos = usuario == null
                ? java.util.Collections.emptyList()
                : Simulador.obter(this).central().listar(usuario.getId());
        findViewById(R.id.estado_vazio).setVisibility(avisos.isEmpty() ? View.VISIBLE : View.GONE);

        LayoutInflater inflater = LayoutInflater.from(this);
        for (AvisoPedido aviso : avisos) {
            View item = inflater.inflate(R.layout.item_notificacao, lista, false);
            ((TextView) item.findViewById(R.id.notif_titulo)).setText(aviso.titulo);
            ((TextView) item.findViewById(R.id.notif_mensagem)).setText(aviso.mensagem);
            ((TextView) item.findViewById(R.id.notif_tempo)).setText(DateUtils.getRelativeTimeSpanString(
                    aviso.criadoEm, System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS,
                    DateUtils.FORMAT_ABBREV_RELATIVE));
            ((ImageView) item.findViewById(R.id.notif_icone)).setImageResource(iconeDaEtapa(aviso));
            item.findViewById(R.id.notif_ponto).setVisibility(aviso.lida ? View.GONE : View.VISIBLE);
            item.setAlpha(aviso.lida ? 0.78f : 1f);
            item.setOnClickListener(v -> abrir(aviso, usuario));
            lista.addView(item);
        }
    }

    private int iconeDaEtapa(AvisoPedido aviso) {
        switch (aviso.etapa) {
            case SEPARACAO:
                return R.drawable.ic_notif_paella;
            case ENVIADO:
                return R.drawable.ic_home_sino;
            case ENTREGUE:
                return R.drawable.ic_home_sacola;
            default:
                return R.drawable.ic_etapa_check;
        }
    }

    /** Marca como lida e abre a jornada do pedido correspondente. */
    private void abrir(AvisoPedido aviso, Usuario usuario) {
        if (usuario != null) {
            Simulador.obter(this).central().marcarLida(aviso.id, usuario.getId());
        }
        if (MockDataRepository.getInstance().getPedidoById(aviso.pedidoId) != null) {
            startActivity(new Intent(this, PedidoDetalheActivity.class)
                    .putExtra(PedidoDetalheActivity.EXTRA_PEDIDO_ID, aviso.pedidoId));
        }
        desenhar();
    }
}
