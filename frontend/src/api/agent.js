import api from './index'

// 合规检查
export const complianceCheck = (data) => api.post('/agent/compliance/check', data)
export const generateComplianceReport = (reportId) => api.post(`/agent/compliance/report/${reportId}`)
export const getComplianceReport = (reportId) => api.get(`/agent/compliance/report/${reportId}`)

// 信息抽取
export const extract = (data) => api.post('/agent/extraction/extract', data)
export const createExtractTemplate = (data) => api.post('/agent/extraction/template', data)
export const getExtractTemplate = (id) => api.get(`/agent/extraction/template/${id}`)