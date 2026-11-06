package com.example.trabalhointerdiciplinar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.trabalhointerdiciplinar.data.CarrinhoManager;
import com.example.trabalhointerdiciplinar.data.MockDataRepository;
import com.example.trabalhointerdiciplinar.data.preferencias.Favoritos;
import com.example.trabalhointerdiciplinar.data.preferencias.PrefsAcessibilidade;
import com.example.trabalhointerdiciplinar.data.preferencias.PrefsAcessibilidade.TamanhoTexto;
import com.example.trabalhointerdiciplinar.data.simulacao.Armazenamento;
import com.example.trabalhointerdiciplinar.model.Produto;
import com.example.trabalhointerdiciplinar.util.Texto;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Etapa 14: busca, favoritos, desfazer e preferências de acessibilidade. */
public class Etapa14Test {

    static final class Memoria implements Armazenamento {
        final Map<String, String> dados = new HashMap<>();
        @Override public String ler(String k) { return dados.get(k); }
        @Override public void gravar(String k, String v) { dados.put(k, v); }
    }

    private final MockDataRepository repo = MockDataRepository.getInstance();
    private final CarrinhoManager carrinho = CarrinhoManager.getInstance();

    @Before
    public void limpar() {
        carrinho.limpar();
    }

    // ---- busca ----

    @Test
    public void buscaPorNomeIgnoraCaixa() {
        List<Produto> r = repo.buscarProdutos("PAELLA MISTA", "Todos");
        assertFalse(r.isEmpty());
        for (Produto p : r) assertTrue(Texto.normalizar(p.getTitulo()).contains("paella mista"));
    }

    @Test
    public void buscaIgnoraAcentos() {
        assertEquals("camarao", Texto.normalizar("Camarão"));
        assertEquals(repo.buscarProdutos("camarão", "Todos").size(),
                repo.buscarProdutos("camarao", "Todos").size());
        assertEquals(repo.buscarProdutos("agua", "Todos").size(),
                repo.buscarProdutos("Água", "Todos").size());
        assertFalse(repo.buscarProdutos("agua", "Todos").isEmpty());
    }

    @Test
    public void buscaPorCategoriaEPorDescricao() {
        assertFalse(repo.buscarProdutos("bebidas", "Todos").isEmpty());
        assertFalse(repo.buscarProdutos("frutos do mar", "Todos").isEmpty());
    }

    @Test
    public void categoriaCombinaComABusca() {
        List<Produto> todos = repo.buscarProdutos("paella", "Todos");
        List<Produto> soBebidas = repo.buscarProdutos("paella", "Bebidas");
        assertTrue(soBebidas.size() < todos.size());
        for (Produto p : soBebidas) assertEquals("Bebidas", p.getCategoria());
        assertTrue(repo.buscarProdutos("zzzz-inexistente", "Todos").isEmpty());
    }

    // ---- favoritos ----

    @Test
    public void favoritosSaoPorUsuarioEPersistem() {
        Memoria memoria = new Memoria();
        Favoritos f = new Favoritos(memoria);
        assertTrue(f.alternar("u1", "p1"));
        assertTrue(f.eFavorito("u1", "p1"));
        assertFalse(f.eFavorito("u2", "p1"));
        f.alternar("u1", "p3");
        assertEquals(2, f.ids("u1").size());

        Favoritos reaberto = new Favoritos(memoria);          // "reiniciar o aplicativo"
        assertTrue(reaberto.eFavorito("u1", "p3"));
        assertFalse(reaberto.alternar("u1", "p1"));           // desfavorita
        assertFalse(reaberto.eFavorito("u1", "p1"));
        assertEquals(0, reaberto.ids("u2").size());
    }

    @Test
    public void semUsuarioNadaEFavoritado() {
        Favoritos f = new Favoritos(new Memoria());
        assertFalse(f.alternar(null, "p1"));
        assertEquals(0, f.ids(null).size());
    }

    // ---- carrinho: desfazer ----

    @Test
    public void desfazerVoltaDeDuasParaUmaPaella() {
        Produto p = repo.getProdutoById("p1");
        carrinho.adicionar(p, 1);
        carrinho.adicionar(p, 1);
        assertEquals(2, carrinho.getQuantidade("p1"));
        assertTrue(carrinho.desfazer(carrinho.ultimaAlteracaoId()));
        assertEquals(1, carrinho.getQuantidade("p1"));
    }

    @Test
    public void desfazerPrimeiraAdicaoRemoveOItem() {
        carrinho.adicionar(repo.getProdutoById("p1"), 2);
        assertTrue(carrinho.desfazer(carrinho.ultimaAlteracaoId()));
        assertEquals(0, carrinho.getTotalItens());
        assertEquals(0.0, carrinho.getTotal(), 0.0001);
    }

    @Test
    public void acaoAntigaNaoSobrescreveAlteracaoMaisRecente() {
        carrinho.adicionar(repo.getProdutoById("p1"), 1);
        long antiga = carrinho.ultimaAlteracaoId();
        carrinho.adicionar(repo.getProdutoById("p2"), 1);
        assertFalse(carrinho.desfazer(antiga));
        assertEquals(1, carrinho.getQuantidade("p1"));
        assertEquals(1, carrinho.getQuantidade("p2"));
    }

    @Test
    public void desfazerSoUmaVezELimparInvalida() {
        carrinho.adicionar(repo.getProdutoById("p1"), 1);
        long id = carrinho.ultimaAlteracaoId();
        assertTrue(carrinho.desfazer(id));
        assertFalse(carrinho.desfazer(id));
        carrinho.adicionar(repo.getProdutoById("p1"), 1);
        carrinho.limpar();
        assertEquals(0, carrinho.ultimaAlteracaoId());
    }

    @Test
    public void desfazerRestauraItemRemovidoPeloStepper() {
        Produto p = repo.getProdutoById("p3");
        carrinho.adicionar(p, 2);
        carrinho.atualizarQuantidade("p3", 0);
        assertEquals(0, carrinho.getQuantidade("p3"));
        assertTrue(carrinho.desfazer(carrinho.ultimaAlteracaoId()));
        assertEquals(2, carrinho.getQuantidade("p3"));
    }

    @Test
    public void totaisEQuantidadeNuncaFicamNegativos() {
        Produto p = repo.getProdutoById("p1");
        carrinho.adicionar(p, 1);
        carrinho.atualizarQuantidade("p1", -5);
        assertEquals(0, carrinho.getTotalItens());
        assertEquals(0.0, carrinho.getTotal(), 0.0001);
        carrinho.adicionar(p, 3);
        assertEquals(3 * p.getPreco(), carrinho.getTotal(), 0.0001);
    }

    // ---- acessibilidade ----

    @Test
    public void preferenciasDeAcessibilidadePersistem() {
        Memoria memoria = new Memoria();
        PrefsAcessibilidade a = new PrefsAcessibilidade(memoria);
        assertEquals(TamanhoTexto.PADRAO, a.tamanhoTexto());
        assertFalse(a.altoContraste());
        assertFalse(a.reduzirAnimacoes());
        a.definirTamanhoTexto(TamanhoTexto.MUITO_MAIOR);
        a.definirAltoContraste(true);
        a.definirReduzirAnimacoes(true);
        PrefsAcessibilidade reaberta = new PrefsAcessibilidade(memoria);
        assertEquals(TamanhoTexto.MUITO_MAIOR, reaberta.tamanhoTexto());
        assertTrue(reaberta.altoContraste());
        assertTrue(reaberta.reduzirAnimacoes());
    }

    @Test
    public void tamanhoDeTextoComplementaAEscalaDoSistemaSemReduzir() {
        PrefsAcessibilidade a = new PrefsAcessibilidade(new Memoria());
        assertEquals(1.5f, a.escalaFinal(1.5f), 0.001f);               // padrão: respeita o sistema
        a.definirTamanhoTexto(TamanhoTexto.MAIOR);
        assertEquals(1.15f, a.escalaFinal(1.0f), 0.001f);
        assertTrue(a.escalaFinal(1.5f) > 1.5f);                         // nunca reduz
        a.definirTamanhoTexto(TamanhoTexto.MUITO_MAIOR);
        assertTrue(a.escalaFinal(2.0f) <= 2.0f);                        // com teto
    }
}
