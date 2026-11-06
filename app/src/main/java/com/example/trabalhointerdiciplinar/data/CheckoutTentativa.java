package com.example.trabalhointerdiciplinar.data;

import com.example.trabalhointerdiciplinar.model.FormaPagamento;

/**
 * Estado de uma única tentativa de checkout (pagamento apenas simulado). Garante que, por mais
 * que o usuário toque repetidamente em "pagar", só uma simulação é iniciada e concluída.
 * Sem dependências de Android, para poder ser testada na JVM.
 */
public final class CheckoutTentativa {

    public enum Estado {
        OCIOSA, PROCESSANDO, CONCLUIDA
    }

    private Estado estado = Estado.OCIOSA;
    private FormaPagamento forma;

    /** Forma de pagamento escolhida; só pode mudar enquanto a tentativa está ociosa. */
    public synchronized boolean escolher(FormaPagamento nova) {
        if (estado != Estado.OCIOSA) return false;
        forma = nova;
        return true;
    }

    public synchronized FormaPagamento getForma() {
        return forma;
    }

    public synchronized Estado getEstado() {
        return estado;
    }

    /**
     * Inicia o processamento.
     *
     * @return true somente na primeira chamada válida (com forma escolhida e tentativa ociosa)
     */
    public synchronized boolean iniciar() {
        if (estado != Estado.OCIOSA || forma == null) return false;
        estado = Estado.PROCESSANDO;
        return true;
    }

    public synchronized void concluir() {
        if (estado == Estado.PROCESSANDO) estado = Estado.CONCLUIDA;
    }

    /** Desiste do processamento (ex.: usuário saiu da tela); permite uma nova tentativa. */
    public synchronized void cancelar() {
        if (estado == Estado.PROCESSANDO) estado = Estado.OCIOSA;
    }
}
