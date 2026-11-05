import { LayoutDashboard, Moon, ShoppingBag, Store, Sun } from 'lucide-react'
import { Link, NavLink } from 'react-router-dom'
import { useCart } from '../context/CartContext'
import { useTheme } from '../context/ThemeContext'

export function Header() {
  const { theme, toggleTheme } = useTheme()
  const { count, setOpen } = useCart()

  const linkClass = ({ isActive }: { isActive: boolean }) =>
    `rounded-full px-3 py-1.5 text-sm font-semibold transition ${
      isActive
        ? 'bg-leaf text-lime'
        : 'text-ink-soft hover:bg-sand dark:text-mist dark:hover:bg-dark-elevated'
    }`

  return (
    <header className="sticky top-0 z-40 border-b border-line/80 bg-mist/85 backdrop-blur-md dark:border-dark-line dark:bg-dark-bg/85">
      <div className="mx-auto flex max-w-6xl items-center justify-between gap-4 px-4 py-3 sm:px-6">
        <Link to="/" className="group flex items-baseline gap-1">
          <span className="font-display text-2xl font-extrabold tracking-tight text-leaf dark:text-lime">
            Vitrine
          </span>
          <span className="hidden text-xs font-medium text-muted sm:inline">
            marketplace
          </span>
        </Link>

        <nav className="hidden items-center gap-1 md:flex">
          <NavLink to="/" end className={linkClass}>
            <span className="inline-flex items-center gap-1.5">
              <Store size={15} />
              Vitrine
            </span>
          </NavLink>
          <NavLink to="/dashboard" className={linkClass}>
            <span className="inline-flex items-center gap-1.5">
              <LayoutDashboard size={15} />
              Lojista
            </span>
          </NavLink>
        </nav>

        <div className="flex items-center gap-2">
          <button
            type="button"
            onClick={toggleTheme}
            className="rounded-full p-2.5 text-ink-soft transition hover:bg-sand dark:text-mist dark:hover:bg-dark-elevated"
            aria-label="Alternar tema"
          >
            {theme === 'light' ? <Moon size={18} /> : <Sun size={18} />}
          </button>

          <button
            type="button"
            onClick={() => setOpen(true)}
            className="relative rounded-full bg-leaf p-2.5 text-lime transition hover:bg-leaf-mid"
            aria-label="Abrir carrinho"
          >
            <ShoppingBag size={18} />
            {count > 0 && (
              <span className="absolute -right-1 -top-1 flex h-5 min-w-5 items-center justify-center rounded-full bg-lime px-1 text-[11px] font-bold text-ink">
                {count}
              </span>
            )}
          </button>
        </div>
      </div>

      <nav className="flex gap-2 overflow-x-auto border-t border-line px-4 py-2 md:hidden dark:border-dark-line">
        <NavLink to="/" end className={linkClass}>
          Vitrine
        </NavLink>
        <NavLink to="/dashboard" className={linkClass}>
          Lojista
        </NavLink>
      </nav>
    </header>
  )
}
