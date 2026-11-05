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

        lojas.add(new Loja("l1", "u2", "Tech Norte", "12.345.678/0001-90", "Eletrônicos",
                4.7f, "", StatusLoja.APROVADA, "(48) 98888-2002",
                "Av. Centenário, 450", "Gadgets e acessórios."));
        lojas.add(new Loja("l2", "u4", "Moda Atlântica", "23.456.789/0001-01", "Moda",
                4.5f, "", StatusLoja.APROVADA, "(48) 97777-3003",
                "Rua Anita, 88", "Roupas casuais."));
        lojas.add(new Loja("l3", "u2", "Padaria do Sol", "34.567.890/0001-12", "Alimentos",
                4.9f, "", StatusLoja.APROVADA, "(48) 96666-4004",
                "Rua das Flores, 10", "Pães e doces artesanais."));
        lojas.add(new Loja("l4", "u4", "Casa & Lar", "45.678.901/0001-23", "Casa",
                0f, "", StatusLoja.PENDENTE, "(48) 95555-5005",
                "Rua Porto Alegre, 210", "Decoração e utensílios."));

        produtos.add(new Produto("p1", "l1", "Tech Norte", "Fone Bluetooth Pro",
                "Cancelamento de ruído e 30h de bateria.", 249.90, 25, "", 4.8f, "Eletrônicos"));
        produtos.add(new Produto("p2", "l1", "Tech Norte", "Carregador GaN 65W",
                "Compacto e rápido para notebook e celular.", 129.00, 40, "", 4.6f, "Eletrônicos"));
        produtos.add(new Produto("p3", "l2", "Moda Atlântica", "Camiseta Atlântica",
                "Algodão orgânico, corte relaxed.", 79.90, 60, "", 4.4f, "Moda"));
        produtos.add(new Produto("p4", "l2", "Moda Atlântica", "Jaqueta Vento",
                "Leve, impermeável e dobrável.", 189.00, 15, "", 4.7f, "Moda"));
        produtos.add(new Produto("p5", "l3", "Padaria do Sol", "Pão Sourdough",
                "Fermentação natural, crosta crocante.", 18.90, 30, "", 4.9f, "Alimentos"));
        produtos.add(new Produto("p6", "l3", "Padaria do Sol", "Kit Café da Manhã",
                "Pão, frios, suco e café.", 32.00, 20, "", 4.8f, "Alimentos"));
        produtos.add(new Produto("p7", "l1", "Tech Norte", "Mouse Sem Fio",
                "Ergonômico com sensor de alta precisão.", 89.90, 50, "", 4.3f, "Eletrônicos"));
        produtos.add(new Produto("p8", "l2", "Moda Atlântica", "Tênis Casual",
                "Conforto para o dia a dia.", 219.00, 12, "", 4.5f, "Moda"));

        List<ItemPedido> itens1 = Arrays.asList(
                new ItemPedido("p1", "Fone Bluetooth Pro", 1, 249.90)
        );
        pedidos.add(new Pedido("ped1", "u1", "l1", "Tech Norte", itens1, 249.90,
                FormaPagamento.PIX, StatusPedido.SEPARACAO, "22/10/2026",
                "Rua das Flores, 120 — Criciúma/SC"));

        List<ItemPedido> itens2 = Arrays.asList(
                new ItemPedido("p5", "Pão Sourdough", 2, 18.90)
        );
        pedidos.add(new Pedido("ped2", "u1", "l3", "Padaria do Sol", itens2, 37.80,
                FormaPagamento.CARTAO, StatusPedido.ENVIADO, "20/10/2026",
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

    public List<Produto> getProdutosPorLoja(String lojaId) {
        List<Produto> result = new ArrayList<>();
        for (Produto p : produtos) {
            if (p.getIdLoja().equals(lojaId)) result.add(p);
        }
        return result;
    }

    public List<Produto> buscarProdutos(String query, String categoria) {
        String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        List<Produto> result = new ArrayList<>();
        for (Produto p : produtos) {
            boolean matchCat = categoria == null || categoria.equals("Todos")
                    || p.getCategoria().equalsIgnoreCase(categoria);
            boolean matchQ = q.isEmpty()
                    || p.getTitulo().toLowerCase(Locale.ROOT).contains(q)
                    || p.getNomeLoja().toLowerCase(Locale.ROOT).contains(q)
                    || p.getDescricao().toLowerCase(Locale.ROOT).contains(q);
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
