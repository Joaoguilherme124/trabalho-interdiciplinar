package com.example.trabalhointerdiciplinar.data.simulacao;

import com.example.trabalhointerdiciplinar.model.FormaPagamento;
import com.example.trabalhointerdiciplinar.model.ItemPedido;
import com.example.trabalhointerdiciplinar.model.Pedido;
import com.example.trabalhointerdiciplinar.model.StatusPedido;

import java.util.ArrayList;
import java.util.List;

/**
 * Registro persistido de um pedido em simulação: pertence a um usuário, guarda o cronograma
 * (início, retirada, etapas já notificadas) e uma cópia do pedido para recriá-lo depois que o
 * aplicativo for fechado (o repositório demonstrativo vive só em memória).
 */
public final class RegistroSimulacao {
    public final String pedidoId;
    public final String usuarioId;
    public long inicioMs;
    public boolean retirado;
    /** Bit i = etapa i (índice em {@link Cronograma#ETAPAS}) já registrada/notificada. */
    public int notificadas;
    public StatusPedido statusSalvo;
    /** A cozinha assumiu o pedido: o cronograma automático não o move mais. */
    public boolean manual;
    /** O lojista confirmou o recebimento do pedido. */
    public boolean recebido;
    /** Previsão ajustada pelo lojista, em minutos (-1 = usar a previsão demonstrativa). */
    public int previsaoMin = -1;
    /** Bit i = notificação do sistema da etapa i já emitida (ou dispensada). */
    public int emitidas;
    /** Instante em que o pedido nasceu; não muda quando o cronograma é rebaseado. */
    public long criadoMs;
    // Cópia do pedido
    final String idLoja;
    final String nomeLoja;
    final FormaPagamento forma;
    final String data;
    final String endereco;
    final double total;
    final List<ItemPedido> itens;

    private static final String CAMPO = "\u001F";
    private static final String ITEM = "\u001E";
    private static final String SUB = "\u001D";

    RegistroSimulacao(Pedido p, long inicioMs) {
        this(p.getId(), p.getIdComprador(), inicioMs, false, 0, p.getStatus(), p.getIdLoja(),
                p.getNomeLoja(), p.getFormaPagamento(), p.getData(), p.getEnderecoEntrega(),
                p.getValorTotal(), new ArrayList<>(p.getListaItens()));
    }

    private RegistroSimulacao(String pedidoId, String usuarioId, long inicioMs, boolean retirado,
                              int notificadas, StatusPedido statusSalvo, String idLoja,
                              String nomeLoja, FormaPagamento forma, String data, String endereco,
                              double total, List<ItemPedido> itens) {
        this.pedidoId = pedidoId;
        this.usuarioId = usuarioId;
        this.inicioMs = inicioMs;
        this.criadoMs = inicioMs;
        this.retirado = retirado;
        this.notificadas = notificadas;
        this.statusSalvo = statusSalvo;
        this.idLoja = idLoja;
        this.nomeLoja = nomeLoja;
        this.forma = forma;
        this.data = data;
        this.endereco = endereco == null ? "" : endereco;
        this.total = total;
        this.itens = itens;
    }

    /** Recria o pedido a partir da cópia guardada. */
    Pedido paraPedido() {
        return new Pedido(pedidoId, usuarioId, idLoja, nomeLoja, new ArrayList<>(itens), total,
                forma, statusSalvo, data, endereco);
    }

    String codificar() {
        StringBuilder it = new StringBuilder();
        for (ItemPedido i : itens) {
            if (it.length() > 0) it.append(ITEM);
            it.append(i.getIdProduto()).append(SUB).append(i.getTitulo()).append(SUB)
                    .append(i.getQuantidade()).append(SUB).append(i.getPrecoUnitario());
        }
        return pedidoId + CAMPO + usuarioId + CAMPO + inicioMs + CAMPO + (retirado ? "1" : "0")
                + CAMPO + notificadas + CAMPO + statusSalvo.name() + CAMPO + idLoja + CAMPO
                + nomeLoja + CAMPO + forma.name() + CAMPO + data + CAMPO + endereco + CAMPO + total
                + CAMPO + it + CAMPO + (manual ? "1" : "0") + CAMPO + (recebido ? "1" : "0")
                + CAMPO + previsaoMin + CAMPO + emitidas + CAMPO + criadoMs;
    }

    static RegistroSimulacao decodificar(String linha) {
        String[] c = linha.split(CAMPO, -1);
        if (c.length < 13) return null;
        try {
            List<ItemPedido> itens = new ArrayList<>();
            if (!c[12].isEmpty()) {
                for (String bruto : c[12].split(ITEM)) {
                    String[] s = bruto.split(SUB, -1);
                    itens.add(new ItemPedido(s[0], s[1], Integer.parseInt(s[2]),
                            Double.parseDouble(s[3])));
                }
            }
            RegistroSimulacao r = new RegistroSimulacao(c[0], c[1], Long.parseLong(c[2]), "1".equals(c[3]),
                    Integer.parseInt(c[4]), StatusPedido.valueOf(c[5]), c[6], c[7],
                    FormaPagamento.valueOf(c[8]), c[9], c[10], Double.parseDouble(c[11]), itens);
            // Registros antigos (13 campos): nada manual e notificações já emitidas.
            r.emitidas = r.notificadas;
            if (c.length >= 17) {
                r.manual = "1".equals(c[13]);
                r.recebido = "1".equals(c[14]);
                r.previsaoMin = Integer.parseInt(c[15]);
                r.emitidas = Integer.parseInt(c[16]);
            }
            if (c.length >= 18) r.criadoMs = Long.parseLong(c[17]);
            return r;
        } catch (RuntimeException e) {
            return null;
        }
    }
}
