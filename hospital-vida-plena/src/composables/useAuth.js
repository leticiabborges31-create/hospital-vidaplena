import { ref, computed } from 'vue'

// Estado de login compartilhado. Guarda só o token e o nome (nunca a senha).
const KEY = 'vp_auth'

function load() {
  try { return JSON.parse(localStorage.getItem(KEY)) || null } catch (e) { return null }
}

const sessao = ref(load())

export function useAuth() {
  const logado = computed(() => !!sessao.value?.token)
  const nome = computed(() => sessao.value?.nome || '')
  const admin = computed(() => sessao.value?.perfil === 'ADMIN')

  const entrar = dados => {
    sessao.value = { token: dados.token, nome: dados.nome, email: dados.email, perfil: dados.perfil }
    try { localStorage.setItem(KEY, JSON.stringify(sessao.value)) } catch (e) {}
  }

  const sair = () => {
    sessao.value = null
    try { localStorage.removeItem(KEY) } catch (e) {}
  }

  return { sessao, logado, nome, admin, entrar, sair }
}
