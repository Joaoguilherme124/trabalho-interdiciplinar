package com.example.trabalhointerdiciplinar.ui.merchant;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.trabalhointerdiciplinar.R;
import com.example.trabalhointerdiciplinar.data.MockDataRepository;
import com.example.trabalhointerdiciplinar.data.SessionManager;
import com.example.trabalhointerdiciplinar.model.Loja;
import com.example.trabalhointerdiciplinar.util.MoneyUtils;

public class MerchantDashboardFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_merchant_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.btn_nova_loja).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), CadastroLojaActivity.class)));
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getView() == null) return;
        String donoId = SessionManager.getInstance(requireContext()).getUsuario().getId();
        MockDataRepository repo = MockDataRepository.getInstance();
        Loja loja = repo.getLojaDoDono(donoId);

        TextView txtNome = getView().findViewById(R.id.txt_loja_nome);
        TextView txtFat = getView().findViewById(R.id.txt_faturado);
        TextView txtPed = getView().findViewById(R.id.txt_pedidos);
        TextView txtProd = getView().findViewById(R.id.txt_produtos);

        if (loja == null) {
            txtNome.setText("Nenhuma loja cadastrada ainda");
            txtFat.setText(MoneyUtils.format(0));
            txtPed.setText("0");
            txtProd.setText("0");
            return;
        }

        txtNome.setText(loja.getNome() + " · " + loja.getStatus().name());
        txtFat.setText(MoneyUtils.format(repo.getFaturamentoLoja(loja.getId())));
        txtPed.setText(String.valueOf(repo.countPedidosHoje(loja.getId())));
        txtProd.setText(String.valueOf(repo.getProdutosPorLoja(loja.getId()).size()));
    }
}
