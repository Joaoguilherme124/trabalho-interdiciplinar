import { stores } from '../data/mock'
import { Modal } from './Modal'

type Props = {
  open: boolean
  onClose: () => void
}

const fieldClass =
  'w-full rounded-xl border border-line bg-mist px-3.5 py-2.5 text-sm outline-none transition focus:border-leaf-mid dark:border-dark-line dark:bg-dark-elevated dark:text-mist'

export function ProductFormModal({ open, onClose }: Props) {
  return (
    <Modal open={open} onClose={onClose} title="Cadastrar produto">
      <form
        className="grid gap-4"
        onSubmit={(e) => {
          e.preventDefault()
          onClose()
        }}
      >
        <label className="block">
          <span className="mb-1.5 block text-xs font-semibold uppercase tracking-wide text-muted">
            Loja parceira
          </span>
          <select className={fieldClass} defaultValue="">
            <option value="" disabled>
              Associar a uma loja
            </option>
            {stores.map((store) => (
              <option key={store.id} value={store.id}>
                {store.name}
              </option>
            ))}
          </select>
        </label>

        <label className="block">
          <span className="mb-1.5 block text-xs font-semibold uppercase tracking-wide text-muted">
            Nome do produto
          </span>
          <input className={fieldClass} placeholder="Ex: Fone Bluetooth Pro" />
        </label>

        <div className="grid gap-4 sm:grid-cols-2">
          <label className="block">
            <span className="mb-1.5 block text-xs font-semibold uppercase tracking-wide text-muted">
              Preço (R$)
            </span>
            <input
              type="number"
              step="0.01"
              className={fieldClass}
              placeholder="0,00"
            />
          </label>

          <label className="block">
            <span className="mb-1.5 block text-xs font-semibold uppercase tracking-wide text-muted">
              Categoria
            </span>
            <input className={fieldClass} placeholder="Ex: Tecnologia" />
          </label>
        </div>

        <label className="block">
          <span className="mb-1.5 block text-xs font-semibold uppercase tracking-wide text-muted">
            Descrição
          </span>
          <textarea
            rows={3}
            className={fieldClass}
            placeholder="Detalhes do produto..."
          />
        </label>

        <div className="flex justify-end gap-3">
          <button
            type="button"
            onClick={onClose}
            className="rounded-full px-5 py-2.5 text-sm font-semibold text-muted hover:bg-sand dark:hover:bg-dark-elevated"
          >
            Cancelar
          </button>
          <button
            type="submit"
            className="rounded-full bg-leaf px-5 py-2.5 text-sm font-semibold text-lime transition hover:bg-leaf-mid"
          >
            Salvar produto
          </button>
        </div>
      </form>
    </Modal>
  )
}
