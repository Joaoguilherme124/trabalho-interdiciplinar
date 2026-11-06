# Dados de demonstração (Cardápio)

**Nada do que está abaixo é oficial.** Produtos, descrições, preços e fotos existem só para
demonstrar o aplicativo. Nenhuma cobrança é feita: o pagamento é apenas simulado.

## Onde estão e como trocar pelos dados reais

| O quê | Onde |
|---|---|
| Catálogo (produtos, preços, categorias) | `data/DemoCatalog.java` → `produtos()` |
| Chave do ambiente demo (avisos na interface) | `DemoCatalog.AMBIENTE_DEMO` (`false` oculta os avisos) |
| Pedidos de exemplo (estados Separação / Entregue) | `data/MockDataRepository.java`, final de `seed()` |
| Lista de categorias exibida | `res/values/strings.xml` → array `categorias` (precisa bater com as categorias dos produtos) |
| Fotos | `res/drawable-nodpi/img_*.jpg` e `res/drawable/paella_hero.jpg`; o campo `urlImagem` do produto guarda o **nome** do drawable |

Ao integrar um backend, substitua `MockDataRepository`/`DemoCatalog` e ajuste `ProdutoImagens`
(hoje só resolve drawables locais; URLs remotas exigem uma biblioteca de imagens).

## Pendências conhecidas

- Cardápio, valores e fotos oficiais.
- Pagamento real (Pix/cartão): hoje só existe o fluxo simulado em `CheckoutActivity`.
- Tela própria de confirmação (hoje: aviso + abertura da aba Pedidos).

## Créditos das fotos de demonstração (Wikimedia Commons)

| Arquivo no app | Origem | Licença |
|---|---|---|
| `paella_hero.jpg` | imagem já existente no projeto | — |
| `img_paella_frutos_do_mar.jpg` | "Paella de fruit de mer.jpg" | CC0 |
| `img_paella_valenciana.jpg` | "Paella valenciana - Casa Diego.jpg", Francesc Fort | CC BY-SA 4.0 |
| `img_bebida_suco.jpg` | "Orange juice 1 edit1.jpg" | Domínio público |
| `img_bebida_agua.jpg` | "Blue glass with carbonated water.jpg" | CC0 |

Substitua por fotografias próprias do evento antes de qualquer uso público; a licença CC BY-SA
exige atribuição enquanto a foto da Casa Diego estiver no app.
