import { useState, useEffect, useCallback } from 'react'
import {
  Box,
  Container,
  Typography,
  Card,
  CardContent,
  Grid,
  Button,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  AppBar,
  Toolbar,
  IconButton,
  Chip,
  CircularProgress,
  TextField,
  InputAdornment,
} from '@mui/material'
import LogoutIcon from '@mui/icons-material/Logout'
import RefreshIcon from '@mui/icons-material/Refresh'
import AdminPanelSettingsIcon from '@mui/icons-material/AdminPanelSettings'
import SearchIcon from '@mui/icons-material/Search'
import TrendingUpIcon from '@mui/icons-material/TrendingUp'
import TrendingDownIcon from '@mui/icons-material/TrendingDown'
import ReceiptIcon from '@mui/icons-material/Receipt'
import { User, TransactionHistory } from '../types'
import { getAllTransactions } from '../services/api'

interface AdminDashboardProps {
  user: User
  onLogout: () => void
}

function AdminDashboard({ user, onLogout }: AdminDashboardProps) {
  const [transactions, setTransactions] = useState<TransactionHistory[]>([])
  const [filteredTransactions, setFilteredTransactions] = useState<TransactionHistory[]>([])
  const [loading, setLoading] = useState(true)
  const [searchTerm, setSearchTerm] = useState('')

  const fetchData = useCallback(async () => {
    setLoading(true)
    const allTx = await getAllTransactions()
    setTransactions(allTx)
    setFilteredTransactions(allTx)
    setLoading(false)
  }, [])

  useEffect(() => {
    fetchData()
  }, [fetchData])

  useEffect(() => {
    if (searchTerm) {
      const filtered = transactions.filter(
        (tx) =>
          tx.cardNumber.includes(searchTerm) ||
          tx.maskedCardNumber.includes(searchTerm) ||
          tx.type.toLowerCase().includes(searchTerm.toLowerCase()) ||
          tx.status.toLowerCase().includes(searchTerm.toLowerCase()) ||
          tx.reason.toLowerCase().includes(searchTerm.toLowerCase())
      )
      setFilteredTransactions(filtered)
    } else {
      setFilteredTransactions(transactions)
    }
  }, [searchTerm, transactions])

  const formatDate = (timestamp: string) => {
    return new Date(timestamp).toLocaleString()
  }

  // Calculate statistics
  const stats = {
    total: transactions.length,
    successful: transactions.filter((tx) => tx.status === 'SUCCESS').length,
    failed: transactions.filter((tx) => tx.status === 'FAILED').length,
    topups: transactions.filter((tx) => tx.type === 'topup' && tx.status === 'SUCCESS').length,
    withdrawals: transactions.filter((tx) => tx.type === 'withdraw' && tx.status === 'SUCCESS').length,
    totalTopupAmount: transactions
      .filter((tx) => tx.type === 'topup' && tx.status === 'SUCCESS')
      .reduce((sum, tx) => sum + tx.amount, 0),
    totalWithdrawalAmount: transactions
      .filter((tx) => tx.type === 'withdraw' && tx.status === 'SUCCESS')
      .reduce((sum, tx) => sum + tx.amount, 0),
  }

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh' }}>
        <CircularProgress />
      </Box>
    )
  }

  return (
    <Box sx={{ flexGrow: 1, minHeight: '100vh', bgcolor: 'background.default' }}>
      {/* App Bar */}
      <AppBar position="static" color="secondary">
        <Toolbar>
          <AdminPanelSettingsIcon sx={{ mr: 2 }} />
          <Typography variant="h6" component="div" sx={{ flexGrow: 1 }}>
            Super Admin Dashboard
          </Typography>
          <Typography variant="body2" sx={{ mr: 2 }}>
            Welcome, {user.username}
          </Typography>
          <IconButton color="inherit" onClick={onLogout} title="Logout">
            <LogoutIcon />
          </IconButton>
        </Toolbar>
      </AppBar>

      <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
        <Grid container spacing={3}>
          {/* Statistics Cards */}
          <Grid item xs={12} sm={6} md={3}>
            <Card sx={{ bgcolor: 'primary.main', color: 'white' }}>
              <CardContent>
                <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                  <Box>
                    <Typography variant="subtitle2" sx={{ opacity: 0.9 }}>
                      Total Transactions
                    </Typography>
                    <Typography variant="h4" fontWeight="bold">
                      {stats.total}
                    </Typography>
                  </Box>
                  <ReceiptIcon sx={{ fontSize: 40, opacity: 0.8 }} />
                </Box>
              </CardContent>
            </Card>
          </Grid>

          <Grid item xs={12} sm={6} md={3}>
            <Card sx={{ bgcolor: 'success.main', color: 'white' }}>
              <CardContent>
                <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                  <Box>
                    <Typography variant="subtitle2" sx={{ opacity: 0.9 }}>
                      Total Top-ups
                    </Typography>
                    <Typography variant="h4" fontWeight="bold">
                      ${stats.totalTopupAmount.toFixed(0)}
                    </Typography>
                    <Typography variant="caption">{stats.topups} transactions</Typography>
                  </Box>
                  <TrendingUpIcon sx={{ fontSize: 40, opacity: 0.8 }} />
                </Box>
              </CardContent>
            </Card>
          </Grid>

          <Grid item xs={12} sm={6} md={3}>
            <Card sx={{ bgcolor: 'warning.main', color: 'white' }}>
              <CardContent>
                <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                  <Box>
                    <Typography variant="subtitle2" sx={{ opacity: 0.9 }}>
                      Total Withdrawals
                    </Typography>
                    <Typography variant="h4" fontWeight="bold">
                      ${stats.totalWithdrawalAmount.toFixed(0)}
                    </Typography>
                    <Typography variant="caption">{stats.withdrawals} transactions</Typography>
                  </Box>
                  <TrendingDownIcon sx={{ fontSize: 40, opacity: 0.8 }} />
                </Box>
              </CardContent>
            </Card>
          </Grid>

          <Grid item xs={12} sm={6} md={3}>
            <Card sx={{ bgcolor: stats.failed > 0 ? 'error.main' : 'success.main', color: 'white' }}>
              <CardContent>
                <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                  <Box>
                    <Typography variant="subtitle2" sx={{ opacity: 0.9 }}>
                      Success Rate
                    </Typography>
                    <Typography variant="h4" fontWeight="bold">
                      {stats.total > 0 ? ((stats.successful / stats.total) * 100).toFixed(0) : 0}%
                    </Typography>
                    <Typography variant="caption">
                      {stats.successful} success / {stats.failed} failed
                    </Typography>
                  </Box>
                </Box>
              </CardContent>
            </Card>
          </Grid>

          {/* Transactions Table */}
          <Grid item xs={12}>
            <Card>
              <CardContent>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                  <Typography variant="h6">All System Transactions</Typography>
                  <Box sx={{ display: 'flex', gap: 2 }}>
                    <TextField
                      size="small"
                      placeholder="Search..."
                      value={searchTerm}
                      onChange={(e) => setSearchTerm(e.target.value)}
                      InputProps={{
                        startAdornment: (
                          <InputAdornment position="start">
                            <SearchIcon />
                          </InputAdornment>
                        ),
                      }}
                    />
                    <Button variant="outlined" startIcon={<RefreshIcon />} onClick={fetchData}>
                      Refresh
                    </Button>
                  </Box>
                </Box>
                <TableContainer component={Paper} variant="outlined">
                  <Table>
                    <TableHead>
                      <TableRow sx={{ bgcolor: 'grey.100' }}>
                        <TableCell><strong>ID</strong></TableCell>
                        <TableCell><strong>Card Number</strong></TableCell>
                        <TableCell><strong>Type</strong></TableCell>
                        <TableCell><strong>Amount</strong></TableCell>
                        <TableCell><strong>Status</strong></TableCell>
                        <TableCell><strong>Date</strong></TableCell>
                        <TableCell><strong>Details</strong></TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {filteredTransactions.length === 0 ? (
                        <TableRow>
                          <TableCell colSpan={7} align="center" sx={{ py: 4 }}>
                            <Typography color="text.secondary">
                              {searchTerm ? 'No matching transactions found' : 'No transactions in the system'}
                            </Typography>
                          </TableCell>
                        </TableRow>
                      ) : (
                        filteredTransactions.map((tx) => (
                          <TableRow key={tx.id} hover>
                            <TableCell>{tx.id}</TableCell>
                            <TableCell>
                              <Typography variant="body2" fontFamily="monospace">
                                {tx.maskedCardNumber}
                              </Typography>
                            </TableCell>
                            <TableCell>
                              <Chip
                                label={tx.type.toUpperCase()}
                                color={tx.type === 'topup' ? 'success' : 'warning'}
                                size="small"
                              />
                            </TableCell>
                            <TableCell>
                              <Typography
                                color={tx.type === 'topup' ? 'success.main' : 'error.main'}
                                fontWeight="medium"
                              >
                                {tx.type === 'topup' ? '+' : '-'}${tx.amount.toFixed(2)}
                              </Typography>
                            </TableCell>
                            <TableCell>
                              <Chip
                                label={tx.status}
                                color={tx.status === 'SUCCESS' ? 'success' : 'error'}
                                size="small"
                                variant="outlined"
                              />
                            </TableCell>
                            <TableCell>{formatDate(tx.timestamp)}</TableCell>
                            <TableCell>{tx.reason}</TableCell>
                          </TableRow>
                        ))
                      )}
                    </TableBody>
                  </Table>
                </TableContainer>
                <Box sx={{ mt: 2, display: 'flex', justifyContent: 'flex-end' }}>
                  <Typography variant="body2" color="text.secondary">
                    Showing {filteredTransactions.length} of {transactions.length} transactions
                  </Typography>
                </Box>
              </CardContent>
            </Card>
          </Grid>
        </Grid>
      </Container>
    </Box>
  )
}

export default AdminDashboard
