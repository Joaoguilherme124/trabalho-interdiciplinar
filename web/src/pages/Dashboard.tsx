import {
  Package,
  Plus,
  Search,
  ShoppingBag,
  Star,
  Store,
  Trash2,
  TrendingUp,
} from 'lucide-react'
import { useMemo, useState } from 'react'
import { ProductFormModal } from '../components/ProductFormModal'
import { StoreFormModal } from '../components/StoreFormModal'
import {
  dashboardMetrics,
  formatPrice,
  products as initialProducts,
  stores,
  type Product,
} from '../data/mock'

export function Dashboard() {
  const [storeModal, setStoreModal] = useState(false)
  const [productModal, setProductModal] = useState(false)
  const [filter, setFilter] = useState('')
  const [storeFilter, setStoreFilter] = useState('todas')
  const [rows, setRows] = useState<Product[]>(initialProducts)

  const filtered = useMemo(() => {
    const q = filter.trim().toLowerCase()
    return rows.filter((p) => {
      const matchStore =
        storeFilter === 'todas' || p.storeId === storeFilter
      const matchQuery =
        !q ||
        p.name.toLowerCase().includes(q) ||
        p.category.toLowerCase().includes(q)
      return matchStore && matchQuery
    })
  }, [rows, filter, storeFilter])

  const metrics = [
    {
      label: 'Vendas',
      value: dashboardMetrics.sales,
      icon: TrendingUp,
      hint: 'este mês',
    },
    {
      label: 'Pedidos recebidos',
      value: String(dashboardMetrics.orders),
      icon: ShoppingBag,
      hint: 'últimos 30 dias',
    },
    {
      label: 'Produtos ativos',
      value: String(dashboardMetrics.activeProducts),
      icon: Package,
      hint: 'no catálogo',
    },
    {
      label: 'Avaliação',
      value: dashboardMetrics.rating.toFixed(1),
      icon: Star,
      hint: 'média geral',
    },
  ]

  return (
    <div className="page-enter mx-auto max-w-6xl px-4 py-8 sm:px-6">
      <div className="flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <p className="text-xs font-semibold uppercase tracking-wider text-muted">
            Portal do lojista
          </p>
          <h1 className="mt-1 font-display text-3xl font-extrabold">
            Dashboard
          </h1>
        </div>
        <div className="flex flex-wrap gap-2">
          <button
            type="button"
            onClick={() => setStoreModal(true)}
            className="inline-flex items-center gap-2 rounded-full border border-line bg-paper px-4 py-2.5 text-sm font-semibold transition hover:border-leaf-mid dark:border-dark-line dark:bg-dark-panel"
          >
            <Store size={16} />
            Nova loja
          </button>
          <button
            type="button"
            onClick={() => setProductModal(true)}
            className="inline-flex items-center gap-2 rounded-full bg-leaf px-4 py-2.5 text-sm font-semibold text-lime transition hover:bg-leaf-mid"
          >
            <Plus size={16} />
            Novo produto
          </button>
        </div>
      </div>

      {/* Metrics */}
      <div className="mt-8 grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
        {metrics.map((m) => (
          <div
            key={m.label}
            className="rounded-3xl border border-line bg-paper p-5 dark:border-dark-line dark:bg-dark-panel"
          >
            <div className="flex items-center justify-between">
              <p className="text-xs font-semibold uppercase tracking-wide text-muted">
                {m.label}
              </p>
              <m.icon size={16} className="text-leaf dark:text-lime" />
            </div>
            <p className="mt-3 font-display text-2xl font-bold">{m.value}</p>
            <p className="mt-1 text-xs text-muted">{m.hint}</p>
          </div>
        ))}
      </div>

      {/* Products table */}
      <section className="mt-10">
        <div className="mb-4 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
          <h2 className="font-display text-xl font-bold">Produtos por loja</h2>
          <div className="flex flex-col gap-2 sm:flex-row">
            <div className="relative">
              <Search
                size={15}
                className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-muted"
              />
              <input
                value={filter}
                onChange={(e) => setFilter(e.target.value)}
                placeholder="Filtrar produtos..."
                className="w-full rounded-xl border border-line bg-paper py-2 pl-9 pr-3 text-sm outline-none focus:border-leaf-mid dark:border-dark-line dark:bg-dark-panel sm:w-52"
              />
            </div>
            <select
              value={storeFilter}
              onChange={(e) => setStoreFilter(e.target.value)}
              className="rounded-xl border border-line bg-paper px-3 py-2 text-sm outline-none focus:border-leaf-mid dark:border-dark-line dark:bg-dark-panel"
            >
              <option value="todas">Todas as lojas</option>
              {stores.map((s) => (
                <option key={s.id} value={s.id}>
                  {s.name}
                </option>
              ))}
            </select>
          </div>
        </div>

        <div className="overflow-hidden rounded-3xl border border-line dark:border-dark-line">
          <div className="overflow-x-auto">
            <table className="min-w-full text-left text-sm">
              <thead className="bg-sand/70 text-xs uppercase tracking-wide text-muted dark:bg-dark-elevated">
                <tr>
                  <th className="px-4 py-3 font-semibold">Produto</th>
                  <th className="px-4 py-3 font-semibold">Loja</th>
                  <th className="px-4 py-3 font-semibold">Categoria</th>
                  <th className="px-4 py-3 font-semibold">Preço</th>
                  <th className="px-4 py-3 font-semibold">Status</th>
                  <th className="px-4 py-3 font-semibold" />
                </tr>
              </thead>
              <tbody className="bg-paper dark:bg-dark-panel">
                {filtered.map((product) => {
                  const store = stores.find((s) => s.id === product.storeId)
                  return (
                    <tr
                      key={product.id}
                      className="border-t border-line dark:border-dark-line"
                    >
                      <td className="px-4 py-3 font-medium">{product.name}</td>
                      <td className="px-4 py-3 text-muted">{store?.name}</td>
                      <td className="px-4 py-3 text-muted">
                        {product.category}
                      </td>
                      <td className="px-4 py-3">
                        {formatPrice(product.price)}
                      </td>
                      <td className="px-4 py-3">
                        <span
                          className={`inline-flex rounded-full px-2.5 py-0.5 text-xs font-semibold ${
                            product.active
                              ? 'bg-lime/40 text-leaf dark:bg-lime/20 dark:text-lime'
                              : 'bg-sand text-muted dark:bg-dark-elevated'
                          }`}
                        >
                          {product.active ? 'Ativo' : 'Inativo'}
                        </span>
                      </td>
                      <td className="px-4 py-3 text-right">
                        <button
                          type="button"
                          onClick={() =>
                            setRows((prev) =>
                              prev.filter((p) => p.id !== product.id),
                            )
                          }
                          className="rounded-full p-2 text-muted transition hover:bg-red-50 hover:text-red-500 dark:hover:bg-red-950/40"
                          aria-label="Remover produto"
                        >
                          <Trash2 size={15} />
                        </button>
                      </td>
                    </tr>
                  )
                })}
                {filtered.length === 0 && (
                  <tr>
                    <td
                      colSpan={6}
                      className="px-4 py-10 text-center text-muted"
                    >
                      Nenhum produto neste filtro.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </div>
      </section>

      <StoreFormModal open={storeModal} onClose={() => setStoreModal(false)} />
      <ProductFormModal
        open={productModal}
        onClose={() => setProductModal(false)}
      />
    </div>
  )
}
