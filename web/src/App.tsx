import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import { CartDrawer } from './components/CartDrawer'
import { Header } from './components/Header'
import { CartProvider } from './context/CartContext'
import { ThemeProvider } from './context/ThemeContext'
import { Dashboard } from './pages/Dashboard'
import { Home } from './pages/Home'
import { StoreDetail } from './pages/StoreDetail'

export default function App() {
  return (
    <ThemeProvider>
      <CartProvider>
        <BrowserRouter>
          <div className="min-h-screen">
            <Header />
            <main>
              <Routes>
                <Route path="/" element={<Home />} />
                <Route path="/loja/:id" element={<StoreDetail />} />
                <Route path="/dashboard" element={<Dashboard />} />
                <Route path="*" element={<Navigate to="/" replace />} />
              </Routes>
            </main>
            <CartDrawer />
          </div>
        </BrowserRouter>
      </CartProvider>
    </ThemeProvider>
  )
}
