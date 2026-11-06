package com.example.trabalhointerdiciplinar.ui.buyer;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trabalhointerdiciplinar.R;
import com.example.trabalhointerdiciplinar.adapter.CarrinhoAdapter;
import com.example.trabalhointerdiciplinar.data.CarrinhoManager;
import com.example.trabalhointerdiciplinar.data.DemoCatalog;
import com.example.trabalhointerdiciplinar.util.MoneyUtils;

public class CarrinhoFragment extends Fragment {

    private CarrinhoAdapter adapter;
    private TextView txtTotal;
    private View txtVazio;
    private View btnCheckout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_buyer_carrinho, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView rv = view.findViewById(R.id.rv_carrinho);
        txtTotal = view.findViewById(R.id.txt_total);
        txtVazio = view.findViewById(R.id.estado_vazio_carrinho);
        btnCheckout = view.findViewById(R.id.btn_checkout);
        view.findViewById(R.id.include_demo)
                .setVisibility(DemoCatalog.AMBIENTE_DEMO ? View.VISIBLE : View.GONE);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        view.findViewById(R.id.btn_voltar_carrinho).setOnClickListener(v -> {
            if (getActivity() != null) getActivity().getOnBackPressedDispatcher().onBackPressed();
        });

        adapter = new CarrinhoAdapter(new CarrinhoAdapter.Listener() {
            @Override
            public void onQtyChanged(String produtoId, int qty) {
                CarrinhoManager.getInstance().atualizarQuantidade(produtoId, qty);
                refresh();
            }

            @Override
            public void onRemove(String produtoId) {
                CarrinhoManager.getInstance().remover(produtoId);
                refresh();
            }
        });
        rv.setAdapter(adapter);

        view.findViewById(R.id.btn_carrinho_ver_cardapio).setOnClickListener(v -> {
            if (getActivity() instanceof BuyerMainActivity) {
                ((BuyerMainActivity) getActivity()).openTab(R.id.nav_categorias);
            }
        });
        btnCheckout.setOnClickListener(v -> {
            if (CarrinhoManager.getInstance().getItens().isEmpty()) {
                Toast.makeText(requireContext(), "Carrinho vazio", Toast.LENGTH_SHORT).show();
                return;
            }
            startActivity(new Intent(requireContext(), CheckoutActivity.class));
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    private void refresh() {
        CarrinhoManager cart = CarrinhoManager.getInstance();
        adapter.submit(cart.getItens());
        boolean empty = cart.getItens().isEmpty();
        txtVazio.setVisibility(empty ? View.VISIBLE : View.GONE);
        txtTotal.setText(MoneyUtils.format(cart.getTotal()));
        btnCheckout.setEnabled(!empty);
    }
}
