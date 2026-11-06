package com.example.trabalhointerdiciplinar.ui.buyer;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.trabalhointerdiciplinar.R;
import com.example.trabalhointerdiciplinar.data.MockDataRepository;
import com.example.trabalhointerdiciplinar.data.SessionManager;
import com.example.trabalhointerdiciplinar.data.simulacao.Cronograma;
import com.example.trabalhointerdiciplinar.data.simulacao.PedidoSimulador;
import com.example.trabalhointerdiciplinar.data.simulacao.Simulador;
import com.example.trabalhointerdiciplinar.model.Pedido;
import com.example.trabalhointerdiciplinar.model.StatusPedido;
import com.google.android.material.button.MaterialButton;
import com.example.trabalhointerdiciplinar.model.Usuario;
import com.example.trabalhointerdiciplinar.util.AcompanhamentoPedido;
import com.example.trabalhointerdiciplinar.util.Animacoes;
import com.example.trabalhointerdiciplinar.util.PedidoApresentacao;
import com.example.trabalhointerdiciplinar.util.PerfilApresentacao;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Collections;
import java.util.List;

/**
 * Tela Início: central do participante. Banner do evento, quatro atalhos, informações
 * essenciais, propósito social e, quando há pedido ativo, um cartão de status.
 */
public class HomeFragment extends Fragment implements RolarAoTopo {

    private static final long TICK_MS = 2000;

    private View raiz;
    private AcompanhamentoPedido acompanhamentoAtual;
    private Boolean modoEventoMontado;
    private final android.os.Handler relogio = new android.os.Handler(android.os.Looper.getMainLooper());
    private final Runnable tick = new Runnable() {
        @Override
        public void run() {
            if (!isAdded() || raiz == null) return;
            atualizarContextoDoPedido();
            relogio.postDelayed(this, TICK_MS);
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_buyer_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        raiz = view;

        bindSaudacao();
        bindDataDoBanner();
        view.findViewById(R.id.btn_notificacoes).setOnClickListener(v -> abrirNotificacoes());

        Animacoes.entradaEmCascata(view.findViewById(R.id.home_content));
        Animacoes.entradaEmCascata(view.findViewById(R.id.atalhos));
    }

    @Override
    public void onResume() {
        super.onResume();
        // Pedidos podem ter mudado desde a última vez (checkout, status, simulação).
        Simulador.obter(requireContext()).reconciliar();
        atualizarContextoDoPedido();
        relogio.removeCallbacks(tick);
        relogio.postDelayed(tick, TICK_MS);
    }

    @Override
    public void onPause() {
        super.onPause();
        relogio.removeCallbacks(tick);
    }

    @Override
    public void rolarAoTopo() {
        if (raiz instanceof androidx.core.widget.NestedScrollView) {
            ((androidx.core.widget.NestedScrollView) raiz).smoothScrollTo(0, 0);
        }
    }

    private Usuario usuario() {
        return SessionManager.getInstance(requireContext()).getUsuario();
    }

    private List<Pedido> pedidosDoUsuario() {
        Usuario u = usuario();
        if (u == null) return Collections.emptyList();
        return MockDataRepository.getInstance().getPedidosDoComprador(u.getId());
    }

    private void bindSaudacao() {
        Usuario u = usuario();
        if (u == null) return;
        String nome = PerfilApresentacao.primeiroNome(u.getNome());
        if (nome.isEmpty()) return;
        ((TextView) raiz.findViewById(R.id.txt_saudacao))
                .setText(getString(R.string.home_saudacao, nome));
    }

    /** "27 NOV" em creme e "2026" em açafrão, num único TextView para quebrar junto. */
    private void bindDataDoBanner() {
        String diaMes = getString(R.string.home_banner_data_dia_mes);
        String ano = getString(R.string.home_banner_data_ano);
        SpannableStringBuilder sb = new SpannableStringBuilder(diaMes).append(' ').append(ano);
        int inicioAno = diaMes.length() + 1;
        sb.setSpan(new ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.brand_acafrao)),
                inicioAno, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ((TextView) raiz.findViewById(R.id.txt_banner_data)).setText(sb);
    }

    private void montarAtalhos(boolean modoEvento) {
        LinearLayout container = raiz.findViewById(R.id.atalhos);
        container.removeAllViews();
        if (modoEvento) {
            adicionarAtalho(container, R.drawable.ic_home_qr, R.string.home_atalho_qr,
                    true, this::abrirQrCode);
            adicionarAtalho(container, R.drawable.ic_nav_orders, R.string.home_atalho_jornada,
                    false, this::abrirJornada);
            adicionarAtalho(container, R.drawable.ic_location, R.string.home_atalho_mapa,
                    false, () -> startActivity(new Intent(requireContext(), MapaEventoActivity.class)));
            adicionarAtalho(container, R.drawable.ic_info, R.string.home_atalho_programacao,
                    false, () -> startActivity(EventoInfoActivity.novaIntent(
                            requireContext(), SecaoEvento.PROGRAMACAO)));
            return;
        }
        adicionarAtalho(container, R.drawable.ic_home_cardapio, R.string.home_atalho_cardapio,
                true, () -> abrirAba(R.id.nav_categorias));
        adicionarAtalho(container, R.drawable.ic_nav_orders, R.string.home_atalho_pedidos,
                false, () -> abrirAba(R.id.nav_pedidos));
        adicionarAtalho(container, R.drawable.ic_home_qr, R.string.home_atalho_qr,
                false, this::abrirQrCode);
        adicionarAtalho(container, R.drawable.ic_info, R.string.home_atalho_evento,
                false, () -> startActivity(
                        EventoInfoActivity.novaIntent(requireContext(), SecaoEvento.TOPO)));
    }

    private void adicionarAtalho(LinearLayout container, @DrawableRes int icone,
                                 @StringRes int rotulo, boolean destaque, Runnable aoTocar) {
        View item = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_home_atalho, container, false);
        ((ImageView) item.findViewById(R.id.atalho_icone)).setImageResource(icone);
        TextView rotuloView = item.findViewById(R.id.atalho_rotulo);
        rotuloView.setText(rotulo);
        // Palavra longa ("Programação"): menor para não quebrar no meio.
        if (rotulo == R.string.home_atalho_programacao) rotuloView.setTextSize(10.5f);
        item.findViewById(R.id.atalho_caixa).setBackgroundResource(
                destaque ? R.drawable.bg_home_icone_acafrao : R.drawable.bg_home_icone_suave);
        item.setContentDescription(getString(rotulo));
        item.setOnClickListener(v -> aoTocar.run());
        container.addView(item);
    }

    /** Mostra o cartão de status, o modo do evento e o ponto do sino conforme os pedidos atuais. */
    private void atualizarContextoDoPedido() {
        PedidoSimulador sim = Simulador.obter(requireContext());
        Usuario u = usuario();
        boolean modoEvento = sim.modoDiaDoEvento();
        if (modoEventoMontado == null || modoEventoMontado != modoEvento) {
            // Montar atalhos depois do attach pode mover o foco e rolar a tela: preserva a posição.
            int rolagem = raiz.getScrollY();
            montarAtalhos(modoEvento);
            modoEventoMontado = modoEvento;
            raiz.post(() -> raiz.scrollTo(0, rolagem));
        }
        raiz.findViewById(R.id.faixa_evento).setVisibility(modoEvento ? View.VISIBLE : View.GONE);
        ((TextView) raiz.findViewById(R.id.txt_subtitulo)).setText(
                modoEvento ? R.string.home_subtitulo_evento : R.string.home_subtitulo);

        atualizarAcompanhamento(sim);
        int naoLidas = u == null ? 0 : sim.central().naoLidas(u.getId());
        raiz.findViewById(R.id.ponto_notificacao).setVisibility(naoLidas > 0 ? View.VISIBLE : View.GONE);
    }

    /** Cartão de acompanhamento: pedido ativo mais recente, com a previsão que a Jornada também usa. */
    private void atualizarAcompanhamento(PedidoSimulador sim) {
        AcompanhamentoPedidoView cartao = raiz.findViewById(R.id.acompanhamento);
        List<Pedido> grupo = AcompanhamentoPedido.escolherGrupo(pedidosDoUsuario(), p -> {
            long criado = sim.criadoEmMs(p.getId());
            return criado >= 0 ? criado : PedidoApresentacao.instanteDaData(p);
        });
        acompanhamentoAtual = AcompanhamentoPedido.calcular(grupo, p -> sim.previsao(p.getId()));
        cartao.aoTocarCartao(v -> abrirJornada());
        cartao.aoTocarQr(v -> {
            Pedido p = acompanhamentoAtual == null ? null
                    : MockDataRepository.getInstance().getPedidoById(acompanhamentoAtual.pedidoId);
            if (p != null) abrirIngresso(p);
        });
        cartao.atualizar(acompanhamentoAtual);
    }

    private void abrirPedido(Pedido pedido) {
        Intent i = new Intent(requireContext(), PedidoDetalheActivity.class);
        i.putExtra(PedidoDetalheActivity.EXTRA_PEDIDO_ID, pedido.getId());
        startActivity(i);
    }

    private void abrirNotificacoes() {
        startActivity(new Intent(requireContext(), NotificacoesActivity.class));
    }

    private void abrirIngresso(Pedido pedido) {
        startActivity(new Intent(requireContext(), IngressoActivity.class)
                .putExtra(IngressoActivity.EXTRA_PEDIDO_ID, pedido.getId()));
    }

    /** Abre Minha Jornada do pedido que o cartão está mostrando; sem pedido, vai para Pedidos. */
    private void abrirJornada() {
        Pedido destaque = acompanhamentoAtual == null ? null
                : MockDataRepository.getInstance().getPedidoById(acompanhamentoAtual.pedidoId);
        if (destaque != null) {
            abrirPedido(destaque);
        } else {
            abrirAba(R.id.nav_pedidos);
        }
    }

    /** Abre o ingresso do primeiro pedido elegível; sem pedido, explica e leva ao cardápio. */
    private void abrirQrCode() {
        Pedido comQr = PedidoApresentacao.primeiroComQr(pedidosDoUsuario());
        if (comQr != null) {
            abrirIngresso(comQr);
            return;
        }
        AlertDialog dialogo = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.home_qr_vazio_titulo)
                .setMessage(R.string.home_qr_vazio_texto)
                .setNegativeButton(R.string.login_fechar, null)
                .setPositiveButton(R.string.home_qr_vazio_acao, (d, w) -> abrirAba(R.id.nav_categorias))
                .show();
        pintarBotoes(dialogo);
    }

    /** Açafrão sobre fundo claro não tem contraste: botões de diálogo em vinho. */
    private void pintarBotoes(AlertDialog dialogo) {
        int vinho = ContextCompat.getColor(requireContext(), R.color.brand_vinho);
        dialogo.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(vinho);
        if (dialogo.getButton(AlertDialog.BUTTON_NEGATIVE) != null) {
            dialogo.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(vinho);
        }
    }

    private void abrirAba(int menuId) {
        if (getActivity() instanceof BuyerMainActivity) {
            ((BuyerMainActivity) getActivity()).openTab(menuId);
        }
    }
}
