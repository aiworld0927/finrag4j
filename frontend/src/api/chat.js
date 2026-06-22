import api from './index'

// 聊天会话
export const createSession = (data) => api.post('/chat/session/create', data)
export const getChatHistory = (sessionId, params) => api.get(`/chat/history/${sessionId}`, { params })
export const deleteSession = (sessionId) => api.delete(`/chat/session/${sessionId}`)
export const favoriteMessage = (sessionId, messageId) => api.post(`/chat/session/${sessionId}/favorite/${messageId}`)

// 发送消息
export const sendMessage = (data) => api.post('/chat/send', data)