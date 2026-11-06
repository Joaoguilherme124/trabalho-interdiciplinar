package com.example.trabalhointerdiciplinar.ui.merchant;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.trabalhointerdiciplinar.R;
import com.example.trabalhointerdiciplinar.data.DemoCatalog;
import com.example.trabalhointerdiciplinar.data.MockDataRepository;
import com.example.trabalhointerdiciplinar.data.SessionManager;
import com.example.trabalhointerdiciplinar.data.simulacao.Cronograma;
import com.example.trabalhointerdiciplinar.data.simulacao.PedidoSimulador;
import com.example.trabalhointerdiciplinar.data.simulacao.PedidoSimulador.AcaoCozinha;
import com.example.trabalhointerdiciplinar.data.simulacao.Simulador;
import com.example.trabalhointerdiciplinar.model.Loja;
import com.example.trabalhointerdiciplinar.model.Pedido;
import com.example.trabalhointerdiciplinar.model.StatusPedido;
import com.example.trabalhointerdiciplinar.model.Usuario;
import com.example.trabalhointerdiciplinar.ui.buyer.PedidoViews;
import com.example.trabalhointerdiciplinar.util.MoneyUtils;
import com.example.trabalhointerdiciplinar.util.PedidoApresentacao;
import com.example.trabalhointerdiciplinar.util.SeloStatus;

import java.util.Collections;

/** Detalhes de um pedido da loja: itens, previsão ajustável e a próxima ação da cozinha. */
public class PedidoCozinhaActivity extends AppCompatActivity {
    public static final String EXTRA_PEDIDO_ID = "pedido_id";
    private static final int PASSO_PREVISAO_MIN = 5;

    private PedidoSimulador sim;
    private Pedido pedido;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pedido_cozinha);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root_pedido_cozinha), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
        findViewById(R.id.include_demo)
                .setVisibility(DemoCatalog.AMBIENTE_DEMO ? View.VISIBLE : View.GONE);
        findViewById(R.id.btn_voltar).setOnClickListener(v -> finish());

        sim = Simulador.obter(this);
        Usuario u = SessionManager.getInstance(this).getUsuario();
        pedido = MockDataRepository.getInstance().getPedidoById(getIntent().getStringExtra(EXTRA_PEDIDO_ID));
        Loja loja = u == null ? null : MockDataRepository.getInstance().getLojaDoDono(u.getId());
        // Isolamento: só o lojista dono da loja do pedido enxerga os detalhes.
        if (pedido == null || loja == null || !loja.getId().equals(pedido.getIdLoja())) {
            Toast.makeText(this, R.string.pedido_cozinha_indisponivel, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        findViewById(R.id.btn_prev_menos).setOnClickListener(v -> ajustarPrevisao(-PASSO_PREVISAO_MIN));
        findViewById(R.id.btn_prev_mais).setOnClickListener(v -> ajustarPrevisao(PASSO_PREVISAO_MIN));
        findViewById(R.id.btn_acao).setOnClickListener(v -> {
            AcaoCozinha acao = CozinhaUi.proximaAcao(sim, pedido);
            if (acao != null) CozinhaUi.executar(this, pedido, acao);
            desenhar();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (pedido == null) return;
        sim.reconciliar();
        desenhar();
    }

    private void ajustarPrevisao(int delta) {
        int atual = sim.previsao(pedido.getId()).minutos;
        sim.ajustarPrevisao(pedido.getId(), pedido.getIdLoja(), atual + delta);
        desenhar();
    }

    private void desenhar() {
        Pedido p = MockDataRepository.getInstance().getPedidoById(pedido.getId());
        pedido = p;
        ((TextView) findViewById(R.id.txt_numero)).setText(PedidoApresentacao.numero(p));
        SeloStatus.aplicar(findViewById(R.id.txt_status), p.getStatus());
        ((TextView) findViewById(R.id.txt_cliente)).setText(getString(R.string.pedido_cliente_data,
                CozinhaUi.nomeDoCliente(p), p.getData()));
        PedidoViews.preencherItens(findViewById(R.id.lista_itens), Collections.singletonList(p));
        ((TextView) findViewById(R.id.txt_pagamento)).setText(getString(R.string.pedido_cozinha_pagamento,
                PedidoApresentacao.rotuloPagamento(p.getFormaPagamento())));
        ((TextView) findViewById(R.id.txt_total)).setText(MoneyUtils.format(p.getValorTotal()));

        boolean ajustavel = sim.registro(p.getId()) != null
                && (p.getStatus() == StatusPedido.PENDENTE || p.getStatus() == StatusPedido.SEPARACAO);
        findViewById(R.id.card_previsao).setVisibility(
                p.getStatus() == StatusPedido.CANCELADO ? View.GONE : View.VISIBLE);
        findViewById(R.id.btn_prev_menos).setEnabled(ajustavel);
        findViewById(R.id.btn_prev_mais).setEnabled(ajustavel);
        ((TextView) findViewById(R.id.txt_previsao_valor)).setText(CozinhaUi.textoPrevisao(this, sim, p));

        AcaoCozinha acao = CozinhaUi.proximaAcao(sim, p);
        android.widget.Button botao = findViewById(R.id.btn_acao);
        botao.setVisibility(acao == null ? View.GONE : View.VISIBLE);
        if (acao != null) botao.setText(CozinhaUi.rotuloAcao(acao));
    }
}
