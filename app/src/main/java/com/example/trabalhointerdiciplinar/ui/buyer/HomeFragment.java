package com.example.trabalhointerdiciplinar.ui.buyer;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trabalhointerdiciplinar.R;
import com.example.trabalhointerdiciplinar.adapter.CategoriaChipAdapter;
import com.example.trabalhointerdiciplinar.adapter.LojaAdapter;
import com.example.trabalhointerdiciplinar.adapter.ProdutoAdapter;

public class HomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_buyer_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        HomeViewModel vm = new ViewModelProvider(this).get(HomeViewModel.class);

        EditText edtBusca = view.findViewById(R.id.edt_busca);
        RecyclerView rvCat = view.findViewById(R.id.rv_categorias);
        RecyclerView rvLojas = view.findViewById(R.id.rv_lojas);
        RecyclerView rvProdutos = view.findViewById(R.id.rv_produtos);

        rvCat.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        rvLojas.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        rvProdutos.setLayoutManager(new LinearLayoutManager(requireContext()));

        String[] categorias = getResources().getStringArray(R.array.categorias);
        rvCat.setAdapter(new CategoriaChipAdapter(categorias, vm::setCategoria));

        LojaAdapter lojaAdapter = new LojaAdapter(loja -> {
            Intent i = new Intent(requireContext(), LojaDetalheActivity.class);
            i.putExtra(LojaDetalheActivity.EXTRA_LOJA_ID, loja.getId());
            startActivity(i);
        });
        rvLojas.setAdapter(lojaAdapter);

        ProdutoAdapter produtoAdapter = new ProdutoAdapter(produto -> {
            Intent i = new Intent(requireContext(), ProdutoDetalheActivity.class);
            i.putExtra(ProdutoDetalheActivity.EXTRA_PRODUTO_ID, produto.getId());
            startActivity(i);
        });
        rvProdutos.setAdapter(produtoAdapter);

        vm.getLojas().observe(getViewLifecycleOwner(), lojaAdapter::submit);
        vm.getProdutos().observe(getViewLifecycleOwner(), produtoAdapter::submit);

        edtBusca.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                vm.setQuery(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        vm.refresh();
    }
}
