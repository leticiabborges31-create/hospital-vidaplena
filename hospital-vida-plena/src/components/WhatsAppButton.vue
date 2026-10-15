<script setup>
const numero = '5598999999999' // troque pelo número real do hospital
const link = `https://wa.me/${numero}?text=${encodeURIComponent('Olá! Gostaria de agendar uma consulta.')}`
</script>

<template>
  <a class="wa" :href="link" target="_blank" rel="noopener">WhatsApp</a>
</template>
