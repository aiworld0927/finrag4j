import api from './index'

export const login = (data) => api.post('/users/login', null, { params: data })

export const getUsers = (tenantId) => api.get('/users', { params: { tenantId } })

export const createUser = (data, tenantId) => api.post('/users', data, { params: { tenantId } })

export const updateUser = (id, data) => api.put(`/users/${id}`, data)

export const deleteUser = (id) => api.delete(`/users/${id}`)

export const getUserRoles = (userId, tenantId) => api.get(`/users/${userId}/roles`, { params: { tenantId } })

export const assignRoles = (userId, roleIds, tenantId) => api.post(`/users/${userId}/roles`, roleIds, { params: { tenantId } })