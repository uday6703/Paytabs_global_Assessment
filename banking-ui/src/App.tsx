import { Routes, Route, Navigate } from 'react-router-dom'
import { useState, useEffect } from 'react'
import Login from './components/Login'
import CustomerDashboard from './pages/CustomerDashboard'
import AdminDashboard from './pages/AdminDashboard'
import { User } from './types'

function App() {
  const [user, setUser] = useState<User | null>(null)

  // Check for stored user on mount
  useEffect(() => {
    const storedUser = localStorage.getItem('bankingUser')
    if (storedUser) {
      setUser(JSON.parse(storedUser))
    }
  }, [])

  const handleLogin = (loggedInUser: User) => {
    setUser(loggedInUser)
    localStorage.setItem('bankingUser', JSON.stringify(loggedInUser))
  }

  const handleLogout = () => {
    setUser(null)
    localStorage.removeItem('bankingUser')
  }

  return (
    <Routes>
      <Route 
        path="/login" 
        element={
          user ? (
            <Navigate to={user.role === 'ADMIN' ? '/admin/dashboard' : '/customer/dashboard'} />
          ) : (
            <Login onLogin={handleLogin} />
          )
        } 
      />
      <Route 
        path="/customer/dashboard" 
        element={
          user && user.role === 'CUSTOMER' ? (
            <CustomerDashboard user={user} onLogout={handleLogout} />
          ) : (
            <Navigate to="/login" />
          )
        } 
      />
      <Route 
        path="/admin/dashboard" 
        element={
          user && user.role === 'ADMIN' ? (
            <AdminDashboard user={user} onLogout={handleLogout} />
          ) : (
            <Navigate to="/login" />
          )
        } 
      />
      <Route path="/" element={<Navigate to="/login" />} />
      <Route path="*" element={<Navigate to="/login" />} />
    </Routes>
  )
}

export default App
