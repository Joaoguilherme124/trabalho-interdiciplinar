package com.example.trabalhointerdiciplinar.ui.merchant;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trabalhointerdiciplinar.R;
import com.example.trabalhointerdiciplinar.adapter.PedidoAdapter;
import com.example.trabalhointerdiciplinar.data.MockDataRepository;
import com.example.trabalhointerdiciplinar.data.SessionManager;
import com.example.trabalhointerdiciplinar.model.Loja;
import com.example.trabalhointerdiciplinar.model.Pedido;
import com.example.trabalhointerdiciplinar.model.StatusPedido;

public class MerchantPedidosFragment extends Fragment {

    private PedidoAdapter adapter;
    private Loja loja;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_merchant_pedidos, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView rv = view.findViewById(R.id.rv_pedidos);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new PedidoAdapter(true, this::avancarStatus);
        rv.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        String donoId = SessionManager.getInstance(requireContext()).getUsuario().getId();
        loja = MockDataRepository.getInstance().getLojaDoDono(donoId);
        if (loja != null) {
            adapter.submit(MockDataRepository.getInstance().getPedidosDaLoja(loja.getId()));
        }
    }

    private void avancarStatus(Pedido pedido) {
        StatusPedido atual = pedido.getStatus();
        StatusPedido novo;
        switch (atual) {
            case PENDENTE:
                novo = StatusPedido.SEPARACAO;
                break;
            case SEPARACAO:
                novo = StatusPedido.ENVIADO;
                break;
            case ENVIADO:
                novo = StatusPedido.ENTREGUE;
                break;
            default:
                return;
        }
        MockDataRepository.getInstance().atualizarStatusPedido(pedido.getId(), novo);
        Toast.makeText(requireContext(), "Status: " + novo.name(), Toast.LENGTH_SHORT).show();
        adapter.submit(MockDataRepository.getInstance().getPedidosDaLoja(loja.getId()));
    }
}
