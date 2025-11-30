import { useState } from 'react'
import {
  Box,
  Card,
  CardContent,
  TextField,
  Button,
  Typography,
  Alert,
  Container,
  Paper,
} from '@mui/material'
import AccountBalanceIcon from '@mui/icons-material/AccountBalance'
import { User, USERS } from '../types'

interface LoginProps {
  onLogin: (user: User) => void
}

function Login({ onLogin }: LoginProps) {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    setLoading(true)

    // Simulate network delay
    await new Promise((resolve) => setTimeout(resolve, 500))

    // Simple hardcoded authentication for POC
    const userConfig = USERS[username]
    
    if (userConfig && userConfig.password === password) {
      const user: User = {
        username,
        role: userConfig.role,
        cardNumber: userConfig.cardNumber,
      }
      onLogin(user)
    } else {
      setError('Invalid username or password')
    }
    
    setLoading(false)
  }

  return (
    <Box
      sx={{
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        background: 'linear-gradient(135deg, #1976d2 0%, #1565c0 100%)',
      }}
    >
      <Container maxWidth="sm">
        <Paper elevation={10} sx={{ borderRadius: 3, overflow: 'hidden' }}>
          <Box
            sx={{
              bgcolor: 'primary.main',
              color: 'white',
              p: 3,
              textAlign: 'center',
            }}
          >
            <AccountBalanceIcon sx={{ fontSize: 60, mb: 1 }} />
            <Typography variant="h4" component="h1" fontWeight="bold">
              Banking System
            </Typography>
            <Typography variant="body2" sx={{ opacity: 0.9, mt: 1 }}>
              Secure • Reliable • Fast
            </Typography>
          </Box>
          
          <Card sx={{ boxShadow: 'none' }}>
            <CardContent sx={{ p: 4 }}>
              <Typography variant="h6" gutterBottom textAlign="center">
                Sign In to Your Account
              </Typography>
              
              {error && (
                <Alert severity="error" sx={{ mb: 2 }}>
                  {error}
                </Alert>
              )}
              
              <form onSubmit={handleSubmit}>
                <TextField
                  fullWidth
                  label="Username"
                  variant="outlined"
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  margin="normal"
                  required
                  autoComplete="username"
                  autoFocus
                />
                <TextField
                  fullWidth
                  label="Password"
                  type="password"
                  variant="outlined"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  margin="normal"
                  required
                  autoComplete="current-password"
                />
                <Button
                  fullWidth
                  type="submit"
                  variant="contained"
                  size="large"
                  disabled={loading}
                  sx={{ mt: 3, mb: 2, py: 1.5 }}
                >
                  {loading ? 'Signing In...' : 'Sign In'}
                </Button>
              </form>
              
              <Box sx={{ mt: 3, p: 2, bgcolor: 'grey.100', borderRadius: 2 }}>
                <Typography variant="caption" color="text.secondary" display="block" gutterBottom>
                  <strong>Test Credentials:</strong>
                </Typography>
                <Typography variant="caption" color="text.secondary" display="block">
                  Customer 1: <code>cust1 / pass</code> (Card: 4123...2345, PIN: 1234)
                </Typography>
                <Typography variant="caption" color="text.secondary" display="block">
                  Customer 2: <code>cust2 / pass</code> (Card: 4987...8765, PIN: 5678)
                </Typography>
                <Typography variant="caption" color="text.secondary" display="block">
                  Admin: <code>admin / admin</code>
                </Typography>
              </Box>
            </CardContent>
          </Card>
        </Paper>
      </Container>
    </Box>
  )
}

export default Login
