import axios from 'axios'
import { TransactionRequest, TransactionResponse, CardInfo, TransactionHistory } from '../types'

// Helper function to ensure URL has https:// prefix
function ensureHttps(url: string | undefined): string {
  if (!url) return ''
  if (url.startsWith('http://') || url.startsWith('https://')) {
    return url
  }
  // Add https:// prefix for production URLs
  return `https://${url}`
}

// API base URLs - use environment variables for production, proxy for development
const rawGatewayUrl = import.meta.env.VITE_GATEWAY_URL || ''
const rawCoreUrl = import.meta.env.VITE_CORE_URL || ''

// In development (no env vars), use proxy paths; in production, ensure https://
const GATEWAY_URL = rawGatewayUrl ? ensureHttps(rawGatewayUrl) : '/api/gateway'
const CORE_URL = rawCoreUrl ? ensureHttps(rawCoreUrl) : '/api/core'

console.log('API Configuration:', { GATEWAY_URL, CORE_URL })

// Create axios instance with default config
const gatewayApi = axios.create({
  baseURL: GATEWAY_URL,
  headers: {
    'Content-Type': 'application/json',
  },
})

const coreApi = axios.create({
  baseURL: CORE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
})

/**
 * Process a transaction through the Gateway (System 1)
 */
export async function processTransaction(request: TransactionRequest): Promise<TransactionResponse> {
  try {
    const response = await gatewayApi.post<TransactionResponse>('/transaction', request)
    return response.data
  } catch (error) {
    if (axios.isAxiosError(error) && error.response) {
      return error.response.data as TransactionResponse
    }
    return {
      success: false,
      message: 'Failed to connect to the banking system. Please try again.',
    }
  }
}

/**
 * Get card info by username (from Core Banking - System 2)
 */
export async function getCardByUsername(username: string): Promise<CardInfo | null> {
  try {
    const response = await coreApi.get<CardInfo>(`/card/by-username/${username}`)
    return response.data
  } catch (error) {
    console.error('Failed to fetch card info:', error)
    return null
  }
}

/**
 * Get card info by card number (from Core Banking - System 2)
 */
export async function getCardByNumber(cardNumber: string): Promise<CardInfo | null> {
  try {
    const response = await coreApi.get<CardInfo>(`/card/${cardNumber}`)
    return response.data
  } catch (error) {
    console.error('Failed to fetch card info:', error)
    return null
  }
}

/**
 * Get transaction history for a specific card (from Core Banking - System 2)
 */
export async function getTransactionHistory(cardNumber: string): Promise<TransactionHistory[]> {
  try {
    const response = await coreApi.get<TransactionHistory[]>(`/transactions/${cardNumber}`)
    return response.data
  } catch (error) {
    console.error('Failed to fetch transaction history:', error)
    return []
  }
}

/**
 * Get all transactions (for admin - from Core Banking - System 2)
 */
export async function getAllTransactions(): Promise<TransactionHistory[]> {
  try {
    const response = await coreApi.get<TransactionHistory[]>('/transactions/all')
    return response.data
  } catch (error) {
    console.error('Failed to fetch all transactions:', error)
    return []
  }
}

/**
 * Check Gateway health
 */
export async function checkGatewayHealth(): Promise<boolean> {
  try {
    await gatewayApi.get('/health')
    return true
  } catch {
    return false
  }
}

/**
 * Check Core Banking health
 */
export async function checkCoreHealth(): Promise<boolean> {
  try {
    await coreApi.get('/health')
    return true
  } catch {
    return false
  }
}
