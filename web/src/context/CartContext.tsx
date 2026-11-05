import {
  createContext,
  useContext,
  useMemo,
  useState,
  type ReactNode,
} from 'react'
import { products, type Product } from '../data/mock'

export type CartItem = {
  product: Product
  qty: number
}

type CartContextValue = {
  items: CartItem[]
  open: boolean
  setOpen: (open: boolean) => void
  count: number
  total: number
  add: (productId: string) => void
  remove: (productId: string) => void
  changeQty: (productId: string, qty: number) => void
}

const CartContext = createContext<CartContextValue | null>(null)

export function CartProvider({ children }: { children: ReactNode }) {
  const [items, setItems] = useState<CartItem[]>([
    { product: products[0], qty: 1 },
    { product: products[2], qty: 1 },
  ])
  const [open, setOpen] = useState(false)

  const add = (productId: string) => {
    const product = products.find((p) => p.id === productId)
    if (!product) return
    setItems((prev) => {
      const existing = prev.find((i) => i.product.id === productId)
      if (existing) {
        return prev.map((i) =>
          i.product.id === productId ? { ...i, qty: i.qty + 1 } : i,
        )
      }
      return [...prev, { product, qty: 1 }]
    })
    setOpen(true)
  }

  const remove = (productId: string) => {
    setItems((prev) => prev.filter((i) => i.product.id !== productId))
  }

  const changeQty = (productId: string, qty: number) => {
    if (qty <= 0) {
      remove(productId)
      return
    }
    setItems((prev) =>
      prev.map((i) => (i.product.id === productId ? { ...i, qty } : i)),
    )
  }

  const count = useMemo(
    () => items.reduce((sum, item) => sum + item.qty, 0),
    [items],
  )

  const total = useMemo(
    () => items.reduce((sum, item) => sum + item.product.price * item.qty, 0),
    [items],
  )

  return (
    <CartContext.Provider
      value={{ items, open, setOpen, count, total, add, remove, changeQty }}
    >
      {children}
    </CartContext.Provider>
  )
}

export function useCart() {
  const ctx = useContext(CartContext)
  if (!ctx) throw new Error('useCart must be used within CartProvider')
  return ctx
}
