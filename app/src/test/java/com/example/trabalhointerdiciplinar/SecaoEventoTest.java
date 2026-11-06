package com.example.trabalhointerdiciplinar;

import static org.junit.Assert.assertEquals;

import com.example.trabalhointerdiciplinar.ui.buyer.SecaoEvento;

import org.junit.Test;

/** A navegação do Perfil para seções da tela de evento depende desta conversão. */
public class SecaoEventoTest {

    @Test
    public void nomeValidoAbreASecaoCorrespondente() {
        assertEquals(SecaoEvento.ABADEUS, SecaoEvento.daString("ABADEUS"));
        assertEquals(SecaoEvento.DUVIDAS, SecaoEvento.daString("DUVIDAS"));
        assertEquals(SecaoEvento.TOPO, SecaoEvento.daString("TOPO"));
        assertEquals(SecaoEvento.PROGRAMACAO, SecaoEvento.daString("PROGRAMACAO"));
    }

    @Test
    public void valorAusenteOuDesconhecidoAbreNoTopo() {
        assertEquals(SecaoEvento.TOPO, SecaoEvento.daString(null));
        assertEquals(SecaoEvento.TOPO, SecaoEvento.daString(""));
        assertEquals(SecaoEvento.TOPO, SecaoEvento.daString("abadeus"));
        assertEquals(SecaoEvento.TOPO, SecaoEvento.daString("INEXISTENTE"));
    }

    @Test
    public void naoHaSecoesEmDuplicidade() {
        assertEquals(4, SecaoEvento.values().length);
    }
}
