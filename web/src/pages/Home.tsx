import { Search, Star } from 'lucide-react'
import { useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { useCart } from '../context/CartContext'
import {
  categories,
  formatPrice,
  products,
  stores,
} from '../data/mock'

export function Home() {
  const [query, setQuery] = useState('')
  const [category, setCategory] = useState('Todos')
  const { add } = useCart()

  const filteredStores = useMemo(() => {
    const q = query.trim().toLowerCase()
    return stores.filter((store) => {
      const matchCat =
        category === 'Todos' || store.category === category
      const matchQuery =
        !q ||
        store.name.toLowerCase().includes(q) ||
        store.description.toLowerCase().includes(q) ||
        store.category.toLowerCase().includes(q)
      return matchCat && matchQuery
    })
  }, [query, category])

  const filteredProducts = useMemo(() => {
    const q = query.trim().toLowerCase()
    return products.filter((product) => {
      const matchCat =
        category === 'Todos' || product.category === category
      const matchQuery =
        !q ||
        product.name.toLowerCase().includes(q) ||
        product.description.toLowerCase().includes(q)
      return matchCat && matchQuery && product.active
    })
  }, [query, category])

  return (
    <div className="page-enter">
      {/* Hero — brand first */}
      <section className="relative overflow-hidden border-b border-line dark:border-dark-line">
        <div className="absolute inset-0 bg-[radial-gradient(ellipse_at_top_right,_#c8f54233,_transparent_55%),radial-gradient(ellipse_at_bottom_left,_#0b3d2e22,_transparent_50%)] dark:bg-[radial-gradient(ellipse_at_top_right,_#c8f54222,_transparent_55%),radial-gradient(ellipse_at_bottom_left,_#146b4e33,_transparent_50%)]" />
        <div className="relative mx-auto grid max-w-6xl gap-8 px-4 py-12 sm:px-6 lg:grid-cols-[1.1fr_0.9fr] lg:items-end lg:py-16">
          <div>
            <p className="font-display text-5xl font-extrabold tracking-tight text-leaf sm:text-6xl dark:text-lime">
              Vitrine
            </p>
            <h1 className="mt-3 max-w-md font-display text-2xl font-bold leading-tight text-ink sm:text-3xl dark:text-mist">
              Várias lojas. Uma só mesa de compras.
            </h1>
            <p className="mt-3 max-w-md text-sm leading-relaxed text-muted sm:text-base">
              Explore parceiros locais, filtre por categoria e monte seu
              pedido em segundos.
            </p>
          </div>

          <div className="relative h-44 overflow-hidden rounded-3xl bg-leaf sm:h-56 lg:h-64">
            <div className="absolute inset-0 bg-[linear-gradient(135deg,#0b3d2e_0%,#146b4e_45%,#1a8f66_100%)]" />
            <div className="absolute -right-8 -top-8 h-40 w-40 rounded-full bg-lime/30 blur-2xl" />
            <div className="absolute -bottom-10 left-10 h-36 w-36 rounded-full bg-lime/20 blur-2xl" />
            <div className="absolute inset-0 flex flex-col justify-end p-6">
              <p className="font-display text-3xl font-extrabold text-lime">
                +{stores.length} lojas
              </p>
              <p className="mt-1 text-sm text-sand/90">
                parceiras ativas na vitrine
              </p>
            </div>
          </div>
        </div>
      </section>

      <div className="mx-auto max-w-6xl px-4 py-8 sm:px-6">
        {/* Search */}
        <div className="relative">
          <Search
            size={18}
            className="pointer-events-none absolute left-4 top-1/2 -translate-y-1/2 text-muted"
          />
          <input
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="Buscar produtos e lojas..."
            className="w-full rounded-2xl border border-line bg-paper py-3.5 pl-11 pr-4 text-sm outline-none transition focus:border-leaf-mid dark:border-dark-line dark:bg-dark-panel"
          />
        </div>

        {/* Chips */}
        <div className="mt-4 flex gap-2 overflow-x-auto pb-1">
          {categories.map((cat) => {
            const active = category === cat
            return (
              <button
                key={cat}
                type="button"
                onClick={() => setCategory(cat)}
                className={`shrink-0 rounded-full px-4 py-2 text-sm font-semibold transition ${
                  active
                    ? 'bg-lime text-ink'
                    : 'bg-sand text-ink-soft hover:bg-line dark:bg-dark-elevated dark:text-mist dark:hover:bg-dark-line'
                }`}
              >
                {cat}
              </button>
            )
          })}
        </div>

        {/* Featured stores */}
        <section className="mt-10">
          <div className="mb-4 flex items-end justify-between gap-3">
            <h2 className="font-display text-xl font-bold sm:text-2xl">
              Lojas em destaque
            </h2>
            <span className="text-xs text-muted">
              {filteredStores.length} encontradas
            </span>
          </div>

          {filteredStores.length === 0 ? (
            <p className="rounded-2xl border border-dashed border-line p-8 text-center text-sm text-muted dark:border-dark-line">
              Nenhuma loja encontrada para essa busca.
            </p>
          ) : (
            <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
              {filteredStores.map((store) => (
                <Link
                  key={store.id}
                  to={`/loja/${store.id}`}
                  className="group rounded-3xl border border-line bg-paper p-5 transition hover:-translate-y-0.5 hover:border-leaf-mid dark:border-dark-line dark:bg-dark-panel dark:hover:border-lime/40"
                >
                  <div className="flex items-start gap-3">
                    <div
                      className="flex h-12 w-12 items-center justify-center rounded-2xl text-sm font-bold text-white"
                      style={{ backgroundColor: store.color }}
                    >
                      {store.initial}
                    </div>
                    <div className="min-w-0 flex-1">
                      <p className="truncate font-semibold group-hover:text-leaf dark:group-hover:text-lime">
                        {store.name}
                      </p>
                      <p className="text-xs text-muted">{store.category}</p>
                    </div>
                    <span className="inline-flex items-center gap-1 text-xs font-semibold text-muted">
                      <Star size={12} className="fill-lime text-leaf" />
                      {store.rating.toFixed(1)}
                    </span>
                  </div>
                  <p className="mt-3 line-clamp-2 text-sm text-muted">
                    {store.description}
                  </p>
                </Link>
              ))}
            </div>
          )}
        </section>

        {/* Products */}
        <section className="mt-12 pb-8">
          <h2 className="mb-4 font-display text-xl font-bold sm:text-2xl">
            Produtos
          </h2>
          {filteredProducts.length === 0 ? (
            <p className="rounded-2xl border border-dashed border-line p-8 text-center text-sm text-muted dark:border-dark-line">
              Nenhum produto encontrado.
            </p>
          ) : (
            <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
              {filteredProducts.map((product) => {
                const store = stores.find((s) => s.id === product.storeId)
                return (
                  <article
                    key={product.id}
                    className="overflow-hidden rounded-3xl border border-line bg-paper dark:border-dark-line dark:bg-dark-panel"
                  >
                    <div
                      className="h-28"
                      style={{ backgroundColor: product.accent }}
                    />
                    <div className="p-4">
                      <p className="text-xs text-muted">{store?.name}</p>
                      <h3 className="mt-1 font-semibold">{product.name}</h3>
                      <p className="mt-1 line-clamp-2 text-sm text-muted">
                        {product.description}
                      </p>
                      <div className="mt-4 flex items-center justify-between gap-2">
                        <span className="font-display text-lg font-bold">
                          {formatPrice(product.price)}
                        </span>
                        <button
                          type="button"
                          onClick={() => add(product.id)}
                          className="rounded-full bg-leaf px-3.5 py-2 text-xs font-bold text-lime transition hover:bg-leaf-mid"
                        >
                          Adicionar
                        </button>
                      </div>
                    </div>
                  </article>
                )
              })}
            </div>
          )}
        </section>
      </div>
    </div>
  )
}
