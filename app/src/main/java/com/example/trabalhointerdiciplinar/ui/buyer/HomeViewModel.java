package com.example.trabalhointerdiciplinar.ui.buyer;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.trabalhointerdiciplinar.data.MockDataRepository;
import com.example.trabalhointerdiciplinar.model.Loja;
import com.example.trabalhointerdiciplinar.model.Produto;

import java.util.List;

public class HomeViewModel extends ViewModel {
    private final MockDataRepository repo = MockDataRepository.getInstance();
    private final MutableLiveData<List<Produto>> produtos = new MutableLiveData<>();
    private final MutableLiveData<List<Loja>> lojas = new MutableLiveData<>();
    private String query = "";
    private String categoria = "Todos";

    public LiveData<List<Produto>> getProdutos() {
        return produtos;
    }

    public LiveData<List<Loja>> getLojas() {
        return lojas;
    }

    public void setQuery(String query) {
        this.query = query == null ? "" : query;
        refresh();
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria == null ? "Todos" : categoria;
        refresh();
    }

    public void refresh() {
        produtos.setValue(repo.buscarProdutos(query, categoria));
        lojas.setValue(repo.buscarLojas(query, categoria));
    }
}
