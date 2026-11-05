export type Store = {
  id: string
  name: string
  category: string
  cnpj: string
  email: string
  phone: string
  address: string
  description: string
  initial: string
  color: string
  rating: number
  featured?: boolean
}

export type Product = {
  id: string
  storeId: string
  name: string
  category: string
  price: number
  description: string
  active: boolean
  accent: string
}

export const categories = [
  'Todos',
  'Alimentação',
  'Moda',
  'Tecnologia',
  'Casa',
  'Beleza',
  'Esportes',
]

export const stores: Store[] = [
  {
    id: '1',
    name: 'Padaria do Sol',
    category: 'Alimentação',
    cnpj: '12.345.678/0001-90',
    email: 'contato@padariadosol.com',
    phone: '(48) 99999-1001',
    address: 'Rua das Flores, 120 — Criciúma, SC',
    description: 'Pães artesanais, doces e café fresco todos os dias.',
    initial: 'PS',
    color: '#E8A317',
    rating: 4.8,
    featured: true,
  },
  {
    id: '2',
    name: 'Tech Norte',
    category: 'Tecnologia',
    cnpj: '23.456.789/0001-01',
    email: 'ola@technorte.com',
    phone: '(48) 98888-2002',
    address: 'Av. Centenário, 450 — Criciúma, SC',
    description: 'Gadgets, acessórios e suporte especializado.',
    initial: 'TN',
    color: '#2B6CB0',
    rating: 4.6,
    featured: true,
  },
  {
    id: '3',
    name: 'Moda Atlântica',
    category: 'Moda',
    cnpj: '34.567.890/0001-12',
    email: 'oi@modaatlantica.com',
    phone: '(48) 97777-3003',
    address: 'Rua Anita Garibaldi, 88 — Criciúma, SC',
    description: 'Roupas casuais com identidade sulista.',
    initial: 'MA',
    color: '#C05621',
    rating: 4.7,
    featured: true,
  },
  {
    id: '4',
    name: 'Casa & Lar',
    category: 'Casa',
    cnpj: '45.678.901/0001-23',
    email: 'hello@casaelar.com',
    phone: '(48) 96666-4004',
    address: 'Rua Porto Alegre, 210 — Criciúma, SC',
    description: 'Decoração, utensílios e conforto para o lar.',
    initial: 'CL',
    color: '#2F855A',
    rating: 4.5,
  },
  {
    id: '5',
    name: 'Beleza Pura',
    category: 'Beleza',
    cnpj: '56.789.012/0001-34',
    email: 'contato@belezapura.com',
    phone: '(48) 95555-5005',
    address: 'Rua João Pessoa, 33 — Criciúma, SC',
    description: 'Cosméticos naturais e cuidados pessoais.',
    initial: 'BP',
    color: '#B83280',
    rating: 4.9,
  },
  {
    id: '6',
    name: 'Esporte Sul',
    category: 'Esportes',
    cnpj: '67.890.123/0001-45',
    email: 'time@esportesul.com',
    phone: '(48) 94444-6006',
    address: 'Av. Universitária, 900 — Criciúma, SC',
    description: 'Equipamentos e vestuário para treinar melhor.',
    initial: 'ES',
    color: '#2C7A7B',
    rating: 4.4,
  },
]

export const products: Product[] = [
  {
    id: 'p1',
    storeId: '1',
    name: 'Pão Sourdough',
    category: 'Alimentação',
    price: 18.9,
    description: 'Fermentação natural, crosta crocante.',
    active: true,
    accent: '#F6E05E',
  },
  {
    id: 'p2',
    storeId: '1',
    name: 'Café da Manhã Completo',
    category: 'Alimentação',
    price: 32.0,
    description: 'Pão, frios, suco e café.',
    active: true,
    accent: '#D69E2E',
  },
  {
    id: 'p3',
    storeId: '2',
    name: 'Fone Bluetooth Pro',
    category: 'Tecnologia',
    price: 249.9,
    description: 'Cancelamento de ruído e 30h de bateria.',
    active: true,
    accent: '#63B3ED',
  },
  {
    id: 'p4',
    storeId: '2',
    name: 'Carregador GaN 65W',
    category: 'Tecnologia',
    price: 129.0,
    description: 'Compacto e rápido para notebook e celular.',
    active: true,
    accent: '#4299E1',
  },
  {
    id: 'p5',
    storeId: '3',
    name: 'Camiseta Atlântica',
    category: 'Moda',
    price: 79.9,
    description: 'Algodão orgânico, corte relaxed.',
    active: true,
    accent: '#ED8936',
  },
  {
    id: 'p6',
    storeId: '3',
    name: 'Jaqueta Vento',
    category: 'Moda',
    price: 189.0,
    description: 'Leve, impermeável e dobrável.',
    active: false,
    accent: '#DD6B20',
  },
  {
    id: 'p7',
    storeId: '4',
    name: 'Vaso Cerâmica M',
    category: 'Casa',
    price: 64.5,
    description: 'Feito à mão, acabamento fosco.',
    active: true,
    accent: '#68D391',
  },
  {
    id: 'p8',
    storeId: '5',
    name: 'Kit Skincare Natural',
    category: 'Beleza',
    price: 119.0,
    description: 'Limpeza, tônico e hidratante.',
    active: true,
    accent: '#F687B3',
  },
  {
    id: 'p9',
    storeId: '6',
    name: 'Tênis Trail',
    category: 'Esportes',
    price: 299.0,
    description: 'Solado grip para trilha e rua.',
    active: true,
    accent: '#4FD1C5',
  },
]

export const dashboardMetrics = {
  sales: 'R$ 12.480',
  orders: 86,
  activeProducts: 42,
  rating: 4.7,
}

export function formatPrice(value: number) {
  return value.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })
}
