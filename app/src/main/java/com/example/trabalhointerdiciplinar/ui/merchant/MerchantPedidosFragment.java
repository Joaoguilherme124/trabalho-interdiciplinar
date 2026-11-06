package com.example.trabalhointerdiciplinar.ui.merchant;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trabalhointerdiciplinar.R;
import com.example.trabalhointerdiciplinar.adapter.CozinhaPedidoAdapter;
import com.example.trabalhointerdiciplinar.data.DemoCatalog;
import com.example.trabalhointerdiciplinar.data.MockDataRepository;
import com.example.trabalhointerdiciplinar.data.SessionManager;
import com.example.trabalhointerdiciplinar.data.simulacao.PedidoSimulador;
import com.example.trabalhointerdiciplinar.data.simulacao.PedidoSimulador.AcaoCozinha;
import com.example.trabalhointerdiciplinar.data.simulacao.Simulador;
import com.example.trabalhointerdiciplinar.model.Loja;
import com.example.trabalhointerdiciplinar.model.Pedido;
import com.example.trabalhointerdiciplinar.model.StatusPedido;

import java.util.ArrayList;
import java.util.List;

/** Fila de Pedidos da loja: ativos (recebe, prepara, marca pronto) e histórico. */
public class MerchantPedidosFragment extends Fragment {
    private CozinhaPedidoAdapter adapter;
    private Loja loja;
    private boolean historico;
    private TextView chipAtivos;
    private TextView chipHistorico;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_merchant_pedidos, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.include_demo)
                .setVisibility(DemoCatalog.AMBIENTE_DEMO ? View.VISIBLE : View.GONE);
        RecyclerView rv = view.findViewById(R.id.rv_pedidos);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new CozinhaPedidoAdapter(this::vincular);
        rv.setAdapter(adapter);
        chipAtivos = view.findViewById(R.id.chip_ativos);
        chipHistorico = view.findViewById(R.id.chip_historico);
        chipAtivos.setOnClickListener(v -> definirHistorico(false));
        chipHistorico.setOnClickListener(v -> definirHistorico(true));
        pintarChips();
    }

    @Override
    public void onResume() {
        super.onResume();
        String donoId = SessionManager.getInstance(requireContext()).getUsuario().getId();
        loja = MockDataRepository.getInstance().getLojaDoDono(donoId);
        atualizar();
    }

    private void definirHistorico(boolean valor) {
        historico = valor;
        pintarChips();
        atualizar();
    }

    private void pintarChips() {
        estilizar(chipAtivos, !historico);
        estilizar(chipHistorico, historico);
    }

    private void estilizar(TextView chip, boolean ativo) {
        chip.setBackgroundResource(ativo ? R.drawable.bg_paella_chip_selected : R.drawable.bg_paella_chip);
        chip.setTextColor(ContextCompat.getColor(requireContext(),
                ativo ? R.color.brand_creme : R.color.brand_vinho));
        chip.setSelected(ativo);
    }

    private void atualizar() {
        View raiz = getView();
        if (raiz == null) return;
        PedidoSimulador sim = Simulador.obter(requireContext());
        sim.reconciliar();
        List<Pedido> lista = new ArrayList<>();
        if (loja != null) {
            List<Pedido> todos = MockDataRepository.getInstance().getPedidosDaLoja(loja.getId());
            for (Pedido p : todos) {
                boolean terminado = p.getStatus() == StatusPedido.ENTREGUE || p.getStatus() == StatusPedido.CANCELADO;
                if (terminado == historico) lista.add(p);
            }
            if (!historico) java.util.Collections.reverse(lista);   // fila: o mais antigo primeiro
        }
        adapter.submit(lista);
        View vazio = raiz.findViewById(R.id.estado_vazio_fila);
        vazio.setVisibility(lista.isEmpty() ? View.VISIBLE : View.GONE);
        if (lista.isEmpty()) {
            TextView t = raiz.findViewById(R.id.txt_vazio_titulo);
            TextView d = raiz.findViewById(R.id.txt_vazio_texto);
            if (loja == null) {
                t.setText(R.string.fila_vazio_ativos_titulo);
                d.setText(R.string.fila_sem_loja);
            } else if (historico) {
                t.setText(R.string.fila_vazio_historico_titulo);
                d.setText(R.string.fila_vazio_historico_texto);
            } else {
                t.setText(R.string.fila_vazio_ativos_titulo);
                d.setText(R.string.fila_vazio_ativos_texto);
            }
        }
    }

    private void vincular(View card, Pedido p) {
        PedidoSimulador sim = Simulador.obter(requireContext());
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
