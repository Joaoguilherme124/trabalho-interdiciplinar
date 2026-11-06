package com.example.trabalhointerdiciplinar.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import com.example.trabalhointerdiciplinar.R;
import com.example.trabalhointerdiciplinar.data.CarrinhoManager;
import com.example.trabalhointerdiciplinar.data.DemoCatalog;
import com.example.trabalhointerdiciplinar.model.Produto;
import com.example.trabalhointerdiciplinar.util.MoneyUtils;
import com.example.trabalhointerdiciplinar.util.ProdutoImagens;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProdutoAdapter extends RecyclerView.Adapter<ProdutoAdapter.VH> {

    public interface OnClick {
        void onProdutoClick(Produto produto);
    }

    /** Notificado quando o usuário muda a quantidade de um produto direto na lista. */
    public interface OnQuantidade {
        void onQuantidade(Produto produto, int novaQuantidade);
    }

    /** Alterna o favorito e devolve o novo estado (true = favorito). */
    public interface OnFavorito {
        boolean onFavorito(Produto produto);
    }

    private final List<Produto> itens = new ArrayList<>();
    private final OnClick listener;
    private OnQuantidade quantidadeListener;
    private OnFavorito favoritoListener;
    private Set<String> favoritos = new HashSet<>();

    public ProdutoAdapter(OnClick listener) {
        this.listener = listener;
    }

    /** Sem este listener os controles de carrinho ficam ocultos (ex.: telas só de consulta). */
    public void setQuantidadeListener(OnQuantidade quantidadeListener) {
        this.quantidadeListener = quantidadeListener;
    }

    /** Sem este listener o coração fica oculto. */
    public void setFavoritoListener(OnFavorito favoritoListener) {
        this.favoritoListener = favoritoListener;
    }

    public void setFavoritos(Set<String> ids) {
        favoritos = ids == null ? new HashSet<>() : new HashSet<>(ids);
    }

    public void submit(List<Produto> data) {
        itens.clear();
        if (data != null) itens.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_produto, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Produto p = itens.get(position);
        holder.titulo.setText(p.getTitulo());
        holder.descricao.setText(p.getDescricao());
        holder.preco.setText(MoneyUtils.format(p.getPreco()));
        ProdutoImagens.carregarReduzida(holder.imagem, p, 320);
        bindCategoria(holder, p);
        bindFavorito(holder, p);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onProdutoClick(p);
        });

        bindCarrinho(holder, p);
    }

    private void bindFavorito(VH holder, Produto p) {
        if (favoritoListener == null) {
            holder.btnFavorito.setVisibility(View.GONE);
            return;
        }
        holder.btnFavorito.setVisibility(View.VISIBLE);
        pintarFavorito(holder, p, favoritos.contains(p.getId()));
        holder.btnFavorito.setOnClickListener(v -> {
            boolean agora = favoritoListener.onFavorito(p);
            if (agora) favoritos.add(p.getId()); else favoritos.remove(p.getId());
            pintarFavorito(holder, p, agora);
        });
    }

    private void pintarFavorito(VH holder, Produto p, boolean favorito) {
        Context ctx = holder.itemView.getContext();
        holder.btnFavorito.setSelected(favorito);
        holder.btnFavorito.setContentDescription(ctx.getString(
                favorito ? R.string.cd_desfavoritar : R.string.cd_favoritar, p.getTitulo()));
    }

    /** Rótulo e cor da categoria: paellas, combos e bebidas se distinguem de relance. */
    private void bindCategoria(VH holder, Produto p) {
        int rotulo;
        int cor;
        if (DemoCatalog.CAT_COMBOS.equals(p.getCategoria())) {
            rotulo = R.string.tag_combo;
            cor = R.color.brand_mediterraneo;
        } else if (DemoCatalog.CAT_BEBIDAS.equals(p.getCategoria())) {
            rotulo = R.string.tag_bebida;
            cor = R.color.brand_vinho;
        } else {
            rotulo = R.string.tag_paella;
            cor = R.color.brand_paprica;
        }
        holder.categoria.setText(rotulo);
        holder.marcador.setBackgroundColor(
                holder.itemView.getContext().getColor(cor));
    }

    private void bindCarrinho(VH holder, Produto p) {
        if (quantidadeListener == null) {
            holder.btnAdd.setVisibility(View.GONE);
            holder.stepper.setVisibility(View.GONE);
            holder.selo.setVisibility(View.GONE);
            return;
        }
        int qtd = CarrinhoManager.getInstance().getQuantidade(p.getId());
        destacarSelecionado(holder, qtd > 0);
        holder.btnAdd.setVisibility(qtd == 0 ? View.VISIBLE : View.GONE);
        holder.stepper.setVisibility(qtd == 0 ? View.GONE : View.VISIBLE);
        holder.qty.setText(String.valueOf(qtd));
        holder.btnAdd.setContentDescription(
                holder.itemView.getContext().getString(R.string.cd_adicionar, p.getTitulo()));

        holder.btnAdd.setOnClickListener(v -> alterar(holder, p, 1));
        holder.btnPlus.setOnClickListener(v -> alterar(holder, p, CarrinhoManager.getInstance()
                .getQuantidade(p.getId()) + 1));
        holder.btnMinus.setOnClickListener(v -> alterar(holder, p, CarrinhoManager.getInstance()
                .getQuantidade(p.getId()) - 1));
    }

    /** Selecionado: borda açafrão e selo "Adicionado"; zerado: volta à borda neutra. */
    private void destacarSelecionado(VH holder, boolean selecionado) {
        Context ctx = holder.itemView.getContext();
        float densidade = ctx.getResources().getDisplayMetrics().density;
        MaterialCardView card = (MaterialCardView) holder.itemView;
        card.setStrokeColor(ctx.getColor(selecionado ? R.color.brand_acafrao : R.color.line));
        card.setStrokeWidth(Math.round((selecionado ? 2 : 1) * densidade));
        holder.selo.setVisibility(selecionado ? View.VISIBLE : View.GONE);
        holder.qty.setContentDescription(ctx.getString(R.string.cd_qtd_produto,
                holder.titulo.getText()));
    }

    private void alterar(VH holder, Produto p, int novaQuantidade) {
        quantidadeListener.onQuantidade(p, Math.max(0, novaQuantidade));
        int pos = holder.getBindingAdapterPosition();
        if (pos != RecyclerView.NO_POSITION) notifyItemChanged(pos);
    }

    @Override
    public int getItemCount() {
        return itens.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView titulo, descricao, preco, qty, categoria;
        final View marcador, selo;
        final ImageView imagem;
        final View btnAdd, stepper, btnMinus, btnPlus;
        final View btnFavorito;

        VH(@NonNull View itemView) {
            super(itemView);
            titulo = itemView.findViewById(R.id.txt_titulo);
            descricao = itemView.findViewById(R.id.txt_descricao);
            preco = itemView.findViewById(R.id.txt_preco);
            qty = itemView.findViewById(R.id.txt_qty);
            categoria = itemView.findViewById(R.id.txt_categoria);
            marcador = itemView.findViewById(R.id.marcador_categoria);
            selo = itemView.findViewById(R.id.selo_adicionado);
            imagem = itemView.findViewById(R.id.img_produto);
            btnAdd = itemView.findViewById(R.id.btn_add);
            stepper = itemView.findViewById(R.id.stepper);
            btnMinus = itemView.findViewById(R.id.btn_minus);
            btnPlus = itemView.findViewById(R.id.btn_plus);
            btnFavorito = itemView.findViewById(R.id.btn_favorito);
        }
    }
}
