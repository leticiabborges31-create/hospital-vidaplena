<script setup>
import { ref, computed } from 'vue'
import LoginModal from './LoginModal.vue'
import { useAuth } from '../composables/useAuth.js'

const { logado, nome, sair } = useAuth()
const aberto = ref(false)
const primeiroNome = computed(() => nome.value.split(' ')[0])
</script>

<template>
  <header class="top">
    <a class="logo" href="#">Hospital Vida Plena</a>
    <nav aria-label="Principal">
      <a href="#checkin">Check-in</a>
      <a href="#checkup">Check-up</a>
      <a href="#faq">Dúvidas</a>
      <template v-if="logado">
        <span class="ola">Olá, {{ primeiroNome }}</span>
        <a class="btn" href="#/admin">Painel</a>
        <button class="btn ghost" @click="sair">Sair</button>
      </template>
      <button v-else class="btn ghost" @click="aberto = true">Entrar</button>
      <a class="btn" href="#agendar">Agendar consulta</a>
    </nav>
  </header>
  <LoginModal :aberto="aberto" @fechar="aberto = false" />
</template>

<style scoped>
.ola{font-weight:500;color:var(--sea-d)}
@media(max-width:900px){.ola{display:none}}
</style>
