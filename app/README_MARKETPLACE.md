# Vitrine — Marketplace Android (Java)

## Contas demo

| E-mail | Senha | Papel |
|--------|-------|--------|
| joao@email.com | 123 | Comprador |
| maria@loja.com | 123 | Lojista |
| admin@vitrine.com | 123 | Admin |

## Estrutura de pacotes

- `model/` — POJOs e enums
- `data/` — MockDataRepository, SessionManager, CarrinhoManager
- `ui/auth/` — Login, Cadastro, Recuperar senha
- `ui/buyer/` — Home, Categorias, Carrinho, Pedidos, Perfil + Detalhes/Checkout
- `ui/merchant/` — Dashboard, Produtos CRUD, Pedidos, Cadastro de loja
- `ui/admin/` — Aprovação de lojas e comissões
- `adapter/` — RecyclerView adapters

## Como rodar

Abra o projeto no Android Studio, Sync Gradle e execute no emulador/dispositivo.
