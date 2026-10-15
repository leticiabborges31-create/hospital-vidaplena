import { ref } from 'vue'

// Estado compartilhado entre os componentes (um único array para todo o app).
// Para integrar ao backend, troque load/salvar por chamadas fetch('/api/agendamentos').
const KEY = 'vp_ag'

function load() {
  try { return JSON.parse(localStorage.getItem(KEY)) || [] } catch (e) { return [] }
}

const agendamentos = ref(load())

function salvar() {
  try { localStorage.setItem(KEY, JSON.stringify(agendamentos.value)) } catch (e) {}
}

export function useAgendamentos() {
  const adicionar = novo => {
    agendamentos.value = [novo, ...agendamentos.value]
    salvar()
  }

  const atualizar = (code, patch) => {
    agendamentos.value = agendamentos.value.map(a => (a.code === code ? { ...a, ...patch } : a))
    salvar()
    return agendamentos.value.find(a => a.code === code)
  }

  const cancelar = code => atualizar(code, { status: 'cancelado' })

  const buscar = code => agendamentos.value.find(a => a.code === code.trim().toUpperCase())

  const horarioOcupado = (medico, data, hora) =>
    agendamentos.value.some(a => a.medico === medico && a.data === data && a.hora === hora && a.status === 'agendado')

  return { agendamentos, adicionar, atualizar, cancelar, buscar, horarioOcupado }
}
