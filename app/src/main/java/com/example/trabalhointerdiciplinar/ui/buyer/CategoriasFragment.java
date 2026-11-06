package com.example.trabalhointerdiciplinar.ui.buyer;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trabalhointerdiciplinar.R;
import com.example.trabalhointerdiciplinar.adapter.CategoriaChipAdapter;
import com.example.trabalhointerdiciplinar.adapter.ProdutoAdapter;
import com.example.trabalhointerdiciplinar.data.CarrinhoManager;
import com.example.trabalhointerdiciplinar.data.DemoCatalog;
import com.example.trabalhointerdiciplinar.data.MockDataRepository;
import com.example.trabalhointerdiciplinar.model.ItemCarrinho;
import com.example.trabalhointerdiciplinar.model.Produto;
import com.example.trabalhointerdiciplinar.data.SessionManager;
import com.example.trabalhointerdiciplinar.data.preferencias.Preferencias;
import com.example.trabalhointerdiciplinar.model.Usuario;
import com.example.trabalhointerdiciplinar.util.BarraPedido;
import com.example.trabalhointerdiciplinar.util.FeedbackPedido;
import com.example.trabalhointerdiciplinar.util.MoneyUtils;
import com.example.trabalhointerdiciplinar.util.ProdutoImagens;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Tela Cardápio (aba "Cardápio"): categorias, produtos com seletor de quantidade e a barra
 * flutuante de pedido. O CarrinhoManager continua sendo a única fonte da seleção.
 */
public class CategoriasFragment extends Fragment implements ProdutoSheetFragment.Host, RolarAoTopo {

    private static final long ENTRADA_MS = 220L;
    private static final long SAIDA_MS = 180L;

    private ProdutoAdapter produtoAdapter;
    private String categoria = "Todos";
    private View barCarrinho;
    private LinearLayout barraMiniaturas;
    private TextView txtBarraItens;
    private TextView txtBarraTotal;
    private TextView txtVazio;
    private TextView txtVazioTitulo;
    private android.widget.Button btnVazioAcao;
    private View estadoVazio;
    private android.widget.EditText edtBusca;
    private View btnLimparBusca;
    private android.widget.ImageButton btnFiltroFavoritos;
    private String consulta = "";
    private boolean soFavoritos;
    private RecyclerView rvProdutos;
    /** Estado lógico da barra; evita que uma animação de saída esconda uma barra já reaberta. */
    private boolean barraVisivel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_buyer_categorias, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView rvCat = view.findViewById(R.id.rv_categorias);
        RecyclerView rvProd = view.findViewById(R.id.rv_produtos);
        barCarrinho = view.findViewById(R.id.bar_carrinho);
        barraMiniaturas = view.findViewById(R.id.barra_miniaturas);
        txtBarraItens = view.findViewById(R.id.txt_barra_itens);
        txtBarraTotal = view.findViewById(R.id.txt_barra_total);
        txtVazio = view.findViewById(R.id.txt_vazio);
        txtVazioTitulo = view.findViewById(R.id.txt_vazio_titulo);
        btnVazioAcao = view.findViewById(R.id.btn_vazio_acao);
        estadoVazio = view.findViewById(R.id.estado_vazio);
        edtBusca = view.findViewById(R.id.edt_busca);
        btnLimparBusca = view.findViewById(R.id.btn_limpar_busca);
        btnFiltroFavoritos = view.findViewById(R.id.btn_filtro_favoritos);
        configurarBusca();
        // Fonte muito ampliada: o cabeçalho cede espaço para a lista de produtos.
        if (getResources().getConfiguration().fontScale >= 1.5f) {
            view.findViewById(R.id.txt_apoio_cardapio).setVisibility(View.GONE);
            view.findViewById(R.id.logo_cardapio).setVisibility(View.GONE);
        }

        view.findViewById(R.id.include_demo)
                .setVisibility(DemoCatalog.AMBIENTE_DEMO ? View.VISIBLE : View.GONE);

        rvCat.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        rvProd.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvProdutos = rvProd;
        // Entrada leve dos cards ao abrir e ao trocar de categoria. O catálogo é local, então não
        // há "loading" artificial; sem animações no sistema, a lista aparece direto.
        if (com.example.trabalhointerdiciplinar.util.Animacoes.ativas()) {
            rvProd.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(
                    requireContext(), R.anim.layout_cards_entrada));
        }

        produtoAdapter = new ProdutoAdapter(produto ->
                ProdutoSheetFragment.mostrar(getChildFragmentManager(), produto.getId()));
        produtoAdapter.setQuantidadeListener((produto, qtd) -> {
            CarrinhoManager cart = CarrinhoManager.getInstance();
            int antes = cart.getQuantidade(produto.getId());
            if (antes == 0) {
                cart.adicionar(produto, qtd);
            } else {
                cart.atualizarQuantidade(produto.getId(), qtd);
            }
            atualizarBarraCarrinho(true);
            if (qtd > antes) {
                barCarrinho.post(() -> FeedbackPedido.adicionado(requireView(), barCarrinho,
                        cart.ultimaAlteracaoId(), () -> carregar()));
            }
        });
        produtoAdapter.setFavoritoListener(produto -> {
            Usuario u = SessionManager.getInstance(requireContext()).getUsuario();
            if (u == null) return false;
            boolean agora = Preferencias.obter(requireContext()).favoritos()
                    .alternar(u.getId(), produto.getId());
            if (soFavoritos && !agora) carregar();   // sai da lista de favoritos
            return agora;
        });
        rvProd.setAdapter(produtoAdapter);

        String[] categorias = getResources().getStringArray(R.array.categorias);
        rvCat.setAdapter(new CategoriaChipAdapter(categorias, cat -> {
            categoria = cat;
            carregar();
            if (rvProdutos.getLayoutAnimation() != null) rvProdutos.scheduleLayoutAnimation();
        }));

        View.OnClickListener abrirPedido = v -> {
            if (getActivity() instanceof BuyerMainActivity) {
                ((BuyerMainActivity) getActivity()).openCarrinhoTab();
            }
        };
        barCarrinho.setOnClickListener(abrirPedido);
        view.findViewById(R.id.btn_barra_ver).setOnClickListener(abrirPedido);
        carregar();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Voltando do detalhe ou do Carrinho, as quantidades podem ter mudado.
        carregar();
    }

    private void carregar() {
        Usuario u = SessionManager.getInstance(requireContext()).getUsuario();
        Set<String> favoritos = u == null ? new HashSet<>()
                : Preferencias.obter(requireContext()).favoritos().ids(u.getId());
        List<Produto> produtos = new ArrayList<>();
        for (Produto p : MockDataRepository.getInstance().buscarProdutos(consulta, categoria)) {
            if (!soFavoritos || favoritos.contains(p.getId())) produtos.add(p);
        }
        produtoAdapter.setFavoritos(favoritos);
        produtoAdapter.submit(produtos);
        mostrarEstadoVazio(produtos.isEmpty());
        atualizarBarraCarrinho(false);
    }

    private void mostrarEstadoVazio(boolean vazio) {
        estadoVazio.setVisibility(vazio ? View.VISIBLE : View.GONE);
        if (!vazio) return;
        boolean buscando = !consulta.trim().isEmpty() || !"Todos".equals(categoria);
        if (soFavoritos && !buscando) {
            txtVazioTitulo.setText(R.string.favoritos_vazio_titulo);
            txtVazio.setText(R.string.favoritos_vazio_texto);
            btnVazioAcao.setText(R.string.favoritos_ver_todos);
            btnVazioAcao.setOnClickListener(v -> definirSoFavoritos(false));
        } else {
            txtVazioTitulo.setText(R.string.busca_vazio_titulo);
            txtVazio.setText(R.string.busca_vazio_texto);
            btnVazioAcao.setText(R.string.busca_limpar);
            btnVazioAcao.setOnClickListener(v -> limparFiltros());
        }
    }

    private void configurarBusca() {
        edtBusca.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { }

            @Override
            public void afterTextChanged(android.text.Editable e) {
                consulta = e.toString();
                btnLimparBusca.setVisibility(consulta.isEmpty() ? View.GONE : View.VISIBLE);
                if (produtoAdapter != null) carregar();
            }
        });
        edtBusca.setOnEditorActionListener((v, acao, ev) -> {
            esconderTeclado();
            return true;
        });
        btnLimparBusca.setOnClickListener(v -> edtBusca.setText(""));
        btnFiltroFavoritos.setOnClickListener(v -> definirSoFavoritos(!soFavoritos));
        definirSoFavoritos(soFavoritos);
    }

    private void definirSoFavoritos(boolean ativo) {
        soFavoritos = ativo;
        btnFiltroFavoritos.setSelected(ativo);
        btnFiltroFavoritos.setContentDescription(getString(
                ativo ? R.string.favoritos_filtro_ativo_cd : R.string.favoritos_filtro_cd));
        if (produtoAdapter != null) carregar();
    }

    private void limparFiltros() {
        edtBusca.setText("");
        categoria = "Todos";
        soFavoritos = false;
        btnFiltroFavoritos.setSelected(false);
        RecyclerView rvCat = requireView().findViewById(R.id.rv_categorias);
        if (rvCat.getAdapter() instanceof CategoriaChipAdapter) {
            ((CategoriaChipAdapter) rvCat.getAdapter()).selecionar("Todos");
        }
        carregar();
    }

    private void esconderTeclado() {
        android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager)
                requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(edtBusca.getWindowToken(), 0);
        edtBusca.clearFocus();
    }

    @Override
    public void rolarAoTopo() {
        if (rvProdutos != null) rvProdutos.smoothScrollToPosition(0);
    }

    // ---- ProdutoSheetFragment.Host ----

    @Override
    public void onProdutoAdicionado(Produto produto, long alteracaoId) {
        carregar();
        barCarrinho.post(() -> FeedbackPedido.adicionado(requireView(), barCarrinho, alteracaoId,
                this::carregar));
    }

    @Override
    public void onSheetFechado() {
        if (isAdded() && getView() != null) carregar();   // favoritos e quantidades podem ter mudado
    }

    /** Sincroniza a barra com o carrinho; {@code animar} só vale para mudanças feitas na tela. */
    private void atualizarBarraCarrinho(boolean animar) {
        List<ItemCarrinho> itens = CarrinhoManager.getInstance().getItens();
        int unidades = BarraPedido.totalUnidades(itens);
        if (unidades == 0) {
            esconderBarra(animar);
            return;
        }
        preencherBarra(itens, unidades);
        mostrarBarra(animar);
    }

    private void preencherBarra(List<ItemCarrinho> itens, int unidades) {
        double total = BarraPedido.totalValor(itens);
        txtBarraItens.setText(getResources().getQuantityString(
                R.plurals.barra_itens_selecionados, unidades, unidades));
        txtBarraTotal.setText(MoneyUtils.format(total));
        barCarrinho.setContentDescription(getString(R.string.barra_cd, unidades,
                MoneyUtils.format(total)));

        int max = getResources().getInteger(R.integer.barra_max_miniaturas);
        barraMiniaturas.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (ItemCarrinho item : BarraPedido.miniaturas(itens, max)) {
            View mini = inflater.inflate(R.layout.item_barra_miniatura, barraMiniaturas, false);
            ProdutoImagens.carregarReduzida(mini.findViewById(R.id.miniatura_img),
                    item.getProduto(), 120);
            ((TextView) mini.findViewById(R.id.miniatura_qtd))
                    .setText(String.valueOf(item.getQuantidade()));
            if (barraMiniaturas.getChildCount() > 0) {
                ((LinearLayout.LayoutParams) mini.getLayoutParams()).setMarginStart(
                        -Math.round(8 * getResources().getDisplayMetrics().density));
            }
            barraMiniaturas.addView(mini);
        }
        int restantes = BarraPedido.restantes(itens, max);
        reorganizarSeApertada();
        if (restantes > 0) {
            barraMiniaturas.addView(criarIndicadorMais(restantes));
        }
    }

    /** Fonte ampliada ou tela estreita: o botão desce para uma linha própria, sem esmagar o texto. */
    private void reorganizarSeApertada() {
        android.content.res.Configuration cfg = getResources().getConfiguration();
        boolean apertada = cfg.fontScale >= 1.3f || cfg.screenWidthDp < 360;
        ViewGroup linha = barCarrinho.findViewById(R.id.barra_linha);
        ViewGroup conteudo = barCarrinho.findViewById(R.id.barra_conteudo);
        View botao = barCarrinho.findViewById(R.id.btn_barra_ver);
        if (apertada && botao.getParent() == linha) {
            linha.removeView(botao);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.topMargin = Math.round(8 * getResources().getDisplayMetrics().density);
            conteudo.addView(botao, lp);
        }
    }

    private View criarIndicadorMais(int restantes) {
        float d = getResources().getDisplayMetrics().density;
        TextView mais = new TextView(requireContext());
        mais.setText(getString(R.string.barra_mais, restantes));
        mais.setGravity(Gravity.CENTER);
        mais.setTextSize(13);
        mais.setTypeface(androidx.core.content.res.ResourcesCompat.getFont(
                requireContext(), R.font.inter_variable), android.graphics.Typeface.BOLD);
        mais.setTextColor(ContextCompat.getColor(requireContext(), R.color.brand_vinho));
        mais.setBackgroundResource(R.drawable.bg_barra_mais);
        mais.setLayoutParams(new LinearLayout.LayoutParams(Math.round(40 * d), Math.round(44 * d)));
        mais.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        return mais;
    }

    private void mostrarBarra(boolean animar) {
        barraVisivel = true;
        barCarrinho.animate().cancel();
        boolean estavaOculta = barCarrinho.getVisibility() != View.VISIBLE;
        barCarrinho.setVisibility(View.VISIBLE);
        if (estavaOculta && animar && com.example.trabalhointerdiciplinar.util.Animacoes.ativas()) {
            float deslocamento = 10 * getResources().getDisplayMetrics().density;
            barCarrinho.setAlpha(0f);
            barCarrinho.setTranslationY(deslocamento);
            barCarrinho.animate().alpha(1f).translationY(0f).setDuration(ENTRADA_MS).start();
        } else {
            barCarrinho.setAlpha(1f);
            barCarrinho.setTranslationY(0f);
        }
    }

    private void esconderBarra(boolean animar) {
        barraVisivel = false;
        if (barCarrinho.getVisibility() != View.VISIBLE) return;
        barCarrinho.animate().cancel();
        if (animar && com.example.trabalhointerdiciplinar.util.Animacoes.ativas()) {
            float deslocamento = 8 * getResources().getDisplayMetrics().density;
            barCarrinho.animate().alpha(0f).translationY(deslocamento).setDuration(SAIDA_MS)
                    .withEndAction(() -> {
                        if (!barraVisivel) barCarrinho.setVisibility(View.GONE);
                    }).start();
        } else {
            barCarrinho.setVisibility(View.GONE);
        }
    }
}
