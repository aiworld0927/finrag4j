import api from './index'

export const getExtractTemplates = (tenantId) => api.get('/agent/extract/templates', { params: { tenantId } })

export const createExtractTemplate = (data, tenantId) => api.post('/agent/extract/template', data, { params: { tenantId } })

export const executeExtract = (documentId, templateId, tenantId) => 
  api.post('/agent/extract/execute', null, { params: { documentId, templateId, tenantId } })

export const reviewExtract = (recordId, correctedResult, comment, reviewerId) =>
  api.post(`/agent/extract/${recordId}/review`, null, { params: { correctedResult, comment, reviewerId } })

export const createComplianceReport = (tenantId, reportName, documentIds) =>
  api.post('/agent/compliance/report', documentIds, { params: { tenantId, reportName } })

export const executeComplianceCheck = (reportId, documentIds, tenantId) =>
  api.post('/agent/compliance/check', documentIds, { params: { reportId, tenantId } })

export const reviewComplianceReport = (reportId, comment, reviewerId) =>
  api.post(`/agent/compliance/report/${reportId}/review`, null, { params: { comment, reviewerId } })

export const queryRegulation = (question, tenantId) =>
  api.post('/agent/regulation/query', null, { params: { question, tenantId } })