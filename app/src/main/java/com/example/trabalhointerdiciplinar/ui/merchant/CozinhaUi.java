package com.example.trabalhointerdiciplinar.ui.merchant;

import android.content.Context;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.trabalhointerdiciplinar.R;
import com.example.trabalhointerdiciplinar.data.MockDataRepository;
import com.example.trabalhointerdiciplinar.data.cozinha.FluxoCozinha;
import com.example.trabalhointerdiciplinar.data.simulacao.Cronograma;
import com.example.trabalhointerdiciplinar.data.simulacao.PedidoSimulador;
import com.example.trabalhointerdiciplinar.data.simulacao.PedidoSimulador.AcaoCozinha;
import com.example.trabalhointerdiciplinar.data.simulacao.Simulador;
import com.example.trabalhointerdiciplinar.model.Pedido;
import com.example.trabalhointerdiciplinar.model.StatusPedido;
import com.example.trabalhointerdiciplinar.model.Usuario;
import com.example.trabalhointerdiciplinar.util.MoneyUtils;
import com.example.trabalhointerdiciplinar.util.PedidoApresentacao;
import com.example.trabalhointerdiciplinar.util.SeloStatus;

/** Montagem comum dos cartões de pedido da cozinha (Central e Fila) e execução das ações. */
final class CozinhaUi {
    private CozinhaUi() {
    }

    static AcaoCozinha proximaAcao(PedidoSimulador sim, Pedido p) {
        boolean podeReceber = sim.registro(p.getId()) != null && !sim.recebido(p.getId());
        return FluxoCozinha.proxima(p.getStatus(), podeReceber);
    }

    static int rotuloAcao(AcaoCozinha acao) {
        switch (acao) {
            case RECEBER:
                return R.string.acao_receber;
            case INICIAR_PREPARO:
                return R.string.acao_iniciar_preparo;
            case MARCAR_PRONTO:
                return R.string.acao_marcar_pronto;
            default:
                return R.string.acao_confirmar_retirada;
        }
    }

    private static int avisoDaAcao(AcaoCozinha acao) {
        switch (acao) {
            case RECEBER:
                return R.string.acao_feita_receber;
            case INICIAR_PREPARO:
                return R.string.acao_feita_preparo;
            case MARCAR_PRONTO:
                return R.string.acao_feita_pronto;
            default:
                return R.string.acao_feita_retirada;
        }
    }

    /** Executa a ação na mesma fonte usada por Minha Jornada e avisa o resultado. */
    static boolean executar(Context ctx, Pedido p, AcaoCozinha acao) {
        PedidoSimulador sim = Simulador.obter(ctx);
        boolean ok = sim.executarAcao(p.getId(), p.getIdLoja(), acao);
        Toast.makeText(ctx, ok ? avisoDaAcao(acao) : R.string.acao_indisponivel, Toast.LENGTH_SHORT).show();
        return ok;
    }

    static String textoPrevisao(Context ctx, PedidoSimulador sim, Pedido p) {
        StatusPedido s = p.getStatus();
        if (s == StatusPedido.ENTREGUE) return ctx.getString(R.string.pedido_retirado);
        if (s == StatusPedido.ENVIADO) return ctx.getString(R.string.pedido_pronto_retirada);
        if (s == StatusPedido.CANCELADO) return PedidoApresentacao.rotuloStatus(s);
        if (s == StatusPedido.PENDENTE && sim.registro(p.getId()) != null && !sim.recebido(p.getId())) {
            return ctx.getString(R.string.pedido_aguardando_recebimento);
        }
        Cronograma.Previsao prev = sim.previsao(p.getId());
        return prev.tipo == Cronograma.TipoPrevisao.MINUTOS
                ? ctx.getString(R.string.pedido_previsao_min, prev.minutos)
                : ctx.getString(R.string.jornada_previsao_finalizando);
    }

    /** Preenche o cartão {@code item_cozinha_pedido}. */
    static void preencherCartao(View card, Pedido p, PedidoSimulador sim) {
        Context ctx = card.getContext();
        ((TextView) card.findViewById(R.id.txt_numero)).setText(PedidoApresentacao.numero(p));
        SeloStatus.aplicar(card.findViewById(R.id.txt_status), p.getStatus());
        ((TextView) card.findViewById(R.id.txt_cliente)).setText(ctx.getString(
                R.string.pedido_cliente_data, nomeDoCliente(p), p.getData()));
        ((TextView) card.findViewById(R.id.txt_itens)).setText(PedidoApresentacao.resumoItens(p));
        ((TextView) card.findViewById(R.id.txt_previsao)).setText(textoPrevisao(ctx, sim, p));
        ((TextView) card.findViewById(R.id.txt_total)).setText(MoneyUtils.format(p.getValorTotal()));
    }

    static String nomeDoCliente(Pedido p) {
        Usuario u = MockDataRepository.getInstance().getUsuarioById(p.getIdComprador());
        return u == null ? "Participante" : u.getNome();
    }
}
