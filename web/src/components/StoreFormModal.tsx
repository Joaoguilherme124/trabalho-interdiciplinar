import { ImagePlus } from 'lucide-react'
import { categories } from '../data/mock'
import { Modal } from './Modal'

type Props = {
  open: boolean
  onClose: () => void
}

const fieldClass =
  'w-full rounded-xl border border-line bg-mist px-3.5 py-2.5 text-sm outline-none transition focus:border-leaf-mid dark:border-dark-line dark:bg-dark-elevated dark:text-mist'

export function StoreFormModal({ open, onClose }: Props) {
  return (
    <Modal open={open} onClose={onClose} title="Cadastrar loja parceira" wide>
      <form
        className="grid gap-4 sm:grid-cols-2"
        onSubmit={(e) => {
          e.preventDefault()
          onClose()
        }}
      >
        <label className="block sm:col-span-2">
          <span className="mb-1.5 block text-xs font-semibold uppercase tracking-wide text-muted">
            Nome da loja
          </span>
          <input className={fieldClass} placeholder="Ex: Padaria do Sol" />
        </label>

        <label className="block">
          <span className="mb-1.5 block text-xs font-semibold uppercase tracking-wide text-muted">
            CNPJ
          </span>
          <input className={fieldClass} placeholder="00.000.000/0000-00" />
        </label>

        <label className="block">
          <span className="mb-1.5 block text-xs font-semibold uppercase tracking-wide text-muted">
            Categoria
          </span>
          <select className={fieldClass} defaultValue="">
            <option value="" disabled>
              Selecione
            </option>
            {categories
              .filter((c) => c !== 'Todos')
              .map((c) => (
                <option key={c} value={c}>
                  {c}
                </option>
              ))}
          </select>
        </label>

        <label className="block">
          <span className="mb-1.5 block text-xs font-semibold uppercase tracking-wide text-muted">
            E-mail
          </span>
          <input
            type="email"
            className={fieldClass}
            placeholder="contato@loja.com"
          />
        </label>

        <label className="block">
          <span className="mb-1.5 block text-xs font-semibold uppercase tracking-wide text-muted">
            Telefone
          </span>
          <input className={fieldClass} placeholder="(00) 00000-0000" />
        </label>

        <div className="sm:col-span-2">
          <span className="mb-1.5 block text-xs font-semibold uppercase tracking-wide text-muted">
            Logo / Avatar
          </span>
          <button
            type="button"
            className="flex w-full items-center justify-center gap-2 rounded-xl border border-dashed border-line bg-mist px-4 py-8 text-sm text-muted transition hover:border-leaf-mid hover:text-leaf dark:border-dark-line dark:bg-dark-elevated dark:hover:text-lime"
          >
            <ImagePlus size={18} />
            Enviar imagem da loja
          </button>
        </div>

        <label className="block sm:col-span-2">
          <span className="mb-1.5 block text-xs font-semibold uppercase tracking-wide text-muted">
            Endereço
          </span>
          <input
            className={fieldClass}
            placeholder="Rua, número — Cidade, UF"
          />
        </label>

        <label className="block sm:col-span-2">
          <span className="mb-1.5 block text-xs font-semibold uppercase tracking-wide text-muted">
            Descrição
          </span>
          <textarea
            rows={3}
            className={fieldClass}
            placeholder="Conte um pouco sobre a loja..."
          />
        </label>

        <div className="flex justify-end gap-3 sm:col-span-2">
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
            Salvar loja
          </button>
        </div>
      </form>
    </Modal>
  )
}
