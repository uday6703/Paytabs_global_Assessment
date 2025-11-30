// User types
export interface User {
  username: string
  role: 'CUSTOMER' | 'ADMIN'
  cardNumber?: string
  customerName?: string
}

// Card info from API
export interface CardInfo {
  cardNumber: string
  maskedCardNumber: string
  balance: number
  customerName: string
  username: string
}

// Transaction request
export interface TransactionRequest {
  cardNumber: string
  pin: string
  amount: number
  type: 'withdraw' | 'topup'
}

// Transaction response
export interface TransactionResponse {
  success: boolean
  message: string
  newBalance?: number
  transactionId?: number
}

// Transaction history item
export interface TransactionHistory {
  id: number
  cardNumber: string
  maskedCardNumber: string
  type: string
  amount: number
  timestamp: string
  status: string
  reason: string
}

// Hardcoded users for POC (in production, use proper authentication)
export const USERS: Record<string, { password: string; role: 'CUSTOMER' | 'ADMIN'; cardNumber?: string }> = {
  cust1: { password: 'pass', role: 'CUSTOMER', cardNumber: '4123456789012345' },
  cust2: { password: 'pass', role: 'CUSTOMER', cardNumber: '4987654321098765' },
  admin: { password: 'admin', role: 'ADMIN' },
}
