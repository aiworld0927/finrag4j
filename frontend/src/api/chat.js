import api from './index'

export const startNewChat = (question, kbId, tenantId, userId) => 
  api.post('/chat/new', null, { params: { question, kbId, tenantId, userId } })

export const continueChat = (sessionId, question, tenantId, userId) => 
  api.post('/chat/continue', null, { params: { sessionId, question, tenantId, userId } })

export const getChatHistory = (sessionId) => api.get(`/chat/history/${sessionId}`)

export const getRecentChats = (tenantId, userId, limit = 10) => 
  api.get('/chat/recent', { params: { tenantId, userId, limit } })

export const addFavorite = (sessionId, chatId, userMessage, aiMessage, tags, tenantId, userId) =>
  api.post('/chat/favorite', null, { params: { sessionId, chatId, userMessage, aiMessage, tags, tenantId, userId } })

export const getFavorites = (tenantId, userId) => api.get('/chat/favorites', { params: { tenantId, userId } })