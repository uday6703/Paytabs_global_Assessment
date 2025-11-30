import { useState, useEffect, useCallback } from 'react'
import {
  Box,
  Container,
  Typography,
  Card,
  CardContent,
  Grid,
  Button,
  TextField,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Alert,
  Snackbar,
  AppBar,
  Toolbar,
  IconButton,
  Chip,
  CircularProgress,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
} from '@mui/material'
import LogoutIcon from '@mui/icons-material/Logout'
import RefreshIcon from '@mui/icons-material/Refresh'
import AccountBalanceWalletIcon from '@mui/icons-material/AccountBalanceWallet'
import AddIcon from '@mui/icons-material/Add'
import HistoryIcon from '@mui/icons-material/History'
import CreditCardIcon from '@mui/icons-material/CreditCard'
import { User, CardInfo, TransactionHistory } from '../types'
import { getCardByUsername, getTransactionHistory, processTransaction } from '../services/api'

interface CustomerDashboardProps {
  user: User
  onLogout: () => void
}

function CustomerDashboard({ user, onLogout }: CustomerDashboardProps) {
  const [cardInfo, setCardInfo] = useState<CardInfo | null>(null)
  const [transactions, setTransactions] = useState<TransactionHistory[]>([])
  const [loading, setLoading] = useState(true)
  const [topUpDialogOpen, setTopUpDialogOpen] = useState(false)
  const [amount, setAmount] = useState('')
  const [pin, setPin] = useState('')
  const [transactionType, setTransactionType] = useState<'topup' | 'withdraw'>('topup')
  const [processing, setProcessing] = useState(false)
  const [snackbar, setSnackbar] = useState<{ open: boolean; message: string; severity: 'success' | 'error' }>({
    open: false,
    message: '',
    severity: 'success',
  })

  const fetchData = useCallback(async () => {
    setLoading(true)
    const card = await getCardByUsername(user.username)
    if (card) {
      setCardInfo(card)
      const history = await getTransactionHistory(card.cardNumber)
      setTransactions(history)
    }
    setLoading(false)
  }, [user.username])

  useEffect(() => {
    fetchData()
  }, [fetchData])

  const handleTransaction = async () => {
    if (!cardInfo || !amount || !pin) return

    setProcessing(true)
    const response = await processTransaction({
      cardNumber: cardInfo.cardNumber,
      pin,
      amount: parseFloat(amount),
      type: transactionType,
    })

    if (response.success) {
      setSnackbar({
        open: true,
        message: `${transactionType === 'topup' ? 'Top-up' : 'Withdrawal'} successful! New balance: $${response.newBalance?.toFixed(2)}`,
        severity: 'success',
      })
      setTopUpDialogOpen(false)
      setAmount('')
      setPin('')
      fetchData() // Refresh data
    } else {
      setSnackbar({
        open: true,
        message: response.message,
        severity: 'error',
      })
    }
    setProcessing(false)
  }

  const formatDate = (timestamp: string) => {
    return new Date(timestamp).toLocaleString()
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
      <AppBar position="static">
        <Toolbar>
          <CreditCardIcon sx={{ mr: 2 }} />
          <Typography variant="h6" component="div" sx={{ flexGrow: 1 }}>
            Customer Dashboard
          </Typography>
          <Typography variant="body2" sx={{ mr: 2 }}>
            Welcome, {cardInfo?.customerName || user.username}
          </Typography>
          <IconButton color="inherit" onClick={onLogout} title="Logout">
            <LogoutIcon />
          </IconButton>
        </Toolbar>
      </AppBar>

      <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
        <Grid container spacing={3}>
          {/* Balance Card */}
          <Grid item xs={12} md={6}>
            <Card sx={{ height: '100%', background: 'linear-gradient(135deg, #1976d2 0%, #1565c0 100%)', color: 'white' }}>
              <CardContent sx={{ p: 3 }}>
                <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                  <AccountBalanceWalletIcon sx={{ fontSize: 40, mr: 2 }} />
                  <Typography variant="h6">Account Balance</Typography>
                </Box>
                <Typography variant="h3" fontWeight="bold" sx={{ mb: 2 }}>
                  ${cardInfo?.balance.toFixed(2) || '0.00'}
                </Typography>
                <Typography variant="body2" sx={{ opacity: 0.9 }}>
                  Card: {cardInfo?.maskedCardNumber}
                </Typography>
              </CardContent>
            </Card>
          </Grid>

          {/* Quick Actions Card */}
          <Grid item xs={12} md={6}>
            <Card sx={{ height: '100%' }}>
              <CardContent sx={{ p: 3 }}>
                <Typography variant="h6" gutterBottom>
                  Quick Actions
                </Typography>
                <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap', mt: 2 }}>
                  <Button
                    variant="contained"
                    startIcon={<AddIcon />}
                    onClick={() => {
                      setTransactionType('topup')
                      setTopUpDialogOpen(true)
                    }}
                    size="large"
                  >
                    Top-Up
                  </Button>
                  <Button
                    variant="outlined"
                    color="secondary"
                    onClick={() => {
                      setTransactionType('withdraw')
                      setTopUpDialogOpen(true)
                    }}
                    size="large"
                  >
                    Withdraw
                  </Button>
                  <Button
                    variant="outlined"
                    startIcon={<RefreshIcon />}
                    onClick={fetchData}
                    size="large"
                  >
                    Refresh
                  </Button>
                </Box>
              </CardContent>
            </Card>
          </Grid>

          {/* Transaction History */}
          <Grid item xs={12}>
            <Card>
              <CardContent>
                <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                  <HistoryIcon sx={{ mr: 1 }} />
                  <Typography variant="h6">Transaction History</Typography>
                </Box>
                <TableContainer component={Paper} variant="outlined">
                  <Table>
                    <TableHead>
                      <TableRow sx={{ bgcolor: 'grey.100' }}>
                        <TableCell><strong>ID</strong></TableCell>
                        <TableCell><strong>Type</strong></TableCell>
                        <TableCell><strong>Amount</strong></TableCell>
                        <TableCell><strong>Status</strong></TableCell>
                        <TableCell><strong>Date</strong></TableCell>
                        <TableCell><strong>Details</strong></TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {transactions.length === 0 ? (
                        <TableRow>
                          <TableCell colSpan={6} align="center" sx={{ py: 4 }}>
                            <Typography color="text.secondary">No transactions found</Typography>
                          </TableCell>
                        </TableRow>
                      ) : (
                        transactions.map((tx) => (
                          <TableRow key={tx.id} hover>
                            <TableCell>{tx.id}</TableCell>
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
              </CardContent>
            </Card>
          </Grid>
        </Grid>
      </Container>

      {/* Transaction Dialog */}
      <Dialog open={topUpDialogOpen} onClose={() => setTopUpDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>
          {transactionType === 'topup' ? 'Top-Up Account' : 'Withdraw Funds'}
        </DialogTitle>
        <DialogContent>
          <Box sx={{ mt: 2 }}>
            <FormControl fullWidth margin="normal">
              <InputLabel>Transaction Type</InputLabel>
              <Select
                value={transactionType}
                label="Transaction Type"
                onChange={(e) => setTransactionType(e.target.value as 'topup' | 'withdraw')}
              >
                <MenuItem value="topup">Top-Up</MenuItem>
                <MenuItem value="withdraw">Withdraw</MenuItem>
              </Select>
            </FormControl>
            <TextField
              fullWidth
              label="Amount"
              type="number"
              value={amount}
              onChange={(e) => setAmount(e.target.value)}
              margin="normal"
              inputProps={{ min: 0.01, step: 0.01 }}
              required
            />
            <TextField
              fullWidth
              label="PIN"
              type="password"
              value={pin}
              onChange={(e) => setPin(e.target.value)}
              margin="normal"
              inputProps={{ maxLength: 4 }}
              helperText="Enter your 4-digit PIN"
              required
            />
            <Alert severity="info" sx={{ mt: 2 }}>
              Card: {cardInfo?.maskedCardNumber} | Current Balance: ${cardInfo?.balance.toFixed(2)}
            </Alert>
          </Box>
        </DialogContent>
        <DialogActions sx={{ p: 2 }}>
          <Button onClick={() => setTopUpDialogOpen(false)}>Cancel</Button>
          <Button
            variant="contained"
            onClick={handleTransaction}
            disabled={processing || !amount || !pin}
          >
            {processing ? 'Processing...' : 'Confirm'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Snackbar for notifications */}
      <Snackbar
        open={snackbar.open}
        autoHideDuration={6000}
        onClose={() => setSnackbar({ ...snackbar, open: false })}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
      >
        <Alert
          onClose={() => setSnackbar({ ...snackbar, open: false })}
          severity={snackbar.severity}
          sx={{ width: '100%' }}
        >
          {snackbar.message}
        </Alert>
      </Snackbar>
    </Box>
  )
}

export default CustomerDashboard
