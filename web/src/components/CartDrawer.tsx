import { Minus, Plus, ShoppingBag, Trash2, X } from 'lucide-react'
import { formatPrice } from '../data/mock'
import { useCart } from '../context/CartContext'

export function CartDrawer() {
  const { open, setOpen, items, total, changeQty, remove } = useCart()

  if (!open) return null

  return (
    <div className="fixed inset-0 z-50 flex justify-end">
      <button
        type="button"
        aria-label="Fechar carrinho"
        className="backdrop-enter absolute inset-0 bg-ink/45 dark:bg-black/65"
        onClick={() => setOpen(false)}
      />
      <aside className="drawer-enter relative z-10 flex h-full w-full max-w-md flex-col bg-paper shadow-2xl dark:bg-dark-panel">
        <div className="flex items-center justify-between border-b border-line px-5 py-4 dark:border-dark-line">
          <div className="flex items-center gap-2">
            <ShoppingBag size={18} className="text-leaf dark:text-lime" />
            <h2 className="font-display text-lg font-bold">Carrinho</h2>
          </div>
          <button
            type="button"
            onClick={() => setOpen(false)}
            className="rounded-full p-2 hover:bg-sand dark:hover:bg-dark-elevated"
          >
            <X size={18} />
          </button>
        </div>

        <div className="flex-1 space-y-3 overflow-y-auto p-5">
          {items.length === 0 ? (
            <p className="text-sm text-muted">Seu carrinho está vazio.</p>
          ) : (
            items.map((item) => (
              <div
                key={item.product.id}
                className="flex gap-3 rounded-2xl border border-line p-3 dark:border-dark-line"
              >
                <div
                  className="h-14 w-14 shrink-0 rounded-xl"
                  style={{ backgroundColor: item.product.accent }}
                />
                <div className="min-w-0 flex-1">
                  <div className="flex items-start justify-between gap-2">
                    <p className="truncate text-sm font-semibold">
                      {item.product.name}
                    </p>
                    <button
                      type="button"
                      onClick={() => remove(item.product.id)}
                      className="text-muted hover:text-red-500"
                    >
                      <Trash2 size={15} />
                    </button>
                  </div>
                  <p className="mt-0.5 text-sm text-muted">
                    {formatPrice(item.product.price)}
                  </p>
                  <div className="mt-2 flex items-center gap-2">
                    <button
                      type="button"
                      onClick={() =>
                        changeQty(item.product.id, item.qty - 1)
                      }
                      className="rounded-full bg-sand p-1 dark:bg-dark-elevated"
                    >
                      <Minus size={14} />
                    </button>
                    <span className="w-5 text-center text-sm font-semibold">
                      {item.qty}
                    </span>
                    <button
                      type="button"
                      onClick={() =>
                        changeQty(item.product.id, item.qty + 1)
                      }
                      className="rounded-full bg-lime p-1 text-ink"
                    >
                      <Plus size={14} />
                    </button>
                  </div>
                </div>
              </div>
            ))
          )}
        </div>

        <div className="border-t border-line p-5 dark:border-dark-line">
          <div className="mb-4 flex items-center justify-between">
            <span className="text-sm text-muted">Total</span>
            <span className="font-display text-xl font-bold">
              {formatPrice(total)}
            </span>
          </div>
          <button
            type="button"
            className="w-full rounded-full bg-leaf py-3 text-sm font-bold text-lime transition hover:bg-leaf-mid"
          >
            Finalizar compra
          </button>
        </div>
      </aside>
    </div>
  )
}
