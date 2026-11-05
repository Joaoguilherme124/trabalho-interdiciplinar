package com.example.trabalhointerdiciplinar.ui.buyer;

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
import com.example.trabalhointerdiciplinar.data.SessionManager;
import com.example.trabalhointerdiciplinar.model.Usuario;
import com.example.trabalhointerdiciplinar.ui.auth.LoginActivity;

public class PerfilFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_buyer_perfil, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Usuario u = SessionManager.getInstance(requireContext()).getUsuario();
        ((TextView) view.findViewById(R.id.txt_nome)).setText(u.getNome());
        ((TextView) view.findViewById(R.id.txt_email)).setText(u.getEmail());
        ((TextView) view.findViewById(R.id.txt_telefone)).setText(u.getTelefone());
        ((TextView) view.findViewById(R.id.txt_endereco)).setText(u.getEndereco());

        view.findViewById(R.id.btn_sair).setOnClickListener(v -> {
            SessionManager.getInstance(requireContext()).logout();
            Intent i = new Intent(requireContext(), LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        });
    }
}
