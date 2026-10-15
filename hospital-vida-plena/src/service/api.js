const BASE = 'http://localhost:8080/api/agendamentos'

import { useAuth } from '../composables/useAuth.js'

const auth = useAuth()

async function req(url, options = {}) {
  // Quem está logado manda o token JWT em toda chamada.
  const headers = { 'Content-Type': 'application/json' }
  if (auth.sessao.value?.token) headers.Authorization = `Bearer ${auth.sessao.value.token}`
  const res = await fetch(url, { ...options, headers })
  // Token vencido ou inválido: encerra a sessão para o painel pedir login de novo.
  // (No login com senha errada o usuário ainda não tem sessão, então não afeta.)
  if (res.status === 401 && auth.sessao.value) auth.sair()
  if (!res.ok) throw new Error(`Erro ${res.status}: ${await res.text()}`)
  return res.status === 204 ? null : res.json()
}

export const api = {
  criar: dados => req(BASE, { method: 'POST', body: JSON.stringify(dados) }),
  listar: () => req(BASE),
  buscar: codigo => req(`${BASE}/${codigo}`),
  cancelar: codigo => req(`${BASE}/${codigo}/cancelar`, { method: 'PATCH' }),
  ocupados: (medico, data) =>
    req(`${BASE}/ocupados?medico=${encodeURIComponent(medico)}&data=${data}`)
}

// ---- Ações do paciente (sem login): código + CPF ----
export const apiPaciente = {
  checkin: (codigo, cpf) => req('http://localhost:8080/api/paciente/checkin', { method: 'POST', body: JSON.stringify({ codigo, cpf }) }),
  cancelar: (codigo, cpf) => req('http://localhost:8080/api/paciente/cancelar', { method: 'POST', body: JSON.stringify({ codigo, cpf }) })
}

// ---- Login e cadastro ----
export const apiAuth = {
  cadastrar: dados => req('http://localhost:8080/api/auth/cadastro', { method: 'POST', body: JSON.stringify(dados) }),
  entrar: dados => req('http://localhost:8080/api/auth/login', { method: 'POST', body: JSON.stringify(dados) })
}

// ---- Painel da equipe ----
const BASE_API = 'http://localhost:8080/api'

export const apiAdmin = {
  checkin: codigo => req(`${BASE}/${codigo}/checkin`, { method: 'POST' }),
  medicos: () => req(`${BASE_API}/medicos`),
  criarMedico: dados => req(`${BASE_API}/medicos`, { method: 'POST', body: JSON.stringify(dados) }),
  atualizarMedico: (id, dados) => req(`${BASE_API}/medicos/${id}`, { method: 'PUT', body: JSON.stringify(dados) }),
  mudarStatusMedico: (id, status) =>
    req(`${BASE_API}/medicos/${id}/status`, { method: 'PATCH', body: JSON.stringify({ status }) })
}