package com.example.trabalhointerdiciplinar.ui.merchant;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.trabalhointerdiciplinar.R;
import com.example.trabalhointerdiciplinar.data.DemoCatalog;
import com.example.trabalhointerdiciplinar.data.MockDataRepository;
import com.example.trabalhointerdiciplinar.data.SessionManager;
import com.example.trabalhointerdiciplinar.data.cozinha.IndicadoresCozinha;
import com.example.trabalhointerdiciplinar.data.simulacao.PedidoSimulador;
import com.example.trabalhointerdiciplinar.data.simulacao.PedidoSimulador.AcaoCozinha;
import com.example.trabalhointerdiciplinar.data.simulacao.Simulador;
import com.example.trabalhointerdiciplinar.model.Loja;
import com.example.trabalhointerdiciplinar.model.Pedido;
import com.example.trabalhointerdiciplinar.model.StatusPedido;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Central da Cozinha: indicadores derivados dos pedidos reais da loja e os próximos pedidos
 * com a ação imediata. Nada aqui é contador fixo.
 */
public class MerchantDashboardFragment extends Fragment {
    private static final int MAX_PROXIMOS = 3;
    private static final long TICK_MS = 3000;

    private final Handler relogio = new Handler(Looper.getMainLooper());
    private final Runnable tick = new Runnable() {
        @Override
        public void run() {
            if (!isAdded()) return;
            atualizar();
            relogio.postDelayed(this, TICK_MS);
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_merchant_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.include_demo)
                .setVisibility(DemoCatalog.AMBIENTE_DEMO ? View.VISIBLE : View.GONE);
        view.findViewById(R.id.btn_nova_loja).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), CadastroLojaActivity.class)));
        view.findViewById(R.id.btn_ver_fila).setOnClickListener(v -> {
            if (getActivity() instanceof MerchantMainActivity) {
                ((MerchantMainActivity) getActivity()).abrirFila();
            }
        });
        com.google.android.material.materialswitch.MaterialSwitch modo = view.findViewById(R.id.switch_cozinha_manual);
        modo.setChecked(Simulador.obter(requireContext()).cozinhaManual());
        modo.setOnCheckedChangeListener((b, marcado) ->
                Simulador.obter(requireContext()).definirCozinhaManual(marcado));
    }

    @Override
    public void onResume() {
        super.onResume();
        atualizar();
        relogio.removeCallbacks(tick);
        relogio.postDelayed(tick, TICK_MS);
    }

    @Override
    public void onPause() {
        super.onPause();
        relogio.removeCallbacks(tick);
    }

    private void atualizar() {
        View raiz = getView();
        if (raiz == null) return;
        String donoId = SessionManager.getInstance(requireContext()).getUsuario().getId();
        MockDataRepository repo = MockDataRepository.getInstance();
        PedidoSimulador sim = Simulador.obter(requireContext());
        sim.reconciliar();
        Loja loja = repo.getLojaDoDono(donoId);
        TextView nome = raiz.findViewById(R.id.txt_loja_nome);
        View conteudo = raiz.findViewById(R.id.conteudo_cozinha);
        View cadastrar = raiz.findViewById(R.id.btn_nova_loja);
        if (loja == null) {
            nome.setText(R.string.cozinha_sem_loja);
            conteudo.setVisibility(View.GONE);
            cadastrar.setVisibility(View.VISIBLE);
            return;
        }
        nome.setText(loja.getNome());
        conteudo.setVisibility(View.VISIBLE);
        cadastrar.setVisibility(View.GONE);

        List<Pedido> pedidos = repo.getPedidosDaLoja(loja.getId());
        IndicadoresCozinha ind = IndicadoresCozinha.calcular(pedidos);
        ((TextView) raiz.findViewById(R.id.txt_ind_novos)).setText(String.valueOf(ind.novos));
        ((TextView) raiz.findViewById(R.id.txt_ind_preparo)).setText(String.valueOf(ind.emPreparo));
        ((TextView) raiz.findViewById(R.id.txt_ind_prontos)).setText(String.valueOf(ind.prontos));
        ((TextView) raiz.findViewById(R.id.txt_ind_finalizados)).setText(String.valueOf(ind.finalizados));

        // Próximos: os mais antigos ainda em andamento (a fila anda do mais velho para o mais novo).
        List<Pedido> ativos = new ArrayList<>();
        for (Pedido p : pedidos) {
            StatusPedido s = p.getStatus();
            if (s == StatusPedido.PENDENTE || s == StatusPedido.SEPARACAO || s == StatusPedido.ENVIADO) {
                ativos.add(p);
            }
        }
        Collections.reverse(ativos);
        LinearLayout lista = raiz.findViewById(R.id.lista_proximos);
        lista.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (int i = 0; i < Math.min(MAX_PROXIMOS, ativos.size()); i++) {
            View card = inflater.inflate(R.layout.item_cozinha_pedido, lista, false);
            vincular(card, ativos.get(i), sim);
            lista.addView(card);
        }
        raiz.findViewById(R.id.txt_proximos_vazio).setVisibility(ativos.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void vincular(View card, Pedido p, PedidoSimulador sim) {
        CozinhaUi.preencherCartao(card, p, sim);
        card.setOnClickListener(v -> startActivity(new Intent(requireContext(), PedidoCozinhaActivity.class)
                .putExtra(PedidoCozinhaActivity.EXTRA_PEDIDO_ID, p.getId())));
        AcaoCozinha acao = CozinhaUi.proximaAcao(sim, p);
        android.widget.Button botao = card.findViewById(R.id.btn_acao);
        if (acao == null) {
            botao.setVisibility(View.GONE);
            return;
        }
        botao.setVisibility(View.VISIBLE);
        botao.setText(CozinhaUi.rotuloAcao(acao));
        botao.setOnClickListener(v -> {
            CozinhaUi.executar(requireContext(), p, acao);
            atualizar();
        });
    }
}
