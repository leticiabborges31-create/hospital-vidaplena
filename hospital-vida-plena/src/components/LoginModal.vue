<script setup>
import { reactive, ref, watch } from 'vue'
import { apiAuth } from '../service/api.js'
import { useAuth } from '../composables/useAuth.js'
import { mascaraTel } from '../utils/formatadores.js'

const props = defineProps({ aberto: Boolean })
const emit = defineEmits(['fechar'])
const { entrar } = useAuth()

const aba = ref('entrar')
const erro = ref('')
const ok = ref('')
const enviando = ref(false)
const verSenha = ref(false)

const login = reactive({ email: '', senha: '' })
const cad = reactive({ nome: '', crm: '', telefone: '', email: '', senha: '' })

const emailValido = v => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v)

watch(() => props.aberto, a => {
  if (a) { erro.value = ''; ok.value = ''; verSenha.value = false }
})

function trocar(nova) {
  aba.value = nova
  erro.value = ''
  ok.value = ''
}

async function enviarLogin() {
  erro.value = ''
  if (!login.email.trim()) return (erro.value = 'Digite seu e-mail ou usuário.')
  if (!login.senha) return (erro.value = 'Digite sua senha.')
  enviando.value = true
  try {
    entrar(await apiAuth.entrar({ email: login.email.trim(), senha: login.senha }))
    location.hash = '#/admin'   // depois de entrar, vai direto para o painel
    login.senha = ''
    emit('fechar')
  } catch (e) {
    erro.value = e.message.includes('401') || e.message.includes('403')
      ? 'E-mail ou senha incorretos.'
      : 'Não foi possível entrar. Confira se o servidor está rodando.'
  } finally {
    enviando.value = false
  }
}

async function enviarCadastro() {
  erro.value = ''
  if (cad.nome.trim().length < 5) return (erro.value = 'Digite seu nome completo.')
  if (cad.crm.length < 3) return (erro.value = 'Digite o número do CRM-MA.')
  if (cad.telefone.replace(/\D/g, '').length < 10) return (erro.value = 'Digite um telefone com DDD.')
  if (!emailValido(cad.email)) return (erro.value = 'Digite um e-mail válido.')
  if (cad.senha.length < 8) return (erro.value = 'A senha precisa ter pelo menos 8 caracteres.')
  enviando.value = true
  try {
    await apiAuth.cadastrar({
      nome: cad.nome.trim(),
      crm: `CRM-MA ${cad.crm}`,
      telefone: cad.telefone,
      email: cad.email.trim(),
      senha: cad.senha
    })
    login.email = cad.email.trim()
    Object.assign(cad, { nome: '', crm: '', telefone: '', email: '', senha: '' })
    aba.value = 'entrar'
    ok.value = 'Cadastro realizado! Entre com seu e-mail e senha.'
  } catch (e) {
    erro.value = e.message.includes('409')
      ? 'Já existe um cadastro com esse e-mail ou CRM.'
      : 'Não foi possível cadastrar. Confira se o servidor está rodando.'
  } finally {
    enviando.value = false
  }
}
</script>

<template>
  <div class="modal" v-if="aberto" @click.self="emit('fechar')" @keydown.esc="emit('fechar')">
    <div class="card caixa" role="dialog" aria-modal="true" aria-label="Acesso">
      <button class="x" @click="emit('fechar')" aria-label="Fechar">×</button>

      <div class="abas" role="tablist">
        <button role="tab" :aria-selected="aba === 'entrar'" :class="{ on: aba === 'entrar' }" @click="trocar('entrar')">Entrar</button>
        <button role="tab" :aria-selected="aba === 'cadastrar'" :class="{ on: aba === 'cadastrar' }" @click="trocar('cadastrar')">Cadastrar</button>
      </div>

      <p class="ok" v-if="ok">{{ ok }}</p>

      <!-- Entrar -->
      <form v-if="aba === 'entrar'" @submit.prevent="enviarLogin" novalidate>
        <label class="lbl" for="l-email">E-mail ou usuário</label>
        <input id="l-email" v-model="login.email" autocomplete="username">
        <label class="lbl" for="l-senha">Senha</label>
        <div class="senha">
          <input id="l-senha" :type="verSenha ? 'text' : 'password'" v-model="login.senha" autocomplete="current-password">
          <button type="button" class="ver" @click="verSenha = !verSenha">{{ verSenha ? 'Ocultar' : 'Mostrar' }}</button>
        </div>
        <p class="err" v-if="erro">{{ erro }}</p>
        <div class="nav"><button class="btn" style="width:100%" :disabled="enviando">{{ enviando ? 'Entrando...' : 'Entrar' }}</button></div>
        <p class="troca">Ainda não tem conta? <a href="#" @click.prevent="trocar('cadastrar')">Cadastre-se</a></p>
      </form>

      <!-- Cadastrar -->
      <form v-else @submit.prevent="enviarCadastro" novalidate>
        <label class="lbl" for="c-nome">Nome completo</label>
        <input id="c-nome" v-model="cad.nome" autocomplete="name">
        <label class="lbl" for="c-crm">CRM-MA</label>
        <div class="crm">
          <span>CRM-MA</span>
          <input id="c-crm" inputmode="numeric" placeholder="0000" :value="cad.crm" @input="cad.crm = $event.target.value.replace(/\D/g, '').slice(0, 7)">
        </div>
        <label class="lbl" for="c-tel">Telefone</label>
        <input id="c-tel" inputmode="tel" placeholder="(98) 99999-9999" :value="cad.telefone" @input="cad.telefone = mascaraTel($event.target.value)">
        <label class="lbl" for="c-email">E-mail</label>
        <input id="c-email" type="email" v-model="cad.email" autocomplete="email">
        <label class="lbl" for="c-senha">Senha</label>
        <div class="senha">
          <input id="c-senha" :type="verSenha ? 'text' : 'password'" v-model="cad.senha" autocomplete="new-password" placeholder="Mínimo de 8 caracteres">
          <button type="button" class="ver" @click="verSenha = !verSenha">{{ verSenha ? 'Ocultar' : 'Mostrar' }}</button>
        </div>
        <p class="err" v-if="erro">{{ erro }}</p>
        <div class="nav"><button class="btn" style="width:100%" :disabled="enviando">{{ enviando ? 'Cadastrando...' : 'Criar cadastro' }}</button></div>
        <p class="troca">Já tem conta? <a href="#" @click.prevent="trocar('entrar')">Entrar</a></p>
      </form>
    </div>
  </div>
</template>

<style scoped>
.modal{position:fixed;inset:0;background:rgba(14,42,47,.5);display:grid;place-items:center;padding:1rem;z-index:20}
.caixa{position:relative;width:100%;max-width:420px;max-height:92vh;overflow:auto}
.x{position:absolute;top:.5rem;right:.7rem;border:0;background:transparent;font-size:1.6rem;line-height:1;cursor:pointer;color:var(--mute)}
.abas{display:flex;gap:.4rem;margin:0 1.5rem 1rem 0}
.abas button{flex:1;border:0;border-bottom:3px solid var(--line);background:transparent;padding:.6rem;font:500 1rem Figtree,sans-serif;color:var(--mute);cursor:pointer}
.abas button.on{border-color:var(--sea);color:var(--ink);font-weight:700}
.senha{position:relative}
.senha input{padding-right:5.2rem}
.ver{position:absolute;right:.4rem;top:50%;transform:translateY(-50%);border:0;background:transparent;color:var(--sea);font:500 .85rem Figtree,sans-serif;cursor:pointer;padding:.3rem .5rem}
.crm{display:flex;align-items:stretch;border:1.5px solid var(--line);border-radius:var(--r);background:#fff;overflow:hidden}
.crm span{display:grid;place-items:center;padding:0 .8rem;background:var(--mist);color:var(--sea-d);font-weight:500;font-size:.92rem}
.crm input{border:0;border-radius:0}
.ok{background:#d9f2e3;color:#166534;padding:.6rem .8rem;border-radius:var(--r);margin-bottom:.5rem;font-size:.92rem}
.troca{text-align:center;margin-top:1rem;font-size:.92rem;color:var(--mute)}
</style>
