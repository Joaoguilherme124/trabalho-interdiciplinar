package com.example.trabalhointerdiciplinar.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trabalhointerdiciplinar.R;
import com.example.trabalhointerdiciplinar.model.ItemCarrinho;
import com.example.trabalhointerdiciplinar.util.MoneyUtils;
import com.example.trabalhointerdiciplinar.util.ProdutoImagens;

import java.util.ArrayList;
import java.util.List;

public class CarrinhoAdapter extends RecyclerView.Adapter<CarrinhoAdapter.VH> {

    public interface Listener {
        void onQtyChanged(String produtoId, int qty);
        void onRemove(String produtoId);
    }

    private final List<ItemCarrinho> itens = new ArrayList<>();
    private final Listener listener;

    public CarrinhoAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submit(List<ItemCarrinho> data) {
        itens.clear();
        if (data != null) itens.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_carrinho, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        ItemCarrinho item = itens.get(position);
        holder.titulo.setText(item.getProduto().getTitulo());
        holder.loja.setText(MoneyUtils.format(item.getProduto().getPreco()) + " cada");
        ProdutoImagens.carregar(holder.imagem, item.getProduto());
        holder.preco.setText(MoneyUtils.format(item.getSubtotal()));
        holder.qty.setText(String.valueOf(item.getQuantidade()));
        holder.btnMinus.setOnClickListener(v ->
                listener.onQtyChanged(item.getProduto().getId(), item.getQuantidade() - 1));
        holder.btnPlus.setOnClickListener(v ->
                listener.onQtyChanged(item.getProduto().getId(), item.getQuantidade() + 1));
        holder.btnRemove.setOnClickListener(v ->
                listener.onRemove(item.getProduto().getId()));
    }

    @Override
    public int getItemCount() {
        return itens.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView titulo, loja, preco, qty;
        final ImageButton btnMinus, btnPlus, btnRemove;
        final ImageView imagem;

        VH(@NonNull View itemView) {
            super(itemView);
            titulo = itemView.findViewById(R.id.txt_titulo);
            loja = itemView.findViewById(R.id.txt_loja);
            preco = itemView.findViewById(R.id.txt_preco);
            qty = itemView.findViewById(R.id.txt_qty);
            imagem = itemView.findViewById(R.id.img_produto);
            btnMinus = itemView.findViewById(R.id.btn_minus);
            btnPlus = itemView.findViewById(R.id.btn_plus);
            btnRemove = itemView.findViewById(R.id.btn_remove);
        }
    }
}
