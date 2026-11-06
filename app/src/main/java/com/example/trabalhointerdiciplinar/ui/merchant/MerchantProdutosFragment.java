package com.example.trabalhointerdiciplinar.ui.merchant;

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
import com.example.trabalhointerdiciplinar.adapter.MerchantProdutoAdapter;
import com.example.trabalhointerdiciplinar.data.DemoCatalog;
import com.example.trabalhointerdiciplinar.data.MockDataRepository;
import com.example.trabalhointerdiciplinar.data.SessionManager;
import com.example.trabalhointerdiciplinar.model.Loja;
import com.example.trabalhointerdiciplinar.model.Produto;

import java.util.List;

/** Produtos da loja: os mesmos que o participante vê no Cardápio (quando disponíveis). */
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
        view.findViewById(R.id.include_demo)
                .setVisibility(DemoCatalog.AMBIENTE_DEMO ? View.VISIBLE : View.GONE);
        RecyclerView rv = view.findViewById(R.id.rv_produtos);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new MerchantProdutoAdapter(new MerchantProdutoAdapter.Listener() {
            @Override
            public void onEdit(Produto produto) {
                startActivity(new Intent(requireContext(), ProdutoFormActivity.class)
                        .putExtra(ProdutoFormActivity.EXTRA_PRODUTO_ID, produto.getId()));
            }

            @Override
            public void onDisponivel(Produto produto, boolean disponivel) {
                produto.setDisponivel(disponivel);
                MockDataRepository.getInstance().updateProduto(produto);
            }
        });
        rv.setAdapter(adapter);
        view.findViewById(R.id.btn_novo).setOnClickListener(v -> {
            if (loja == null) {
                Toast.makeText(requireContext(), R.string.produtos_sem_loja, Toast.LENGTH_SHORT).show();
                return;
            }
            startActivity(new Intent(requireContext(), ProdutoFormActivity.class)
                    .putExtra(ProdutoFormActivity.EXTRA_LOJA_ID, loja.getId()));
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        String donoId = SessionManager.getInstance(requireContext()).getUsuario().getId();
        loja = MockDataRepository.getInstance().getLojaDoDono(donoId);
        List<Produto> produtos = loja == null ? null
                : MockDataRepository.getInstance().getProdutosPorLoja(loja.getId());
        adapter.submit(produtos);
        View raiz = getView();
        if (raiz == null) return;
        boolean vazio = produtos == null || produtos.isEmpty();
        raiz.findViewById(R.id.estado_vazio_produtos).setVisibility(vazio ? View.VISIBLE : View.GONE);
        ((TextView) raiz.findViewById(R.id.txt_vazio_texto)).setText(
                loja == null ? R.string.produtos_sem_loja : R.string.produtos_vazio_texto);
    }
}
