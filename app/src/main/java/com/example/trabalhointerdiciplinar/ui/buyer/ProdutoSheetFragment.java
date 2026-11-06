package com.example.trabalhointerdiciplinar.ui.buyer;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import com.example.trabalhointerdiciplinar.R;
import com.example.trabalhointerdiciplinar.data.CarrinhoManager;
import com.example.trabalhointerdiciplinar.data.MockDataRepository;
import com.example.trabalhointerdiciplinar.data.SessionManager;
import com.example.trabalhointerdiciplinar.data.preferencias.Preferencias;
import com.example.trabalhointerdiciplinar.model.Loja;
import com.example.trabalhointerdiciplinar.model.Produto;
import com.example.trabalhointerdiciplinar.model.Usuario;
import com.example.trabalhointerdiciplinar.util.MoneyUtils;
import com.example.trabalhointerdiciplinar.util.ProdutoImagens;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

/**
 * Detalhes do produto em painel deslizante sobre o Cardápio. O carrinho continua sendo o
 * CarrinhoManager; quem abriu o painel recebe o aviso para mostrar "Adicionado" com Desfazer.
 */
public class ProdutoSheetFragment extends BottomSheetDialogFragment {
    private static final String ARG_PRODUTO = "produto_id";
    private static final String STATE_QTD = "qtd";
    private static final String TAG = "produto_sheet";

    /** Implementado pela tela que abre o painel (fragmento pai ou activity). */
    public interface Host {
        void onProdutoAdicionado(Produto produto, long alteracaoId);

        void onSheetFechado();
    }

    private Produto produto;
    private int quantidade = 1;
    private ImageButton btnFavorito;

    public static void mostrar(FragmentManager fm, String produtoId) {
        if (fm.findFragmentByTag(TAG) != null) return;
        ProdutoSheetFragment f = new ProdutoSheetFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PRODUTO, produtoId);
        f.setArguments(args);
        f.show(fm, TAG);
    }

    @Override
    public int getTheme() {
        return R.style.Theme_Paella_BottomSheetDialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.sheet_produto, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String id = getArguments() == null ? null : getArguments().getString(ARG_PRODUTO);
        produto = MockDataRepository.getInstance().getProdutoById(id);
        if (produto == null) {
            Toast.makeText(requireContext(), R.string.sheet_produto_indisponivel, Toast.LENGTH_SHORT).show();
            dismissAllowingStateLoss();
            return;
        }
        if (savedInstanceState != null) quantidade = Math.max(1, savedInstanceState.getInt(STATE_QTD, 1));

        ImageView foto = view.findViewById(R.id.img_produto_sheet);
        if (getResources().getConfiguration().fontScale >= 1.5f) {
            // Fonte muito ampliada: foto mais baixa para sobrar espaço para o texto e o botão.
            foto.getLayoutParams().height = Math.round(150 * getResources().getDisplayMetrics().density);
        }
        ProdutoImagens.carregar(foto, produto);
        ((TextView) view.findViewById(R.id.txt_categoria_sheet)).setText(produto.getCategoria());
        ((TextView) view.findViewById(R.id.txt_titulo_sheet)).setText(produto.getTitulo());
        ((TextView) view.findViewById(R.id.txt_preco_sheet)).setText(MoneyUtils.format(produto.getPreco()));
        ((TextView) view.findViewById(R.id.txt_descricao_sheet)).setText(produto.getDescricao());
        bindLoja(view);

        view.findViewById(R.id.btn_fechar_sheet).setOnClickListener(v -> dismiss());
        btnFavorito = view.findViewById(R.id.btn_favorito_sheet);
        pintarFavorito();
        btnFavorito.setOnClickListener(v -> {
            Usuario u = SessionManager.getInstance(requireContext()).getUsuario();
            if (u != null) {
                Preferencias.obter(requireContext()).favoritos().alternar(u.getId(), produto.getId());
            }
            pintarFavorito();
        });

        view.findViewById(R.id.btn_menos_sheet).setContentDescription(
                getString(R.string.cd_diminuir_qtd_de, produto.getTitulo()));
        view.findViewById(R.id.btn_mais_sheet).setContentDescription(
                getString(R.string.cd_aumentar_qtd_de, produto.getTitulo()));
        view.findViewById(R.id.btn_menos_sheet).setOnClickListener(v -> {
            if (quantidade > 1) {
                quantidade--;
                atualizarQuantidade();
            }
        });
        view.findViewById(R.id.btn_mais_sheet).setOnClickListener(v -> {
            quantidade++;
            atualizarQuantidade();
        });
        view.findViewById(R.id.btn_adicionar_sheet).setOnClickListener(v -> adicionar());
        atualizarQuantidade();
    }

    private void bindLoja(View view) {
        Loja loja = MockDataRepository.getInstance().getLojaById(produto.getIdLoja());
        String nome = loja != null ? loja.getNome() : produto.getNomeLoja();
        ((TextView) view.findViewById(R.id.txt_loja_nome)).setText(nome);
        String inicial = nome == null || nome.isEmpty() ? "?" : nome.substring(0, 1).toUpperCase();
        ((TextView) view.findViewById(R.id.txt_loja_inicial)).setText(inicial);
        String desc = loja == null || loja.getDescricao() == null || loja.getDescricao().isEmpty()
                ? getString(R.string.sheet_loja_sem_descricao) : loja.getDescricao();
        ((TextView) view.findViewById(R.id.txt_loja_desc)).setText(desc);
        View botao = view.findViewById(R.id.btn_ver_loja);
        if (loja == null) {
            botao.setVisibility(View.GONE);
            return;
        }
        botao.setContentDescription(getString(R.string.sheet_ver_loja) + ", " + nome);
        botao.setOnClickListener(v -> {
            Intent i = new Intent(requireContext(), LojaDetalheActivity.class);
            i.putExtra(LojaDetalheActivity.EXTRA_LOJA_ID, loja.getId());
            dismiss();
            startActivity(i);
        });
    }

    private void pintarFavorito() {
        Usuario u = SessionManager.getInstance(requireContext()).getUsuario();
        boolean fav = u != null && Preferencias.obter(requireContext()).favoritos()
                .eFavorito(u.getId(), produto.getId());
        btnFavorito.setSelected(fav);
        btnFavorito.setContentDescription(getString(
                fav ? R.string.cd_desfavoritar : R.string.cd_favoritar, produto.getTitulo()));
    }

    private void atualizarQuantidade() {
        View v = requireView();
        TextView qtd = v.findViewById(R.id.txt_qtd_sheet);
        qtd.setText(String.valueOf(quantidade));
        qtd.setContentDescription(getString(R.string.cd_qtd_atual, quantidade));
        ((TextView) v.findViewById(R.id.txt_total_sheet))
                .setText(MoneyUtils.format(produto.getPreco() * quantidade));
    }

    private void adicionar() {
        CarrinhoManager cart = CarrinhoManager.getInstance();
        cart.adicionar(produto, quantidade);
        Host host = host();
        if (host != null) host.onProdutoAdicionado(produto, cart.ultimaAlteracaoId());
        dismiss();
    }

    private Host host() {
        if (getParentFragment() instanceof Host) return (Host) getParentFragment();
        if (getActivity() instanceof Host) return (Host) getActivity();
        return null;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog d = getDialog();
        if (!(d instanceof BottomSheetDialog)) return;
        FrameLayout sheet = d.findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (sheet == null) return;
        // Ocupa boa parte da tela, mas deixa o Cardápio visível por trás.
        int altura = Math.round(getResources().getDisplayMetrics().heightPixels * 0.92f);
        sheet.getLayoutParams().height = altura;
        BottomSheetBehavior<FrameLayout> b = BottomSheetBehavior.from(sheet);
        b.setSkipCollapsed(true);
        b.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    @Override
    public void onDismiss(@NonNull android.content.DialogInterface dialog) {
        super.onDismiss(dialog);
        Host host = host();
        if (host != null) host.onSheetFechado();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_QTD, quantidade);
    }
}
