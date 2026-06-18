import api from './index'

export const getKnowledgeBases = (tenantId) => api.get('/kb', { params: { tenantId } })

export const createKnowledgeBase = (data, tenantId) => api.post('/kb', data, { params: { tenantId } })

export const updateKnowledgeBase = (id, data) => api.put(`/kb/${id}`, data)

export const deleteKnowledgeBase = (id, tenantId) => api.delete(`/kb/${id}`, { params: { tenantId } })

export const getDocuments = (tenantId) => api.get('/documents', { params: { tenantId } })

export const uploadDocument = (formData, tenantId) => api.post('/documents/upload', formData, {
  params: { tenantId },
  headers: { 'Content-Type': 'multipart/form-data' }
})

export const deleteDocument = (id, tenantId) => api.delete(`/documents/${id}`, { params: { tenantId } })

export const previewDocument = (id, tenantId) => api.get(`/documents/${id}/preview`, { params: { tenantId } })