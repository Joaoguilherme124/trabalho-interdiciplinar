import { ArrowLeft, MapPin, Star } from 'lucide-react'
import { Link, useParams } from 'react-router-dom'
import { useCart } from '../context/CartContext'
import { formatPrice, products, stores } from '../data/mock'

export function StoreDetail() {
  const { id } = useParams()
  const { add } = useCart()
  const store = stores.find((s) => s.id === id)
  const catalog = products.filter((p) => p.storeId === id)

  if (!store) {
    return (
      <div className="page-enter mx-auto max-w-6xl px-4 py-16 text-center">
        <p className="text-muted">Loja não encontrada.</p>
        <Link to="/" className="mt-4 inline-block text-sm font-semibold text-leaf dark:text-lime">
          Voltar à vitrine
        </Link>
      </div>
    )
  }

  return (
    <div className="page-enter">
      <section
        className="relative overflow-hidden"
        style={{
          background: `linear-gradient(135deg, ${store.color} 0%, #0f1412 100%)`,
        }}
      >
        <div className="mx-auto max-w-6xl px-4 py-8 sm:px-6 sm:py-12">
          <Link
            to="/"
            className="inline-flex items-center gap-2 text-sm font-semibold text-white/80 transition hover:text-white"
          >
            <ArrowLeft size={16} />
            Voltar
          </Link>

          <div className="mt-8 flex flex-col gap-5 sm:flex-row sm:items-end sm:justify-between">
            <div className="flex items-start gap-4">
              <div
                className="flex h-16 w-16 items-center justify-center rounded-2xl text-lg font-bold text-white ring-4 ring-white/20"
                style={{ backgroundColor: store.color }}
              >
                {store.initial}
              </div>
              <div>
                <p className="text-xs font-semibold uppercase tracking-wider text-white/70">
                  {store.category}
                </p>
                <h1 className="font-display text-3xl font-extrabold text-white sm:text-4xl">
                  {store.name}
                </h1>
                <p className="mt-2 max-w-xl text-sm text-white/80">
                  {store.description}
                </p>
              </div>
            </div>

            <div className="flex items-center gap-4 text-sm text-white/90">
              <span className="inline-flex items-center gap-1.5 rounded-full bg-white/15 px-3 py-1.5 backdrop-blur">
                <Star size={14} className="fill-lime text-lime" />
                {store.rating.toFixed(1)}
              </span>
            </div>
          </div>

          <p className="mt-5 inline-flex items-center gap-2 text-sm text-white/75">
            <MapPin size={15} />
            {store.address}
          </p>
        </div>
      </section>

      <div className="mx-auto max-w-6xl px-4 py-8 sm:px-6">
        <h2 className="font-display text-xl font-bold sm:text-2xl">
          Catálogo da loja
        </h2>
        <p className="mt-1 text-sm text-muted">
          {catalog.length} produto{catalog.length !== 1 ? 's' : ''}
        </p>

        <div className="mt-6 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {catalog.map((product) => (
            <article
              key={product.id}
              className="overflow-hidden rounded-3xl border border-line bg-paper dark:border-dark-line dark:bg-dark-panel"
            >
              <div
                className="h-28"
                style={{ backgroundColor: product.accent }}
              />
              <div className="p-4">
                <div className="flex items-start justify-between gap-2">
                  <h3 className="font-semibold">{product.name}</h3>
                  {!product.active && (
                    <span className="rounded-full bg-sand px-2 py-0.5 text-[10px] font-bold uppercase text-muted dark:bg-dark-elevated">
                      Inativo
                    </span>
                  )}
                </div>
                <p className="mt-1 text-sm text-muted">{product.description}</p>
                <div className="mt-4 flex items-center justify-between">
                  <span className="font-display text-lg font-bold">
                    {formatPrice(product.price)}
                  </span>
                  <button
                    type="button"
                    disabled={!product.active}
                    onClick={() => add(product.id)}
                    className="rounded-full bg-leaf px-3.5 py-2 text-xs font-bold text-lime transition hover:bg-leaf-mid disabled:cursor-not-allowed disabled:opacity-40"
                  >
                    Adicionar
                  </button>
                </div>
              </div>
            </article>
          ))}
        </div>
      </div>
    </div>
  )
}
