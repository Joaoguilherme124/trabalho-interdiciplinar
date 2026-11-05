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
import com.example.trabalhointerdiciplinar.util.MoneyUtils;

public class CarrinhoFragment extends Fragment {

    private CarrinhoAdapter adapter;
    private TextView txtTotal;
    private TextView txtVazio;
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
        txtVazio = view.findViewById(R.id.txt_vazio);
        btnCheckout = view.findViewById(R.id.btn_checkout);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

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
        txtTotal.setText("Total: " + MoneyUtils.format(cart.getTotal()));
        btnCheckout.setEnabled(!empty);
    }
}
