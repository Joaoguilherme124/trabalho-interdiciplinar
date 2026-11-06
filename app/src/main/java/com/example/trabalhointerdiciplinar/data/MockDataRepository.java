package com.example.trabalhointerdiciplinar.data;

import com.example.trabalhointerdiciplinar.model.FormaPagamento;
import com.example.trabalhointerdiciplinar.model.ItemPedido;
import com.example.trabalhointerdiciplinar.model.Loja;
import com.example.trabalhointerdiciplinar.model.Pedido;
import com.example.trabalhointerdiciplinar.model.Produto;
import com.example.trabalhointerdiciplinar.model.StatusLoja;
import com.example.trabalhointerdiciplinar.model.StatusPedido;
import com.example.trabalhointerdiciplinar.model.TipoUsuario;
import com.example.trabalhointerdiciplinar.model.Usuario;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class MockDataRepository {
    private static MockDataRepository instance;

    private final List<Usuario> usuarios = new ArrayList<>();
    private final List<Loja> lojas = new ArrayList<>();
    private final List<Produto> produtos = new ArrayList<>();
    private final List<Pedido> pedidos = new ArrayList<>();

    private MockDataRepository() {
        seed();
    }

    public static synchronized MockDataRepository getInstance() {
        if (instance == null) {
            instance = new MockDataRepository();
        }
        return instance;
    }

    private void seed() {
        usuarios.add(new Usuario("u1", "João Comprador", "joao@email.com", "123",
                TipoUsuario.COMPRADOR, "Rua das Flores, 120 — Criciúma/SC", "(48) 99999-1001"));
        usuarios.add(new Usuario("u2", "Maria Lojista", "maria@loja.com", "123",
                TipoUsuario.LOJISTA, "Av. Centenário, 450 — Criciúma/SC", "(48) 98888-2002"));
        usuarios.add(new Usuario("u3", "Admin Vitrine", "admin@vitrine.com", "123",
                TipoUsuario.ADMIN, "Plataforma Vitrine", "(48) 90000-0000"));
        usuarios.add(new Usuario("u4", "Carlos Vendedor", "carlos@loja.com", "123",
                TipoUsuario.LOJISTA, "Rua Anita, 88 — Criciúma/SC", "(48) 97777-3003"));

        lojas.add(new Loja(DemoCatalog.LOJA_ID, "u2", DemoCatalog.LOJA_NOME,
                "00.000.000/0000-00", "Paellas", 0f, "", StatusLoja.APROVADA, "(48) 98888-2002",
                "Local do evento a confirmar", "Loja do evento (dados de demonstração)."));
        lojas.add(new Loja("l4", "u4", "Casa & Lar", "45.678.901/0001-23", "Casa",
                0f, "", StatusLoja.PENDENTE, "(48) 95555-5005",
                "Rua Porto Alegre, 210", "Decoração e utensílios."));

        produtos.addAll(DemoCatalog.produtos());

        // Pedidos de demonstração em estados diferentes (nenhuma cobrança real).
        pedidos.add(new Pedido("ped1", "u1", DemoCatalog.LOJA_ID, DemoCatalog.LOJA_NOME,
                Arrays.asList(new ItemPedido("p4", "Combo Paella Mista + Suco", 1, 48.00)),
                48.00, FormaPagamento.PIX, StatusPedido.SEPARACAO, "05/11/2026",
                "Rua das Flores, 120 — Criciúma/SC"));
        pedidos.add(new Pedido("ped2", "u1", DemoCatalog.LOJA_ID, DemoCatalog.LOJA_NOME,
                Arrays.asList(
                        new ItemPedido("p3", "Paella Valenciana", 2, 44.00),
                        new ItemPedido("p7", "Água com Gás", 2, 5.00)),
                98.00, FormaPagamento.CARTAO, StatusPedido.ENTREGUE, "04/11/2026",
                "Rua das Flores, 120 — Criciúma/SC"));
    }

    public Usuario autenticar(String email, String senha) {
        for (Usuario u : usuarios) {
            if (u.getEmail().equalsIgnoreCase(email) && u.getSenha().equals(senha)) {
                return u;
            }
        }
        return null;
    }

    public Usuario getUsuarioById(String id) {
        for (Usuario u : usuarios) {
            if (u.getId().equals(id)) return u;
        }
        return null;
    }

    public Usuario getUsuarioPorEmail(String email) {
        for (Usuario u : usuarios) {
            if (u.getEmail().equalsIgnoreCase(email)) return u;
        }
        return null;
    }

    public Usuario cadastrarUsuario(String nome, String email, String senha,
                                    TipoUsuario tipo, String endereco, String telefone) {
        Usuario u = new Usuario(UUID.randomUUID().toString(), nome, email, senha,
                tipo, endereco, telefone);
        usuarios.add(u);
        return u;
    }

    public List<Loja> getLojasAprovadas() {
        List<Loja> result = new ArrayList<>();
        for (Loja l : lojas) {
            if (l.getStatus() == StatusLoja.APROVADA) result.add(l);
        }
        return result;
    }

    public List<Loja> getLojasPendentes() {
        List<Loja> result = new ArrayList<>();
        for (Loja l : lojas) {
            if (l.getStatus() == StatusLoja.PENDENTE) result.add(l);
        }
        return result;
    }

    public List<Loja> getTodasLojas() {
        return new ArrayList<>(lojas);
    }

    public Loja getLojaById(String id) {
        for (Loja l : lojas) {
            if (l.getId().equals(id)) return l;
        }
        return null;
    }

    public Loja getLojaDoDono(String donoId) {
        for (Loja l : lojas) {
            if (l.getIdDono().equals(donoId) && l.getStatus() == StatusLoja.APROVADA) {
                return l;
            }
        }
        for (Loja l : lojas) {
            if (l.getIdDono().equals(donoId)) return l;
        }
        return null;
    }

    public void addLoja(Loja loja) {
        lojas.add(loja);
    }

    /** Atualiza os dados da loja e o nome exibido em seus produtos (mesma loja em todo o app). */
    public void atualizarLoja(Loja loja) {
        for (int i = 0; i < lojas.size(); i++) {
            if (lojas.get(i).getId().equals(loja.getId())) {
                lojas.set(i, loja);
                break;
            }
        }
        for (Produto p : produtos) {
            if (p.getIdLoja().equals(loja.getId())) p.setNomeLoja(loja.getNome());
        }
    }

    public void atualizarStatusLoja(String id, StatusLoja status) {
        Loja l = getLojaById(id);
        if (l != null) l.setStatus(status);
    }

    public List<Produto> getProdutos() {
        return new ArrayList<>(produtos);
    }

    public Produto getProdutoById(String id) {
        for (Produto p : produtos) {
            if (p.getId().equals(id)) return p;
        }
        return null;
    }

    /** Regra de publicação: produto disponível, de loja aprovada, aparece para os participantes. */
    public boolean estaPublicado(Produto p) {
        if (p == null || !p.isDisponivel()) return false;
        Loja l = getLojaById(p.getIdLoja());
        return l != null && l.getStatus() == StatusLoja.APROVADA;
    }

    /** Produtos da loja visíveis aos participantes (a gestão da loja usa getProdutosPorLoja). */
    public List<Produto> getProdutosPublicadosDaLoja(String lojaId) {
        List<Produto> result = new ArrayList<>();
        for (Produto p : getProdutosPorLoja(lojaId)) {
            if (estaPublicado(p)) result.add(p);
        }
        return result;
    }

    public List<Produto> getProdutosPorLoja(String lojaId) {
        List<Produto> result = new ArrayList<>();
        for (Produto p : produtos) {
            if (p.getIdLoja().equals(lojaId)) result.add(p);
        }
        return result;
    }

    public List<Produto> buscarProdutos(String query, String categoria) {
        String q = com.example.trabalhointerdiciplinar.util.Texto.normalizar(query);
        List<Produto> result = new ArrayList<>();
        for (Produto p : produtos) {
            if (!estaPublicado(p)) continue;
            boolean matchCat = categoria == null || categoria.equals("Todos")
                    || p.getCategoria().equalsIgnoreCase(categoria);
            boolean matchQ = q.isEmpty()
                    || com.example.trabalhointerdiciplinar.util.Texto.normalizar(p.getTitulo()).contains(q)
                    || com.example.trabalhointerdiciplinar.util.Texto.normalizar(p.getNomeLoja()).contains(q)
                    || com.example.trabalhointerdiciplinar.util.Texto.normalizar(p.getCategoria()).contains(q)
                    || com.example.trabalhointerdiciplinar.util.Texto.normalizar(p.getDescricao()).contains(q)
                    || com.example.trabalhointerdiciplinar.util.Texto.normalizar(p.getIngredientes()).contains(q);
            if (matchCat && matchQ) result.add(p);
        }
        return result;
    }

    public List<Loja> buscarLojas(String query, String categoria) {
        String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        List<Loja> result = new ArrayList<>();
        for (Loja l : getLojasAprovadas()) {
            boolean matchCat = categoria == null || categoria.equals("Todos")
                    || l.getCategoria().equalsIgnoreCase(categoria);
            boolean matchQ = q.isEmpty()
                    || l.getNome().toLowerCase(Locale.ROOT).contains(q)
                    || l.getDescricao().toLowerCase(Locale.ROOT).contains(q);
            if (matchCat && matchQ) result.add(l);
        }
        return result;
    }

    public void addProduto(Produto produto) {
        produtos.add(produto);
    }

    public void updateProduto(Produto produto) {
        for (int i = 0; i < produtos.size(); i++) {
            if (produtos.get(i).getId().equals(produto.getId())) {
                produtos.set(i, produto);
                return;
            }
        }
    }

    public void removeProduto(String id) {
        for (int i = 0; i < produtos.size(); i++) {
            if (produtos.get(i).getId().equals(id)) {
                produtos.remove(i);
                return;
            }
        }
    }

    public List<Pedido> getPedidosDoComprador(String compradorId) {
        List<Pedido> result = new ArrayList<>();
        for (Pedido p : pedidos) {
            if (p.getIdComprador().equals(compradorId)) result.add(p);
        }
        Collections.reverse(result);
        return result;
    }

    public List<Pedido> getPedidosDaLoja(String lojaId) {
        List<Pedido> result = new ArrayList<>();
        for (Pedido p : pedidos) {
            if (p.getIdLoja().equals(lojaId)) result.add(p);
        }
        Collections.reverse(result);
        return result;
    }

    public Pedido getPedidoById(String id) {
        for (Pedido p : pedidos) {
            if (p.getId().equals(id)) return p;
        }
        return null;
    }

    public void addPedido(Pedido pedido) {
        pedidos.add(pedido);
    }

    public void atualizarStatusPedido(String id, StatusPedido status) {
        for (Pedido p : pedidos) {
            if (p.getId().equals(id)) {
                p.setStatus(status);
                return;
            }
        }
    }

    public double getTotalComissoes(double taxa) {
        double total = 0;
        for (Pedido p : pedidos) {
            if (p.getStatus() != StatusPedido.CANCELADO) {
                total += p.getValorTotal() * taxa;
            }
        }
        return total;
    }

    public double getFaturamentoLoja(String lojaId) {
        double total = 0;
        for (Pedido p : pedidos) {
            if (p.getIdLoja().equals(lojaId) && p.getStatus() != StatusPedido.CANCELADO) {
                total += p.getValorTotal();
            }
        }
        return total;
    }

    public int countPedidosHoje(String lojaId) {
        int count = 0;
        for (Pedido p : getPedidosDaLoja(lojaId)) {
            if (p.getStatus() != StatusPedido.CANCELADO) count++;
        }
        return count;
    }
}
