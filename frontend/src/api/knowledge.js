import api from './index'

// 知识库管理
export const getKnowledgeBases = () => api.get('/knowledge-base')
export const getKnowledgeBaseById = (id) => api.get(`/knowledge-base/${id}`)
export const createKnowledgeBase = (data) => api.post('/knowledge-base', data)
export const updateKnowledgeBase = (id, data) => api.put(`/knowledge-base/${id}`, data)
export const deleteKnowledgeBase = (id) => api.delete(`/knowledge-base/${id}`)
export const bindDocument = (kbId, docId) => api.post(`/knowledge-base/${kbId}/documents/${docId}`)
export const unbindDocument = (kbId, docId) => api.delete(`/knowledge-base/${kbId}/documents/${docId}`)

// 文档管理
export const getDocuments = (params) => api.get('/document', { params })
export const getDocumentById = (id) => api.get(`/document/${id}`)
export const uploadDocument = (formData) => api.post('/document/upload', formData, {
  headers: { 'Content-Type': 'multipart/form-data' }
})
export const deleteDocument = (id) => api.delete(`/document/${id}`)
export const recoverDocument = (id) => api.post(`/document/${id}/recover`)
export const permanentDeleteDocument = (id) => api.delete(`/document/${id}/permanent`)
export const getDocumentVersions = (id) => api.get(`/document/${id}/versions`)
export const restoreDocumentVersion = (id, versionId) => api.post(`/document/${id}/versions/${versionId}/restore`)
export const getDocumentStatus = (taskId) => api.get(`/document/status/${taskId}`)

// 搜索检索
export const ragRetrieve = (data) => api.post('/rag/retrieve', data)
export const semanticSearch = (data) => api.post('/rag/search', data)
export const keywordSearch = (data) => api.post('/rag/keyword-search', data)

// 向量管理
export const addVectorChunk = (data) => api.post('/vector/chunk', data)
export const addVectorChunkBatch = (data) => api.post('/vector/chunk/batch', data)
export const deleteVectorChunk = (id) => api.delete(`/vector/chunk/${id}`)
export const deleteVectorByDocument = (docId) => api.delete(`/vector/document/${docId}`)
export const rebuildVectorIndex = (kbId) => api.post('/vector/rebuild-index', null, { params: { kbId } })