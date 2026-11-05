package com.example.trabalhointerdiciplinar.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trabalhointerdiciplinar.R;
import com.example.trabalhointerdiciplinar.model.Loja;
import com.example.trabalhointerdiciplinar.model.StatusLoja;

import java.util.ArrayList;
import java.util.List;

public class AdminLojaAdapter extends RecyclerView.Adapter<AdminLojaAdapter.VH> {

    public interface Listener {
        void onAprovar(Loja loja);
        void onRejeitar(Loja loja);
    }

    private final List<Loja> itens = new ArrayList<>();
    private final Listener listener;

    public AdminLojaAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submit(List<Loja> data) {
        itens.clear();
        if (data != null) itens.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_loja, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Loja l = itens.get(position);
        holder.nome.setText(l.getNome());
        holder.cnpj.setText(l.getCnpj());
        holder.categoria.setText(l.getCategoria());
        holder.status.setText(l.getStatus().name());
        boolean pendente = l.getStatus() == StatusLoja.PENDENTE;
        holder.btnAprovar.setVisibility(pendente ? View.VISIBLE : View.GONE);
        holder.btnRejeitar.setVisibility(pendente ? View.VISIBLE : View.GONE);
        holder.btnAprovar.setOnClickListener(v -> listener.onAprovar(l));
        holder.btnRejeitar.setOnClickListener(v -> listener.onRejeitar(l));
    }

    @Override
    public int getItemCount() {
        return itens.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView nome, cnpj, categoria, status;
        final Button btnAprovar, btnRejeitar;

        VH(@NonNull View itemView) {
            super(itemView);
            nome = itemView.findViewById(R.id.txt_nome);
            cnpj = itemView.findViewById(R.id.txt_cnpj);
            categoria = itemView.findViewById(R.id.txt_categoria);
            status = itemView.findViewById(R.id.txt_status);
            btnAprovar = itemView.findViewById(R.id.btn_aprovar);
            btnRejeitar = itemView.findViewById(R.id.btn_rejeitar);
        }
    }
}
