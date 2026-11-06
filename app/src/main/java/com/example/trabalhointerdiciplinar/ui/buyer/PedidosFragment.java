package com.example.trabalhointerdiciplinar.ui.buyer;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trabalhointerdiciplinar.R;
import com.example.trabalhointerdiciplinar.adapter.MeusPedidosAdapter;
import com.example.trabalhointerdiciplinar.data.DemoCatalog;
import com.example.trabalhointerdiciplinar.data.MockDataRepository;
import com.example.trabalhointerdiciplinar.data.SessionManager;
import com.example.trabalhointerdiciplinar.model.Pedido;

import java.util.List;

/** Aba "Pedidos" do comprador: lista de pedidos, estado vazio e acesso aos detalhes. */
public class PedidosFragment extends Fragment {

    private MeusPedidosAdapter adapter;
    private View rv;
    private View estadoVazio;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_buyer_pedidos, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.include_demo)
                .setVisibility(DemoCatalog.AMBIENTE_DEMO ? View.VISIBLE : View.GONE);
        RecyclerView list = view.findViewById(R.id.rv_pedidos);
        rv = list;
        estadoVazio = view.findViewById(R.id.estado_vazio);
        list.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new MeusPedidosAdapter(pedido -> {
            Intent i = new Intent(requireContext(), PedidoDetalheActivity.class);
            i.putExtra(PedidoDetalheActivity.EXTRA_PEDIDO_ID, pedido.getId());
            startActivity(i);
        });
        list.setAdapter(adapter);

        view.findViewById(R.id.btn_ver_cardapio).setOnClickListener(v -> {
            if (getActivity() instanceof BuyerMainActivity) {
                ((BuyerMainActivity) getActivity()).openTab(R.id.nav_categorias);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        String userId = SessionManager.getInstance(requireContext()).getUsuario().getId();
        List<Pedido> pedidos = MockDataRepository.getInstance().getPedidosDoComprador(userId);
        adapter.submit(pedidos);
        rv.setVisibility(pedidos.isEmpty() ? View.GONE : View.VISIBLE);
        estadoVazio.setVisibility(pedidos.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
