package com.example.trabalhointerdiciplinar.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trabalhointerdiciplinar.R;
import com.example.trabalhointerdiciplinar.model.Usuario;

import java.util.ArrayList;
import java.util.List;

public class AdminLojistaAdapter extends RecyclerView.Adapter<AdminLojistaAdapter.VH> {

    private final List<Usuario> itens = new ArrayList<>();

    public void submit(List<Usuario> data) {
        itens.clear();
        if (data != null) itens.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_lojista, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Usuario u = itens.get(position);
        holder.nome.setText(u.getNome());
        holder.email.setText(u.getEmail());
        if (u.isPrecisaTrocarCredenciais()) {
            holder.status.setText(R.string.admin_lojista_aguardando);
            holder.status.setTextColor(holder.itemView.getResources().getColor(R.color.brand_paprica, null));
        } else {
            holder.status.setText(R.string.admin_lojista_ok);
            holder.status.setTextColor(holder.itemView.getResources().getColor(R.color.leaf, null));
        }
    }

    @Override
    public int getItemCount() {
        return itens.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView nome, email, status;

        VH(@NonNull View itemView) {
            super(itemView);
            nome = itemView.findViewById(R.id.txt_nome);
            email = itemView.findViewById(R.id.txt_email);
            status = itemView.findViewById(R.id.txt_status);
        }
    }
}
