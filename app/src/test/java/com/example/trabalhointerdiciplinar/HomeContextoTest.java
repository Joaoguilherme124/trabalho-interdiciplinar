package com.example.trabalhointerdiciplinar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.example.trabalhointerdiciplinar.model.FormaPagamento;
import com.example.trabalhointerdiciplinar.model.Pedido;
import com.example.trabalhointerdiciplinar.model.StatusPedido;
import com.example.trabalhointerdiciplinar.util.PedidoApresentacao;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Regras que decidem o que a Início mostra: status contextual e atalho "Meu QR Code". */
public class HomeContextoTest {

    private Pedido pedido(String id, StatusPedido status) {
        return new Pedido(id, "u", "l", "Loja", new ArrayList<>(), 10.0, FormaPagamento.PIX,
                status, "05/11/2026", "Rua");
    }

    @Test
    public void semPedidosNaoHaPedidoAtivoNemQr() {
        assertNull(PedidoApresentacao.primeiroAtivo(Collections.emptyList()));
        assertNull(PedidoApresentacao.primeiroComQr(Collections.emptyList()));
        assertNull(PedidoApresentacao.primeiroAtivo(null));
        assertNull(PedidoApresentacao.primeiroComQr(null));
    }

    @Test
    public void somentePedidosEntreguesOuCanceladosNaoGeramContexto() {
        List<Pedido> lista = Arrays.asList(
                pedido("a", StatusPedido.ENTREGUE), pedido("b", StatusPedido.CANCELADO));
        assertNull(PedidoApresentacao.primeiroAtivo(lista));
        assertNull(PedidoApresentacao.primeiroComQr(lista));
    }

    @Test
    public void usaOPrimeiroPedidoDaListaQueEstaAtivo() {
        Pedido entregue = pedido("a", StatusPedido.ENTREGUE);
        Pedido separacao = pedido("b", StatusPedido.SEPARACAO);
        Pedido pendente = pedido("c", StatusPedido.PENDENTE);
        List<Pedido> lista = Arrays.asList(entregue, separacao, pendente);
        assertSame(separacao, PedidoApresentacao.primeiroAtivo(lista));
    }

    private Pedido pedidoEm(String id, StatusPedido status, String data) {
        return new Pedido(id, "u", "l", "Loja", new ArrayList<>(), 10.0, FormaPagamento.PIX,
                status, data, "Rua");
    }

    @Test
    public void pedidoAtivoMaisRecentePorDataVenceAOrdemDaLista() {
        Pedido antigo = pedidoEm("a", StatusPedido.PENDENTE, "01/11/2026");
        Pedido recente = pedidoEm("b", StatusPedido.SEPARACAO, "09/11/2026");
        // A lista traz o antigo primeiro de propósito: a data decide, não a posição.
        assertSame(recente, PedidoApresentacao.primeiroAtivo(Arrays.asList(antigo, recente)));
    }

    @Test
    public void dataComparadaComoDataENaoComoTexto() {
        // Como texto, "20/10/2026" > "05/11/2026"; como data, novembro é mais recente.
        Pedido outubro = pedidoEm("a", StatusPedido.PENDENTE, "20/10/2026");
        Pedido novembro = pedidoEm("b", StatusPedido.PENDENTE, "05/11/2026");
        assertSame(novembro, PedidoApresentacao.primeiroAtivo(Arrays.asList(outubro, novembro)));
    }

    @Test
    public void empateDeDataMantemAOrdemDaLista() {
        Pedido primeiro = pedidoEm("a", StatusPedido.PENDENTE, "05/11/2026");
        Pedido segundo = pedidoEm("b", StatusPedido.SEPARACAO, "05/11/2026");
        assertSame(primeiro, PedidoApresentacao.primeiroAtivo(Arrays.asList(primeiro, segundo)));
    }

    @Test
    public void dataIlegivelNuncaVenceUmPedidoDatadoEPedidoEntregueNaoConta() {
        Pedido semData = pedidoEm("a", StatusPedido.PENDENTE, "não é data");
        Pedido datado = pedidoEm("b", StatusPedido.PENDENTE, "01/01/2026");
        Pedido entregueRecente = pedidoEm("c", StatusPedido.ENTREGUE, "30/11/2026");
        assertSame(datado, PedidoApresentacao.primeiroAtivo(Arrays.asList(semData, datado, entregueRecente)));
        assertSame(semData, PedidoApresentacao.primeiroAtivo(Arrays.asList(semData, entregueRecente)));
    }

    @Test
    public void pedidoProntoParaRetirarContaComoAtivoETemQr() {
        Pedido enviado = pedido("a", StatusPedido.ENVIADO);
        assertTrue(PedidoApresentacao.estaAtivo(StatusPedido.ENVIADO));
        assertSame(enviado, PedidoApresentacao.primeiroAtivo(Collections.singletonList(enviado)));
        assertSame(enviado, PedidoApresentacao.primeiroComQr(Collections.singletonList(enviado)));
    }

    @Test
    public void qrSoParaPendenteOuEmSeparacao() {
        Pedido pendente = pedido("a", StatusPedido.PENDENTE);
        assertSame(pendente, PedidoApresentacao.primeiroComQr(Collections.singletonList(pendente)));
        assertEquals("b", PedidoApresentacao.primeiroComQr(Arrays.asList(
                pedido("x", StatusPedido.ENTREGUE), pedido("b", StatusPedido.SEPARACAO))).getId());
    }

    @Test
    public void estaAtivoCobreTodosOsStatus() {
        assertTrue(PedidoApresentacao.estaAtivo(StatusPedido.PENDENTE));
        assertTrue(PedidoApresentacao.estaAtivo(StatusPedido.SEPARACAO));
        assertFalse(PedidoApresentacao.estaAtivo(StatusPedido.ENTREGUE));
        assertFalse(PedidoApresentacao.estaAtivo(StatusPedido.CANCELADO));
    }
}
