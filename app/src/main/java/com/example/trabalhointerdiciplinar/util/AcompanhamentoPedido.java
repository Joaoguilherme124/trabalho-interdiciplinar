package com.example.trabalhointerdiciplinar.util;

import com.example.trabalhointerdiciplinar.data.simulacao.Cronograma;
import com.example.trabalhointerdiciplinar.model.Pedido;
import com.example.trabalhointerdiciplinar.model.StatusPedido;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.ToLongFunction;

/**
 * Acompanhamento do pedido no topo da Início: escolhe qual pedido destacar e traduz os estados
 * reais do pedido nas quatro etapas visuais. Não guarda estado próprio: tudo vem do pedido e da
 * previsão que Minha Jornada também usa.
 */
public final class AcompanhamentoPedido {
    /** Pedidos criados quase juntos (mesma compra em lojas diferentes) formam um só grupo. */
    public static final long JANELA_GRUPO_MS = 10_000L;

    public enum Estado {
        REGISTRADO(0), EM_PREPARO(1), QUASE_PRONTO(2), PRONTO(3);

        /** Posição da etapa destacada na linha de progresso (0..3). */
        public final int etapa;

        Estado(int etapa) {
            this.etapa = etapa;
        }
    }

    public final Estado estado;
    /** Número do pedido em destaque, ex.: "#PED-3B3801". */
    public final String numero;
    public final String pedidoId;
    /** Quantos outros pedidos da mesma compra entram neste acompanhamento. */
    public final int extras;
    /** Previsão em minutos, ou -1 quando não há (pronto, ou fase final sem número). */
    public final int minutos;

    private AcompanhamentoPedido(Estado estado, String numero, String pedidoId, int extras, int minutos) {
        this.estado = estado;
        this.numero = numero;
        this.pedidoId = pedidoId;
        this.extras = extras;
        this.minutos = minutos;
    }

    /**
     * Pedido ativo mais recente (e os que nasceram junto com ele). Regra determinística: maior
     * instante de criação; em empate vale a ordem da lista (que já vem do mais novo para o mais
     * antigo). Devolve lista vazia quando não há pedido ativo.
     */
    public static List<Pedido> escolherGrupo(List<Pedido> pedidos, ToLongFunction<Pedido> criadoEm) {
        if (pedidos == null) return Collections.emptyList();
        Pedido principal = null;
        long melhor = Long.MIN_VALUE;
        for (Pedido p : pedidos) {
            if (!PedidoApresentacao.estaAtivo(p.getStatus())) continue;
            long t = criadoEm.applyAsLong(p);
            if (principal == null || t > melhor) {
                principal = p;
                melhor = t;
            }
        }
        if (principal == null) return Collections.emptyList();
        List<Pedido> grupo = new ArrayList<>();
        grupo.add(principal);
        for (Pedido p : pedidos) {
            if (p == principal || !PedidoApresentacao.estaAtivo(p.getStatus())) continue;
            if (Math.abs(criadoEm.applyAsLong(p) - melhor) <= JANELA_GRUPO_MS) grupo.add(p);
        }
        return grupo;
    }

    /**
     * Estado do grupo: o do pedido menos adiantado (nunca "pronto" enquanto algum não estiver).
     * "Quase pronto" só aparece quando a própria previsão da Jornada entra na fase final; na
     * cozinha manual essa fase não existe, então a etapa só avança por ação do lojista.
     */
    public static AcompanhamentoPedido calcular(List<Pedido> grupo,
                                                Function<Pedido, Cronograma.Previsao> previsao) {
        if (grupo == null || grupo.isEmpty()) return null;
        Estado menor = null;
        int minutos = -1;
        for (Pedido p : grupo) {
            Cronograma.Previsao prev = previsao.apply(p);
            Estado e = estadoDe(p.getStatus(), prev);
            if (e == null) continue;
            if (menor == null || e.ordinal() < menor.ordinal()) menor = e;
            if (e != Estado.PRONTO && prev != null && prev.tipo == Cronograma.TipoPrevisao.MINUTOS) {
                minutos = Math.max(minutos, prev.minutos);
            }
        }
        if (menor == null) return null;
        if (menor == Estado.PRONTO) minutos = -1;
        Pedido principal = grupo.get(0);
        return new AcompanhamentoPedido(menor, PedidoApresentacao.numero(principal), principal.getId(),
                grupo.size() - 1, minutos);
    }

    static Estado estadoDe(StatusPedido status, Cronograma.Previsao previsao) {
        if (status == null) return null;
        switch (status) {
            case PENDENTE:
                return Estado.REGISTRADO;
            case SEPARACAO:
                return previsao != null && previsao.tipo == Cronograma.TipoPrevisao.FINALIZANDO
                        ? Estado.QUASE_PRONTO : Estado.EM_PREPARO;
            case ENVIADO:
                return Estado.PRONTO;
            default:
                return null;       // retirado ou cancelado: sem acompanhamento ativo
        }
    }

    public boolean pronto() {
        return estado == Estado.PRONTO;
    }

    /** Dois acompanhamentos iguais não precisam redesenhar a tela. */
    public boolean mesmoQue(AcompanhamentoPedido o) {
        return o != null && estado == o.estado && minutos == o.minutos && extras == o.extras
                && pedidoId.equals(o.pedidoId);
    }
}
