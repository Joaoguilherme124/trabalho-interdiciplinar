package com.example.trabalhointerdiciplinar.ui.merchant;

import android.content.Intent;
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
import com.example.trabalhointerdiciplinar.adapter.MerchantProdutoAdapter;
import com.example.trabalhointerdiciplinar.data.MockDataRepository;
import com.example.trabalhointerdiciplinar.data.SessionManager;
import com.example.trabalhointerdiciplinar.model.Loja;
import com.example.trabalhointerdiciplinar.model.Produto;

public class MerchantProdutosFragment extends Fragment {

    private MerchantProdutoAdapter adapter;
    private Loja loja;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_merchant_produtos, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView rv = view.findViewById(R.id.rv_produtos);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new MerchantProdutoAdapter(new MerchantProdutoAdapter.Listener() {
            @Override
            public void onEdit(Produto produto) {
                Intent i = new Intent(requireContext(), ProdutoFormActivity.class);
                i.putExtra(ProdutoFormActivity.EXTRA_PRODUTO_ID, produto.getId());
                startActivity(i);
            }

            @Override
            public void onDelete(Produto produto) {
                MockDataRepository.getInstance().removeProduto(produto.getId());
                refresh();
                Toast.makeText(requireContext(), "Produto removido", Toast.LENGTH_SHORT).show();
            }
        });
        rv.setAdapter(adapter);

        view.findViewById(R.id.btn_novo).setOnClickListener(v -> {
            if (loja == null) {
                Toast.makeText(requireContext(), "Cadastre/aguarde aprovação da loja", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent i = new Intent(requireContext(), ProdutoFormActivity.class);
            i.putExtra(ProdutoFormActivity.EXTRA_LOJA_ID, loja.getId());
            startActivity(i);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    private void refresh() {
        String donoId = SessionManager.getInstance(requireContext()).getUsuario().getId();
        loja = MockDataRepository.getInstance().getLojaDoDono(donoId);
        if (loja != null) {
            adapter.submit(MockDataRepository.getInstance().getProdutosPorLoja(loja.getId()));
        } else {
            adapter.submit(null);
        }
    }
}
