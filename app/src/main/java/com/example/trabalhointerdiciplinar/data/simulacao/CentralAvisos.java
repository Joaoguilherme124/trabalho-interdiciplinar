package com.example.trabalhointerdiciplinar.data.simulacao;

import com.example.trabalhointerdiciplinar.model.StatusPedido;

import java.util.ArrayList;
import java.util.List;

/**
 * Central de notificações do aplicativo: lista local, persistida, separada por usuário. Existe
 * mesmo que o sistema não permita notificações (o aviso continua aparecendo dentro do app).
 */
public final class CentralAvisos {
    private static final String CHAVE = "avisos";
    private static final String REG = "\u001C";
    private static final String CAMPO = "\u001F";

    private final Armazenamento armazenamento;
    private final List<AvisoPedido> avisos = new ArrayList<>();

    public CentralAvisos(Armazenamento armazenamento) {
        this.armazenamento = armazenamento;
        carregar();
    }

    public synchronized void adicionar(AvisoPedido aviso) {
        avisos.add(aviso);
        salvar();
    }

    /** Avisos do usuário, do mais novo ao mais antigo. */
    public synchronized List<AvisoPedido> listar(String usuarioId) {
        List<AvisoPedido> resultado = new ArrayList<>();
        for (int i = avisos.size() - 1; i >= 0; i--) {
            AvisoPedido a = avisos.get(i);
            if (a.usuarioId.equals(usuarioId)) resultado.add(a);
        }
        return resultado;
    }

    public synchronized int naoLidas(String usuarioId) {
        int total = 0;
        for (AvisoPedido a : avisos) {
            if (a.usuarioId.equals(usuarioId) && !a.lida) total++;
        }
        return total;
    }

    public synchronized void marcarLida(String avisoId, String usuarioId) {
        for (AvisoPedido a : avisos) {
            if (a.id.equals(avisoId) && a.usuarioId.equals(usuarioId) && !a.lida) {
                a.lida = true;
                salvar();
                return;
            }
        }
    }

    public synchronized void removerDoPedido(String pedidoId) {
        boolean mudou = false;
        for (int i = avisos.size() - 1; i >= 0; i--) {
            if (avisos.get(i).pedidoId.equals(pedidoId)) {
                avisos.remove(i);
                mudou = true;
            }
        }
        if (mudou) salvar();
    }

    private void salvar() {
        StringBuilder sb = new StringBuilder();
        for (AvisoPedido a : avisos) {
            if (sb.length() > 0) sb.append(REG);
            sb.append(a.id).append(CAMPO).append(a.pedidoId).append(CAMPO).append(a.usuarioId)
                    .append(CAMPO).append(a.etapa.name()).append(CAMPO).append(a.titulo)
                    .append(CAMPO).append(a.mensagem).append(CAMPO).append(a.criadoEm)
                    .append(CAMPO).append(a.lida ? "1" : "0");
        }
        armazenamento.gravar(CHAVE, sb.toString());
    }

    private void carregar() {
        String bruto = armazenamento.ler(CHAVE);
        if (bruto == null || bruto.isEmpty()) return;
        for (String linha : bruto.split(REG)) {
            String[] c = linha.split(CAMPO, -1);
            if (c.length < 8) continue;
            try {
                avisos.add(new AvisoPedido(c[0], c[1], c[2], StatusPedido.valueOf(c[3]), c[4], c[5],
                        Long.parseLong(c[6]), "1".equals(c[7])));
            } catch (IllegalArgumentException ignorado) {
                // Registro corrompido: descarta apenas ele.
            }
        }
    }
}
