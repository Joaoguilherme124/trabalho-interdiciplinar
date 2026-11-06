package com.example.trabalhointerdiciplinar.ui.buyer;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.drawable.Animatable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.trabalhointerdiciplinar.R;
import com.example.trabalhointerdiciplinar.data.DemoCatalog;
import com.example.trabalhointerdiciplinar.data.MockDataRepository;
import com.example.trabalhointerdiciplinar.data.SessionManager;
import com.example.trabalhointerdiciplinar.data.simulacao.Cronograma;
import com.example.trabalhointerdiciplinar.data.simulacao.PedidoSimulador;
import com.example.trabalhointerdiciplinar.data.simulacao.Simulador;
import com.example.trabalhointerdiciplinar.model.Pedido;
import com.example.trabalhointerdiciplinar.model.StatusPedido;
import com.example.trabalhointerdiciplinar.model.Usuario;
import com.example.trabalhointerdiciplinar.util.MoneyUtils;
import com.example.trabalhointerdiciplinar.util.PedidoApresentacao;
import com.example.trabalhointerdiciplinar.util.QrCodes;
import com.example.trabalhointerdiciplinar.util.SeloStatus;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.Collections;

/**
 * Minha Jornada: acompanhamento do pedido (cartão de status, previsão demonstrativa, linha do
 * tempo e QR Code). Lê tudo do estado do pedido; o tempo só redesenha a tela, não a controla.
 */
public class PedidoDetalheActivity extends AppCompatActivity {
    public static final String EXTRA_PEDIDO_ID = "pedido_id";
    private static final int QR_TAMANHO_PX = 640;
    private static final long TICK_MS = 1_000L;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable tick = new Runnable() {
        @Override
        public void run() {
            simulador.reconciliar();
            renderizar(true);
            handler.postDelayed(this, TICK_MS);
        }
    };

    private PedidoSimulador simulador;
    private Pedido pedido;
    private int indiceExibido = -2;     // etapa já desenhada; -2 = ainda nada
    private boolean qrDesenhado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pedido_detalhe);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root_pedido_detalhe), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        simulador = Simulador.obter(this);
        pedido = MockDataRepository.getInstance()
                .getPedidoById(getIntent().getStringExtra(EXTRA_PEDIDO_ID));
        Usuario usuario = SessionManager.getInstance(this).getUsuario();
        if (pedido == null) {
            Toast.makeText(this, R.string.pedido_nao_encontrado, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        // Isolamento: a jornada só abre para quem é dono do pedido.
        if (usuario == null || !usuario.getId().equals(pedido.getIdComprador())) {
            Toast.makeText(this, R.string.pedido_de_outra_conta, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        findViewById(R.id.include_demo)
                .setVisibility(DemoCatalog.AMBIENTE_DEMO ? View.VISIBLE : View.GONE);
        findViewById(R.id.btn_voltar).setOnClickListener(v -> finish());
        findViewById(R.id.btn_ingresso).setOnClickListener(v -> startActivity(
                new Intent(this, IngressoActivity.class)
                        .putExtra(IngressoActivity.EXTRA_PEDIDO_ID, pedido.getId())));

        ((TextView) findViewById(R.id.txt_numero)).setText(PedidoApresentacao.numero(pedido));
        ((TextView) findViewById(R.id.txt_data))
                .setText(getString(R.string.pedido_data, pedido.getData()));
        PedidoViews.preencherItens(findViewById(R.id.lista_itens), Collections.singletonList(pedido));
        ((TextView) findViewById(R.id.txt_pagamento)).setText(
                PedidoApresentacao.rotuloPagamento(pedido.getFormaPagamento()));
        ((TextView) findViewById(R.id.txt_total)).setText(MoneyUtils.format(pedido.getValorTotal()));
        montarLinhaDoTempo();
        renderizar(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (pedido == null) return;
        simulador.reconciliar();
        renderizar(false);
        handler.postDelayed(tick, TICK_MS);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(tick);
    }

    // ---------------------------------------------------------------- linha do tempo

    private void montarLinhaDoTempo() {
        LinearLayout lista = findViewById(R.id.linha_tempo);
        lista.removeAllViews();
        String[] titulos = getResources().getStringArray(R.array.jornada_etapa_titulos);
        String[] subs = getResources().getStringArray(R.array.jornada_etapa_subs);
        LayoutInflater inflater = LayoutInflater.from(this);
        for (int i = 0; i < titulos.length; i++) {
            View etapa = inflater.inflate(R.layout.item_jornada_etapa, lista, false);
            ((TextView) etapa.findViewById(R.id.etapa_titulo)).setText(titulos[i]);
            ((TextView) etapa.findViewById(R.id.etapa_sub)).setText(subs[i]);
            if (i == titulos.length - 1) etapa.findViewById(R.id.etapa_linha).setVisibility(View.INVISIBLE);
            lista.addView(etapa);
        }
    }

    private int corDaEtapa(int indice) {
        switch (indice) {
            case 0:
                return R.color.brand_mediterraneo;
            case 1:
            case 2:
                return R.color.brand_acafrao;
            default:
                return R.color.brand_vinho;
        }
    }

    private int iconeDaEtapa(int indice) {
        switch (indice) {
            case 1:
                return R.drawable.ic_notif_paella;
            case 2:
                return R.drawable.ic_home_sino;
            case 3:
                return R.drawable.ic_home_sacola;
            default:
                return R.drawable.ic_etapa_check;
        }
    }

    // ---------------------------------------------------------------- renderização

    /** Redesenha a partir do estado atual. {@code animar} só vale quando a etapa realmente muda. */
    private void renderizar(boolean animar) {
        StatusPedido status = pedido.getStatus();
        int indice = Cronograma.indice(status);
        boolean mudou = indice != indiceExibido;
        boolean transicaoReal = mudou && indiceExibido != -2;
        long decorrido = simulador.decorridoMs(pedido.getId());

        SeloStatus.aplicar(findViewById(R.id.txt_status), status);
        desenharCartao(status, indice, mudou, transicaoReal);
        desenharPrevisao(status, decorrido, animar || mudou);
        desenharEtapas(indice, mudou && transicaoReal);
        desenharQr(status);
        indiceExibido = indice;
    }

    private void desenharCartao(StatusPedido status, int indice, boolean mudou, boolean transicaoReal) {
        if (!mudou) return;
        TextView titulo = findViewById(R.id.txt_jornada_titulo);
        TextView desc = findViewById(R.id.txt_jornada_desc);
        ImageView icone = findViewById(R.id.img_jornada_icone);
        View cartao = findViewById(R.id.card_jornada);
        if (indice < 0) {
            titulo.setText(R.string.jornada_cancelado_titulo);
            desc.setText(R.string.jornada_cancelado_desc);
            icone.setImageResource(R.drawable.ic_home_sacola);
            return;
        }
        titulo.setText(getResources().getStringArray(R.array.jornada_card_titulos)[indice]);
        desc.setText(getResources().getStringArray(R.array.jornada_card_descricoes)[indice]);
        icone.setImageResource(iconeDaEtapa(indice));
        icone.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.brand_vinho)));
        if (transicaoReal && com.example.trabalhointerdiciplinar.util.Animacoes.ativas()) {
            cartao.setAlpha(0.5f);
            cartao.animate().alpha(1f).setDuration(300).start();
            if (status == StatusPedido.ENVIADO) {
                // Pedido pronto: o selo de confirmação aparece uma única vez.
                icone.setImageTintList(null);
                icone.setImageResource(R.drawable.avd_confirmacao);
                if (icone.getDrawable() instanceof Animatable) ((Animatable) icone.getDrawable()).start();
            }
        }
    }

    private void desenharPrevisao(StatusPedido status, long decorrido, boolean animarBarra) {
        Cronograma.Previsao previsao = simulador.previsao(pedido.getId());
        TextView valor = findViewById(R.id.txt_previsao_valor);
        switch (previsao.tipo) {
            case MINUTOS:
                valor.setText(getResources().getQuantityString(
                        R.plurals.jornada_previsao_minutos, previsao.minutos, previsao.minutos));
                break;
            case FINALIZANDO:
                valor.setText(R.string.jornada_previsao_finalizando);
                break;
            case PRONTO:
                valor.setText(R.string.jornada_previsao_pronto);
                break;
            default:
                valor.setText(R.string.jornada_previsao_finalizado);
                break;
        }
        LinearProgressIndicator barra = findViewById(R.id.barra_preparo);
        barra.setProgressCompat(Math.round(Cronograma.progresso(status, decorrido) * 100), animarBarra);
        findViewById(R.id.txt_previsao_nota).setVisibility(
                status == StatusPedido.ENTREGUE ? View.GONE : View.VISIBLE);
    }

    private void desenharEtapas(int indiceAtual, boolean animarAtual) {
        LinearLayout lista = findViewById(R.id.linha_tempo);
        String[] titulos = getResources().getStringArray(R.array.jornada_etapa_titulos);
        for (int i = 0; i < lista.getChildCount(); i++) {
            View etapa = lista.getChildAt(i);
            View marcador = etapa.findViewById(R.id.etapa_marcador);
            ImageView icone = etapa.findViewById(R.id.etapa_icone);
            View halo = etapa.findViewById(R.id.etapa_halo);
            View linha = etapa.findViewById(R.id.etapa_linha);
            TextView titulo = etapa.findViewById(R.id.etapa_titulo);
            TextView sub = etapa.findViewById(R.id.etapa_sub);

            // Etapa final já concluída conta como concluída (não como "atual").
            boolean finalizado = indiceAtual == Cronograma.ETAPAS.length - 1;
            boolean concluida = i < indiceAtual || (finalizado && i == indiceAtual);
            boolean atual = i == indiceAtual && !finalizado;
            int cor = ContextCompat.getColor(this, corDaEtapa(i));

            if (concluida || atual) {
                marcador.setBackgroundResource(R.drawable.bg_etapa_circulo);
                marcador.setBackgroundTintList(ColorStateList.valueOf(cor));
                icone.setImageResource(concluida ? R.drawable.ic_etapa_check : iconeDaEtapa(i));
                boolean fundoClaro = cor == ContextCompat.getColor(this, R.color.brand_acafrao);
                icone.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(this,
                        fundoClaro ? R.color.brand_vinho : R.color.white)));
            } else {
                marcador.setBackgroundResource(R.drawable.bg_etapa_futura);
                marcador.setBackgroundTintList(null);
                icone.setImageResource(iconeDaEtapa(i));
                icone.setImageTintList(ColorStateList.valueOf(0x8026282D));
            }
            halo.setVisibility(atual ? View.VISIBLE : View.GONE);
            linha.setBackgroundColor(i < indiceAtual
                    ? ContextCompat.getColor(this, R.color.brand_vinho) : 0x3326282D);
            titulo.setTextColor(ContextCompat.getColor(this,
                    atual || concluida ? R.color.brand_grafite : R.color.brand_grafite_muted));
            sub.setVisibility(atual ? View.VISIBLE : View.GONE);
            etapa.setContentDescription(getString(concluida ? R.string.cd_etapa_concluida
                    : atual ? R.string.cd_etapa_atual : R.string.cd_etapa_futura, titulos[i]));

            if (atual && animarAtual && com.example.trabalhointerdiciplinar.util.Animacoes.ativas()) {
                marcador.setScaleX(0.8f);
                marcador.setScaleY(0.8f);
                marcador.animate().scaleX(1f).scaleY(1f).setDuration(300).start();
            }
        }
    }

    private void desenharQr(StatusPedido status) {
        View bloco = findViewById(R.id.bloco_qr);
        boolean exibe = PedidoApresentacao.exibeQrDemo(status);
        bloco.setVisibility(exibe ? View.VISIBLE : View.GONE);
        if (!exibe || qrDesenhado) return;
        Bitmap qr = QrCodes.gerarEmPretoEBranco(PedidoApresentacao.conteudoQrDemo(pedido), QR_TAMANHO_PX);
        if (qr == null) {
            bloco.setVisibility(View.GONE);
            return;
        }
        ImageView img = findViewById(R.id.img_qr);
        img.setImageBitmap(qr);
        img.setContentDescription(getString(R.string.qr_cd, PedidoApresentacao.numero(pedido)));
        qrDesenhado = true;
    }
}
