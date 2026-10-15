<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import AppHeader from './components/AppHeader.vue'
import HeroSection from './components/HeroSection.vue'
import CheckinSection from './components/CheckinSection.vue'
import PackagesSection from './components/PackagesSection.vue'
import FaqSection from './components/FaqSection.vue'
import AppFooter from './components/AppFooter.vue'
import WhatsAppButton from './components/WhatsAppButton.vue'
import Admin from './components/Admin.vue'
import { useAuth } from './composables/useAuth.js'

// Contador que avisa o formulário de agendamento que um pacote foi escolhido.
const pacoteEscolhido = ref(0)

// Painel da equipe: abre em http://localhost:5173/#/admin
// Quem já está logado e abre o site (ou recarrega a página) vai direto para o painel.
const { logado } = useAuth()
if (logado.value && (location.hash === '' || location.hash === '#')) location.hash = '#/admin'
const admin = ref(location.hash === '#/admin')
const atualizar = () => (admin.value = location.hash === '#/admin')
onMounted(() => window.addEventListener('hashchange', atualizar))
onUnmounted(() => window.removeEventListener('hashchange', atualizar))
</script>

<template>
  <Admin v-if="admin" />
  <template v-else>
    <AppHeader />
    <main>
      <HeroSection :pacote-escolhido="pacoteEscolhido" />
      <CheckinSection />
      <PackagesSection @escolher="pacoteEscolhido++" />
      <FaqSection />
    </main>
    <AppFooter />
    <WhatsAppButton />
  </template>
</template>
