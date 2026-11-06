package com.example.trabalhointerdiciplinar.data.simulacao;

import com.example.trabalhointerdiciplinar.model.StatusPedido;

/** Item da central de notificações do aplicativo (sempre pertence a um pedido e a um usuário). */
public final class AvisoPedido {
    public final String id;
    public final String pedidoId;
    public final String usuarioId;
    /** Etapa que originou o aviso (PENDENTE, SEPARACAO, ENVIADO ou ENTREGUE). */
    public final StatusPedido etapa;
    public final String titulo;
    public final String mensagem;
    public final long criadoEm;
    public boolean lida;

    public AvisoPedido(String id, String pedidoId, String usuarioId, StatusPedido etapa,
                       String titulo, String mensagem, long criadoEm, boolean lida) {
        this.id = id;
        this.pedidoId = pedidoId;
        this.usuarioId = usuarioId;
        this.etapa = etapa;
        this.titulo = titulo;
        this.mensagem = mensagem;
        this.criadoEm = criadoEm;
        this.lida = lida;
    }
}
