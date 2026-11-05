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
import com.example.trabalhointerdiciplinar.adapter.CategoriaChipAdapter;
import com.example.trabalhointerdiciplinar.adapter.ProdutoAdapter;
import com.example.trabalhointerdiciplinar.data.MockDataRepository;

public class CategoriasFragment extends Fragment {

    private ProdutoAdapter produtoAdapter;
    private String categoria = "Todos";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_buyer_categorias, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView rvCat = view.findViewById(R.id.rv_categorias);
        RecyclerView rvProd = view.findViewById(R.id.rv_produtos);
        rvCat.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        rvProd.setLayoutManager(new LinearLayoutManager(requireContext()));

        produtoAdapter = new ProdutoAdapter(produto -> {
            Intent i = new Intent(requireContext(), ProdutoDetalheActivity.class);
            i.putExtra(ProdutoDetalheActivity.EXTRA_PRODUTO_ID, produto.getId());
            startActivity(i);
        });
        rvProd.setAdapter(produtoAdapter);

        String[] categorias = getResources().getStringArray(R.array.categorias);
        rvCat.setAdapter(new CategoriaChipAdapter(categorias, cat -> {
            categoria = cat;
            carregar();
        }));
        carregar();
    }

    private void carregar() {
        produtoAdapter.submit(MockDataRepository.getInstance().buscarProdutos("", categoria));
    }
}
