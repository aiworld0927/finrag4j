import api from './index'

// 用户认证
export const login = (data) => api.post('/auth/login', data)
export const register = (data) => api.post('/auth/register', data)
export const logout = (token) => api.post('/auth/logout', null, { headers: { Authorization: `Bearer ${token}` } })
export const refreshToken = (refreshToken) => api.post('/auth/refresh', { refreshToken })
export const getCurrentUser = (token) => api.get('/auth/me', { headers: { Authorization: `Bearer ${token}` } })

// 用户管理
export const getUsers = (params) => api.get('/users', { params })
export const getUserById = (id) => api.get(`/users/${id}`)
export const createUser = (data) => api.post('/users', data)
export const updateUser = (id, data) => api.put(`/users/${id}`, data)
export const deleteUser = (id) => api.delete(`/users/${id}`)
export const updateUserStatus = (id, status) => api.put(`/users/${id}/status`, null, { params: { status } })
export const assignRoles = (id, roleIds) => api.post(`/users/${id}/roles`, roleIds)

// 角色管理
export const getRoles = () => api.get('/roles')
export const getRoleById = (id) => api.get(`/roles/${id}`)
export const createRole = (data) => api.post('/roles', data)
export const updateRole = (id, data) => api.put(`/roles/${id}`, data)
export const deleteRole = (id) => api.delete(`/roles/${id}`)
export const assignPermissions = (id, permissionIds) => api.post(`/roles/${id}/permissions`, permissionIds)

// 权限管理
export const getPermissions = () => api.get('/permissions')
export const getPermissionTree = () => api.get('/permissions/tree')
export const getPermissionById = (id) => api.get(`/permissions/${id}`)
export const createPermission = (data) => api.post('/permissions', data)
export const updatePermission = (id, data) => api.put(`/permissions/${id}`, data)
export const deletePermission = (id) => api.delete(`/permissions/${id}`)