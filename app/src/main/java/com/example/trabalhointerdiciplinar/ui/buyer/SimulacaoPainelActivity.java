package com.example.trabalhointerdiciplinar.ui.buyer;

import android.os.Bundle;
import android.view.View;
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
import com.example.trabalhointerdiciplinar.data.simulacao.PedidoSimulador;
import com.example.trabalhointerdiciplinar.data.simulacao.Simulador;
import com.example.trabalhointerdiciplinar.model.Pedido;
import com.example.trabalhointerdiciplinar.model.StatusPedido;
import com.example.trabalhointerdiciplinar.model.Usuario;
import com.example.trabalhointerdiciplinar.util.PedidoApresentacao;
import com.google.android.material.materialswitch.MaterialSwitch;

import java.util.Collections;
import java.util.List;

/**
 * Painel discreto de demonstração: adianta etapas, simula retirada, reinicia e liga o Modo Dia do
 * Evento. Age somente sobre pedidos da conta conectada.
 */
public class SimulacaoPainelActivity extends AppCompatActivity {
    private PedidoSimulador simulador;
    private MaterialSwitch chaveModo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_simulacao_painel);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root_painel), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
        findViewById(R.id.include_demo)
                .setVisibility(DemoCatalog.AMBIENTE_DEMO ? View.VISIBLE : View.GONE);
        findViewById(R.id.btn_voltar).setOnClickListener(v -> finish());
        simulador = Simulador.obter(this);

        findViewById(R.id.btn_preparo).setOnClickListener(v -> avancar(StatusPedido.SEPARACAO));
        findViewById(R.id.btn_pronto).setOnClickListener(v -> avancar(StatusPedido.ENVIADO));
        findViewById(R.id.btn_retirada).setOnClickListener(v -> avancar(StatusPedido.ENTREGUE));
        findViewById(R.id.btn_reiniciar).setOnClickListener(v -> {
            Pedido p = pedidoAtivo();
            Usuario u = usuario();
            if (p != null && u != null && simulador.reiniciar(p.getId(), u.getId())) {
                feito();
            } else {
                Toast.makeText(this, R.string.painel_indisponivel, Toast.LENGTH_SHORT).show();
            }
            atualizar();
        });
        chaveModo = findViewById(R.id.switch_modo_evento);
        chaveModo.setChecked(simulador.modoDiaDoEvento());
        chaveModo.setOnCheckedChangeListener((b, marcado) -> simulador.definirModoDiaDoEvento(marcado));
    }

    @Override
    protected void onResume() {
        super.onResume();
        simulador.reconciliar();
        chaveModo.setChecked(simulador.modoDiaDoEvento());
        atualizar();
    }

    private Usuario usuario() {
        return SessionManager.getInstance(this).getUsuario();
    }

    /** Primeiro pedido em andamento da conta conectada que já está sendo simulado. */
    private Pedido pedidoAtivo() {
        Usuario u = usuario();
        if (u == null) return null;
        List<Pedido> todos = MockDataRepository.getInstance().getPedidosDoComprador(u.getId());
        List<Pedido> simulados = new java.util.ArrayList<>();
        for (Pedido p : todos) {
            if (simulador.registro(p.getId()) != null) simulados.add(p);
        }
        Pedido ativo = PedidoApresentacao.primeiroAtivo(simulados);
        if (ativo != null) return ativo;
        // Sem pedido em andamento: o mais recente já simulado (permite reiniciar um retirado).
        return simulados.isEmpty() ? null : simulados.get(0);
    }

    private void avancar(StatusPedido alvo) {
        Pedido p = pedidoAtivo();
        Usuario u = usuario();
        if (p != null && u != null && simulador.avancarPara(p.getId(), u.getId(), alvo)) {
            feito();
        } else {
            Toast.makeText(this, R.string.painel_indisponivel, Toast.LENGTH_SHORT).show();
        }
        atualizar();
    }

    private void feito() {
        Toast.makeText(this, R.string.painel_feito, Toast.LENGTH_SHORT).show();
    }

    private void atualizar() {
        Pedido p = pedidoAtivo();
        TextView rotulo = findViewById(R.id.txt_painel_pedido);
        View acoes = findViewById(R.id.grupo_acoes);
        if (p == null) {
            rotulo.setText(R.string.painel_sem_pedido);
            acoes.setVisibility(View.GONE);
            return;
        }
        acoes.setVisibility(View.VISIBLE);
        rotulo.setText(getString(R.string.painel_pedido_atual, PedidoApresentacao.numero(p),
                PedidoApresentacao.rotuloStatus(p.getStatus())));
        StatusPedido s = p.getStatus();
        findViewById(R.id.btn_preparo).setEnabled(s == StatusPedido.PENDENTE);
        findViewById(R.id.btn_pronto).setEnabled(s == StatusPedido.PENDENTE || s == StatusPedido.SEPARACAO);
        findViewById(R.id.btn_retirada).setEnabled(s == StatusPedido.ENVIADO);
    }
}
