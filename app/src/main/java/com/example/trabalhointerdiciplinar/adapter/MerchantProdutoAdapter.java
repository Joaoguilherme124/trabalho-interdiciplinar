package com.example.trabalhointerdiciplinar.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trabalhointerdiciplinar.R;
import com.example.trabalhointerdiciplinar.model.Produto;
import com.example.trabalhointerdiciplinar.util.MoneyUtils;
import com.example.trabalhointerdiciplinar.util.ProdutoImagens;

import com.google.android.material.materialswitch.MaterialSwitch;

import java.util.ArrayList;
import java.util.List;

/** Lista de produtos da loja: abrir para editar e ativar/desativar no cardápio. */
public class MerchantProdutoAdapter extends RecyclerView.Adapter<MerchantProdutoAdapter.VH> {

    public interface Listener {
        void onEdit(Produto produto);

        void onDisponivel(Produto produto, boolean disponivel);
    }

    private final List<Produto> itens = new ArrayList<>();
    private final Listener listener;

    public MerchantProdutoAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submit(List<Produto> data) {
        itens.clear();
        if (data != null) itens.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_merchant_produto, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Produto p = itens.get(position);
        Context ctx = h.itemView.getContext();
        h.titulo.setText(p.getTitulo());
        h.meta.setText(ctx.getString(R.string.pedido_cliente_data, p.getCategoria(),
                MoneyUtils.format(p.getPreco())));
        ProdutoImagens.carregarReduzida(h.imagem, p, 200);
        pintarEstado(h, p, ctx);
        h.itemView.setAlpha(p.isDisponivel() ? 1f : 0.7f);
        h.itemView.setContentDescription(ctx.getString(R.string.produto_editar_cd, p.getTitulo()));
        h.itemView.setOnClickListener(v -> listener.onEdit(p));
        h.chave.setOnCheckedChangeListener(null);
        h.chave.setChecked(p.isDisponivel());
        h.chave.setContentDescription(ctx.getString(p.isDisponivel()
                ? R.string.produto_publicado_cd : R.string.produto_desativado_cd, p.getTitulo()));
        h.chave.setOnCheckedChangeListener((b, marcado) -> {
            listener.onDisponivel(p, marcado);
            pintarEstado(h, p, ctx);
            h.itemView.setAlpha(marcado ? 1f : 0.7f);
            b.setContentDescription(ctx.getString(marcado
                    ? R.string.produto_publicado_cd : R.string.produto_desativado_cd, p.getTitulo()));
        });
    }

    private void pintarEstado(VH h, Produto p, Context ctx) {
        h.estado.setText(p.isDisponivel() ? R.string.produto_disponivel : R.string.produto_indisponivel);
        h.estado.setTextColor(ctx.getColor(p.isDisponivel() ? R.color.brand_mediterraneo : R.color.brand_grafite_muted));
    }

    @Override
    public int getItemCount() {
        return itens.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView titulo, meta, estado;
        final ImageView imagem;
        final MaterialSwitch chave;

        VH(@NonNull View itemView) {
            super(itemView);
            titulo = itemView.findViewById(R.id.txt_titulo);
            meta = itemView.findViewById(R.id.txt_meta);
            estado = itemView.findViewById(R.id.txt_estado);
            imagem = itemView.findViewById(R.id.img_produto);
            chave = itemView.findViewById(R.id.switch_disponivel);
        }
    }
}
