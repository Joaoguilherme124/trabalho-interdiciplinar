package com.example.trabalhointerdiciplinar.ui.buyer;

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
import com.example.trabalhointerdiciplinar.adapter.PedidoAdapter;
import com.example.trabalhointerdiciplinar.data.MockDataRepository;
import com.example.trabalhointerdiciplinar.data.SessionManager;

public class PedidosFragment extends Fragment {

    private PedidoAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_buyer_pedidos, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView rv = view.findViewById(R.id.rv_pedidos);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new PedidoAdapter(false, null);
        rv.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        String userId = SessionManager.getInstance(requireContext()).getUsuario().getId();
        adapter.submit(MockDataRepository.getInstance().getPedidosDoComprador(userId));
    }
}
